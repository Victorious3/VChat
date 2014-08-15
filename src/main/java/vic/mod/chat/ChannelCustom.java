package vic.mod.chat;

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
	
	public ChannelCustom(String name)
	{
		this.name = name;
	}

	@Override
	public void write(JsonObject obj) 
	{
		if(range != 0) obj.addProperty("range", range);
		obj.addProperty("color", color.name());
		obj.addProperty("whitelisted", whitelisted);
		if(prefix != null) obj.addProperty("prefix", prefix);
		obj.addProperty("autojoin", autojoin);
		
		super.write(obj);
	}

	@Override
	public void read(JsonObject obj) 
	{
		if(obj.has("range")) range = obj.get("range").getAsInt();
		if(obj.has("color")) color = EnumChatFormatting.getValueByName(obj.get("color").getAsString());
		if(color == null) color = EnumChatFormatting.WHITE;
		if(obj.has("whitelisted")) whitelisted = obj.get("whitelisted").getAsBoolean();
		if(obj.has("prefix")) prefix = obj.get("prefix").getAsString();
		if(obj.has("autojoin")) autojoin = obj.get("autojoin").getAsBoolean();
		
		super.read(obj);
	}
	
	@Override
	public boolean canJoin(ChatEntity player) 
	{
		if(whitelisted) return banned.contains(player);
		return !banned.contains(player);
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
	public EnumChatFormatting getColor() 
	{
		return color;
	}
	
	@Override
	public boolean canReceiveChat(ChatEntity sender, ChatEntity receiver, IChatComponent message) 
	{
		if(receiver.isServer() || sender.isServer() || sender.equals(receiver)) return true;
		
		EntityPlayerMP player1 = sender.toPlayer();
		EntityPlayerMP player2 = receiver.toPlayer();
		
		if(range == 0)
		{
			if(!multiDim) return player1.worldObj.provider.dimensionId == player2.worldObj.provider.dimensionId;
			return true;
		}
		
		int distance = (int)player1.getDistanceToEntity(player2);
		return distance <= range && player1.worldObj.provider.dimensionId == player2.worldObj.provider.dimensionId;
	}

	@Override
	public IChatComponent formatChat(ChatEntity sender, ChatEntity receiver, IChatComponent message) 
	{
		message = super.formatChat(sender, receiver, message);
		if(range == 0) return message;
		
		if(sender.equals(ChatEntity.SERVER)) return message;
		if(sender.equals(receiver)) return message;
		
		int distance = (int)sender.toPlayer().getDistanceToEntity(receiver.toPlayer());
		ChatComponentText comp = new ChatComponentText("[" + distance + "m] ");
		comp.appendSibling(message);
		
		return comp;
	}

	@Override
	public boolean autoJoin(ChatEntity player) 
	{
		return autojoin;
	}
}
