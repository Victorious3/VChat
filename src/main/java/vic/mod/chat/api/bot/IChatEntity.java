package vic.mod.chat.api.bot;

public interface IChatEntity 
{
	public boolean isServer();
	
	public boolean isBot();
	
	public String getUsername();
	
	public String getNickname();
}
