package moe.nightfall.vic.chat.api.bot;

import java.io.File;
import java.util.List;

public interface IBotHandler 
{
	public IChannelBase getChannelForName(String name);
	
	/** The global channel **/
	public IChannelBase getDefaultChannel();
	
	public IChatEntity getChatEntityForName(String name);
	
	public IChatEntity getServer();
	
	/** A list of every {@link IChatEntity} connected to the server. Includes bots.**/
	public List<IChatEntity> getAllChatEntities();
	
	public IChatBot getBotForName(String name);
	
	public boolean isPlayerOnline(String name);
	
	public boolean isOnChannel(IChatEntity entity, IChannelBase channel);
	
	/** Should be used with caution, will have the same priority as the server sending a message. Appears on every channel **/
	public void sendGlobalMessage(String message);
	
	public void sendMessage(IChannelBase channel, String message);
	
	public void sendPrivateMessage(IChatEntity entity, String message);
	
	public void sendCommand(String command, String[] args);
	
	public void log(LogLevel level, String message);
	
	/** Uses {@link LogLevel#INFO} **/
	public void log(String message);
	
	/** Equivalent of log(level, String.format(message, args)) **/
	public void logf(LogLevel level, String message, Object... args);
	
	/** Uses {@link LogLevel#INFO} **/
	public void logf(String message, Object... args);
	
	public File getBotDir();
}
