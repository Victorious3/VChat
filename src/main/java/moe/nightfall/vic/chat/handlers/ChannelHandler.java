package moe.nightfall.vic.chat.handlers;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import moe.nightfall.vic.chat.commands.channels.CommandChannel;
import moe.nightfall.vic.chat.commands.channels.CommandGlobal;
import moe.nightfall.vic.chat.commands.channels.CommandLocal;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import moe.nightfall.vic.chat.channels.ChannelCustom;
import moe.nightfall.vic.chat.channels.ChannelGlobal;
import moe.nightfall.vic.chat.channels.ChannelLocal;
import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.Misc;
import moe.nightfall.vic.chat.VChat;
import moe.nightfall.vic.chat.api.IChannel;
import moe.nightfall.vic.chat.api.bot.IChatEntity;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

public class ChannelHandler extends ChatHandler
{
    private final HashMap<ChatEntity, ArrayList<String>> members;
    private final HashMap<String, IChannel> channels;

    private File channelfile;
    private File playerfile;

    public ChannelHandler(VChat instance)
    {
        super(instance);

        this.members = new HashMap<ChatEntity, ArrayList<String>>();
        this.channels = new HashMap<String, IChannel>();

        if(Config.localEnabled)
            this.registerChannel(new ChannelLocal(this.instance));

        this.registerChannel(new ChannelGlobal(this.instance));

        this.channelfile = new File("vChat/vchat_channels.json");
        this.playerfile = new File("vChat/vchat_players.json");
    }

    @Override
    public void onServerLoad(FMLServerStartingEvent event)
    {
        try
        {
            if(this.playerfile.exists())
            {
                JsonParser parser = new JsonParser();
                JsonArray players = (JsonArray)parser.parse(new JsonReader(new FileReader(this.playerfile)));

                for(int i = 0; i < players.size(); i++)
                {
                    JsonObject obj = (JsonObject)players.get(i);
                    String name = obj.get("username").getAsString();
                    JsonArray channels = obj.get("channels").getAsJsonArray();
                    ChatEntity entity = new ChatEntity(name);

                    this.members.put(entity, new ArrayList<String>());

                    for(int j = 0; j < channels.size(); j++)
                    {
                        String channel = channels.get(j).getAsString();
                        this.members.get(entity).add(channel);
                    }
                }
            }
        }
        catch (Exception e)
        {
            VChat.instance.getLogger().error("Could not read the player file. Maybe it's disrupted or the access is restricted. Try deleting it.");
            e.printStackTrace();
        }

        try
        {
            if(this.channelfile.exists())
            {
                JsonParser parser = new JsonParser();
                JsonArray chans = (JsonArray)parser.parse(new JsonReader(new FileReader(this.channelfile)));

                for(int i = 0; i < chans.size(); i++)
                {
                    JsonObject channel = (JsonObject)chans.get(i);
                    String name = channel.get("name").getAsString();

                    if(this.channels.containsKey(name))
                    {
                        this.channels.get(name).read(channel);
                    }
                    else
                    {
                        this.registerChannel(new ChannelCustom(this.instance, name));
                        this.channels.get(name).read(channel);
                    }
                }
            }
        }
        catch (JsonIOException e)
        {
            VChat.instance.getLogger().error("Could not read the channel file. Maybe it's disrupted or the access is restricted. Try deleting it.");
            e.printStackTrace();
        }
        catch (JsonSyntaxException e)
        {
            VChat.instance.getLogger().error("The channel file contains invalid syntax. It has to be a valid JSON file!");
            e.printStackTrace();
        }
        catch (NullPointerException e)
        {
            VChat.instance.getLogger().error("The channel file is missing a required field.");
            e.printStackTrace();
        }
        catch (Exception e)
        {
            VChat.instance.getLogger().error("Failed to read properly the channel file.");
            e.printStackTrace();
        }

        event.registerServerCommand(new CommandChannel(this));

        if(Config.localEnabled)
            event.registerServerCommand(new CommandLocal());

        event.registerServerCommand(new CommandGlobal());
    }

