package vic.mod.chat;

import java.io.File;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import vic.mod.chat.api.IChannel;
import vic.mod.chat.api.bot.IBotHandler;
import vic.mod.chat.api.bot.IChannelBase;
import vic.mod.chat.api.bot.IChatBot;
import vic.mod.chat.api.bot.IChatEntity;
import vic.mod.chat.handler.ChannelHandler;

public class BotHandler implements IBotHandler
{
	public IChatBot owningBot;
	public ChatEntity botEntity;
	
	public BotHandler(IChatBot owningBot) 
	{
		this.owningBot = owningBot;
		botEntity = new ChatEntity(owningBot.getName(), true);
	}
	
	@Override
	public IChannelBase getChannelForName(String name) 
	{
		return ChannelHandler.getChannel(name);
	}

	@Override
	public IChannelBase getDefaultChannel() 
	{
		return ChannelHandler.getChannel("global");
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

	@Override
	public boolean isPlayerOnline(String name) 
	{
		return getChannelForName(name) != null;
	}

	@Override
	public boolean isOnChannel(IChatEntity entity, IChannelBase channel) 
	{
		if(entity == null) return false;
		return ChannelHandler.getJoinedChannels(entity).contains(channel);
	}

	@Override
	public void sendGlobalMessage(String message) 
	{
		ChatComponentText text = new ChatComponentText("");
		text.appendSibling(Misc.getComponent(botEntity));
		text.appendText(": " + message);
		ChannelHandler.broadcast(text);
	}

	@Override
	public void sendMessage(IChannelBase channel, String message) 
	{
		ChatComponentText text = new ChatComponentText("");
		text.appendSibling(Misc.getComponent(botEntity));
		text.appendText(": " + message);
		ChannelHandler.broadcastOnChannel((IChannel)channel, botEntity, text);
	}

	@Override
	public void sendPrivateMessage(IChatEntity entity, String message) 
	{
		EntityPlayerMP player = Misc.getPlayer(entity.getUsername());
		
		ChatComponentText text = new ChatComponentText("");
		text.appendSibling(Misc.getComponent(botEntity));
		ChatComponentText mcomp = new ChatComponentText(" whispers to you: " + message);
		mcomp.getChatStyle().setItalic(true);
		mcomp.getChatStyle().setColor(EnumChatFormatting.GRAY);
		text.appendSibling(mcomp);
		
		if(player != null) player.addChatComponentMessage(text);
		else if(entity.isBot()) VChat.botLoader.getBot(entity.getUsername()).owningBot.onPrivateMessage(message, botEntity);
		else if(entity.isServer()) MinecraftServer.getServer().addChatMessage(text);
	}

	@Override
	public File getBotDir()
	{
		return new File("/vBots");
	}
}
