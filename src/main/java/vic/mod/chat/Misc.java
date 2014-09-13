package vic.mod.chat;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.event.HoverEvent.Action;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class Misc 
{
	public static Pattern urlPattern = Pattern.compile("\\b(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
	public static Pattern ytVideoPattern = Pattern.compile("(http:|https:)?\\/\\/(www\\.)?(youtube.com|youtu.be)\\/(watch)?(\\?v=)?(\\S+)?");
	
	public static String[] parseModt(String modt, EntityPlayerMP player)
	{
		if(modt.contains("%NAME%")) modt = modt.replaceAll("%NAME%", player.getDisplayName());
		if(modt.contains("%TIME%")) modt = modt.replaceAll("%TIME%", new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime()));
		if(modt.contains("%ONLINE%")) modt = modt.replaceAll("%ONLINE%", String.valueOf(player.mcServer.getCurrentPlayerCount()));
		if(modt.contains("%ONLINE_MAX%")) modt = modt.replaceAll("%ONLINE_MAX%", String.valueOf(player.mcServer.getMaxPlayers()));
		if(modt.contains("%DIM%")) modt = modt.replaceAll("%DIM%", String.valueOf(player.worldObj.provider.dimensionId));
		if(modt.contains("%DIM_NAME%")) modt = modt.replaceAll("%DIM_NAME%", player.worldObj.provider.getDimensionName());
		if(modt.contains("%MODT%")) modt = modt.replaceAll("%MODT%", player.mcServer.getMOTD());
		
		return modt.split("/n");
	}
	
	public static ArrayList<ChatEntity> getOnlinePlayers()
	{
		ArrayList<ChatEntity> list = new ArrayList<ChatEntity>();
		for(Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList) list.add(new ChatEntity(obj));
		return list;
	}
	
	public static EntityPlayerMP getPlayer(String player)
	{
		Iterator iterator = MinecraftServer.getServer().getConfigurationManager().playerEntityList.iterator();
		while(iterator.hasNext())
		{
			EntityPlayerMP entity = (EntityPlayerMP)iterator.next();
			if(entity.getCommandSenderName().equalsIgnoreCase(player)) return entity;
		}
		return null;
	}
	
	public static ChatComponentText getComponent(ChatEntity entity)
	{
		String nick = entity.getNickname();
		ChatComponentText nameComponent = new ChatComponentText(nick != null ? nick : entity.getUsername());
		if(nick != null)
		{
			if(Config.afkEnabled && VChat.afkHandler.isAfk(entity))
			{
				nameComponent.getChatStyle().setColor(EnumChatFormatting.GRAY);
				nameComponent.getChatStyle().setChatHoverEvent(
					new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(entity.getUsername() + " (AFK - " + VChat.afkHandler.getReason(entity) + ")")));
			}	
			else 
			{
				nameComponent.getChatStyle().setColor(Config.colorNickName);
				nameComponent.getChatStyle().setChatHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ChatComponentText(entity.getUsername())));
				nameComponent.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + entity.getUsername()));
			}
		}
		else nameComponent.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + entity.getUsername()));
		
		return nameComponent;
	}
	
	public static abstract class CommandOverrideAccess extends CommandBase 
	{
		@Override
		public boolean canCommandSenderUseCommand(ICommandSender sender)
		{
			if(getRequiredPermissionLevel() == 0) return true;
			return sender.canCommandSenderUseCommand(getRequiredPermissionLevel(), getCommandName());
		}
	}
	
	public static HashMap<String, String> getQueryMap(URL url)  
	{
		if(url.getQuery() == null) return new HashMap<String, String>();
		String[] params = url.getQuery().split("&");
		HashMap<String, String> map = new HashMap<String, String>();
		for(String param : params)
		{
			String name = param.split("=")[0];
			String value = param.split("=")[1];
			map.put(name, value);
		}
		return map;
	}
}
