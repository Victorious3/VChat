package moe.nightfall.vic.chat.handlers;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import moe.nightfall.vic.chat.commands.CommandTrack;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import moe.nightfall.vic.chat.tracks.Track;
import moe.nightfall.vic.chat.VChat;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class TrackHandler extends ChatHandler
{
    private final LinkedHashMap<String, Track> tracks;
    private final LinkedHashSet<Track> playing;

    public TrackHandler(VChat instance)
    {
        super(instance);

        this.tracks = new LinkedHashMap<String, Track>();
        this.playing = new LinkedHashSet<Track>();

        try
        {
            File trackDir = new File("vChat/tracks");
            if(!trackDir.exists()) trackDir.mkdirs();

            this.instance.getLogger().info("Loading Tracks...");
            long startTime = System.currentTimeMillis();

            for(File file : trackDir.listFiles())
            {
                if(file.getName().endsWith(".track"))
                {
                    try
                    {
                        String contents = FileUtils.readFileToString(file);
                        Track track = Track.parseTrack(FilenameUtils.removeExtension(file.getName()), contents);

                        this.tracks.put(track.getName(), track);
                        this.instance.getLogger().info("Loaded track " + this.tracks.size() + ": \"" + track.getName() + "\"");
                    }
                    catch (Track.ParseException e)
                    {
                        this.instance.getLogger().error("Failed to parse track " + file.getName() + "!");
                        e.printStackTrace();
                    }
                    catch (Exception e)
                    {
                        this.instance.getLogger().error("Failed to load track " + file.getName() + "!");
                        e.printStackTrace();
                    }
                }
            }

            this.instance.getLogger().info("Done! A total of " + this.tracks.size() + " tracks loaded in " + (System.currentTimeMillis() - startTime) + " ms");
        }
        catch (Exception e)
        {
            this.instance.getLogger().error("Loading of the tracks failed!");
            e.printStackTrace();
        }
    }

    @Override
    public void onServerLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandTrack(this));
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        if(event.phase == Phase.END)
            for(Track track : (LinkedHashSet<Track>) this.playing.clone())
                track.play();
    }

    public void startTrack(Track track)
    {
        this.playing.add(track);
    }

    public void stopTrack(Track track)
    {
        this.playing.remove(track);
    }

    public void stopAll()
    {
        this.playing.clear();
    }

    public void stopAll(EntityPlayerMP player)
    {
        for(Track track : (LinkedHashSet<Track>) this.playing.clone())
            track.stopTrack(player);
    }

    public Track getTrack(String name)
    {
        return this.tracks.get(name);
    }

    public Track getTrack(int index)
    {
        if(index < 0 || index >= this.tracks.size())
            return null;

        return (Track) this.tracks.values().toArray()[index];
    }

    public Track getTrack(Object o)
    {
        Track track;

        if(StringUtils.isNumeric(o.toString()))
            track = this.getTrack(Integer.parseInt(o.toString()));
        else
            track = this.getTrack(o.toString());

        return track;
    }

    public LinkedHashMap<String, Track> getTracks()
    {
        return this.tracks;
    }
}
