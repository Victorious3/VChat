package vic.mod.chat;

import java.io.File;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = "vchat", name = "vChat", version = "0.1r11", acceptableRemoteVersions = "*")
public class VChat {
	
	@Instance("vchat")
	public static VChat instance;  
	
	public static CommonHandler commonHandler;
	public static ChannelHandler channelHandler;
	public static NickHandler nickHandler;
	public static Config config;
	
	public static Logger logger;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) 
	{
		logger = event.getModLog();
		/*if(event.getSide() == Side.CLIENT) {
			logger.fatal("This mod is meant to be used for dedicated servers only! You should remove it!");
			return;
		}*/
		
		config = new Config();
		config.initialize(event.getSuggestedConfigurationFile());
		
		File rootDir = event.getModConfigurationDirectory().getParentFile();
		rootDir.setWritable(true);
		commonHandler = new CommonHandler();
		channelHandler = new ChannelHandler();
		if(config.nickEnabled) nickHandler = new NickHandler();			
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		
	}
	
	@EventHandler
	public void onServerLoad(FMLServerStartingEvent event)
	{
		if(config != null)
		{
			if(config.nickEnabled) nickHandler.onServerLoad(event);
			channelHandler.onServerLoad(event);
		}
	}
	
	@EventHandler
	public void onServerUnload(FMLServerStoppingEvent event)
	{
		if(config != null) 
		{
			if(config.nickEnabled) nickHandler.onServerUnload(event);
			channelHandler.onServerUnload(event);
		}
	}
}
