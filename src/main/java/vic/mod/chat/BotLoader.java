package vic.mod.chat;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import vic.mod.chat.api.IChannel;
import vic.mod.chat.api.bot.IChatBot;
import vic.mod.chat.handler.ChannelHandler;
import vic.mod.chat.handler.ChatHandlerImpl;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

public class BotLoader extends ChatHandlerImpl
{
	public HashMap<String, BotHandler> bots = new HashMap<String, BotHandler>();

	public BotLoader()
	{
		super();
		loadBots();	
	}
	
	public boolean containsBot(String name)
	{
		return bots.containsKey(name);
	}
	
	public BotHandler getBot(String name)
	{
		return bots.get(name);
	}
	
	public void loadBots()
	{
		try {
			File botDir = new File("vBots/");
			if(!botDir.exists()) botDir.mkdirs();
			
			VChat.logger.info("Attempting to load bots...");
			
			int loaded = 0;
			
			for(File botFile : botDir.listFiles())
			{
				if(botFile.getName().endsWith(".jar"))
				{
					VChat.logger.info("Attempting to load bots from file \"" + botFile.getName() + "\"...");
					try {
						JarFile file = new JarFile(botFile);
						URLClassLoader classLoader = new URLClassLoader(new URL[]{botFile.toURI().toURL()}, getClass().getClassLoader());
						Enumeration<JarEntry> enumeration = file.entries();
						
						while(enumeration.hasMoreElements())
						{
							JarEntry entry = enumeration.nextElement();
							if(entry.isDirectory() || !entry.getName().endsWith(".class")) continue;
							String className = entry.getName().substring(0, entry.getName().length() - 6);
							className = className.replace('/', '.');
							if(className.startsWith("vic.mod.chat.api")) continue;
							Class clazz = classLoader.loadClass(className);

							if(IChatBot.class.isAssignableFrom(clazz))
							{
								IChatBot bot = (IChatBot)clazz.newInstance();
								if(containsBot(bot.getName()))
								{
									VChat.logger.error("Loading of bot \"" + bot.getName() + "\" failed! There is already a bot present with the same name.");
									continue;
								}
								BotHandler handler = new BotHandler(bot);
								bots.put(bot.getName(), handler);
								bot.onLoad(handler);
								
								for(IChannel channel : ChannelHandler.channels.values())
								{
									if(!channel.getName().equals("local") && !(channel instanceof ChannelCustom && ((ChannelCustom)channel).hasRange()))
										ChannelHandler.joinChannel(handler.botEntity, channel, true);
								}
								
								loaded++;
								VChat.logger.info("Bot \"" + bot.getName() + "\" was successfully loaded and is ready for use!");
							}
						}
						
						file.close();
						classLoader.close();
					} catch (Exception e) {
						VChat.logger.error("Loading of bot \"" + botFile.getName() + "\" failed!");
						e.printStackTrace();
					}
				}
			}	
			VChat.logger.info("...done! A total of " + loaded + " bots loaded.");
		} catch (Exception e) {
			VChat.logger.error("Loading of the bots failed!");
		}
	}

	@Override
	public void onServerLoad(FMLServerStartingEvent event) 
	{
		for(BotHandler bot : bots.values())
		{
			bot.owningBot.onServerLoad();
		}
	}

	@Override
	public void onServerUnload(FMLServerStoppingEvent event) 
	{
		for(BotHandler bot : bots.values())
		{
			bot.owningBot.onServerUnload();
		}
	}
}
