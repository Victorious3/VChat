package vic.mod.chat;

import net.minecraft.util.ChatComponentText;

public interface IChatFormatter 
{
	public void apply(ChatComponentText text);
	
	public boolean matched();
}
