package moe.nightfall.vic.chat.channels;

import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.VChat;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

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
    public TextFormatting getColor()
    {
        return TextFormatting.WHITE;
    }

    @Override
    public boolean canReceiveChat(ChatEntity sender, ChatEntity receiver, ITextComponent message)
    {
        if (sender.equals(receiver))
            return true;

        EntityPlayerMP player1 = sender.toPlayer();
        EntityPlayerMP player2 = receiver.toPlayer();

        return player1 == null || player2 == null || (player1.getEntityWorld().provider.getDimension() == player2.getEntityWorld().provider.getDimension()) || Config.globalCrossDimEnabled;

    }
}
