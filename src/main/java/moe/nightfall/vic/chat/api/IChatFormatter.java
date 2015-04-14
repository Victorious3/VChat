package moe.nightfall.vic.chat.api;

import net.minecraft.util.ChatComponentText;

public interface IChatFormatter 
{
	public void apply(ChatComponentText text);
	
	public boolean matched();
}
