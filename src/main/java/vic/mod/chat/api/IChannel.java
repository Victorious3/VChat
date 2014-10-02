package vic.mod.chat.api;

import java.util.Collection;

import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import vic.mod.chat.ChatEntity;
import vic.mod.chat.api.bot.IChannelBase;

import com.google.gson.JsonObject;

public interface IChannel extends IChannelBase
{
	public IChatComponent formatChat(ChatEntity sender, ChatEntity receiver, IChatComponent message);
	
	@Override
	public String getPrefix();
	
	@Override
	public String getName();

	public boolean isOnChannel(ChatEntity player);
	
	public boolean isMuted(ChatEntity player);
	
	@Override
	public boolean isWhitelisted();
	
	public boolean canReceiveChat(ChatEntity sender, ChatEntity receiver, IChatComponent message);
	
	public boolean canJoin(ChatEntity player);
	
	public boolean autoJoin(ChatEntity player);
	
	public void onJoin(ChatEntity player, boolean initial);
	
	public void onLeave(ChatEntity player, boolean initial);
	
	public void mute(ChatEntity issuer, ChatEntity player, boolean unmute);
	
	public void kick(ChatEntity issuer, ChatEntity player);
	
	public void ban(ChatEntity issuer, ChatEntity player, boolean unban);
	
	public void write(JsonObject obj);
	
	public void read(JsonObject obj);
	
	public EnumChatFormatting getColor();
	
	public Collection<ChatEntity> getMembers();
}
