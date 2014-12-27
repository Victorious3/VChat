package vic.mod.chat.api.bot;

/**
 * To use the Bot API, simply put this interface on any class you desire.
 * It has to define an empty constructor or else the initialization will fail.
 * Don't forget to put the {@link Version} annotation on your class.
 * Every interaction with the server is done via the {@link IBotHandler} 
 * that you get from the onLoad method.
 * 
 * @author "VicNightfall"
 */
public interface IChatBot
{
	public void onLoad(IBotHandler handler);
	
	public void onServerLoad();
	
	public void onServerUnload();
	
	/** Is getting called on the server tick, every 50ms. 
	 * For time-intensive tasks, please create your own {@link Thread}. **/
	public void onTick();
	
	public String getName();
	
	public void onMessage(String message, IChatEntity sender, IChannelBase channel);
	
	public void onPrivateMessage(String message, IChatEntity sender);
	
	/** A callback if you use {@link IBotHandler#sendCommand(String, String[])} **/
	public void onCommandMessage(String command, String[] args, String message);
}
