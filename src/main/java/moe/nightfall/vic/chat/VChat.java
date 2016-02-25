package moe.nightfall.vic.chat;

import java.io.File;
import java.util.ArrayList;

import moe.nightfall.vic.chat.bots.BotLoader;
import moe.nightfall.vic.chat.handlers.*;
import moe.nightfall.vic.chat.integrations.ChatFormatter;
import moe.nightfall.vic.chat.integrations.github.GitHubChatFormatter;
import moe.nightfall.vic.chat.integrations.soundcloud.SoundCloudChatFormatter;
import moe.nightfall.vic.chat.integrations.twitter.TwitterChatFormatter;
import moe.nightfall.vic.chat.integrations.youtube.YoutubeChatFormatter;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = "vchat", name = "vChat", version = Constants.VERSION, acceptableRemoteVersions = "*", acceptedMinecraftVersions = "1.8, 1.8.8, 1.8.9")
public class VChat
{	
    @Instance("vchat")
    public static VChat instance;

    private ArrayList<ChatHandler> chatHandlers;
    private Logger logger;

    private CommonHandler commonHandler;
    private ChannelHandler channelHandler;
    private BotLoader botLoader;
    private TrackHandler trackHandler;
    private NickHandler nickHandler;
    private AFKHandler afkHandler;
    private AutoAFKHandler autoAFKHandler;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        this.chatHandlers = new ArrayList<ChatHandler>();
        this.logger = event.getModLog();

        Config.initialize(event.getSuggestedConfigurationFile());

        File rootDir = event.getModConfigurationDirectory().getParentFile();
        rootDir.setWritable(true);

        /**
         * Common
         */

        this.commonHandler = new CommonHandler(this);
        this.channelHandler = new ChannelHandler(this);

        this.botLoader = new BotLoader(this);

        if(Config.nickEnabled) this.nickHandler = new NickHandler(this);
        if(Config.trackEnabled) this.trackHandler = new TrackHandler(this);

        if(Config.afkEnabled)
        {
            this.afkHandler = new AFKHandler(this);

            if(Config.autoAfkEnabled)
                this.autoAFKHandler = new AutoAFKHandler(this);
        }

        /**
         * Chat formatters
         */

        if(Config.urlEnabledYoutube) this.commonHandler.registerChatFormatter(new YoutubeChatFormatter(this));
        if(Config.urlEnabledSoundCloud) this.commonHandler.registerChatFormatter(new SoundCloudChatFormatter(this));
        if(Config.urlEnabledGitHub) this.commonHandler.registerChatFormatter(new GitHubChatFormatter(this));
        if(Config.urlEnabledTwitter) this.commonHandler.registerChatFormatter(new TwitterChatFormatter(this));

        this.commonHandler.registerChatFormatter(new ChatFormatter.ChatFormatterURL(this));
    }

    @EventHandler
    public void onServerLoad(FMLServerStartingEvent event)
    {
        for(ChatHandler handler : this.chatHandlers)
            handler.onServerLoad(event);
    }

    @EventHandler
    public void onServerUnload(FMLServerStoppingEvent event)
    {
        for(ChatHandler handler : this.chatHandlers)
            handler.onServerUnload(event);
    }

    public void registerChatHandler(ChatHandler chatHandler)
    {
        MinecraftForge.EVENT_BUS.register(chatHandler);
        this.chatHandlers.add(chatHandler);
    }

    public Logger getLogger()
    {
        return this.logger;
    }

    public CommonHandler getCommonHandler()
    {
        return this.commonHandler;
    }

    public ChannelHandler getChannelHandler()
    {
        return this.channelHandler;
    }

    public BotLoader getBotLoader()
    {
        return this.botLoader;
    }

    public TrackHandler getTrackHandler()
    {
        return this.trackHandler;
    }

    public NickHandler getNickHandler()
    {
        return this.nickHandler;
    }

    public AFKHandler getAfkHandler()
    {
        return this.afkHandler;
    }

    public AutoAFKHandler getAutoAFKHandler()
    {
        return this.autoAFKHandler;
    }
}
