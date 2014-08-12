package vic.mod.chat;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import com.google.gson.JsonObject;

public abstract class ChannelBase implements IChannel
{
	public ArrayList<ChatEntity> joined = new ArrayList<ChatEntity>();
	public ArrayList<ChatEntity> muted = new ArrayList<ChatEntity>();
	public ArrayList<ChatEntity> banned = new ArrayList<ChatEntity>();
	
	@Override
	public IChatComponent formatChat(ChatEntity sender, ChatEntity receiver, IChatComponent message) 
	{
		if(sender.isServer()) 
		{
			ChatComponentText comp = new ChatComponentText("*");
			comp.getChatStyle().setItalic(true);
			comp.getChatStyle().setColor(EnumChatFormatting.GRAY);
			comp.appendSibling(message);
			return comp;
		}
		return message;
	}

	@Override
	public boolean isMuted(ChatEntity player) 
	{
		return muted.contains(player);
	}

	@Override
	public boolean canReceiveChat(ChatEntity sender, ChatEntity receiver, IChatComponent message) 
	{
		return true;
	}

	@Override
	public boolean canJoin(ChatEntity player) 
	{
		return !banned.contains(player);
	}

	@Override
	public void onJoin(ChatEntity player, boolean initial) 
	{
		if(!joined.contains(player))
		{
			if(!initial) ChannelHandler.broadcastOnChannel(this, ChatEntity.SERVER, new ChatComponentText(player + " joined the channel."));
			joined.add(player);
		}
	}

	@Override
	public void mute(ChatEntity issuer, ChatEntity player, boolean unmute) 
	{
		ChannelHandler.broadcastOnChannel(this, ChatEntity.SERVER, new ChatComponentText(player + " was " + (unmute ? "unmuted" : "muted") +" by " + issuer));
		if(unmute) muted.remove(player);
		else muted.add(player);
	}

	@Override
	public void onLeave(ChatEntity player, boolean initial) 
	{
		if(!joined.contains(player)) return;
		joined.remove(player);
		if(!initial) ChannelHandler.broadcastOnChannel(this, ChatEntity.SERVER, new ChatComponentText(player + " left the channel."));
	}

	@Override
	public void kick(ChatEntity issuer, ChatEntity player) 
	{
		if(!joined.contains(player))
		{
			ChannelHandler.privateMessageOnChannel(this, ChatEntity.SERVER, issuer, new ChatComponentText("There is no player called \"" + player.getUsername() + "\" on the channel!"));
			return;
		}
		ChannelHandler.broadcastOnChannel(this, ChatEntity.SERVER, new ChatComponentText(player + " was kicked from the channel by " + issuer));
		ChannelHandler.leaveChannel(player, this);
	}

	@Override
	public void ban(ChatEntity issuer, ChatEntity player, boolean unban) 
	{
		if(unban) 
		{
			if(banned.contains(player))
			{
				banned.remove(player);
				ChannelHandler.privateMessageOnChannel(this, ChatEntity.SERVER, issuer, new ChatComponentText("You unbanned " + player.getUsername() + " from the channel."));
			}
			else ChannelHandler.privateMessageOnChannel(this, ChatEntity.SERVER, issuer, new ChatComponentText("You haven't banned " + player.getUsername() + " from the channel!"));
		}
		else if(!unban)
		{
			if(!banned.contains(player))
			{
				banned.add(player);
				ChannelHandler.broadcastOnChannel(this, ChatEntity.SERVER, new ChatComponentText(player + " was banned from the channel by " + issuer));
				if(joined.contains(player)) ChannelHandler.leaveChannel(player, this);
			}
			else ChannelHandler.privateMessageOnChannel(this, ChatEntity.SERVER, issuer, new ChatComponentText("You already banned " + player.getUsername() + " from the channel!"));
		}
	}

	@Override
	public void write(JsonObject obj) 
	{
		
	}

	@Override
	public void read(JsonObject obj) 
	{
		
	}

	@Override
	public boolean isOnChannel(ChatEntity player) 
	{
		return joined.contains(player);
	}

	@Override
	public Collection<ChatEntity> getMembers() 
	{
		return joined;
	}	
}
