package moe.nightfall.vic.chat.channels;

import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.VChat;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

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
    public IChatComponent formatChat(ChatEntity sender, ChatEntity receiver, IChatComponent message)
    {
        message = super.formatChat(sender, receiver, message);

        if(sender.isServer() || sender.isBot()) return message;
        if(sender.equals(receiver)) return message;

        int distance = (int)(sender.toPlayer()).getDistanceToEntity(receiver.toPlayer());
        ChatComponentText text = new ChatComponentText("[" + distance + "m] ");
        text.appendSibling(message);

        return text;
    }

    @Override
    public EnumChatFormatting getColor()
    {
        return EnumChatFormatting.YELLOW;
    }

    @Override
    public boolean canReceiveChat(ChatEntity sender, ChatEntity receiver, IChatComponent message)
    {
        if(sender.equals(receiver))
            return true;

        EntityPlayerMP player1 = sender.toPlayer();
        EntityPlayerMP player2 = receiver.toPlayer();

        if(player1 == null || player2 == null)
            return true;

        int distance = (int)player1.getDistanceToEntity(player2);
        return distance <= Config.localRange && player1.worldObj.provider.getDimensionId() == player2.worldObj.provider.getDimensionId();
    }
}
