package vic.mod.chat.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import vic.mod.chat.Misc.CommandOverrideAccess;
import vic.mod.chat.Track;
import vic.mod.chat.Track.ParseException;
import vic.mod.chat.VChat;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class TrackHandler extends ChatHandlerImpl
{
	private LinkedHashMap<String, Track> tracks = new LinkedHashMap<String, Track>();
	private ArrayList<Track> playing = new ArrayList<Track>();
	
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
					} catch (ParseException e) {
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
			for(Track track : (ArrayList<Track>) playing.clone()) 
				track.play();
	}
	
	public Track getTrack(String name)
	{
		return tracks.get(name);
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
	
	public static class CommandTrack extends CommandOverrideAccess
	{
		@Override
		public String getCommandName() 
		{
			return "track";
		}

		@Override
		public String getCommandUsage(ICommandSender sender) 
		{
			return "/track <broadcast/play/stop> [name] [track]";
		}

		@Override
		public void processCommand(ICommandSender sender, String[] args) 
		{
			if(args.length == 0 || StringUtils.isNumeric(args[0]))
			{
				HashMap<String, Track> tracks = VChat.trackHandler.tracks;
				if(tracks.isEmpty()) 
				{
					sender.addChatMessage(new ChatComponentText("There are no tracks loaded on the server."));
					return;
				}
				
				int page = (args.length > 0 && StringUtils.isNumeric(args[0])) ? Integer.parseInt(args[0]) : 0;
				page = page < 0 ? 0 : page;
				int numPages = tracks.size() / 6 + 1;
				if(page > numPages) throw new CommandException("Exceeded the number of pages, " + numPages + ".");
				List<Track> trackList = new ArrayList(tracks.values()).subList(page * 6, MathHelper.clamp_int((page + 1 * 6) - 1, 0, tracks.size()));
				sender.addChatMessage(new ChatComponentTranslation("Currently loaded tracks (Page %s of %s):", page, numPages));
				for(int i = 0; i < trackList.size(); i++)
				{
					int num = i * page + 1;
					IChatComponent comp = new ChatComponentTranslation("%s: \"%s\"", num, trackList.get(i).name);
					comp.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to play!")));
					comp.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/track play " + num));
					sender.addChatMessage(comp);
				}
			}
			else
			{
				if(sender instanceof EntityPlayerMP)
				{
					EntityPlayerMP player = (EntityPlayerMP)sender;
					try {
						Track track = Track.parseTrack("Test Track", StringUtils.join(Arrays.asList(args).subList(0, args.length), " "));
						track.start(Arrays.asList(player));
					} catch (ParseException e) {
						throw new CommandException(e.getMessage());
					}
				}
				else throw new CommandException("You *might* just not want to do that.");
			}	
		}	
	}
}
