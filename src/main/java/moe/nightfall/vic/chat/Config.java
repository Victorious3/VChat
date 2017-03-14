package moe.nightfall.vic.chat;

import java.io.File;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Configuration;

public class Config 
{
    /**
     * General
     */

    public static String modt;

    public static boolean modtEnabled;
    public static boolean channelListEnabled;
    public static boolean nickEnabled;
    public static boolean afkEnabled;
    public static boolean autoAfkEnabled;
    public static boolean localEnabled;
    public static boolean globalCrossDimEnabled;
    public static boolean onlineTrackerEnabled;

    public static int nickPermissionLevel;
    public static int colorPermissionLevel;
    public static int topPermissionLevel;
    public static int posPermissionLevel;

    public static int autoAfkTime;
    public static int localRange;
    public static int nickMin;
    public static int nickMax;

    public static boolean pingHighlighted;
    public static float pingPitch;
    public static float pingVolume;
    public static String pingSound;

    public static boolean trackEnabled;
    public static boolean trackEnabledCustom;
    public static int trackPermissionLevel;
    public static int trackPermissionLevelSelf;

    public static boolean classPathBot;
    public static boolean skipVersionCheck;


    /**
     * Integrations
     */

    public static boolean urlEnabled;
    public static boolean urlEnabledYoutube;
    public static boolean urlEnabledSoundCloud;
    public static boolean urlEnabledGitHub;
    public static boolean urlEnabledTwitter;
    public static boolean urlEnabledReddit;

    public static String twitterConsumerKey;
    public static String twitterConsumerSecret;
    public static String twitterAccessKey;
    public static String twitterAccessSecret;

    public static int urlPermissionLevel;


    /**
     * Style
     */

    public static TextFormatting colorHighlight;
    public static TextFormatting colorHighlightSelf;
    public static TextFormatting colorNickName;
    public static TextFormatting colorBot;

