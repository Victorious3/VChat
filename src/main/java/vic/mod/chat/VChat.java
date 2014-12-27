package vic.mod.chat;

import java.io.File;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;

import vic.mod.chat.handler.AFKHandler;
import vic.mod.chat.handler.AutoAFKHandler;
import vic.mod.chat.handler.ChannelHandler;
import vic.mod.chat.handler.CommonHandler;
import vic.mod.chat.handler.IChatHandler;
import vic.mod.chat.handler.NickHandler;
import vic.mod.chat.handler.TrackHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = "vchat", name = "vChat", version = Constants.version, acceptableRemoteVersions = "*")
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
