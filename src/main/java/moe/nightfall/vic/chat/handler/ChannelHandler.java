package moe.nightfall.vic.chat.handler;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import moe.nightfall.vic.chat.ChannelCustom;
import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.Misc;
import moe.nightfall.vic.chat.api.IChannel;
import moe.nightfall.vic.chat.api.bot.IChatEntity;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.ForgeHooks;

import org.apache.commons.lang3.StringUtils;

import moe.nightfall.vic.chat.ChannelGlobal;
import moe.nightfall.vic.chat.ChannelLocal;
import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.VChat;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

public class ChannelHandler extends ChatHandlerImpl
{
	public static HashMap<ChatEntity, ArrayList<String>> members = new HashMap<ChatEntity, ArrayList<String>>();
	public static HashMap<String, IChannel> channels = new HashMap<String, IChannel>();
	
	private File channelfile;
	private File playerfile;
	
	public ChannelHandler()
	{
		super();
		
		if(Config.localEnabled) registerChannel(new ChannelLocal());
		registerChannel(new ChannelGlobal());
		
		channelfile = new File("vChat/vchat_channels.json");
		playerfile = new File("vChat/vchat_players.json");
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event)
	{
		ChatEntity player = new ChatEntity(event.player);
		if(!members.containsKey(player))
		{
			for(IChannel channel : channels.values())
				if(channel instanceof ChannelCustom && channel.autoJoin(player)) joinChannel(player, channel, true);
			if(Config.localEnabled) joinChannel(player, getChannel("local"), true);
			joinChannel(player, getChannel("global"), true);
		}
		else
		{
			for(String ch : members.get(player))
			{
				IChannel channel = getChannel(ch);
				if(channel != null) channel.onJoin(player, true);
				else members.get(player).remove(ch);
			}
		}
		if(Config.channelListEnabled) showInfo((EntityPlayerMP)event.player);
	}
	
	@SubscribeEvent()
	public void onPlayerLeft(PlayerEvent.PlayerLoggedOutEvent event)
	{
		ChatEntity player = new ChatEntity(event.player);
		if(members.get(player) == null) return;
		for(String channel : (List<String>)members.get(player).clone())
		{
			getChannel(channel).onLeave(player, true);
		}
	}
	
