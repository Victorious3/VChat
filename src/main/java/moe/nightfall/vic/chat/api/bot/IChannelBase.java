package moe.nightfall.vic.chat.api.bot;

public interface IChannelBase 
{
    String getPrefix();

    String getName();

    boolean isWhitelisted();
}
