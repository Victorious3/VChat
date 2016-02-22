package moe.nightfall.vic.chat.api.bot;

import java.io.File;
import java.util.List;

public interface IBotHandler 
{
    /** Should be used with caution, will have the same priority as the server sending a message. Appears on every channel **/
    void sendGlobalMessage(String message);

    void sendMessage(IChannelBase channel, String message);

    void sendPrivateMessage(IChatEntity entity, String message);

    void sendCommand(String command, String[] args);

    void log(LogLevel level, String message);

    /** Uses {@link LogLevel#INFO} **/
    void log(String message);

    /** Equivalent of log(level, String.format(message, args)) **/
    void logf(LogLevel level, String message, Object... args);

    /** Uses {@link LogLevel#INFO} **/
    void logf(String message, Object... args);

    IChannelBase getChannelForName(String name);

    /** The global channel **/
    IChannelBase getDefaultChannel();

    IChatEntity getChatEntityForName(String name);

    IChatEntity getServer();

    /** A list of every {@link IChatEntity} connected to the server. Includes bots.**/
    List<IChatEntity> getAllChatEntities();

    IChatBot getBotForName(String name);

    File getBotDir();

    boolean isPlayerOnline(String name);

    boolean isOnChannel(IChatEntity entity, IChannelBase channel);
}
