package vic.mod.chat.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import org.apache.commons.lang3.StringUtils;

import vic.mod.chat.Misc.CommandOverrideAccess;
import vic.mod.chat.Track;
import vic.mod.chat.Track.ParseException;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class TrackHandler extends ChatHandlerImpl
{
	private HashMap<String, Track> tracks = new HashMap<String, Track>();
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
