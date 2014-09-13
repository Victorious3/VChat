package vic.mod.chat;

import java.io.File;

import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.config.Configuration;

public class Config 
{
	public static String modt;
	
	public static boolean modtEnabled;
	public static boolean channelListEnabled;
	public static boolean nickEnabled;
	public static boolean localEnabled;
	public static boolean globalCrossDim;
	public static boolean urlEnabled;
	public static boolean urlEnabledYoutube;
	
	public static int nickPermissionLevel;
	public static int colorPermissionLevel;
	public static int urlPermissionLevel;
	
	public static int localRange;
	public static int ytTitleLimit;
	public static int nickMin;
	public static int nickMax;
	
	public static EnumChatFormatting colorHighlight;
	public static EnumChatFormatting colorHighlightSelf;
	
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
		nickEnabled = config.get("GENERAL", "nick_enabled", true, "Disable or enable the ability to choose a nickname via /nick").getBoolean(true);
		nickPermissionLevel = config.get("GENERAL", "nick_permlevel", 3, "Change the permission level required to use the /nick command. 3 is OP by default.").getInt(3);
		nickMin = config.get("GENERAL", "nick_size_min", 3, "Change the minimum nick length").getInt(3);
		nickMax = config.get("GENERAL", "nick_size_max", 14, "Change the maximum nick length").getInt(3);
		
		channelListEnabled = config.get("GENERAL", "channel_list_enabled", true, "Disable or enable to show the list of joined channels when joining the server.").getBoolean(true);
		colorPermissionLevel = config.get("GENERAL", "color_permlevel", 3, "Change the permission level required to use chat formatting with \"&\" as prefix. 3 is OP by default.").getInt(3);
		localEnabled = config.get("GENERAL", "local_enabled", true, "Disable or enable the local chat.").getBoolean(true);
		localRange = config.get("GENERAL", "local_range", 50, "Change the block distance in which players receive the local chat.").getInt(50);
		globalCrossDim = config.get("GENERAL", "global_cross_dim", true, "Enable if you want the global chat to be cross-dimensional.").getBoolean(true);
		
		config.addCustomCategoryComment("STYLE", "Valid colors are: BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE");
		
		colorHighlight = EnumChatFormatting.getValueByName(config.get("STYLE", "color_highlight", "DARK_AQUA", "The color used by the name hightlighting.").getString());
		if(colorHighlight == null) colorHighlight = EnumChatFormatting.DARK_AQUA;
		
		colorHighlightSelf = EnumChatFormatting.getValueByName(config.get("STYLE", "color_highlight_self", "DARK_RED", "The color used by the name hightlighting if yourself gets highlighted.").getString());
		if(colorHighlightSelf == null) colorHighlightSelf = EnumChatFormatting.DARK_RED;
		
		urlEnabled = config.get("GENERAL", "url_enabled", true, "Disable or enable the option to post clickable links in chat.").getBoolean(true);
		urlEnabledYoutube = config.get("GENERAL", "url_enabled_yt", true, "Disable or enable the option to post youtube links in chat.").getBoolean(true);
		ytTitleLimit =  config.get("GENERAL", "yt_title_limit", 48, "Specify the size at which video tites will get cut.").getInt(48);
		urlPermissionLevel = config.get("GENERAL", "url_permlevel", 0, "Change the permission level required to post clickable links in chat. 0 is everyone by default.").getInt();
		
		config.save();
	}
}
