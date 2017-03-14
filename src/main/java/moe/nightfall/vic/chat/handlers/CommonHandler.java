package moe.nightfall.vic.chat.handlers;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import moe.nightfall.vic.chat.commands.CommandList;
import moe.nightfall.vic.chat.commands.CommandPos;
import moe.nightfall.vic.chat.commands.CommandTop;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import moe.nightfall.vic.chat.bots.BotHandler;
import moe.nightfall.vic.chat.channels.ChannelCustom;
import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.integrations.ChatFormatter;
import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.Misc;
import moe.nightfall.vic.chat.VChat;
import moe.nightfall.vic.chat.api.IChannel;
import net.minecraft.command.server.CommandEmote;
import net.minecraft.command.server.CommandMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class CommonHandler extends ChatHandler
{
    private final HashMap<String, OnlineTracker> playerTrackers;
    private final ArrayList<ChatFormatter> chatFormatters;
    private final ChatFormatter.ChatFormatterColor chatFormatterColor;
    private final File playerFile;

    public CommonHandler(VChat instance)
    {
        super(instance);

        this.playerTrackers = new HashMap<String, OnlineTracker>();
        this.chatFormatters = new ArrayList<ChatFormatter>();
        this.chatFormatterColor = new ChatFormatter.ChatFormatterColor(instance);
        this.playerFile = new File("vChat/vchat_playertracker.json");
    }

    public void loadPlayers()
    {
        try
        {
            if(this.playerFile.exists())
            {
                JsonParser parser = new JsonParser();
                JsonArray players = (JsonArray)parser.parse(new JsonReader(new FileReader(this.playerFile)));

                for(int i = 0; i < players.size(); i++)
                {
                    JsonObject obj = (JsonObject)players.get(i);
                    String name = obj.get("username").getAsString();
                    this.playerTrackers.put(name, new OnlineTracker(name, obj.get("lastSeen").getAsLong(), obj.get("online").getAsLong()));
                }
            }
        }
        catch (Exception e)
        {
            this.instance.getLogger().error("Could not read the player file. Maybe it's disrupted or the access is restricted. Try deleting it.");
            e.printStackTrace();
        }
    }

    public void savePlayers()
    {
        try
        {
            if(!this.playerFile.exists())
            {
                this.playerFile.getParentFile().mkdirs();
                this.playerFile.createNewFile();
            }

            JsonArray playerArray = new JsonArray();

            for(OnlineTracker tracker : this.playerTrackers.values())
            {
                JsonObject obj = new JsonObject();
                obj.addProperty("username", tracker.name);
                obj.addProperty("lastSeen", tracker.lastSeen);
                obj.addProperty("online", tracker.onlineTime);
                playerArray.add(obj);
            }

            FileWriter writer = new FileWriter(this.playerFile);
            writer.write(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(playerArray));
            writer.flush();
            writer.close();
        }
        catch (Exception e)
        {
            this.instance.getLogger().error("Could not save the player file. Maybe it's disrupted or the access is restricted. Try deleting it.");
            e.printStackTrace();
        }
    }

    public void registerChatFormatter(ChatFormatter chatFormatter)
    {
        this.chatFormatters.add(chatFormatter);
    }

    @Override
    public void onServerLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandPos());
        event.registerServerCommand(new CommandTop());

        if(Config.onlineTrackerEnabled)
            event.registerServerCommand(new CommandList());

        this.loadPlayers();
    }

    @Override
    public void onServerUnload(FMLServerStoppingEvent event)
    {
        if(Config.onlineTrackerEnabled) savePlayers();
    }

    @SubscribeEvent
    public void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(Config.modtEnabled)
            for(String s : Misc.parseModt(Config.modt, (EntityPlayerMP) event.player))
                event.player.sendMessage(new TextComponentString(s));

        if(Config.onlineTrackerEnabled)
        {
            String name = event.player.getName();

            if(!this.playerTrackers.containsKey(name))
                this.playerTrackers.put(name, new OnlineTracker(name, System.currentTimeMillis(), 0));
        }
    }

    @SubscribeEvent
    public void onPlayerLeft(PlayerEvent.PlayerLoggedOutEvent event)
    {
        if(Config.onlineTrackerEnabled)
        {
            String name = event.player.getName();
            OnlineTracker tracker = this.playerTrackers.get(name);

            if(tracker == null)
                return;

            tracker.setOnlineTime(tracker.getOnlineTime());
            tracker.setLastSeen(System.currentTimeMillis());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChat(ServerChatEvent event)
    {
        ChatEntity entity = new ChatEntity(event.getPlayer());
        IChannel channel = this.instance.getChannelHandler().getActiveChannel(entity);

        if(channel == null)
        {
            TextComponentString text = new TextComponentString("You have to join a channel to use the chat!");
            text.getStyle().setColor(TextFormatting.RED);

            event.getPlayer().sendMessage(text);
            event.setCanceled(true);

            return;
        }
        else if(channel.isMuted(entity))
        {
            TextComponentString text = new TextComponentString("You are muted on this channel!");
            text.getStyle().setColor(TextFormatting.RED);

            this.instance.getChannelHandler().privateMessageOnChannel(channel, ChatEntity.SERVER, entity, text);

            event.setCanceled(true);

            return;
        }

        String message = event.getMessage();

        boolean applyFormat = true;

        if(message.startsWith("#"))
        {
            message = message.replaceFirst("#", "");
            applyFormat = false;
        }

        TextComponentString computed = new TextComponentString("");
        computed.appendSibling(new TextComponentString(message));
        computed.getStyle().setColor(channel.getColor());

        if(applyFormat && Config.urlEnabled)
            if(Config.urlPermissionLevel == 0 || event.getPlayer().canUseCommand(Config.urlPermissionLevel, null))
                for (ChatFormatter chatFormatter : this.chatFormatters)
                    chatFormatter.apply(computed);

        if(Config.colorPermissionLevel == 0 || event.getPlayer().canUseCommand(Config.colorPermissionLevel, null))
            this.chatFormatterColor.apply(computed);

        TextComponentString componentName = Misc.getComponent(entity);
        ArrayList<EntityPlayerMP> mentioned = new ArrayList<EntityPlayerMP>();

        for(EntityPlayerMP playerMp : event.getPlayer().getServer().getPlayerList().getPlayers())
        {
            TextComponentString computed2 = computed.createCopy();
            ChatEntity receiver = new ChatEntity(playerMp);

            if(applyFormat)
            {
                for(Object playerMp2 : event.getPlayer().getServer().getPlayerList().getPlayers())
                {
                    ChatEntity player = new ChatEntity(playerMp2);
                    new ChatFormatter.ChatFormatterUsername(this.instance, player, receiver, false, mentioned).apply(computed2);
                }

                if(Config.nickEnabled)
                {
                    for(Object playerMp2 : event.getPlayer().getServer().getPlayerList().getPlayers())
                    {
                        ChatEntity player = new ChatEntity(playerMp2);
                        new ChatFormatter.ChatFormatterUsername(this.instance, player, receiver, true, mentioned).apply(computed2);
                    }
                }

                if(Config.pingHighlighted)
                {
                    EntityPlayerMP player = receiver.toPlayer();

                    if(player != null && mentioned.contains(player) && this.instance.getChannelHandler().getJoinedChannels(receiver).contains(channel))
                        player.connection.sendPacket(new SPacketSoundEffect(SoundEvent.REGISTRY.getObject(new ResourceLocation(Config.pingSound)), SoundCategory.MASTER, player.posX, player.posY, player.posZ, Config.pingVolume, Config.pingPitch));

                    mentioned.clear();
                }
            }

            this.instance.getChannelHandler().privateMessageOnChannel(channel, entity, receiver, new TextComponentTranslation("chat.type.text", componentName, computed2));
        }

        if(!channel.getName().equals("local") && !(channel instanceof ChannelCustom && ((ChannelCustom)channel).hasRange()))
            for(BotHandler bot : this.instance.getBotLoader().getBots().values())
                bot.getOwningBot().onMessage(message, entity, channel);

        event.getPlayer().getServer().sendMessage(new TextComponentTranslation("chat.type.text", componentName, computed));
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onCommand(CommandEvent event)
    {
        if(event.getCommand() instanceof CommandEmote)
        {
            if(event.getSender().canUseCommand(Config.colorPermissionLevel, null))
            {
                if(event.getParameters().length < 1)
                    return;

                String out = "";

                for(int i = 0; i < event.getParameters().length; i++)
                {
                    out += event.getParameters()[i] + " ";

                    if(i != 0)
                        event.getParameters()[i] = "";
                }

                event.getParameters()[0] = out.replaceAll("&", "\u00A7");
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onCommandPost(CommandEvent event)
    {
        if(event.getCommand() instanceof CommandMessage)
        {
            if(event.getParameters().length > 0)
            {
                String name = event.getParameters()[0];

                if(Misc.getPlayer(name) == null && this.instance.getBotLoader().containsBot(name))
                {
                    BotHandler bot = this.instance.getBotLoader().getBot(name);
                    String message = StringUtils.join(Arrays.asList(event.getParameters()).subList(1, event.getParameters().length).toArray(), " ");
                    ChatEntity entity;

                    if(event.getSender() instanceof EntityPlayerMP)
                        entity = new ChatEntity(event.getSender());
                    else
                        entity = ChatEntity.SERVER;

                    bot.getOwningBot().onPrivateMessage(message, entity);
                    event.setCanceled(true);
                }
            }
        }
    }

    public HashMap<String, OnlineTracker> getPlayerTrackers()
    {
        return this.playerTrackers;
    }

    public static class OnlineTracker implements Comparable<OnlineTracker>
    {
        private final String name;
        private long lastSeen, onlineTime;

        public OnlineTracker(String name, long lastSeen, long onlineTime)
        {
            this.name = name;
            this.lastSeen = lastSeen;
            this.onlineTime = onlineTime;
        }

        public TextComponentString toChatComponent()
        {
            TextComponentString text = new TextComponentString(this.name + "--" + Misc.getDuration(getOnlineTime()) + "--");

            if(this.isOnline())
            {
                TextComponentString comp1 = new TextComponentString("online");
                comp1.getStyle().setColor(TextFormatting.GREEN);
                text.appendSibling(comp1);
            }
            else
            {
                text.appendText(Misc.getDuration(System.currentTimeMillis() - this.lastSeen));
            }

            return text;
        }

        public void setLastSeen(long lastSeen)
        {
            this.lastSeen = lastSeen;
        }

        public void setOnlineTime(long time)
        {
            this.onlineTime = time;
        }

        public long getOnlineTime()
        {
            return this.onlineTime + (this.isOnline() ? System.currentTimeMillis() - this.lastSeen : 0);
        }

        public boolean isOnline()
        {
            return Misc.getPlayer(this.name) != null;
        }

        @Override
        public int compareTo(OnlineTracker other)
        {
            if(other.isOnline() && !this.isOnline())
                return -1;
            else if(!other.isOnline() && this.isOnline())
                return 1;
            else
                return this.name.compareTo(other.name);
        }
    }
}
