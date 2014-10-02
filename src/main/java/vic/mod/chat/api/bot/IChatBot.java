package vic.mod.chat.api.bot;

public interface IChatBot
{
	public void onLoad(IBotHandler handler);
	
	public void onUnload();
	
	public String getName();
	
	public void onMessage(String message, IChatEntity sender, IChannelBase channel);
	
	public void onPrivateMessage(String message, IChatEntity sender);
}
