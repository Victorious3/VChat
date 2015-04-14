package moe.nightfall.vic.chat.api.bot;

public interface IChannelBase 
{
	public String getPrefix();
	
	public String getName();

	public boolean isWhitelisted();
}
