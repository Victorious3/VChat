package moe.nightfall.vic.chat.handler;

import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

public interface IChatHandler 
{
	public void onServerLoad(FMLServerStartingEvent event);
	
	public void onServerUnload(FMLServerStoppingEvent event);
}
