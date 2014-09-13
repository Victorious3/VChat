package vic.mod.chat.handler;

import net.minecraftforge.common.MinecraftForge;
import vic.mod.chat.VChat;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

public abstract class ChatHandlerImpl implements IChatHandler
{
	public ChatHandlerImpl()
	{
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		VChat.handlers.add(this);
	}
	
	@Override public void onServerLoad(FMLServerStartingEvent event) {}
	@Override public void onServerUnload(FMLServerStoppingEvent event) {}
}
