package vic.mod.chat;

import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.minecraft.launchwrapper.LaunchClassLoader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import vic.mod.chat.api.IChannel;
import vic.mod.chat.api.bot.IChatBot;
import vic.mod.chat.api.bot.Version;
import vic.mod.chat.handler.ChannelHandler;
import vic.mod.chat.handler.ChatHandlerImpl;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModClassLoader;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.relauncher.CoreModManager;

public class BotLoader extends ChatHandlerImpl
{
	public HashMap<String, BotHandler> bots = new HashMap<String, BotHandler>();
	public final List<String> knownPackages = Arrays.asList(
		"org.apache", "com.google", "java", "scala", "tv.twitch", 
		"org.lwjgl", "net.java", "com.mojang", "io.netty", "paulscode.sound", 
		"com.jcraft", "com.ibm.icu", "gnu.trove", "LZMA", "joptsimple", 
		"com.typesafe", "akka", "org.objectweb", "ibxm"
	);

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
	
	int loaded = 0;
	private HashSet<Class> loadedBots = new HashSet<Class>();
	
	public void loadBots()
	{
		try {
			long startTime = System.currentTimeMillis();
			File botDir = new File("vChat/bots");
			if(!botDir.exists()) botDir.mkdirs();
			
			VChat.logger.info("Attempting to load bots...");	
			ModClassLoader classLoader = (ModClassLoader)Loader.instance().getModClassLoader();
			LaunchClassLoader launchClassLoader = (LaunchClassLoader)Loader.class.getClassLoader();
			
			loaded = 0;
			loadedBots.clear();
			if(Config.classPathBot)
			{
				VChat.logger.info("Searching the classpath for bots, this may take a while. If you don't need this, disable it from the config file.");
				File[] sources = classLoader.getParentSources();
				List<String> knownLibraries = ImmutableList.<String>builder()
					.addAll(classLoader.getDefaultLibraries())
					.addAll(CoreModManager.getLoadedCoremods())
					.addAll(CoreModManager.getReparseableCoremods())
					.build();
				
				for(File f : sources)
				{
					if(knownLibraries.contains(f.getName())) continue;
					
					if(f.isDirectory())
					{
						for(File f2 : FileUtils.listFiles(f, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE))
						{
							if(!f2.getName().endsWith(".class")) continue;
							String className = f2.toString();
							className = className.substring(f.getAbsolutePath().length() + 1, className.length() - 6).replaceAll("\\" + File.separator, ".");
							if(className.startsWith("vic.mod.chat.api")) continue;
							try {
								Class clazz = launchClassLoader.findClass(className);
								loadBot(clazz);
							} catch (Exception e) {
								
							}
						}
					}
					else if(f.getName().endsWith(".jar"))
					{
						//Load jar entries
						JarFile file = new JarFile(f);
						Enumeration<JarEntry> enumeration = file.entries();
						
						outer:
						while(enumeration.hasMoreElements())
						{
							JarEntry entry = enumeration.nextElement();
							if(entry.isDirectory() || !entry.getName().endsWith(".class")) continue;
							String className = entry.getName().substring(0, entry.getName().length() - 6);
							className = className.replace('/', '.');
							if(className.startsWith("vic.mod.chat.api")) continue;
							
							for(String s : knownPackages) 
								if(className.startsWith(s)) continue outer;
							try {
								Class clazz = launchClassLoader.findClass(className);
								loadBot(clazz);
							} catch (Exception e) {
								
							}
						}
						file.close();
					}
				}
			}
			for(File botFile : botDir.listFiles())
			{
				if(botFile.getName().endsWith(".jar"))
				{
					VChat.logger.info("Attempting to load bots from file \"" + botFile.getName() + "\"...");
					try {
						JarFile file = new JarFile(botFile);
						
						Enumeration<JarEntry> enumeration = file.entries();
						classLoader.addFile(botFile);
						while(enumeration.hasMoreElements())
						{
							JarEntry entry = enumeration.nextElement();
							if(entry.isDirectory() || !entry.getName().endsWith(".class")) continue;
							String className = entry.getName().substring(0, entry.getName().length() - 6);
							className = className.replace('/', '.');
							if(className.startsWith("vic.mod.chat.api")) continue;
							Class clazz = classLoader.loadClass(className);
							loadBot(clazz);
						}
						
						file.close();
					} catch (Exception e) {
						VChat.logger.error("Loading of bot \"" + botFile.getName() + "\" failed!");
						e.printStackTrace();
					}
				}
			}	
			VChat.logger.info("...done! A total of " + loaded + " bots loaded in " + (System.currentTimeMillis() - startTime) + " ms");
		} catch (Exception e) {
			VChat.logger.error("Loading of the bots failed!");
			e.printStackTrace();
		}
	}
	
	public void loadBot(Class clazz) throws InstantiationException, IllegalAccessException
	{
		if(loadedBots.contains(clazz)) return;
		if(IChatBot.class.isAssignableFrom(clazz))
		{
			if(!Config.skipVersionCheck)
			{
				Version version = (Version)clazz.getAnnotation(Version.class);
				if(version != null)
				{
					if(!version.version().equals(Constants.apiVersion))
					{
						VChat.logger.error("The bot from class file " + clazz.getName() + " is using the API version " + version.version() +  ". Your version: " + Constants.apiVersion);
						return;
					}
				}
				else 
				{
					VChat.logger.error("The bot from class file " + clazz.getName() + " doesn't specify a version! Please contact the owner!");
					return;
				}
			}
			else VChat.logger.warn("Version check disabled! You might run into serious problems when using bots that use an outdated version!");
			
			IChatBot bot = (IChatBot)clazz.newInstance();
			if(containsBot(bot.getName()))
			{
				VChat.logger.error("Loading of bot \"" + bot.getName() + "\" failed! There is already a bot present with the same name.");
				return;
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
			loadedBots.add(clazz);
			VChat.logger.info("Bot \"" + bot.getName() + "\" was successfully loaded and is ready for use!");
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
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			for(BotHandler bot : bots.values())
			{
				bot.owningBot.onTick();
			}
		}
	}
}
