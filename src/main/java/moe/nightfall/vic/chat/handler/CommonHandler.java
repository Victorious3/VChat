package moe.nightfall.vic.chat.handler;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import moe.nightfall.vic.chat.BotHandler;
import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.ChatFormatter;
import moe.nightfall.vic.chat.Misc;
import moe.nightfall.vic.chat.api.IChannel;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.command.server.CommandEmote;
import net.minecraft.command.server.CommandMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;

import org.apache.commons.lang3.StringUtils;

import moe.nightfall.vic.chat.ChannelCustom;
import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.VChat;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

public class CommonHandler extends ChatHandlerImpl
{
	private File playerFile;
	private HashMap<String, OnlineTracker> playerTracker = new HashMap<String, OnlineTracker>();
	
	public CommonHandler()
	{
		super();
		playerFile = new File("vChat/vchat_playertracker.json");
	}
	
	public void loadPlayers()
	{
		try {
			if(playerFile.exists())
			{
				JsonParser parser = new JsonParser();
				JsonArray players = (JsonArray)parser.parse(new JsonReader(new FileReader(playerFile)));
				for(int i = 0; i < players.size(); i++)
				{
					JsonObject obj = (JsonObject)players.get(i);
					String name = obj.get("username").getAsString();
					playerTracker.put(name, new OnlineTracker(name, obj.get("lastSeen").getAsLong(), obj.get("online").getAsLong()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			VChat.logger.error("Could not read the player file. Maybe it's disrupted or the access is restricted. Try deleting it.");
		}
	}
	
	public void savePlayers()
	{
		try {
			if(!playerFile.exists()) 
			{
				playerFile.getParentFile().mkdirs();
				playerFile.createNewFile();
			}
			JsonArray playerArray = new JsonArray();
			for(OnlineTracker tracker : playerTracker.values())
			{
				JsonObject obj = new JsonObject();
				obj.addProperty("username", tracker.name);
				obj.addProperty("lastSeen", tracker.lastSeen);
				obj.addProperty("online", tracker.online);
				playerArray.add(obj);
			}
			
			FileWriter writer = new FileWriter(playerFile);
			writer.write(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(playerArray));
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			VChat.logger.error("Could not save the player file. Maybe it's disrupted or the access is restricted. Try deleting it.");
		}
	}
	
	@SubscribeEvent
	public void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event)
	{
		if(Config.modtEnabled)
			for(String s : Misc.parseModt(Config.modt, (EntityPlayerMP) event.player))
				event.player.addChatComponentMessage(new ChatComponentText(s));
		
		if(Config.onlineTrackerEnabled)
		{
			String name = event.player.getCommandSenderName();
			if(!playerTracker.containsKey(name))
				playerTracker.put(name, new OnlineTracker(name, System.currentTimeMillis(), 0));
		}
	}
	
	@SubscribeEvent
	public void onPlayerLeft(PlayerEvent.PlayerLoggedOutEvent event)
	{
		if(Config.onlineTrackerEnabled)
		{
			String name = event.player.getCommandSenderName();
			OnlineTracker tracker = playerTracker.get(name);
			if(tracker == null) return;
			tracker.online = tracker.getOnlineTime();
			tracker.lastSeen = System.currentTimeMillis();
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onChat(ServerChatEvent event)
	{
		ChatEntity entity = new ChatEntity(event.player);
		IChannel channel = ChannelHandler.getActiveChannel(entity);
		if(channel == null)
		{
			ChatComponentText text = new ChatComponentText("You have to join a channel to use the chat!");
			text.getChatStyle().setColor(EnumChatFormatting.RED);
			event.player.addChatComponentMessage(text);
			event.setCanceled(true);
			return;
		}
		else if(channel.isMuted(entity)) 
		{
			ChatComponentText text = new ChatComponentText("You are muted on this channel!");
			text.getChatStyle().setColor(EnumChatFormatting.RED);
			ChannelHandler.privateMessageOnChannel(channel, ChatEntity.SERVER, entity, text);
			event.setCanceled(true);
			return;
		}
		
		String message = event.message;
		
		boolean applyFormat = true;
		if(message.startsWith("#"))
		{
			message = message.replaceFirst("#", "");
			applyFormat = false;
		}
		
		ChatComponentText computed = new ChatComponentText("");
		computed.appendSibling(new ChatComponentText(message));
		computed.getChatStyle().setColor(channel.getColor());
		
		if(applyFormat && Config.urlEnabled)
			if(Config.urlPermissionLevel == 0 || event.player.canCommandSenderUseCommand(Config.urlPermissionLevel, null))
			{
				if(Config.urlEnabledYoutube) new ChatFormatter.ChatFormatterYoutube().apply(computed);
				if(Config.urlEnabledSoundCloud) new ChatFormatter.ChatFormatterSoundCloud().apply(computed);
				new ChatFormatter.ChatFormatterURL().apply(computed);
			}		
		if(Config.colorPermissionLevel == 0 || event.player.canCommandSenderUseCommand(Config.colorPermissionLevel, null)) new ChatFormatter.ChatFormatterColor().apply(computed);

		ChatComponentText componentName = Misc.getComponent(entity);
		
		ArrayList<EntityPlayerMP> mentioned = new ArrayList<EntityPlayerMP>();
		
		for(Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList)
		{
			ChatComponentText computed2 = computed.createCopy();
			ChatEntity receiver = new ChatEntity(obj);
			if(applyFormat) 
			{
				for(Object obj2 : MinecraftServer.getServer().getConfigurationManager().playerEntityList)
				{
					ChatEntity player = new ChatEntity(obj2);
					new ChatFormatter.ChatFormatterUsername(player, receiver, false, mentioned).apply(computed2);	
				}
				if(Config.nickEnabled)
				{
					for(Object obj2 : MinecraftServer.getServer().getConfigurationManager().playerEntityList)
					{
						ChatEntity player = new ChatEntity(obj2);
						new ChatFormatter.ChatFormatterUsername(player, receiver, true, mentioned).apply(computed2);	
					}
				}
				if(Config.pingHighlighted)
				{
					EntityPlayerMP player = receiver.toPlayer();
					if(player != null && mentioned.contains(player) && ChannelHandler.getJoinedChannels(receiver).contains(channel))
						player.playerNetServerHandler.sendPacket(new S29PacketSoundEffect(Config.pingSound, player.posX, player.posY, player.posZ, Config.pingVolume, Config.pingPitch));
					mentioned.clear();
				}			
			}
			VChat.channelHandler.privateMessageOnChannel(channel, entity, receiver, new ChatComponentTranslation("chat.type.text", componentName, computed2));
		}
		
		if(!channel.getName().equals("local") && !(channel instanceof ChannelCustom && ((ChannelCustom)channel).hasRange()))
			for(BotHandler bot : VChat.botLoader.bots.values())
				bot.owningBot.onMessage(message, entity, channel);
		
		MinecraftServer.getServer().addChatMessage(new ChatComponentTranslation("chat.type.text", componentName, computed));
		event.setCanceled(true);
	}	
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onCommand(CommandEvent event)
	{
		if(event.command instanceof CommandEmote)
		{
			if(event.sender.canCommandSenderUseCommand(Config.colorPermissionLevel, null))
			{
				if(event.parameters.length < 1) return;
				String out = "";
				for(int i = 0; i < event.parameters.length; i++) 
				{
					out += event.parameters[i] + " ";
					if(i != 0) event.parameters[i] = "";
				}
				event.parameters[0] = out.replaceAll("&", "\u00A7");
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onCommandPost(CommandEvent event)
	{
		if(event.command instanceof CommandMessage)
		{
			if(event.parameters.length > 0)
			{
				String name = event.parameters[0];
				if(Misc.getPlayer(name) == null && VChat.botLoader.containsBot(name))
				{
					BotHandler bot = VChat.botLoader.getBot(name);
					String message = StringUtils.join(Arrays.asList(event.parameters).subList(1, event.parameters.length).toArray(), " ");
					ChatEntity entity;
					if(event.sender instanceof EntityPlayerMP) entity = new ChatEntity(event.sender);
					else entity = ChatEntity.SERVER;
					bot.owningBot.onPrivateMessage(message, entity);
					event.setCanceled(true);
				}
			}
		}
	}
	
	@Override
	public void onServerLoad(FMLServerStartingEvent event) 
	{
		event.registerServerCommand(new CommandPos());
		event.registerServerCommand(new CommandTop());
		if(Config.onlineTrackerEnabled) 
			event.registerServerCommand(new CommandList());
		loadPlayers();
	}

	@Override
	public void onServerUnload(FMLServerStoppingEvent event) 
	{
		if(Config.onlineTrackerEnabled) savePlayers();
	}

	public static class CommandList extends Misc.CommandOverrideAccess
	{
		@Override
		public String getCommandName() 
		{
			return "list";
		}
		
		@Override
		public int getRequiredPermissionLevel() 
		{
			return 0;
		}
		
		@Override
		public String getCommandUsage(ICommandSender sender) 
		{
			return "/list";
		}

		@Override
		public void processCommand(ICommandSender sender, String[] args) 
		{
			sender.addChatMessage(new ChatComponentText("--Name--Playtime--Last seen--"));
			for(OnlineTracker tracker : VChat.commonHandler.playerTracker.values())
			{
				sender.addChatMessage(tracker.toChatComponent());
			}
		}
	}
	
	public static class OnlineTracker implements Comparable<OnlineTracker>
	{
		private long lastSeen, online;
		String name;
		
		public OnlineTracker(String name, long lastSeen, long online)
		{
			this.name = name;
			this.lastSeen = lastSeen;
			this.online = online;
		}
		
		public IChatComponent toChatComponent()
		{
			ChatComponentText text = new ChatComponentText(name + "--" + Misc.getDuration(getOnlineTime()) + "--");
			if(isOnline())
			{
				ChatComponentText comp1 = new ChatComponentText("online");
				comp1.getChatStyle().setColor(EnumChatFormatting.GREEN);
				text.appendSibling(comp1);
			}
			else text.appendText(Misc.getDuration(System.currentTimeMillis() - lastSeen));
			return text;
		}
		
		public long getOnlineTime()
		{
			return online + (isOnline() ? System.currentTimeMillis() - lastSeen : 0);
		}
		
		public boolean isOnline()
		{
			return Misc.getPlayer(name) != null;
		}
		
		@Override
		public int compareTo(OnlineTracker arg0) 
		{
			if(arg0.isOnline() && !isOnline()) return -1;
			else if(!arg0.isOnline() && isOnline()) return 1;
			else return name.compareTo(arg0.name);
		}
	}
	
	public static class CommandPos extends Misc.CommandOverrideAccess
	{
		@Override
		public String getCommandName() 
		{
			return "checkpos";
		}
		
		@Override
		public List getCommandAliases() 
		{
			return Arrays.asList("pos");
		}
		
		@Override
		public int getRequiredPermissionLevel() 
		{
			return Config.posPermissionLevel;
		}

		@Override
		public String getCommandUsage(ICommandSender sender) 
		{
			return "/checkpos <player>";
		}

		@Override
		public void processCommand(ICommandSender sender, String[] args) 
		{
			if(args.length < 1) throw new WrongUsageException(getCommandUsage(sender));
			EntityPlayerMP player = Misc.getPlayer(args[0]);
			if(player == null) throw new PlayerNotFoundException();
			sender.addChatMessage(new ChatComponentText(player.getCommandSenderName() + ": [" + (int)player.posX + ", " + (int)player.posY + ", " + (int)player.posZ + "]"));
		}

		@Override
		public List addTabCompletionOptions(ICommandSender sender, String[] args)
		{
			return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
		}
		
		@Override
		public boolean isUsernameIndex(String[] args, int index) 
		{
			return index == 0;
		}
	}

	public static class CommandTop extends Misc.CommandOverrideAccess
	{
		@Override
		public String getCommandName() 
		{
			return "top";
		}

		@Override
		public int getRequiredPermissionLevel() 
		{
			return Config.topPermissionLevel;
		}

		@Override
		public String getCommandUsage(ICommandSender sender) 
		{
			return "/top";
		}

		@Override
		public void processCommand(ICommandSender sender, String[] args) 
		{
			Runtime runtime = Runtime.getRuntime();
			int proc = runtime.availableProcessors();
			long uptime = MinecraftServer.getServer().getTickCounter() * 50;
			long sysmem = Misc.getDeviceMemory() / (1024 * 1024);
			long freemem = runtime.totalMemory() - runtime.freeMemory();
			long maxmem = runtime.maxMemory();
			float usedmem = (float)(freemem / (double)maxmem);
			freemem /= (1024 * 1024);
			maxmem /= (1024 * 1024);
			float cpuload = (float)Misc.getCPULoad();
			
			int day = (int)TimeUnit.MILLISECONDS.toHours(uptime) / 24;
			int hrs = (int)TimeUnit.MILLISECONDS.toHours(uptime) % 24;
			int min = (int)TimeUnit.MILLISECONDS.toMinutes(uptime) % 60;
			int sec = (int)TimeUnit.MILLISECONDS.toSeconds(uptime) % 60;
			
			ChatComponentText uptComp = new ChatComponentText(String.format("Uptime: %d:%02d:%02d:%02d", day, hrs, min, sec));
			ChatComponentText aprComp = new ChatComponentText(String.format("Available processors: %d", proc));
			ChatComponentText sysmemComp = new ChatComponentText(String.format("System memory: %d MB", sysmem));
			
			ChatComponentText memComp = new ChatComponentText("RAM: ");
			memComp.appendSibling(createBar(usedmem, freemem + "/" + maxmem + "MB", 50));
			
			ChatComponentText cpuComp = new ChatComponentText("CPU: ");
			cpuComp.appendSibling(createBar(cpuload, (int)(cpuload * 100) + "%", 50));
			
			sender.addChatMessage(uptComp);
			sender.addChatMessage(aprComp);
			sender.addChatMessage(sysmemComp);
			sender.addChatMessage(memComp);
			sender.addChatMessage(cpuComp);
		}
		
		private ChatComponentText createBar(float value, String inlay, int maxLength)
		{
			value = MathHelper.clamp_float(value, 0, 1);
			maxLength -= 2;
			int length = (int)(value * maxLength);
			String out = StringUtils.repeat('#', MathHelper.clamp_int(length, 0, maxLength - inlay.length()));
			out += StringUtils.repeat('_', MathHelper.clamp_int(maxLength - length - inlay.length(), 0, maxLength - inlay.length()));
			if(length > maxLength - inlay.length())
			{
				int off = inlay.length() - (maxLength - length);
				out += inlay.substring(0, off);
				inlay = inlay.substring(off, inlay.length());
			}
			length = out.length();
			out += StringUtils.repeat('_', maxLength - out.length());
			
			ChatComponentText comp = new ChatComponentText("[");
			
			int i1 = MathHelper.clamp_int(maxLength / 3, 0, length);
			ChatComponentText c1 = new ChatComponentText(out.substring(0, i1));
			int i2 = MathHelper.clamp_int(i1 + maxLength / 3, 0, length);
			ChatComponentText c2 = new ChatComponentText(out.substring(i1, i2));
			ChatComponentText c3 = new ChatComponentText(out.substring(i2, length));
			
			c1.getChatStyle().setColor(EnumChatFormatting.GREEN);
			c2.getChatStyle().setColor(EnumChatFormatting.YELLOW);
			c3.getChatStyle().setColor(EnumChatFormatting.RED);
			
			comp.appendSibling(c1);
			comp.appendSibling(c2);
			comp.appendSibling(c3);
			comp.appendText(inlay + "]");
			
			return comp;
		}
	}
}
