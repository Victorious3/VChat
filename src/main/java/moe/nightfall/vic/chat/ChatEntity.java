package moe.nightfall.vic.chat;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import moe.nightfall.vic.chat.api.bot.IChatEntity;
import moe.nightfall.vic.chat.handler.NickHandler;

public class ChatEntity implements IChatEntity
{
	private final String username;
	private boolean isBot = false;
	
	public static ChatEntity SERVER = new ChatEntity((String)null);
	
	public ChatEntity(String username, boolean isBot)
	{
		this.isBot = isBot;
		this.username = username;
	}
	
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
	
	@Override
	public boolean isServer()
	{
		return this == SERVER;
	}
	
	@Override
	public boolean isBot()
	{
		return isBot;
	}
	
	@Override
	public boolean isOperator() 
	{
		if(isBot() || isServer()) return true;
		EntityPlayerMP player = toPlayer();
		if(player == null) return false;
		return player.canCommandSenderUseCommand(MinecraftServer.getServer().getOpPermissionLevel(), null);
	}

	@Override
	public String getUsername()
	{
		return username;
	}
	
	@Override
	public String getNickname()
	{
		if(Config.nickEnabled)
			if(isBot) return username;
			else return NickHandler.nickRegistry.get(this.username);
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

	@Override
	public String getDisplayName() 
	{
		String nick = getNickname();
		if(nick != null) return nick;
		return toString();
	}
}
