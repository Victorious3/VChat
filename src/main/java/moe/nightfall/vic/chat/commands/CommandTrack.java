package moe.nightfall.vic.chat.commands;

import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.util.Misc;
import moe.nightfall.vic.chat.handlers.TrackHandler;
import moe.nightfall.vic.chat.tracks.Track;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class CommandTrack extends CommandOverrideAccess
{
    private final TrackHandler trackHandler;

    public CommandTrack(TrackHandler trackHandler)
    {
        this.trackHandler = trackHandler;
    }

    @Override
    public String getName()
    {
        return "track";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/track <broadcast/stopbr/play/stop> [name] [track]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        args = Misc.parseArgs(args);

        if(args.length == 0 || StringUtils.isNumeric(args[0]))
        {
            HashMap<String, Track> tracks = this.trackHandler.getTracks();

            if(tracks.isEmpty())
            {
                sender.sendMessage(new TextComponentString("There are no tracks loaded on the server."));
                return;
            }

            int page = (args.length > 0 && StringUtils.isNumeric(args[0])) ? Integer.parseInt(args[0]) - 1 : 0;
            page = page < 0 ? 0 : page;

            int numPages = tracks.size() / 6 + 1;

            if(page >= numPages)
                throw new CommandException("Exceeded the number of pages, " + numPages + ".");

            List<Track> trackList = new ArrayList(tracks.values()).subList(page * 6, MathHelper.clamp(((page + 1) * 6) - 1, 0, tracks.size()));
            sender.sendMessage(new TextComponentTranslation("Currently loaded tracks (Page %s of %s):", page + 1, numPages));

            for(int i = 0; i < trackList.size(); i++)
            {
                int num = page * 6 + i + 1;

                TextComponentTranslation text = new TextComponentTranslation("%s: \"%s\"", num, trackList.get(i).getName());
                text.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to play!")));
                text.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/track play " + num));
                sender.sendMessage(text);
            }
        }
        else if(args[0].equalsIgnoreCase("broadcast") || args[0].equalsIgnoreCase("play"))
        {
            boolean broadcast = args[0].equalsIgnoreCase("broadcast");

            if(broadcast && !Misc.checkPermission(sender, Config.trackPermissionLevel))
                return;
            else if(!broadcast && !Misc.checkPermission(sender, Config.trackPermissionLevelSelf))
                return;

            if(args.length > 1)
            {
                Track track;

                if(args.length == 2)
                {
                    track = this.trackHandler.getTrack((Object)args[1]);

                    if(track == null)
                        throw new CommandException("No track with the specified name/index " + args[1] + " found!");
                }
                else
                {
                    try
                    {
                        track = Track.parseTrack(args[1], StringUtils.join(Arrays.asList(args).subList(2, args.length), " "));
                    }
                    catch (Track.ParseException e)
                    {
                        throw new CommandException(e.getMessage());
                    }
                }

                sender.sendMessage(new TextComponentString("Broadcasting track \"" + args[1] + "\""));

                if(broadcast)
                {
                    track.start();
                }
                else
                {
                    if(sender instanceof EntityPlayerMP)
                        track.start(new ArrayList(Collections.singletonList(sender)));

                    else throw new CommandException("You *might* just not want to do that. Sorry, the console has no sound output.");
                }
            }
            else
            {
                throw new WrongUsageException(getUsage(sender));
            }
        }
        else if(args[0].equalsIgnoreCase("stopbr"))
        {
            if(!Misc.checkPermission(sender, Config.trackPermissionLevel))
                return;

            if(args.length == 1)
            {
                this.trackHandler.stopAll();
                sender.sendMessage(new TextComponentString("Stopped all tracks."));
            }
            else
            {
                Track track = this.trackHandler.getTrack((Object)args[1]);

                if(track == null)
                    throw new CommandException("No track with the specified name/index " + args[1] + " found!");

                this.trackHandler.stopTrack(track);
                sender.sendMessage(new TextComponentString("Stopped track \"" + args[1] + "\"."));
            }
        }
        else if(args[0].equalsIgnoreCase("stop"))
        {
            if(!(sender instanceof EntityPlayerMP))
                return;

            EntityPlayerMP player = (EntityPlayerMP)sender;

            if(args.length == 1)
            {
                this.trackHandler.stopAll(player);
                sender.sendMessage(new TextComponentString("Stopped all tracks."));
            }
            else
            {
                Track track = this.trackHandler.getTrack((Object)args[1]);

                if(track == null)
                    throw new CommandException("No track with the specified name/index " + args[1] + " found!");

                track.stopTrack(player);
                sender.sendMessage(new TextComponentString("Stopped track \"" + args[1] + "\"."));
            }
        }
        else
        {
            throw new WrongUsageException(getUsage(sender));
        }
    }
}
