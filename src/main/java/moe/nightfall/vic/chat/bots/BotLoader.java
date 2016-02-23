package moe.nightfall.vic.chat.bots;

import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.Constants;
import moe.nightfall.vic.chat.VChat;
import moe.nightfall.vic.chat.channels.ChannelCustom;
import moe.nightfall.vic.chat.handlers.ChatHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.google.common.collect.ImmutableList;

import moe.nightfall.vic.chat.api.IChannel;
import moe.nightfall.vic.chat.api.bot.IChatBot;
import moe.nightfall.vic.chat.api.bot.Version;
import net.minecraft.launchwrapper.LaunchClassLoader;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModClassLoader;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.relauncher.CoreModManager;

public class BotLoader extends ChatHandler
{
    private static final List<String> knownPackages = Arrays.asList(
        "org.apache", "com.google", "java", "scala", "tv.twitch",
        "org.lwjgl", "net.java", "com.mojang", "io.netty", "paulscode.sound",
        "com.jcraft", "com.ibm.icu", "gnu.trove", "LZMA", "joptsimple",
        "com.typesafe", "akka", "org.objectweb", "ibxm"
    );

    private final HashMap<String, BotHandler> bots;
    private final HashSet<Class> loadedBots;

    public BotLoader(VChat instance)
    {
        super(instance);

        this.bots = new HashMap<String, BotHandler>();
        this.loadedBots = new HashSet<Class>();

        this.loadBots();
    }

    public boolean containsBot(String name)
    {
        return this.bots.containsKey(name);
    }

    public BotHandler getBot(String name)
    {
        return this.bots.get(name);
    }

