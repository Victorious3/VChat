package moe.nightfall.vic.chat.channels;

import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.VChat;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class ChannelGlobal extends ChannelBase 
{
    public ChannelGlobal(VChat instance)
    {
        super(instance);
    }

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
    public EnumChatFormatting getColor()
    {
        return EnumChatFormatting.WHITE;
    }

    @Override
    public boolean canReceiveChat(ChatEntity sender, ChatEntity receiver, IChatComponent message)
    {
        if (sender.equals(receiver))
            return true;

        EntityPlayerMP player1 = sender.toPlayer();
        EntityPlayerMP player2 = receiver.toPlayer();

        return player1 == null || player2 == null || (player1.worldObj.provider.getDimensionId() == player2.worldObj.provider.getDimensionId()) || Config.globalCrossDimEnabled;

    }
}
