package vic.mod.chat.api.bot;

public interface IChatEntity 
{
	public boolean isServer();
	
	public boolean isBot();
	
	/** Will return true for opped players, bots and the server entity. **/
	public boolean isOperator();
	
	public String getUsername();
	
	public String getNickname();
	
	public String getDisplayName();
}
