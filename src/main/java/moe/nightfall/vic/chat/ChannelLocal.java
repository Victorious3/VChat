package moe.nightfall.vic.chat;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class ChannelLocal extends ChannelBase
{	
	@Override
	public String getPrefix() 
	{
		return getName();
	}

	@Override
	public String getName() 
	{
		return "local";
	}

	@Override
	public EnumChatFormatting getColor() 
	{
		return EnumChatFormatting.YELLOW;
	}

	@Override
	public IChatComponent formatChat(ChatEntity sender, ChatEntity receiver, IChatComponent message) 
	{
		message = super.formatChat(sender, receiver, message);
		if(sender.isServer() || sender.isBot()) return message;
		if(sender.equals(receiver)) return message;
		
		int distance = (int)(sender.toPlayer()).getDistanceToEntity(receiver.toPlayer());
		ChatComponentText comp = new ChatComponentText("[" + distance + "m] ");
		comp.appendSibling(message);
		
		return comp;
	}

	@Override
	public boolean canReceiveChat(ChatEntity sender, ChatEntity receiver, IChatComponent message) 
	{
		if(sender.equals(receiver)) return true;
		
		EntityPlayerMP player1 = sender.toPlayer();
		EntityPlayerMP player2 = receiver.toPlayer();
		
		if(player1 == null || player2 == null) return true;
		
		int distance = (int)player1.getDistanceToEntity(player2);
		return distance <= Config.localRange && player1.worldObj.provider.dimensionId == player2.worldObj.provider.dimensionId;
	}
}
