package moe.nightfall.vic.chat.channels;

import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.VChat;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import com.google.gson.JsonObject;

public class ChannelCustom extends ChannelBase
{
    private final String name;
    private String prefix;
    private int range = 0;
    private boolean multiDim = true;
    private EnumChatFormatting color = EnumChatFormatting.WHITE;
    private boolean whitelisted = false;
    private boolean autojoin = false;

    public ChannelCustom(VChat instance, String name)
    {
        super(instance);

        this.name = name;
    }

    @Override
    public String getPrefix()
    {
        return prefix;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public IChatComponent formatChat(ChatEntity sender, ChatEntity receiver, IChatComponent message)
    {
        message = super.formatChat(sender, receiver, message);

        if(this.range == 0) return message;
        if(sender.isServer() || sender.isBot()) return message;
        if(sender.equals(receiver)) return message;

        int distance = (int) sender.toPlayer().getDistanceToEntity(receiver.toPlayer());
        ChatComponentText text = new ChatComponentText("[" + distance + "m] ");
        text.appendSibling(message);

        return text;
    }

    @Override
    public void write(JsonObject obj)
    {
        if(this.range != 0)
            obj.addProperty("range", this.range);

        obj.addProperty("color", this.color.name());
        obj.addProperty("whitelisted", this.whitelisted);

        if(this.prefix != null)
            obj.addProperty("prefix", this.prefix);

        obj.addProperty("autojoin", this.autojoin);
        obj.addProperty("multiDim", this.multiDim);

        super.write(obj);
    }

    @Override
    public void read(JsonObject obj)
    {
        if(obj.has("range"))
            this.range = obj.get("range").getAsInt();

        if(obj.has("color"))
            this.color = EnumChatFormatting.getValueByName(obj.get("color").getAsString());

        if(this.color == null)
            this.color = EnumChatFormatting.WHITE;

        if(obj.has("whitelisted"))
            this.whitelisted = obj.get("whitelisted").getAsBoolean();

        if(obj.has("prefix"))
            this.prefix = obj.get("prefix").getAsString();

        if(obj.has("autojoin"))
            this.autojoin = obj.get("autojoin").getAsBoolean();

        if(obj.has("multiDim"))
            this.multiDim = obj.get("multiDim").getAsBoolean();

        super.read(obj);
    }

    @Override
    public EnumChatFormatting getColor()
    {
        return this.color;
    }

    @Override
    public boolean autoJoin(ChatEntity player)
    {
        return this.autojoin;
    }

    @Override
    public boolean isWhitelisted()
    {
        return this.whitelisted;
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

        if(this.range == 0)
        {
            return this.multiDim || player1.worldObj.provider.dimensionId == player2.worldObj.provider.dimensionId;
        }

        int distance = (int) player1.getDistanceToEntity(player2);
        return distance <= this.range && player1.worldObj.provider.dimensionId == player2.worldObj.provider.dimensionId;
    }

    @Override
    public boolean canJoin(ChatEntity player)
    {
        return (this.whitelisted == this.banned.contains(player));
    }

    public boolean hasRange()
    {
        return this.range > 0;
    }
}
