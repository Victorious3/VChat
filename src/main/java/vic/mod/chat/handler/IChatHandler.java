package vic.mod.chat.handler;

import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

public interface IChatHandler 
{
	public void onServerLoad(FMLServerStartingEvent event);
	
	public void onServerUnload(FMLServerStoppingEvent event);
}
