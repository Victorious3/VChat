package moe.nightfall.vic.chat.channels;

import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.VChat;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class ChannelLocal extends ChannelBase
{
    public ChannelLocal(VChat instance)
    {
        super(instance);
    }

    @Override
    public String getPrefix()
    {
        return this.getName();
    }

    @Override
    public String getName()
    {
        return "local";
    }

    @Override
    public ITextComponent formatChat(ChatEntity sender, ChatEntity receiver, ITextComponent message)
    {
        message = super.formatChat(sender, receiver, message);

        if(sender.isServer() || sender.isBot()) return message;
        if(sender.equals(receiver)) return message;

        int distance = (int)(sender.toPlayer()).getDistanceToEntity(receiver.toPlayer());
        TextComponentString text = new TextComponentString("[" + distance + "m] ");
        text.appendSibling(message);

        return text;
    }

    @Override
    public TextFormatting getColor()
    {
        return TextFormatting.YELLOW;
    }

    @Override
    public boolean canReceiveChat(ChatEntity sender, ChatEntity receiver, ITextComponent message)
    {
        if(sender.equals(receiver))
            return true;

        EntityPlayerMP player1 = sender.toPlayer();
        EntityPlayerMP player2 = receiver.toPlayer();

        if(player1 == null || player2 == null)
            return true;

        int distance = (int)player1.getDistanceToEntity(player2);
        return distance <= Config.localRange && player1.getEntityWorld().provider.getDimension() == player2.getEntityWorld().provider.getDimension();
    }
}