    public static void initialize(File file)
    {
        Configuration config = new Configuration(file);
        config.load();


        /**
         * General
         */

        modt = config.get("GENERAL", "modt",
                "Welcome, \u00A7e\u00A7l%NAME%\u00A7r to \u00A7o%MODT%\u00A7r!/n"
                + "Currently, there are %ONLINE%/%ONLINE_MAX% players online./n"
                + "You are playing on dimension %DIM% (\u00A7o%DIM_NAME%\u00A7r)/n"
                + "and the local time is %TIME%./n"
                + "The server is running vChat to display this motd,/n"
                + "Have a good one! \u00A7o~Vic\u00A7r",

                "The \"Message of the Day\". It will be sent to every player that joins the server.\n"
                + "You can use the following shortcuts: %NAME% = The player's username. %TIME% = The server's local time (HH:mm).\n"
                + "%ONLINE% = The amount of players that are playing on the server.\n"
                + "%ONLINE_MAX% = The amount of players which can play on the server.\n"
                + "%DIM% = The dimension id of the player. %DIM_NAME% = The name of the dimension of the player.\n"
                + "%MODT% = The modt specified in the server.properties. Can be used to specify a server name.\n"
                + "You can also use color codes, see http://minecraft.gamepedia.com/Formatting_codes.\n"
                + "/n is used for a line feed.\n").getString();

        modtEnabled = config.get("GENERAL", "modt_enabled", true, "Disable or enable the \"Message of the Day.\"").getBoolean(true);

        trackEnabled = config.get("GENERAL", "track_enabled", true, "Disable or enable the /track commands for playing tracks.").getBoolean(true);
        trackEnabledCustom = config.get("GENERAL", "track_custom_enabled", true, "Disable or enable the the possibility to play custom tracks on the fly.").getBoolean(true);
        trackPermissionLevel = config.get("GENERAL", "track_permlevel", 3, "Change the permission level required to broadcast a track. 3 is OP by default.").getInt(3);
        trackPermissionLevelSelf = config.get("GENERAL", "track_self_permlevel", 0, "Change the permission level required to play a track. 0 is everyone by default.").getInt();

        afkEnabled = config.get("GENERAL", "afk_enabled", true, "Disable or enable the /afk command").getBoolean(true);
        autoAfkEnabled = config.get("GENERAL", "auto_afk_enabled", true, "Disable or enable the auto afk. Needs \"afk_enabled\" to be set to \"true\"").getBoolean(true);
        autoAfkTime = config.get("GENERAL", "auto_afk_timeout", 120, "Change the timeout at which a player will be marked as afk, in seconds.").getInt(120);

        nickEnabled = config.get("GENERAL", "nick_enabled", true, "Disable or enable the ability to choose a nickname via /nick").getBoolean(true);
        nickPermissionLevel = config.get("GENERAL", "nick_permlevel", 3, "Change the permission level required to use the /nick command. 3 is OP by default.").getInt(3);
        nickMin = config.get("GENERAL", "nick_size_min", 3, "Change the minimum nick length").getInt(3);
        nickMax = config.get("GENERAL", "nick_size_max", 14, "Change the maximum nick length").getInt(3);

        pingHighlighted = config.get("GENERAL", "ping_enabled", true, "Enable to let players get pinged when they get mentioned in chat.").getBoolean(true);
        pingPitch = (float) config.get("GENERAL", "ping_pitch", 0.8, "Change the pitch of the sound that will be played on player mention.").getDouble();
        pingVolume = MathHelper.clamp((float) config.get("GENERAL", "ping_pitch", 1.0, "Change the volume of the sound that will be played on player mention. (0.0 - 1.0)").getDouble(), 0, 1);
        pingSound = config.get("GENERAL", "ping_sound", "random.levelup", "Change the sound that will be played on player mention. Here is a complete list: http://minecraft.gamepedia.com/Sounds.json").getString();

        channelListEnabled = config.get("GENERAL", "channel_list_enabled", true, "Disable or enable to show the list of joined channels when joining the server.").getBoolean(true);
        colorPermissionLevel = config.get("GENERAL", "color_permlevel", 3, "Change the permission level required to use chat formatting with \"&\" as prefix. 3 is OP by default.").getInt(3);
        localEnabled = config.get("GENERAL", "local_enabled", true, "Disable or enable the local chat.").getBoolean(true);
        localRange = config.get("GENERAL", "local_range", 50, "Change the block distance in which players receive the local chat.").getInt(50);
        globalCrossDimEnabled = config.get("GENERAL", "global_cross_dim", true, "Enable if you want the global chat to be cross-dimensional.").getBoolean(true);

        onlineTrackerEnabled = config.get("GENERAL", "online_tracker_enabled", true, "Enable if you want to log player's online times.").getBoolean(true);
        classPathBot = config.get("GENERAL", "classpath_bots_enabled", false, "Enable if you want to load bots from the current classpath, can be useful if any mods add bots or you want to debug your own.").getBoolean(false);
        skipVersionCheck = config.get("GENERAL", "skip_version_enabled", false, "Enable if you want to skip the version check of a bot before attempting to load it. Has to be enabled for use with a dev environement.").getBoolean(false);

        topPermissionLevel = config.get("GENERAL", "top_permlevel", 3, "Change the permission level required to use the /top command. 3 is OP by default.").getInt(3);
        posPermissionLevel = config.get("GENERAL", "checkpos_permlevel", 3, "Change the permission level required to use the /checkpos command. 3 is OP by default.").getInt(3);


        /**
         * Integrations
         */

        urlEnabled = config.get("INTEGRATIONS", "url_enabled", true, "Disable or enable the option to post clickable links in chat.").getBoolean(true);
        urlEnabledYoutube = config.get("INTEGRATIONS", "url_enabled_youtube", true, "Disable or enable the option to post YouTube links in chat.").getBoolean(true);
        urlEnabledSoundCloud = config.get("INTEGRATIONS", "url_enabled_soundcloud", true, "Disable or enable the option to post SoundCloud links in chat.").getBoolean(true);
        urlEnabledGitHub = config.get("INTEGRATIONS", "url_enabled_github", true, "Disable or enable the option to post GitHub links in chat.").getBoolean(true);
        urlEnabledTwitter = config.get("INTEGRATIONS", "url_enabled_twitter", true, "Disable or enable the option to post Twitter links in chat.").getBoolean(true);
        urlEnabledReddit = config.get("INTEGRATIONS", "url_enabled_reddit", true, "Disable or enable the option to post Reddit links in chat.").getBoolean(true);

        twitterConsumerKey = config.get("INTEGRATIONS", "twitter_consumer_key", "", "Specify a Twitter application's consumer key to enable Twitter links integration into the chat.").getString();
        twitterConsumerSecret = config.get("INTEGRATIONS", "twitter_consumer_secret", "", "Specify a Twitter application's consumer secret to enable Twitter links integration into the chat.").getString();
        twitterAccessKey = config.get("INTEGRATIONS", "twitter_access_token_key", "", "Specify a Twitter application's access token key to enable Twitter links integration into the chat.").getString();
        twitterAccessSecret = config.get("INTEGRATIONS", "twitter_access_token_secret", "", "Specify a Twitter application's access token secret to enable Twitter links integration into the chat.").getString();

        urlPermissionLevel = config.get("GENERAL", "url_permlevel", 0, "Change the permission level required to post clickable links in chat. 0 is everyone by default.").getInt();


        /**
         * Style
         */

        config.addCustomCategoryComment("STYLE", "Valid colors are: BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE");

        colorHighlight = TextFormatting.getValueByName(config.get("STYLE", "color_highlight", "DARK_AQUA", "The color used by the name hightlighting.").getString());
        if(colorHighlight == null) colorHighlight = TextFormatting.DARK_AQUA;

        colorHighlightSelf = TextFormatting.getValueByName(config.get("STYLE", "color_highlight_self", "DARK_RED", "The color used by the name hightlighting if yourself gets highlighted.").getString());
        if(colorHighlightSelf == null) colorHighlightSelf = TextFormatting.DARK_RED;

        colorNickName = TextFormatting.getValueByName(config.get("STYLE", "color_nick", "YELLOW", "Change the color applied to nicknames").getString());
        if(colorNickName == null) colorNickName = TextFormatting.YELLOW;

        colorBot = TextFormatting.getValueByName(config.get("STYLE", "color_bot", "GREEN", "Change the color applied to bots").getString());
        if(colorNickName == null) colorNickName = TextFormatting.GREEN;

        config.save();
    }
}