    @Override
    public void onServerUnload(FMLServerStoppingEvent event)
    {
        try
        {
            if(!this.playerfile.exists())
            {
                this.playerfile.getParentFile().mkdirs();
                this.playerfile.createNewFile();
            }

            JsonArray players = new JsonArray();

            for(ChatEntity entity : this.members.keySet())
            {
                JsonObject obj = new JsonObject();
                obj.addProperty("username", entity.getUsername());

                JsonArray channels = new JsonArray();
                for(String channel : this.members.get(entity))

                    channels.add(new JsonPrimitive(channel));

                obj.add("channels", channels);
                players.add(obj);
            }

            FileWriter writer = new FileWriter(this.playerfile);
            writer.write(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(players));
            writer.flush();
            writer.close();

        }
        catch (Exception e)
        {
            this.instance.getLogger().error("Could not save the player file. Maybe it's disrupted or the access is restricted. Try deleting it.");
            e.printStackTrace();
        }

        try
        {
            if(!this.channelfile.exists())
            {
                this.channelfile.getParentFile().mkdirs();
                this.channelfile.createNewFile();
            }

            JsonArray channels = new JsonArray();

            for(IChannel channel : this.channels.values())
            {
                JsonObject obj = new JsonObject();
                obj.addProperty("name", channel.getName());
                channel.write(obj);
                channels.add(obj);
            }

            FileWriter writer = new FileWriter(this.channelfile);
            writer.write(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(channels));
            writer.flush();
            writer.close();

        }
        catch (Exception e)
        {
            this.instance.getLogger().error("Could not save the channel file. Maybe it's disrupted or the access is restricted. Try deleting it.");
            e.printStackTrace();
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event)
    {
        ChatEntity player = new ChatEntity(event.player);

        if(!this.members.containsKey(player))
        {
            for(IChannel channel : this.channels.values())
                if(channel instanceof ChannelCustom && channel.autoJoin(player))
                    this.joinChannel(player, channel, true);

            if(Config.localEnabled)
                this.joinChannel(player, getChannel("local"), true);

            this.joinChannel(player, getChannel("global"), true);
        }
        else
        {
            for(String channelName : this.members.get(player))
            {
                IChannel channel = this.getChannel(channelName);

                if(channel != null)
                    channel.onJoin(player, true);
                else
                    this.members.get(player).remove(channelName);
            }
        }

        if(Config.channelListEnabled)
            this.showInfo((EntityPlayerMP) event.player);
    }

    @SubscribeEvent()
    public void onPlayerLeft(PlayerEvent.PlayerLoggedOutEvent event)
    {
        ChatEntity player = new ChatEntity(event.player);

        if(this.members.get(player) == null)
            return;

        for(String channel : (List<String>) this.members.get(player).clone())
            this.getChannel(channel).onLeave(player, true);
    }

    public boolean joinChannel(ChatEntity player, IChannel channel)
    {
        return this.joinChannel(player, channel, false);
    }

    public boolean joinChannel(ChatEntity player, IChannel channel, boolean initial)
    {
        IChannel active = getActiveChannel(player);

        if(active != null && active == channel)
        {
            this.members.get(player).remove(active.getName());
            this.members.get(player).add(active.getName());

            return true;
        }
        else if(channel.canJoin(player) || player.isBot() || player.isServer())
        {
            channel.onJoin(player, initial);

            if(!this.members.containsKey(player))
                this.members.put(player, new ArrayList<String>());

            if(this.members.get(player).contains(channel.getName()))
                this.members.get(player).remove(channel.getName());

            this.members.get(player).add(channel.getName());

            return true;
        }

        return false;
    }

    public void leaveChannel(ChatEntity player, IChannel channel)
    {
        this.leaveChannel(player, channel, false);
    }

    public void leaveChannel(ChatEntity player, IChannel channel, boolean initial)
    {
        if(this.members.get(player) == null)
            return;

        this.members.get(player).remove(channel.getName());
        channel.onLeave(player, initial);
    }

    public void registerChannel(IChannel channel)
    {
        this.channels.put(channel.getName(), channel);
    }

    public void broadcast(IChatComponent component, ChatEntity sender)
    {
        for(ChatEntity player : Misc.getOnlinePlayersAsEntity())
            this.privateMessageTo(sender, player, component);
    }

    public void broadcastOnChannel(IChannel channel, ChatEntity sender, IChatComponent component)
    {
        for(ChatEntity receiver : channel.getMembers())
            this.privateMessageOnChannel(channel, sender, receiver, component);
    }

    public void privateMessageOnChannel(IChannel channel, ChatEntity sender, ChatEntity receiver, IChatComponent component)
    {
        if((channel.isOnChannel(receiver) || receiver.isServer() || receiver.isBot()) && channel.canReceiveChat(sender, receiver, component))
        {
            component = channel.formatChat(sender, receiver, component);

            if(channel.getPrefix() != null)
            {
                ChatComponentText text = new ChatComponentText("");
                text.appendText("[" + channel.getPrefix() + "] ");
                text.appendSibling(component);

                this.privateMessageTo(sender, receiver, text);
            }
            else
            {
                privateMessageTo(sender, receiver, component);
            }
        }
    }

    public void privateMessageTo(ChatEntity sender, ChatEntity receiver, IChatComponent message)
    {
        if(receiver == null || sender == null)
            return;

        EntityPlayerMP player = Misc.getPlayer(receiver.getUsername());

        if(player != null)
            player.addChatComponentMessage(message);
        else if(receiver.isBot())
            this.instance.getBotLoader().getBot(receiver.getUsername()).getOwningBot().onPrivateMessage(message.getUnformattedText(), sender);
        else if(receiver.isServer())
            MinecraftServer.getServer().addChatMessage(message);
    }

    public void showInfo(EntityPlayerMP player)
    {
        ChatEntity entity = new ChatEntity(player);
        IChannel channel = getActiveChannel(entity);

        if(channel != null)
        {
            player.addChatMessage(new ChatComponentText("You are talking on channel \"" + channel.getName() + "\"."));
            player.addChatMessage(new ChatComponentText("Currently joined channels: " + this.members.get(entity).toString()));
        }
        else
        {
            player.addChatMessage(new ChatComponentText("You haven't joined any channel."));
        }
    }

    public IChannel getChannel(String name)
    {
        return this.channels.get(name);
    }

    public IChannel getActiveChannel(IChatEntity player)
    {
        ArrayList<String> joined = this.members.get(player);

        if(joined == null || joined.size() == 0)
            return null;

        return this.channels.get(joined.get(joined.size() - 1));
    }

    public ArrayList<IChannel> getJoinedChannels(IChatEntity player)
    {
        ArrayList<IChannel> list = new ArrayList<IChannel>();

        for(String s : this.members.get(player))
            list.add(this.channels.get(s));

        return list;
    }

    public HashMap<String, IChannel> getChannels()
    {
        return this.channels;
    }

    public static class ChannelNotFoundException extends CommandException
    {
        public ChannelNotFoundException(String channel)
        {
            super("The specified channel \"" + channel + "\" does not exist!");
        }
    }

    public static class ChannelNotJoinedException extends CommandException
    {
        public ChannelNotJoinedException()
        {
            super("You haven't joined any channel.");
        }

        public ChannelNotJoinedException(IChannel channel)
        {
            super("You haven't joined \"" + channel.getName() + "\"!");
        }
    }
}
