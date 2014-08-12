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
	
	public static int nickPermissionLevel;
	public static int colorPermissionLevel;
	public static int urlPermissionLevel;
	public static int localRange;
	
	public static EnumChatFormatting colorHighlight;
	public static EnumChatFormatting colorHighlightSelf;
	
	public static void initialize(File file)
	{
		Configuration config = new Configuration(file);
		config.load();
		
		modt = config.getString("modt", "GENERAL", 		
				"Welcome, \u00A7e\u00A7l%NAME%\u00A7r to \u00A7o%MODT%\u00A7r!/n"
				+ "Currently, there are %ONLINE%/%ONLINE_MAX% players online./n"
				+ "You are playing on dimension %DIM% (\u00A7o%DIM_NAME%\u00A7r) and local time is %TIME%./n"
				+ "The server is running vChat to display this MotD,/n"
				+ "Have a good one! \u00A7o~Vic\u00A7r", 
				
				"The \"Message of the Day\". It will be sent to every player that joins the server.\n"
				+ "You can use the following shortcuts: %NAME% = The player's username. %TIME% = The server's local time (HH:mm).\n"
				+ "%ONLINE% = The amount of players that are playing on the server.\n"
				+ "%ONLINE_MAX% = The amount of players which can play on the server.\n"
				+ "%DIM% = The dimension id of the player. %DIM_NAME% = The name of the dimension of the player.\n"
				+ "%MODT% = The modt specified in the server.properties. Can be used to specify a server name.\n"
				+ "You can also use color codes, see http://minecraft.gamepedia.com/Formatting_codes.\n"
				+ "/n is used for a line feed.\n");
		
		modtEnabled = config.getBoolean("modt_enabled", "GENERAL", true, "Disable or enable the \"Message of the Day.\"");
		nickEnabled = config.getBoolean("nick_enabled", "GENERAL", true, "Disable or enable the ability to choose a nickname via /nick");
		channelListEnabled = config.getBoolean("channel_list_enabled", "GENERAL", true, "Disable or enable to show the list of joined channels when joining the server.");
		nickPermissionLevel = config.getInt("nick_permlevel", "GENERAL", 3, 0, Integer.MAX_VALUE, "Change the permission level required to use the /nick command. 3 is OP by default.");
		colorPermissionLevel = config.getInt("color_permlevel", "GENERAL", 3, 0, Integer.MAX_VALUE, "Change the permission level required to use chat formatting with \"&\" as prefix. 3 is OP by default.");
		localEnabled = config.getBoolean("local_enabled", "GENERAL", true, "Disable or enable the local chat.");
		localRange = config.getInt("local_range", "GENERAL", 50, 10, Integer.MAX_VALUE, "Change the block distance in which players receive the local chat.");
		globalCrossDim = config.getBoolean("global_cross_dim", "GENERAL", true, "Enable if you want the global chat to be cross-dimensional.");
		
		config.addCustomCategoryComment("STYLE", "Valid colors are: BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE");
		
		colorHighlight = EnumChatFormatting.getValueByName(config.getString("color_highlight", "STYLE", "DARK_AQUA", "The color used by the name hightlighting."));
		if(colorHighlight == null) colorHighlight = EnumChatFormatting.DARK_AQUA;
		
		colorHighlightSelf = EnumChatFormatting.getValueByName(config.getString("color_highlight_self", "STYLE", "DARK_RED", "The color used by the name hightlighting if yourself gets highlighted."));
		if(colorHighlightSelf == null) colorHighlightSelf = EnumChatFormatting.DARK_RED;
		
		urlPermissionLevel = config.getInt("url_permlevel", "GENERAL", 0, 0, Integer.MAX_VALUE, "Change the permission level required to post clickable links in chat. 0 is everyone by default.");
		
		config.save();
	}
}
