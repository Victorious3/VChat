package moe.nightfall.vic.chat.commands.channels;

import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.util.Misc;
import moe.nightfall.vic.chat.VChat;
import moe.nightfall.vic.chat.api.IChannel;
import moe.nightfall.vic.chat.commands.CommandOverrideAccess;
import moe.nightfall.vic.chat.handlers.ChannelHandler;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.ForgeHooks;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class CommandChannel extends CommandOverrideAccess
{
    private final ChannelHandler channelHandler;

    public CommandChannel(ChannelHandler channelHandler)
    {
        this.channelHandler = channelHandler;
    }

    @Override
    public String getName()
    {
        return "channel";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        if(sender instanceof EntityPlayerMP)
            return "/channel [join/leave/msg/list/ban/unban/whitelist add/whitelist remove/kick/mute/unmute] [...]";

        return "/channel <msg/list/ban/unban/whitelist add/whitelist remove/kick/mute/unmute> [...]";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public List<String> getAliases()
    {
        return Collections.singletonList("ch");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        boolean isPlayer = sender instanceof EntityPlayerMP;
        EntityPlayerMP player = (EntityPlayerMP)(isPlayer ? sender : null);

        if(args.length == 0)
        {
            if(isPlayer)
                this.channelHandler.showInfo(player);
            else
                throw new WrongUsageException(getUsage(sender));
        }
        else
        {
            if(args[0].equalsIgnoreCase("join") && isPlayer)
            {
                if(args.length != 2) throw new WrongUsageException("/channel join <channel>");

                IChannel channel = this.channelHandler.getChannel(args[1]);

                if(channel == null)
                    throw new ChannelHandler.ChannelNotFoundException(args[1]);

                if(!channel.canJoin(new ChatEntity(player)))
                    throw new CommandException("You are not allowed to join channel \"" + channel.getName() + "\"!");

                if(this.channelHandler.joinChannel(new ChatEntity(player), channel))
                {
                    sender.sendMessage(new TextComponentString("You are now talking on \"" + channel.getName() + "\"."));

                    TextComponentString text = new TextComponentString("Currently active: ");
                    text.appendText("[");

                    Iterator<ChatEntity> iterator = channel.getMembers().iterator();

                    while(iterator.hasNext())
                    {
                        TextComponentString nameComponent = Misc.getComponent(iterator.next());
                        text.appendSibling(nameComponent);

                        if(iterator.hasNext())
                            text.appendText(", ");
                    }

                    text.appendText("]");
                    sender.sendMessage(text);
                }
            }
            else if(args[0].equalsIgnoreCase("leave") && isPlayer)
            {
                if(args.length > 2) throw new WrongUsageException("/channel leave [channel]");

                IChannel channel;

                if(args.length == 2)
                {
                    channel = this.channelHandler.getChannel(args[1]);

                    if(channel == null)
                        throw new ChannelHandler.ChannelNotFoundException(args[1]);

                    if(!channel.isOnChannel(new ChatEntity(player)))
                        throw new ChannelHandler.ChannelNotJoinedException(channel);
                }
                else
                {
                    channel = this.channelHandler.getActiveChannel(new ChatEntity(player));
                    if(channel == null) throw new ChannelHandler.ChannelNotJoinedException();
                }

                this.channelHandler.leaveChannel(new ChatEntity(player), channel);
                sender.sendMessage(new TextComponentString("You left channel \"" + channel.getName() + "\"."));
            }
            else if(args[0].equalsIgnoreCase("msg"))
            {
                if(isPlayer)
                {
                    if(args.length < 3)
                        throw new CommandException("/channel msg <channel> <message>");

                    IChannel channel = this.channelHandler.getChannel(args[1]);

                    if(channel == null)
                        throw new ChannelHandler.ChannelNotFoundException(args[1]);

                    if(!channel.isOnChannel(new ChatEntity(player)))
                        throw new ChannelHandler.ChannelNotJoinedException(channel);

                    String message = StringUtils.join(Arrays.asList(args).subList(2, args.length).toArray(), " ");
                    ITextComponent text = new TextComponentTranslation("chat.type.text", player.getTabListDisplayName(), message);

                    ChatEntity entity = new ChatEntity(player);
                    IChannel current = this.channelHandler.getActiveChannel(entity);

                    this.channelHandler.joinChannel(entity, channel, true);
                    text = ForgeHooks.onServerChatEvent(player.connection, message, text);

                    if(text != null)
                        server.sendMessage(text);

                    this.channelHandler.joinChannel(entity, current, true);
                }
                else
                {
                    if(args.length < 3)
                        throw new CommandException("/channel msg <channel> <message>");

                    IChannel channel = this.channelHandler.getChannel(args[1]);

                    if(channel == null)
                        throw new ChannelHandler.ChannelNotFoundException(args[1]);

                    String message = StringUtils.join(Arrays.asList(args).subList(2, args.length).toArray(), " ");
                    this.channelHandler.broadcastOnChannel(channel, ChatEntity.SERVER, new TextComponentString(message));
                }
            }
            else if(args[0].equalsIgnoreCase("list"))
            {
                if(args.length == 1)
                {
                    TextComponentString text = new TextComponentString("Currently active channels: ");
                    Iterator<IChannel> iterator = this.channelHandler.getChannels().values().iterator();

                    while(iterator.hasNext())
                    {
                        IChannel channel = iterator.next();
                        text.appendText(channel.getName() + " [" + channel.getMembers().size() + "]" + (iterator.hasNext() ? ", " : ""));
                    }

                    sender.sendMessage(text);
                }
                else if(args.length == 2)
                {
                    IChannel channel = this.channelHandler.getChannel(args[1]);

                    if(channel == null)
                        throw new ChannelHandler.ChannelNotFoundException(args[1]);

                    if(isPlayer && !channel.isOnChannel(new ChatEntity(player)))
                        throw new ChannelHandler.ChannelNotJoinedException(channel);

                    sender.sendMessage(new TextComponentString(channel.getMembers().size() + " player(s) active on channel \"" + channel.getName() +"\":"));

                    TextComponentString text = new TextComponentString("");
                    text.appendText("[");

                    Iterator<ChatEntity> iterator = channel.getMembers().iterator();

                    while(iterator.hasNext())
                    {
                        TextComponentString nameComponent = Misc.getComponent(iterator.next());
                        text.appendSibling(nameComponent);

                        if(iterator.hasNext())
                            text.appendText(", ");
                    }

                    text.appendText("]");

                    sender.sendMessage(text);
                }
                else
                {
                    throw new WrongUsageException("/ch list [channel]");
                }
            }
            else if(args[0].equalsIgnoreCase("ban") || args[0].equalsIgnoreCase("unban") || args[0].equalsIgnoreCase("kick") || args[0].equalsIgnoreCase("mute") || args[0].equalsIgnoreCase("unmute"))
            {
                if(Misc.checkPermission(sender, 3))
                {
                    if(args.length != 3)
                        throw new WrongUsageException("/channel " + args[0].toLowerCase(Locale.ROOT) + " <channel> <player>");

                    boolean u = args[0].equalsIgnoreCase("unban") || args[0].equalsIgnoreCase("unmute");

                    IChannel channel = this.channelHandler.getChannel(args[1]);

                    if(channel == null)
                        throw new ChannelHandler.ChannelNotFoundException(args[1]);

                    if((args[0].equalsIgnoreCase("ban") || args[0].equalsIgnoreCase("unban")) && channel.isWhitelisted())
                        throw new CommandException("The channel " + channel.getName() + " is whitelisted. Use /channel whitelist <add/remove> instead.");

                    ChatEntity entity = new ChatEntity(args[2]);

                    if(entity.toPlayer() == null && args[0].equalsIgnoreCase("kick"))
                    {
                        String s = VChat.instance.getNickHandler().getPlayerFromNick(args[2]);

                        if(s != null)
                            entity = new ChatEntity(s);
                    }

                    if(args[0].equalsIgnoreCase("ban") || args[0].equalsIgnoreCase("unban"))
                        channel.ban(isPlayer ? new ChatEntity(player) : ChatEntity.SERVER, entity, u);
                    else if(args[0].equalsIgnoreCase("mute") || args[0].equalsIgnoreCase("unmute"))
                        channel.mute(isPlayer ? new ChatEntity(player) : ChatEntity.SERVER, entity, u);
                    else
                        channel.kick(isPlayer ? new ChatEntity(player) : ChatEntity.SERVER, entity);
                }
            }
            else if(args[0].equalsIgnoreCase("whitelist"))
            {
                if(Misc.checkPermission(sender, 3))
                {
                    if(args.length != 4 || (!args[1].equalsIgnoreCase("add") && !args[1].equalsIgnoreCase("remove")))
                        throw new WrongUsageException("/channel whitelist <add/remove> <channel> <player>");

                    boolean u = args[1].equalsIgnoreCase("remove");
                    IChannel channel = this.channelHandler.getChannel(args[2]);

                    if(channel == null)
                        throw new ChannelHandler.ChannelNotFoundException(args[2]);

                    ChatEntity entity = new ChatEntity(args[3]);
                    channel.ban(isPlayer ? new ChatEntity(player) : ChatEntity.SERVER, entity, u);
                }
            }
            else
            {
                throw new WrongUsageException(getUsage(sender));
            }
        }
    }
}
