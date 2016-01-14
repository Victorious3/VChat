package moe.nightfall.vic.chat;

import java.io.File;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;

import moe.nightfall.vic.chat.handler.AFKHandler;
import moe.nightfall.vic.chat.handler.AutoAFKHandler;
import moe.nightfall.vic.chat.handler.ChannelHandler;
import moe.nightfall.vic.chat.handler.CommonHandler;
import moe.nightfall.vic.chat.handler.IChatHandler;
import moe.nightfall.vic.chat.handler.NickHandler;
import moe.nightfall.vic.chat.handler.TrackHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = "vchat", name = "vChat", version = Constants.version, acceptableRemoteVersions = "*", acceptedMinecraftVersions = "1.8, 1.8.8, 1.8.9")
public class VChat
{	
	@Instance("vchat")
	public static VChat instance;  
	
	public static BotLoader botLoader;
	
	public static CommonHandler commonHandler;
	public static ChannelHandler channelHandler;
	public static AFKHandler afkHandler;
	public static AutoAFKHandler autoAfkHandler;
	public static NickHandler nickHandler;
	public static TrackHandler trackHandler;
	
	public static Config config;
	public static ArrayList<IChatHandler> handlers = new ArrayList<IChatHandler>();
	public static Logger logger;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) 
	{
		logger = event.getModLog();
		
		config = new Config();
		config.initialize(event.getSuggestedConfigurationFile());
		
		File rootDir = event.getModConfigurationDirectory().getParentFile();
		rootDir.setWritable(true);
		commonHandler = new CommonHandler();
		channelHandler = new ChannelHandler();
		botLoader = new BotLoader();
		
		if(config.nickEnabled) nickHandler = new NickHandler();
		if(config.trackEnabled) trackHandler = new TrackHandler();
		if(config.afkEnabled) 
		{
			afkHandler = new AFKHandler();
			if(config.autoAfkEnabled) autoAfkHandler = new AutoAFKHandler();
		}
	}
	
	@EventHandler
	public void onServerLoad(FMLServerStartingEvent event)
	{
		for(IChatHandler handler : handlers)
		{
			handler.onServerLoad(event);
		}
	}
	
	@EventHandler
	public void onServerUnload(FMLServerStoppingEvent event)
	{
		for(IChatHandler handler : handlers)
		{
			handler.onServerUnload(event);
		}
	}
}