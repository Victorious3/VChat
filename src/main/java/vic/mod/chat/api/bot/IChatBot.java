package vic.mod.chat.api.bot;

public interface IChatBot
{
	public void onLoad(IBotHandler handler);
	
	public void onServerLoad();
	
	public void onServerUnload();
	
	public String getName();
	
	public void onMessage(String message, IChatEntity sender, IChannelBase channel);
	
	public void onPrivateMessage(String message, IChatEntity sender);
	
	/** A callback if you use {@link IBotHandler#sendCommand(String, String[])} **/
	public void onCommandMessage(String command, String[] args, String message);
}
