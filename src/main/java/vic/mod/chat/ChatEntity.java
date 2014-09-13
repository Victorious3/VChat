package vic.mod.chat;

import vic.mod.chat.handler.NickHandler;
import net.minecraft.entity.player.EntityPlayerMP;

public class ChatEntity 
{
	private final String username;
	
	public static ChatEntity SERVER = new ChatEntity((String)null);
	
	public ChatEntity(String username)
	{
		this.username = username;
	}
	
	public ChatEntity(EntityPlayerMP player)
	{
		this.username = player.getCommandSenderName();
	}
	
	public ChatEntity(Object obj) 
	{
		if(obj instanceof EntityPlayerMP) this.username = ((EntityPlayerMP)obj).getCommandSenderName();
		else if(obj instanceof String)
		{
			if(Misc.getPlayer((String)obj) != null) 
				this.username = (String)obj;
			else if(Config.nickEnabled)
			{
				String player = NickHandler.getPlayerFromNick((String)obj);
				if(player != null) this.username = player;
				else this.username = (String)obj;
			}
			else this.username = (String)obj;
		}
		else this.username = null;
	}

	public EntityPlayerMP toPlayer()
	{
		return Misc.getPlayer(username);
	}
	
	public boolean isServer()
	{
		return this == SERVER;
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public String getNickname()
	{
		if(Config.nickEnabled) return NickHandler.nickRegistry.get(this.username);
		return null;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if(obj == null) return false;
		if(this.username == null) return obj == SERVER;
		else if(obj instanceof ChatEntity)
			return this.username.equalsIgnoreCase(((ChatEntity)obj).username);
		else if(obj instanceof EntityPlayerMP)
			return this.username.equalsIgnoreCase(((EntityPlayerMP)obj).getCommandSenderName());
		else if(obj instanceof String)
			if(((String)obj).equalsIgnoreCase(this.username)) return true;
			else if(Config.nickEnabled)
			{
				String nick = getNickname();
				if(nick != null && nick.equalsIgnoreCase((String)obj)) return true;
			}
		
		return false;
	}

	@Override
	public String toString() 
	{
		if(username == null) return "SERVER";
		return username;
	}

	@Override
	public int hashCode() 
	{
		return username.hashCode();
	}
}
