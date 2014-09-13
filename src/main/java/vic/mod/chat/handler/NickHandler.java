package vic.mod.chat.handler;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.command.server.CommandMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import vic.mod.chat.ChatEntity;
import vic.mod.chat.Config;
import vic.mod.chat.Misc.CommandOverrideAccess;
import vic.mod.chat.VChat;

import com.google.common.collect.HashBiMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class NickHandler extends ChatHandlerImpl
{
	public static HashBiMap<String, String> nickRegistry;
	
	private File nickfile;
	
	public NickHandler()
	{
		super();
		
		nickRegistry = HashBiMap.create(new HashMap<String, String>());
		nickfile = new File("vchat_nicks.json");
	}
	
	@SubscribeEvent
	public void getPlayerName(PlayerEvent.NameFormat event)
	{
		if(nickRegistry.containsKey(event.username)) event.displayname = nickRegistry.get(event.username);
	}
	
	public void loadNicks()
	{
		try {
			if(nickfile.exists())
			{
				JsonParser parser = new JsonParser();
				JsonArray nicks = (JsonArray)parser.parse(new JsonReader(new FileReader(nickfile)));
				for(int i = 0; i < nicks.size(); i++)
				{
					JsonObject obj = (JsonObject)nicks.get(i);
					nickRegistry.put(obj.get("username").getAsString(), obj.get("nickname").getAsString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			VChat.logger.error("Could not read the nick file. Maybe it's disrupted or the access is restricted. Try deleting it.");
		}
	}
	
	public static String getPlayerFromNick(String nick)
	{
		if(nickRegistry.containsValue(nick))
		{
			return nickRegistry.inverse().get(nick);
		}
		return null;
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onCommand(CommandEvent event)
	{
		if(event.command instanceof CommandMessage)
		{
			if(event.parameters.length > 0)
			{
				ChatEntity entity = new ChatEntity((Object)event.parameters[0]);
				if(entity.getUsername() != null) event.parameters[0] = entity.getUsername();
			}
		}	
	}
	
	public void saveNicks()
	{
		try {
			if(!nickfile.exists()) nickfile.createNewFile();
			JsonArray nickArray = new JsonArray();
			for(Entry<String, String> entry : nickRegistry.entrySet())
			{
				JsonObject obj = new JsonObject();
				obj.addProperty("username", entry.getKey());
				obj.addProperty("nickname", entry.getValue());
				nickArray.add(obj);
			}
			
			FileWriter writer = new FileWriter(nickfile);
			writer.write(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(nickArray));
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			VChat.logger.error("Could not save the nick file. Maybe it's disrupted or the access is restricted. Try deleting it.");
		}
	}
	
	@Override
	public void onServerLoad(FMLServerStartingEvent event)
	{
		nickRegistry.clear();
		loadNicks();
		event.registerServerCommand(new CommandNick());
	}
	
	@Override
	public void onServerUnload(FMLServerStoppingEvent event)
	{
		saveNicks();
	}
	
	public static class CommandNick extends CommandOverrideAccess
	{
		@Override
		public String getCommandName() 
		{
			return "nick";
		}

		@Override
		public String getCommandUsage(ICommandSender sender) 
		{
			return "/nick <player> [nickname]";
		}

		@Override
		public int getRequiredPermissionLevel() 
		{
			return Config.nickPermissionLevel;
		}

		@Override
		public void processCommand(ICommandSender sender, String[] args) 
		{
			if(args.length > 0 && args.length < 3)
			{			
				EntityPlayerMP player = null;
				try {
					player = getPlayer(sender, args[0]);
				} catch (PlayerNotFoundException exeption) {
					
				}

				if(args.length == 1)
				{
					if(!nickRegistry.containsKey(args[0])) throw new CommandException("The given player has no nickname!", sender);
					nickRegistry.remove(args[0]);
					if(player != null) player.refreshDisplayName();
					sender.addChatMessage(new ChatComponentText("Removed nickname from player \"" + args[0] + "\"."));
				}
				else
				{					
					if(args[1].contains("\u00A7")) throw new CommandException("You can not use color codes inside nicknames.", sender);
 					String playername = getPlayerFromNick(args[1]);
					if(playername == null)
					{
						if(args[0].equals(args[1])) throw new CommandException("You can not create a nickname which is equivalent to the username!", sender);
						if(args[1].length() < Config.nickMin) throw new CommandException("You can not create a nickname which has less than " + Config.nickMin + " characters!", sender);
						if(args[1].length() > Config.nickMax) throw new CommandException("You can not create a nickname which has more than " + Config.nickMax + " characters!", sender);
						if(!args[1].matches("[A-z0-9_]+")) throw new CommandException("A nickname can only contain alphanumeric characters and underscores.");
						nickRegistry.put(args[0], args[1]);
						if(player != null) player.refreshDisplayName();
						sender.addChatMessage(new ChatComponentText("Added nickname \"" + args[1] + "\" to player \"" + args[0] + "\"."));
					}
					else throw new CommandException("The given nickname is already taken by player \"" + playername + "\". You can remove it with /nick " + playername + ".", sender);
				}
			}
			else throw new WrongUsageException(getCommandUsage(sender));
		}

		@Override
		public List addTabCompletionOptions(ICommandSender sender, String[] args) 
		{
			return args.length == 1 ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : null;
		}
	}
}
