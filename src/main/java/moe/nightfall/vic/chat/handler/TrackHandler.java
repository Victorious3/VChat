package moe.nightfall.vic.chat.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.Misc;
import moe.nightfall.vic.chat.Track;
import moe.nightfall.vic.chat.VChat;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class TrackHandler extends ChatHandlerImpl
{
	private LinkedHashMap<String, Track> tracks = new LinkedHashMap<String, Track>();
	private LinkedHashSet<Track> playing = new LinkedHashSet<Track>();
	
	public TrackHandler()
	{
		super();
		
		try {
			File trackDir = new File("vChat/tracks");
			if(!trackDir.exists()) trackDir.mkdirs();
			
			VChat.logger.info("Loading Tracks...");	
			long startTime = System.currentTimeMillis();
			for(File f : trackDir.listFiles())
			{
				if(f.getName().endsWith(".track"))
				{
					try {
						String contents = FileUtils.readFileToString(f);
						Track track = Track.parseTrack(FilenameUtils.removeExtension(f.getName()), contents);
						tracks.put(track.name, track);
						VChat.logger.info("Loaded track " + tracks.size() + ": \"" + track.name + "\"");
					} catch (Track.ParseException e) {
						VChat.logger.error("Failed to parse track " + f.getName() + ":" + e.getMessage());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			VChat.logger.info("...done! A total of " + tracks.size() + " tracks loaded in " + (System.currentTimeMillis() - startTime) + " ms");
		} catch (Exception e) {
			VChat.logger.error("Loading of the tracks failed!");
			e.printStackTrace();
		}
	}
	
	@Override
	public void onServerLoad(FMLServerStartingEvent event) 
	{
		event.registerServerCommand(new CommandTrack());
	}
	
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event)
	{
		if(event.phase == Phase.END)
			for(Track track : (LinkedHashSet<Track>) playing.clone()) 
				track.play();
	}
	
	public Track getTrack(String name)
	{
		return tracks.get(name);
	}
	
	public Track getTrack(int index)
	{
		if(index < 0 || index >= tracks.size()) return null;
		return (Track)tracks.values().toArray()[index];
	}
	
	public Track getTrack(Object o)
	{
		Track track = null;
		if(StringUtils.isNumeric(o.toString()))
			track = VChat.trackHandler.getTrack(Integer.parseInt(o.toString()));
		else track = VChat.trackHandler.getTrack(o.toString());
		return track;
	}
	
	public void startTrack(Track track)
	{
		playing.add(track);
	}
	
	public void stopTrack(Track track)
	{
		playing.remove(track);
	}
	
	public void stopAll()
	{
		playing.clear();
	}
	
	public void stopAll(EntityPlayerMP player)
	{
		for(Track t : (LinkedHashSet<Track>)playing.clone())
			t.stopTrack(player);
	}
	
	public static class CommandTrack extends Misc.CommandOverrideAccess
	{
		@Override
		public String getCommandName() 
		{
			return "track";
		}

		@Override
		public String getCommandUsage(ICommandSender sender) 
		{
			return "/track <broadcast/stopbr/play/stop> [name] [track]";
		}

		@Override
		public void processCommand(ICommandSender sender, String[] args) throws CommandException 
		{
			args = Misc.parseArgs(args);
			if(args.length == 0 || StringUtils.isNumeric(args[0]))
			{
				HashMap<String, Track> tracks = VChat.trackHandler.tracks;
				if(tracks.isEmpty()) 
				{
					sender.addChatMessage(new ChatComponentText("There are no tracks loaded on the server."));
					return;
				}
				
				int page = (args.length > 0 && StringUtils.isNumeric(args[0])) ? Integer.parseInt(args[0]) - 1 : 0;
				page = page < 0 ? 0 : page;
				int numPages = tracks.size() / 6 + 1;
				if(page >= numPages) throw new CommandException("Exceeded the number of pages, " + numPages + ".");
				List<Track> trackList = new ArrayList(tracks.values()).subList(page * 6, MathHelper.clamp_int(((page + 1) * 6) - 1, 0, tracks.size()));
				sender.addChatMessage(new ChatComponentTranslation("Currently loaded tracks (Page %s of %s):", page + 1, numPages));
				for(int i = 0; i < trackList.size(); i++)
				{
					int num = page * 6 + i + 1;
					IChatComponent comp = new ChatComponentTranslation("%s: \"%s\"", num, trackList.get(i).name);
					comp.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to play!")));
					comp.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/track play " + num));
					sender.addChatMessage(comp);
				}
			}
			else if(args[0].equalsIgnoreCase("broadcast") || args[0].equalsIgnoreCase("play"))
			{
				boolean broadcast = args[0].equalsIgnoreCase("broadcast");
				if(broadcast && !Misc.checkPermission(sender, Config.trackPermissionLevel)) return;
				else if(!broadcast && !Misc.checkPermission(sender, Config.trackPermissionLevelSelf)) return;
				
				if(args.length > 1)
				{
					Track track = null;
					if(args.length == 2)
					{
						track = VChat.trackHandler.getTrack((Object)args[1]);
						if(track == null) throw new CommandException("No track with the specified name/index " + args[1] + " found!");
					}
					else
					{
						try {
							track = Track.parseTrack(args[1], StringUtils.join(Arrays.asList(args).subList(2, args.length), " "));
						} catch (Track.ParseException e) {
							throw new CommandException(e.getMessage());
						}
					}
					sender.addChatMessage(new ChatComponentText("Broadcasting track \"" + args[1] + "\""));
					if(broadcast) track.start();
					else 
					{
						if(sender instanceof EntityPlayerMP)
							track.start(new ArrayList(Arrays.asList(sender)));
						else throw new CommandException("You *might* just not want to do that. Sorry, the console has no sound output.");
					}
				}
				else throw new WrongUsageException(getCommandUsage(sender));
			}
			else if(args[0].equalsIgnoreCase("stopbr"))
			{
				if(!Misc.checkPermission(sender, Config.trackPermissionLevel)) return;
				if(args.length == 1)
				{
					VChat.trackHandler.stopAll();
					sender.addChatMessage(new ChatComponentText("Stopped all tracks."));
				}
				else
				{
					Track track = VChat.trackHandler.getTrack((Object)args[1]);
					if(track == null) throw new CommandException("No track with the specified name/index " + args[1] + " found!");
					VChat.trackHandler.stopTrack(track);
					sender.addChatMessage(new ChatComponentText("Stopped track \"" + args[1] + "\"."));
				}
			}
			else if(args[0].equalsIgnoreCase("stop"))
			{
				if(!(sender instanceof EntityPlayerMP)) return;
				EntityPlayerMP player = (EntityPlayerMP)sender;
				if(args.length == 1)
				{
					VChat.trackHandler.stopAll(player);
					sender.addChatMessage(new ChatComponentText("Stopped all tracks."));
				}
				else
				{
					Track track = VChat.trackHandler.getTrack((Object)args[1]);
					if(track == null) throw new CommandException("No track with the specified name/index " + args[1] + " found!");
					track.stopTrack(player);
					sender.addChatMessage(new ChatComponentText("Stopped track \"" + args[1] + "\"."));
				}
			}
			else throw new WrongUsageException(getCommandUsage(sender));
		}	
	}
}
