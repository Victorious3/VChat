package vic.mod.chat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.regex.Pattern;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class Misc 
{
	public static Pattern urlPattern = Pattern.compile("\\b(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
	
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
	
	public static abstract class CommandOverrideAccess extends CommandBase 
	{
		@Override
		public boolean canCommandSenderUseCommand(ICommandSender sender)
		{
			if(getRequiredPermissionLevel() == 0) return true;
			return sender.canCommandSenderUseCommand(getRequiredPermissionLevel(), getCommandName());
		}
	}
}
