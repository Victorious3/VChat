package moe.nightfall.vic.chat.bots;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.Misc;
import moe.nightfall.vic.chat.VChat;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;

import moe.nightfall.vic.chat.api.IChannel;
import moe.nightfall.vic.chat.api.bot.IBotHandler;
import moe.nightfall.vic.chat.api.bot.IChannelBase;
import moe.nightfall.vic.chat.api.bot.IChatBot;
import moe.nightfall.vic.chat.api.bot.IChatEntity;
import moe.nightfall.vic.chat.api.bot.LogLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class BotHandler implements IBotHandler
{
    private final VChat instance;
    private final IChatBot owningBot;
    private final ChatEntity botEntity;
    private final BotCommandSender botSender;

    public BotHandler(VChat instance, IChatBot owningBot)
    {
        this.instance = instance;
        this.owningBot = owningBot;
        this.botEntity = new ChatEntity(owningBot.getName(), true);
        this.botSender = new BotCommandSender(this.owningBot, this.botEntity);
    }

    @Override
    public void sendGlobalMessage(String message)
    {
        ChatComponentText text = new ChatComponentText("");
        text.appendSibling(Misc.getComponent(this.botEntity));
        text.appendText(": " + message);

        this.instance.getChannelHandler().broadcast(text, this.botEntity);
        MinecraftServer.getServer().addChatMessage(text);
    }

    @Override
    public void sendMessage(IChannelBase channel, String message)
    {
        ChatComponentText text = new ChatComponentText("");
        text.appendSibling(Misc.getComponent(this.botEntity));
        text.appendText(": " + message);

        this.instance.getChannelHandler().broadcastOnChannel((IChannel) channel, this.botEntity, text);
        MinecraftServer.getServer().addChatMessage(text);
    }

    @Override
    public void sendPrivateMessage(IChatEntity entity, String message)
    {
        ChatComponentText text = new ChatComponentText("");
        text.appendSibling(Misc.getComponent(this.botEntity));

        ChatComponentText mcomp = new ChatComponentText(" whispers to you: " + message);
        mcomp.getChatStyle().setItalic(true);
        mcomp.getChatStyle().setColor(EnumChatFormatting.GRAY);

        text.appendSibling(mcomp);

        this.instance.getChannelHandler().privateMessageTo(this.botEntity, (ChatEntity)entity, text);
    }

    @Override
    public void sendCommand(String command, String[] args)
    {
        this.botSender.setActiveCommand(command);
        this.botSender.setActiveArgs(args);

        try
        {
            MinecraftServer.getServer().getCommandManager().executeCommand(this.botSender, command + " " + StringUtils.join(Arrays.asList(args), " "));
        }
        catch (Exception e)
        {
            this.botSender.addChatMessage(new ChatComponentText("$COMMANDEXECFAILED " + e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    @Override
    public void log(LogLevel level, String message)
    {
        message = "[" + this.owningBot.getName() + "]: " + message;
        this.instance.getLogger().log(Level.toLevel(level.name(), Level.INFO), message);
    }

    @Override
    public void log(String message)
    {
        message = "[" + this.owningBot.getName() + "]: " + message;
        this.instance.getLogger().log(Level.INFO, message);
    }

    @Override
    public void logf(LogLevel level, String message, Object... args)
    {
        message = "[" + this.owningBot.getName() + "]: " + message;
        this.instance.getLogger().log(Level.toLevel(level.name(), Level.INFO), message, args);
    }

    @Override
    public void logf(String message, Object... args)
    {
        message = "[" + this.owningBot.getName() + "]: " + message;
        this.instance.getLogger().log(Level.INFO, message, args);
    }

    @Override
    public IChannelBase getChannelForName(String name)
    {
        return this.instance.getChannelHandler().getChannel(name);
    }

    @Override
    public IChannelBase getDefaultChannel()
    {
        return this.instance.getChannelHandler().getChannel("global");
    }

    @Override
    public IChatEntity getChatEntityForName(String name)
    {
        return new ChatEntity(Misc.getPlayer(name));
    }

    @Override
    public IChatEntity getServer()
    {
        return ChatEntity.SERVER;
    }

    public IChatBot getOwningBot()
    {
        return this.owningBot;
    }

    public ChatEntity getBotEntity()
    {
        return this.botEntity;
    }

    @Override
    public List<IChatEntity> getAllChatEntities()
    {
        List<IChatEntity> list = new ArrayList<IChatEntity>();
        list.addAll(Misc.getOnlinePlayersAsEntity());

        for(BotHandler bot : this.instance.getBotLoader().getBots().values())
            list.add(bot.botEntity);

        return list;
    }

    @Override
    public boolean isPlayerOnline(String name)
    {
        return this.getChannelForName(name) != null;
    }

    @Override
    public boolean isOnChannel(IChatEntity entity, IChannelBase channel)
    {
        return entity != null && this.instance.getChannelHandler().getJoinedChannels(entity).contains(channel);
    }

    @Override
    public File getBotDir()
    {
        return new File("vChat/bots");
    }

    @Override
    public IChatBot getBotForName(String name)
    {
        return this.instance.getBotLoader().getBot(name).owningBot;
    }
}
