package moe.nightfall.vic.chat.handler;

import moe.nightfall.vic.chat.VChat;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

public abstract class ChatHandlerImpl implements IChatHandler
{
	public ChatHandlerImpl()
	{
		MinecraftForge.EVENT_BUS.register(this);
		VChat.handlers.add(this);
	}
	
	@Override public void onServerLoad(FMLServerStartingEvent event) {}
	@Override public void onServerUnload(FMLServerStoppingEvent event) {}
}