    public void loadBots()
    {
        try
        {
            long startTime = System.currentTimeMillis();
            File botDir = new File("vChat/bots");

            if(!botDir.exists())
                botDir.mkdirs();

            this.instance.getLogger().info("Attempting to load bots...");

            ModClassLoader classLoader = (ModClassLoader) Loader.instance().getModClassLoader();
            LaunchClassLoader launchClassLoader = (LaunchClassLoader)Loader.class.getClassLoader();

            this.loadedBots.clear();

            if(Config.classPathBot)
            {
                VChat.instance.getLogger().info("Searching the classpath for bots, this may take a while. If you don't need this, disable it from the config file.");
                File[] sources = classLoader.getParentSources();

                List<String> knownLibraries = ImmutableList.<String>builder()
                    .addAll(classLoader.getDefaultLibraries())
                    .addAll(CoreModManager.getReparseableCoremods())
                    .build();

                for(File f : sources)
                {
                    if(knownLibraries.contains(f.getName()))
                        continue;

                    if(f.isDirectory())
                    {
                        for(File f2 : FileUtils.listFiles(f, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE))
                        {
                            if(!f2.getName().endsWith(".class"))
                                continue;

                            String className = f2.toString();
                            className = className.substring(f.getAbsolutePath().length() + 1, className.length() - 6).replaceAll("\\" + File.separator, ".");

                            if(className.startsWith("vic.mod.chat.api"))
                                continue;

                            try
                            {
                                Class clazz = launchClassLoader.findClass(className);
                                this.loadBot(clazz);
                            }
                            catch (Exception e)
                            {
                                this.instance.getLogger().error("Failed to load the bot '" + className + "'!");
                                e.printStackTrace();
                            }
                        }
                    }
                    else if(f.getName().endsWith(".jar"))
                    {
                        JarFile file = new JarFile(f);
                        Enumeration<JarEntry> enumeration = file.entries();

                        outer:
                        while(enumeration.hasMoreElements())
                        {
                            JarEntry entry = enumeration.nextElement();

                            if(entry.isDirectory() || !entry.getName().endsWith(".class"))
                                continue;

                            String className = entry.getName().substring(0, entry.getName().length() - 6);
                            className = className.replace('/', '.');

                            if(className.startsWith("vic.mod.chat.api"))
                                continue;

                            for(String s : knownPackages)
                                if(className.startsWith(s))
                                    continue outer;

                            try
                            {
                                Class clazz = launchClassLoader.findClass(className);
                                this.loadBot(clazz);
                            }
                            catch (Exception e)
                            {
                                this.instance.getLogger().error("Failed to load the bot '" + className + "'!");
                                e.printStackTrace();
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
                    this.instance.getLogger().info("Attempting to load bots from file \"" + botFile.getName() + "\"...");

                    try
                    {
                        JarFile file = new JarFile(botFile);

                        Enumeration<JarEntry> enumeration = file.entries();
                        classLoader.addFile(botFile);

                        while(enumeration.hasMoreElements())
                        {
                            JarEntry entry = enumeration.nextElement();

                            if(entry.isDirectory() || !entry.getName().endsWith(".class"))
                                continue;

                            String className = entry.getName().substring(0, entry.getName().length() - 6);
                            className = className.replace('/', '.');

                            if(className.startsWith("vic.mod.chat.api"))
                                continue;

                            Class clazz = classLoader.loadClass(className);
                            this.loadBot(clazz);
                        }

                        file.close();
                    }
                    catch (Exception e)
                    {
                        this.instance.getLogger().error("Failed to load the bot '" + botFile.getName() + "'!");
                        e.printStackTrace();
                    }
                }
            }

            this.instance.getLogger().info("Done! A total of " + this.loadedBots.size() + " bots loaded in " + (System.currentTimeMillis() - startTime) + " ms");
        }
        catch (Exception e)
        {
            this.instance.getLogger().error("Failed to load the bots!");
            e.printStackTrace();
        }
    }

    public void loadBot(Class clazz) throws InstantiationException, IllegalAccessException
    {
        if(this.loadedBots.contains(clazz))
            return;

        if(IChatBot.class.isAssignableFrom(clazz))
        {
            if(!Config.skipVersionCheck)
            {
                Version version = (Version)clazz.getAnnotation(Version.class);

                if(version != null)
                {
                    if(!version.version().equals(Constants.API_VERSION))
                    {
                        this.instance.getLogger().error("The bot from class file " + clazz.getName() + " is using the API version " + version.version() +  ". Your version: " + Constants.API_VERSION);
                        return;
                    }
                }
                else
                {
                    this.instance.getLogger().error("The bot from class file " + clazz.getName() + " doesn't specify a version! Please contact the owner!");
                    return;
                }
            }
            else
            {
                this.instance.getLogger().warn("Version check disabled! You might run into serious problems when using bots that use an outdated version!");
            }

            IChatBot bot = (IChatBot)clazz.newInstance();

            if(containsBot(bot.getName()))
            {
                this.instance.getLogger().error("Loading of bot \"" + bot.getName() + "\" failed! There is already a bot present with the same name.");
                return;
            }

            BotHandler handler = new BotHandler(this.instance, bot);
            this.bots.put(bot.getName(), handler);
            bot.onLoad(handler);

            for(IChannel channel : this.instance.getChannelHandler().getChannels().values())
                if(!channel.getName().equals("local") && !(channel instanceof ChannelCustom && ((ChannelCustom)channel).hasRange()))
                    this.instance.getChannelHandler().joinChannel(handler.getBotEntity(), channel, true);

            this.loadedBots.add(clazz);
            this.instance.getLogger().info("Bot \"" + bot.getName() + "\" was successfully loaded and is ready for use!");
        }
    }

    @Override
    public void onServerLoad(FMLServerStartingEvent event)
    {
        for(BotHandler bot : this.bots.values())
            bot.getOwningBot().onServerLoad();
    }

    @Override
    public void onServerUnload(FMLServerStoppingEvent event)
    {
        for(BotHandler bot : this.bots.values())
            bot.getOwningBot().onServerUnload();
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event)
    {
        if(event.phase == Phase.END)
            for(BotHandler bot : bots.values())
                bot.getOwningBot().onTick();
    }

    public HashMap<String, BotHandler> getBots()
    {
        return this.bots;
    }
}
