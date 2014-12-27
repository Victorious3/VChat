package vic.mod.chat;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class ChannelGlobal extends ChannelBase 
{

	@Override
	public String getPrefix() 
	{
		return null;
	}

	@Override
	public String getName() 
	{
		return "global";
	}

	@Override
	public boolean canReceiveChat(ChatEntity sender, ChatEntity receiver, IChatComponent message) 
	{
		if(sender.equals(receiver)) return true;
		
		EntityPlayerMP player1 = sender.toPlayer();
		EntityPlayerMP player2 = receiver.toPlayer();
		
		if(player1 == null || player2 == null) return true;

		return (player1.worldObj.provider.dimensionId == player2.worldObj.provider.dimensionId) || Config.globalCrossDimEnabled;
	}

	@Override
	public EnumChatFormatting getColor() 
	{
		return EnumChatFormatting.WHITE;
	}
}