	@Override
	public void onServerLoad(FMLServerStartingEvent event)
	{
		try {
			if(playerfile.exists())
			{
				JsonParser parser = new JsonParser();
				JsonArray players = (JsonArray)parser.parse(new JsonReader(new FileReader(playerfile)));
				for(int i = 0; i < players.size(); i++)
				{
					JsonObject obj = (JsonObject)players.get(i);
					String name = obj.get("username").getAsString();
					JsonArray channels = obj.get("channels").getAsJsonArray();
					ChatEntity entity = new ChatEntity(name);
					members.put(entity, new ArrayList<String>());
					
					for(int j = 0; j < channels.size(); j++)
					{
						String channel = channels.get(j).getAsString();
						members.get(entity).add(channel);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			VChat.logger.error("Could not read the player file. Maybe it's disrupted or the access is restricted. Try deleting it.");
		}
		
		try {
			if(channelfile.exists())
			{
				JsonParser parser = new JsonParser();
				JsonArray chans = (JsonArray)parser.parse(new JsonReader(new FileReader(channelfile)));
				for(int i = 0; i < chans.size(); i++)
				{
					JsonObject channel = (JsonObject)chans.get(i);
					String name = channel.get("name").getAsString();
					if(channels.containsKey(name)) channels.get(name).read(channel);
					else
					{
						registerChannel(new ChannelCustom(name));
						channels.get(name).read(channel);
					}
				}
			}
		} catch (JsonIOException e) {
			e.printStackTrace();
			VChat.logger.error("Could not read the channel file. Maybe it's disrupted or the access is restricted. Try deleting it.");
		} catch (JsonSyntaxException e2) {
			e2.printStackTrace();
			VChat.logger.error("The channel file contains invalid syntax. It has to be a valid JSON file");
		} catch (NullPointerException e3) {
			e3.printStackTrace();
			VChat.logger.error("The channel file is missing a required field.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		event.registerServerCommand(new CommandChannel());
		if(Config.localEnabled) event.registerServerCommand(new CommandLocal());
		event.registerServerCommand(new CommandGlobal());
	}
	
	@Override
	public void onServerUnload(FMLServerStoppingEvent event) 
	{
		try {
			if(!playerfile.exists()) 
			{
				playerfile.getParentFile().mkdirs();
				playerfile.createNewFile();
			}
			
			JsonArray players = new JsonArray();
			for(ChatEntity entity : members.keySet())
			{
				JsonObject obj = new JsonObject();
				obj.addProperty("username", entity.getUsername());
				JsonArray channels = new JsonArray();
				for(String channel : members.get(entity)) channels.add(new JsonPrimitive(channel));
				obj.add("channels", channels);
				players.add(obj);
			}
			
			FileWriter writer = new FileWriter(playerfile);
			writer.write(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(players));
			writer.flush();
			writer.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			VChat.logger.error("Could not save the player file. Maybe it's disrupted or the access is restricted. Try deleting it.");
		}
		
		try {
			if(!channelfile.exists()) 
			{
				channelfile.getParentFile().mkdirs();
				channelfile.createNewFile();
			}
			
			JsonArray chans = new JsonArray();
			for(IChannel channel : channels.values())
			{
				JsonObject obj = new JsonObject();
				obj.addProperty("name", channel.getName());
				channel.write(obj);
				chans.add(obj);
			}
			
			FileWriter writer = new FileWriter(channelfile);
			writer.write(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(chans));
			writer.flush();
			writer.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			VChat.logger.error("Could not save the channel file. Maybe it's disrupted or the access is restricted. Try deleting it.");
		}
	}
	
	public static boolean joinChannel(ChatEntity player, IChannel channel)
	{
		return joinChannel(player, channel, false);
	}
	
	public static boolean joinChannel(ChatEntity player, IChannel channel, boolean initial)
	{
		IChannel active = getActiveChannel(player);
		if(active != null && active == channel)
		{
			members.get(player).remove(active.getName());
			members.get(player).add(active.getName());
			return true;
		}
		else if(channel.canJoin(player) || player.isBot() || player.isServer())
		{
			channel.onJoin(player, initial);
			if(!members.containsKey(player)) members.put(player, new ArrayList<String>());
			if(members.get(player).contains(channel.getName())) members.get(player).remove(channel.getName());
			members.get(player).add(channel.getName());
			return true;
		}
		return false;
	}
	
	public static void leaveChannel(ChatEntity player, IChannel channel)
	{
		leaveChannel(player, channel, false);
	}
	
	public static void leaveChannel(ChatEntity player, IChannel channel, boolean initial)
	{
		if(members.get(player) == null) return;
		members.get(player).remove(channel.getName());
		channel.onLeave(player, initial);
	}
	
	public static void registerChannel(IChannel channel)
	{
		channels.put(channel.getName(), channel);
	}
	
	public static IChannel getChannel(String name)
	{
		return channels.get(name);
	}
	
	public static IChannel getActiveChannel(IChatEntity player)
	{
		ArrayList<String> joined = members.get(player);
		if(joined == null || joined.size() == 0) return null;
		return channels.get(joined.get(joined.size() - 1));
	}
	
	public static ArrayList<IChannel> getJoinedChannels(IChatEntity player)
	{
		ArrayList<IChannel> list = new ArrayList<IChannel>();
		for(String s : members.get(player)) list.add(channels.get(s));
		return list;
	}
	
	public static void broadcast(IChatComponent component, ChatEntity sender)
	{
		for(ChatEntity player : Misc.getOnlinePlayersAsEntity())
			privateMessageTo(sender, player, component);
	}
	
	public static void broadcastOnChannel(IChannel channel, ChatEntity sender, IChatComponent component)
	{
		for(ChatEntity receiver : channel.getMembers()) privateMessageOnChannel(channel, sender, receiver, component);
	}
	
	public static void privateMessageOnChannel(IChannel channel, ChatEntity sender, ChatEntity receiver, IChatComponent component)
	{
		if((channel.isOnChannel(receiver) || receiver.isServer() || receiver.isBot()) && channel.canReceiveChat(sender, receiver, component))
		{
			component = channel.formatChat(sender, receiver, component);
			if(channel.getPrefix() != null)
			{
				ChatComponentText text = new ChatComponentText("");
				text.appendText("[" + channel.getPrefix() + "] ");
				text.appendSibling(component);
				privateMessageTo(sender, receiver, text);
			}	
			else privateMessageTo(sender, receiver, component);
		}
	}
	
	public static void privateMessageTo(ChatEntity sender, ChatEntity receiver, IChatComponent message)
	{
		if(receiver == null || sender == null) return;
		EntityPlayerMP player = Misc.getPlayer(receiver.getUsername());
		
		if(player != null) player.addChatComponentMessage(message);
		else if(receiver.isBot()) VChat.botLoader.getBot(receiver.getUsername()).owningBot.onPrivateMessage(message.getUnformattedText(), sender);
		else if(receiver.isServer()) MinecraftServer.getServer().addChatMessage(message);
	}
	
	public static void showInfo(EntityPlayerMP player)
	{
		ChatEntity entity = new ChatEntity(player);
		IChannel channel = getActiveChannel(entity);
		if(channel != null) 
		{
			player.addChatMessage(new ChatComponentText("You are talking on channel \"" + channel.getName() + "\"."));
			player.addChatMessage(new ChatComponentText("Currently joined channels: " + members.get(entity).toString()));
		}
		else player.addChatMessage(new ChatComponentText("You haven't joined any channel."));
	}
	
	public static class CommandLocal extends Misc.CommandOverrideAccess
	{
		@Override
		public String getCommandName() 
		{
			return "local";
		}

		@Override
		public String getCommandUsage(ICommandSender sender) 
		{
			return "/local <message>";
		}

		@Override
		public List getCommandAliases() 
		{
			return Arrays.asList("l"); 
		}

		@Override
		public int getRequiredPermissionLevel() 
		{
			return 0;
		}

		@Override
		public void processCommand(ICommandSender sender, String[] args) 
		{
			if(args.length < 1) throw new WrongUsageException(getCommandUsage(sender));
			MinecraftServer.getServer().getCommandManager().executeCommand(sender, "/channel msg local " + StringUtils.join(Arrays.asList(args), " "));
		}	
	}
	
	public static class CommandGlobal extends Misc.CommandOverrideAccess
	{
		@Override
		public String getCommandName() 
		{
			return "global";
		}

		@Override
		public String getCommandUsage(ICommandSender sender) 
		{
			return "/global <message>";
		}
		
		@Override
		public List getCommandAliases() 
		{
			return Arrays.asList("g"); 
		}
		
		@Override
		public int getRequiredPermissionLevel() 
		{
			return 0;
		}

		@Override
		public void processCommand(ICommandSender sender, String[] args) 
		{
			if(args.length < 1) throw new WrongUsageException(getCommandUsage(sender));
			MinecraftServer.getServer().getCommandManager().executeCommand(sender, "/channel msg global " + StringUtils.join(Arrays.asList(args), " "));
		}	
	}
	
	public static class CommandChannel extends Misc.CommandOverrideAccess
	{
		@Override
		public String getCommandName() 
		{
			return "channel";
		}

		@Override
		public String getCommandUsage(ICommandSender sender) 
		{
			if(sender instanceof EntityPlayerMP) return "/channel [join/leave/msg/list/ban/unban/whitelist add/whitelist remove/kick/mute/unmute] [...]";
			return "/channel <msg/list/ban/unban/whitelist add/whitelist remove/kick/mute/unmute> [...]";
		}

		@Override
		public int getRequiredPermissionLevel() 
		{
			return 0;
		}

		@Override
		public List getCommandAliases() 
		{
			return Arrays.asList(new String[]{"ch"});
		}

		@Override
		public void processCommand(ICommandSender sender, String[] args) 
		{
			boolean isPlayer = sender instanceof EntityPlayerMP;
			EntityPlayerMP player = (EntityPlayerMP)(isPlayer ? sender : null);
			
			if(args.length == 0)
			{
				if(isPlayer) showInfo(player);	
				else throw new WrongUsageException(getCommandUsage(sender));
			}
			else
			{
				if(args[0].equalsIgnoreCase("join") && isPlayer)
				{
					if(args.length != 2) throw new WrongUsageException("/channel join <channel>");

					IChannel channel = getChannel(args[1]);
					if(channel == null) throw new ChannelNotFoundException(args[1]);
					if(!channel.canJoin(new ChatEntity(player))) throw new CommandException("You are not allowed to join channel \"" + channel.getName() + "\"!");
					if(joinChannel(new ChatEntity(player), channel))
					{
						sender.addChatMessage(new ChatComponentText("You are now talking on \"" + channel.getName() + "\"."));
						
						ChatComponentText comp = new ChatComponentText("Currently active: ");
						comp.appendText("[");
						
						Iterator<ChatEntity> iterator = channel.getMembers().iterator();
						while(iterator.hasNext())
						{
							ChatComponentText nameComponent = Misc.getComponent(iterator.next());
							comp.appendSibling(nameComponent);
							if(iterator.hasNext()) comp.appendText(", ");
						}
						comp.appendText("]");
						
						sender.addChatMessage(comp);
					}	
				}
				else if(args[0].equalsIgnoreCase("leave") && isPlayer)
				{
					if(args.length > 2) throw new WrongUsageException("/channel leave [channel]");
					
					IChannel channel = null;
					if(args.length == 2)
					{
						channel = getChannel(args[1]);
						if(channel == null) throw new ChannelNotFoundException(args[1]);
						if(!channel.isOnChannel(new ChatEntity(player))) throw new ChannelNotJoinedException(channel);
					}
					else 
					{
						channel = getActiveChannel(new ChatEntity(player));
						if(channel == null) throw new ChannelNotJoinedException();
					}
					
					leaveChannel(new ChatEntity(player), channel);
					sender.addChatMessage(new ChatComponentText("You left channel \"" + channel.getName() + "\"."));
				}
				else if(args[0].equalsIgnoreCase("msg"))
				{
					if(isPlayer)
					{
						if(args.length < 3) throw new CommandException("/channel msg <channel> <message>");
						IChannel channel = getChannel(args[1]);
						if(channel == null) throw new ChannelNotFoundException(args[1]);
						if(!channel.isOnChannel(new ChatEntity(player))) throw new ChannelNotJoinedException(channel);		
						
						String message = StringUtils.join(Arrays.asList(args).subList(2, args.length).toArray(), " ");
						ChatComponentTranslation component = new ChatComponentTranslation("chat.type.text", player.func_145748_c_(), message);
						
						ChatEntity entity = new ChatEntity(player);
						IChannel current = getActiveChannel(entity);
						
						joinChannel(entity, channel, true);
						//Call the event to give other mods the chance to modify the chat as well
						component = ForgeHooks.onServerChatEvent(player.playerNetServerHandler, message, component);
						if(component != null) MinecraftServer.getServer().getConfigurationManager().sendChatMsg(component);
						joinChannel(entity, current, true);
					}
					else
					{
						if(args.length < 3) throw new CommandException("/channel msg <channel> <message>");
						IChannel channel = getChannel(args[1]);
						if(channel == null) throw new ChannelNotFoundException(args[1]);
						String message = StringUtils.join(Arrays.asList(args).subList(2, args.length).toArray(), " ");
						broadcastOnChannel(channel, ChatEntity.SERVER, new ChatComponentText(message));
					}
				}
				else if(args[0].equalsIgnoreCase("list"))
				{
					if(args.length == 1)
					{
						ChatComponentText text = new ChatComponentText("Currently active channels: ");
						Iterator<IChannel> iterator = channels.values().iterator();
						while(iterator.hasNext())
						{
							IChannel channel = iterator.next();
							text.appendText(channel.getName() + " [" + channel.getMembers().size() + "]" + (iterator.hasNext() ? ", " : ""));
						}
						sender.addChatMessage(text);
					}
					else if(args.length == 2)
					{
						IChannel channel = getChannel(args[1]);
						if(channel == null) throw new ChannelNotFoundException(args[1]);
						if(isPlayer && !channel.isOnChannel(new ChatEntity(player))) throw new ChannelNotJoinedException(channel);
						sender.addChatMessage(new ChatComponentText(channel.getMembers().size() + " player(s) active on channel \"" + channel.getName() +"\":"));
						
						ChatComponentText comp = new ChatComponentText("");
						comp.appendText("[");
						
						Iterator<ChatEntity> iterator = channel.getMembers().iterator();
						while(iterator.hasNext())
						{
							ChatComponentText nameComponent = Misc.getComponent(iterator.next());
							comp.appendSibling(nameComponent);
							if(iterator.hasNext()) comp.appendText(", ");
						}
						comp.appendText("]");
						
						sender.addChatMessage(comp);
					}
					else throw new WrongUsageException("/ch list [channel]");
				}
				else if(args[0].equalsIgnoreCase("ban") || args[0].equalsIgnoreCase("unban") || args[0].equalsIgnoreCase("kick") || args[0].equalsIgnoreCase("mute") || args[0].equalsIgnoreCase("unmute"))
				{
					if(Misc.checkPermission(sender, 3))
					{
						if(args.length != 3) throw new WrongUsageException("/channel " + args[0].toLowerCase(Locale.ROOT) + " <channel> <player>");
						
						boolean u = args[0].equalsIgnoreCase("unban") || args[0].equalsIgnoreCase("unmute");
						
						IChannel channel = getChannel(args[1]);
						if(channel == null) throw new ChannelNotFoundException(args[1]);
						if((args[0].equalsIgnoreCase("ban") || args[0].equalsIgnoreCase("unban")) && channel.isWhitelisted())
							throw new CommandException("The channel " + channel.getName() + " is whitelisted. Use /channel whitelist <add/remove> instead.");
						
						ChatEntity entity = new ChatEntity(args[2]);
						if(entity.toPlayer() == null && args[0].equalsIgnoreCase("kick"))
						{
							//Allows kicking of nicknamed players
							String s = NickHandler.getPlayerFromNick(args[2]);
							if(s != null) entity = new ChatEntity(s);
						}
						
						if(args[0].equalsIgnoreCase("ban") || args[0].equalsIgnoreCase("unban")) channel.ban(isPlayer ? new ChatEntity(player) : ChatEntity.SERVER, entity, u);
						else if(args[0].equalsIgnoreCase("mute") || args[0].equalsIgnoreCase("unmute")) channel.mute(isPlayer ? new ChatEntity(player) : ChatEntity.SERVER, entity, u);
						else channel.kick(isPlayer ? new ChatEntity(player) : ChatEntity.SERVER, entity);
					}
				}
				else if(args[0].equalsIgnoreCase("whitelist"))
				{
					if(Misc.checkPermission(sender, 3))
					{
						if(args.length != 4 || (!args[1].equalsIgnoreCase("add") && !args[1].equalsIgnoreCase("remove"))) 
							throw new WrongUsageException("/channel whitelist <add/remove> <channel> <player>");
						
						boolean u = args[1].equalsIgnoreCase("remove");
						IChannel channel = getChannel(args[2]);
						if(channel == null) throw new ChannelNotFoundException(args[2]);
						
						ChatEntity entity = new ChatEntity(args[3]);
						channel.ban(isPlayer ? new ChatEntity(player) : ChatEntity.SERVER, entity, u);
					}
				}
				else throw new WrongUsageException(getCommandUsage(sender));
			}
		}
	}
	
	public static class ChannelNotFoundException extends CommandException
	{
		public ChannelNotFoundException(String channel) 
		{
			super("The specified channel \"" + channel + "\" does not exist!");
		}	
	}
	
	public static class ChannelNotJoinedException extends CommandException
	{
		public ChannelNotJoinedException() 
		{
			super("You haven't joined any channel.");
		}
		
		public ChannelNotJoinedException(IChannel channel)
		{
			super("You haven't joined \"" + channel.getName() + "\"!");
		}
	}
}
