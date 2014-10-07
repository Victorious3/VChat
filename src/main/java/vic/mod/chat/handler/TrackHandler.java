package vic.mod.chat.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;

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
				
				int page = (args.length > 0 && StringUtils.isNumeric(args[0])) ? Integer.parseInt(args[0]) : 1;
				page = page < 1 ? 1 : page;
				int numPages = tracks.size() / 6 + 1;
				if(page > numPages) throw new CommandException("Exceeded the number of pages, " + numPages + ".");
				List<Track> trackList = new ArrayList(tracks.values()).subList(page * 6, MathHelper.clamp_int((page + 1 * 6) - 1, 0, tracks.size()));
				sender.addChatMessage(new ChatComponentTranslation("Currently loaded tracks (Page %d of %d):", page, numPages));
				for(int i = 0; i < trackList.size(); i++)
					sender.addChatMessage(new ChatComponentTranslation("%d: \"%s\"", i * page, trackList.get(i).name));
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
