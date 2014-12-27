package vic.mod.chat;

import java.io.File;

import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.config.Configuration;

public class Config 
{
	public static String modt;
	
	public static boolean modtEnabled;
	public static boolean channelListEnabled;
	public static boolean nickEnabled;
	public static boolean afkEnabled;
	public static boolean autoAfkEnabled;
	public static boolean localEnabled;
	public static boolean globalCrossDimEnabled;
	public static boolean onlineTrackerEnabled;
	
	public static boolean urlEnabled;
	public static boolean urlEnabledYoutube;
	public static boolean urlEnabledSoundCloud;
	
	public static int nickPermissionLevel;
	public static int colorPermissionLevel;
	public static int urlPermissionLevel;
	public static int topPermissionLevel;
	public static int posPermissionLevel;
	
	public static int autoAfkTime;	
	public static int localRange;
	public static int ytTitleLimit;
	public static int nickMin;
	public static int nickMax;
	
	public static EnumChatFormatting colorHighlight;
	public static EnumChatFormatting colorHighlightSelf;
	public static EnumChatFormatting colorNickName;
	public static EnumChatFormatting colorBot;
	
	public static boolean pingHighlighted;
	public static float pingPitch;
	public static float pingVolume;
	public static String pingSound;
	
	public static boolean trackEnabled;
	public static boolean trackEnabledCustom;
	public static int trackPermissionLevel;
	public static int trackPermissionLevelSelf;
	
	public static boolean classPathBot;
	
	public static void initialize(File file)
	{
		Configuration config = new Configuration(file);
		config.load();
		
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
		pingVolume = (float) config.get("GENERAL", "ping_pitch", 1.0, "Change the volume of the sound that will be played on player mention. (0.0 - 1.0)").getDouble();
		pingVolume = MathHelper.clamp_float(pingVolume, 0, 1);
		pingSound = config.get("GENERAL", "ping_sound", "random.levelup", "Change the sound that will be played on player mention. Here is a complete list: http://minecraft.gamepedia.com/Sounds.json").getString();
		
		channelListEnabled = config.get("GENERAL", "channel_list_enabled", true, "Disable or enable to show the list of joined channels when joining the server.").getBoolean(true);
		colorPermissionLevel = config.get("GENERAL", "color_permlevel", 3, "Change the permission level required to use chat formatting with \"&\" as prefix. 3 is OP by default.").getInt(3);
		localEnabled = config.get("GENERAL", "local_enabled", true, "Disable or enable the local chat.").getBoolean(true);
		localRange = config.get("GENERAL", "local_range", 50, "Change the block distance in which players receive the local chat.").getInt(50);
		globalCrossDimEnabled = config.get("GENERAL", "global_cross_dim", true, "Enable if you want the global chat to be cross-dimensional.").getBoolean(true);
		
		urlEnabled = config.get("GENERAL", "url_enabled", true, "Disable or enable the option to post clickable links in chat.").getBoolean(true);
		urlEnabledYoutube = config.get("GENERAL", "url_enabled_yt", true, "Disable or enable the option to post youtube links in chat.").getBoolean(true);
		urlEnabledSoundCloud = config.get("GENERAL", "url_enabled_sc", true, "Disable or enable the option to post soundcloud links in chat.").getBoolean(true);
		ytTitleLimit =  config.get("GENERAL", "yt_title_limit", 48, "Specify the size at which video tites will get cut.").getInt(48);
		urlPermissionLevel = config.get("GENERAL", "url_permlevel", 0, "Change the permission level required to post clickable links in chat. 0 is everyone by default.").getInt();
		
		topPermissionLevel = config.get("GENERAL", "top_permlevel", 3, "Change the permission level required to use the /top command. 3 is OP by default.").getInt(3);
		posPermissionLevel = config.get("GENERAL", "checkpos_permlevel", 3, "Change the permission level required to use the /checkpos command. 3 is OP by default.").getInt(3);
		
		config.addCustomCategoryComment("STYLE", "Valid colors are: BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE");
		
		colorHighlight = EnumChatFormatting.getValueByName(config.get("STYLE", "color_highlight", "DARK_AQUA", "The color used by the name hightlighting.").getString());
		if(colorHighlight == null) colorHighlight = EnumChatFormatting.DARK_AQUA;
		
		colorHighlightSelf = EnumChatFormatting.getValueByName(config.get("STYLE", "color_highlight_self", "DARK_RED", "The color used by the name hightlighting if yourself gets highlighted.").getString());
		if(colorHighlightSelf == null) colorHighlightSelf = EnumChatFormatting.DARK_RED;
		
		colorNickName = EnumChatFormatting.getValueByName(config.get("STYLE", "color_nick", "YELLOW", "Change the color applied to nicknames").getString());
		if(colorNickName == null) colorNickName = EnumChatFormatting.YELLOW;
		
		colorBot = EnumChatFormatting.getValueByName(config.get("STYLE", "color_bot", "GREEN", "Change the color applied to bots").getString());
		if(colorNickName == null) colorNickName = EnumChatFormatting.GREEN;
		
		onlineTrackerEnabled = config.get("GENERAL", "online_tracker_enabled", true, "Enable if you want to log player's online times.").getBoolean(true);
		classPathBot = config.get("GENERAL", "classpath_bots_enabled", false, "Enable if you want to load bots from the current classpath, can be useful if any mods add bots or you want to debug your own.").getBoolean(false);
		
		config.save();
	}
}
