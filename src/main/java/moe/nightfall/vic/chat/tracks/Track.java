package moe.nightfall.vic.chat.tracks;

import java.util.ArrayList;
import java.util.List;

import moe.nightfall.vic.chat.Misc;
import moe.nightfall.vic.chat.VChat;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

import com.google.common.collect.Lists;

public class Track 
{
    private String name;
    private TrackElement[] elements;
    private ArrayList<EntityPlayerMP> players;
    private int pointer;
    private int timeout;

    private Track() {}

    public static Track parseTrack(String name, String track)
    {
        if(track == null)
            throw new NullPointerException();

        if(!track.matches("[A-Z0-9\\[\\]:#?\\s]+"))
            throw new ParseException("Track contains illegal characters.");

        int level = 0;
        int index = 0;
        int curType = 0;

        for(char ch : track.toCharArray())
        {
            if(ch == '[') level++;
            else if(ch == ']') level--;
        }

        if(level != 0)
            throw new ParseException("Track contains non enclosing brackets.");

        List<TrackElement> list = Lists.newArrayList();
        List<Integer> jumper = Lists.newArrayList();
        Track ret = new Track();

        while(index < track.length())
        {
            char ch = track.charAt(index);

            if(Character.isUpperCase(ch))
            {
                float pitch = (ch - 65) / 24F + 1F;
                int type = curType;
                int duration = 0;

                if(index + 1 < track.length())
                {
                    char ch1 = track.charAt(index + 1);

                    if(Character.isDigit(ch1))
                    {
                        type = ch1 - 48;
                        index++;

                        if(index + 1 < track.length())
                        {
                            char ch2 = track.charAt(index + 1);

                            if(Character.isDigit(ch2))
                            {
                                duration = ch2 - 48;
                                index++;
                            }
                        }
                    }
                }

                if(type > 6)
                    throw new ParseException("Instrument modifier out of bounds (0-6) at index " + index + ".");

                String sound;

                switch (type)
                {
                    case 0: sound = "note.bass"; break;
                    case 1: sound = "note.bassattack"; break;
                    case 2: sound = "note.bd"; break;
                    case 3: sound = "note.harp"; break;
                    case 4: sound = "note.hat"; break;
                    case 5: sound = "note.pling"; break;
                    default: sound = "note.snare"; break;
                }

                list.add(new TrackElement.ElementNote(ret, duration, pitch, sound));
            }
            else if(ch == '#')
            {
                int duration = 1;

                if(index + 1 < track.length())
                {
                    char ch1 = track.charAt(index + 1);

                    if(Character.isDigit(ch1))
                    {
                        duration = ch1 - 48;
                        index++;
                    }
                }

                list.add(new TrackElement.ElementPause(ret, duration));
            }
            else if(ch == '[') jumper.add(list.size());
            else if(ch == ':')
            {
                int times;

                if(index + 1 < track.length())
                {
                    char ch1 = track.charAt(index + 1);

                    if(Character.isDigit(ch1))
                    {
                        times = ch1 - 48;
                        index++;
                    }

                    else throw new ParseException("Non numeric repetition count at index " + index + ".");
                }
                else throw new ParseException("Found single ':' at index " + index + ".");

                if(index + 1 < track.length())
                {
                    char ch1 = track.charAt(index + 1);

                    if(ch1 != ']')
                        throw new ParseException("No enclosing ']' found to ':' at index " + index + ".");

                    index++;
                }

                list.add(new TrackElement.ElementRepeat(ret, times, jumper.remove(jumper.size() - 1)));
            }
            else if(ch == ']')
            {
                list.add(new TrackElement.ElementRepeat(ret, 1, jumper.remove(jumper.size() - 1)));
            }
            else if(ch == '?')
            {
                curType = 0;

                if(index + 1 < track.length())
                {
                    char ch1 = track.charAt(index + 1);

                    if(Character.isDigit(ch1))
                    {
                        curType = ch1 - 48;
                        index++;
                    }
                }
                if(curType > 6) throw new ParseException("Instrument Type at index " + index + " is out of bounds (0-6).");
            }
            else if(ch != ' ') throw new ParseException("Misplaced character at index " + index + ": " + ch);

            index++;
        }

        ret.elements = list.toArray(new TrackElement[list.size()]);
        ret.name = name;

        return ret;
    }

    public void start(ArrayList players)
    {
        this.pointer = 0;

        if(players.isEmpty() || this.elements.length == 0)
            return;

        this.players = players;

        for(EntityPlayerMP player : this.players)
            player.addChatComponentMessage(new ChatComponentText("\u25B6 Now playing: \"" + name + "\""));

        this.timeout = 0;

        for(TrackElement element : this.elements)
            element.reset();

        VChat.instance.getTrackHandler().startTrack(this);
    }

    public void start()
    {
        this.start(Misc.getOnlinePlayers());
    }

    public void stopTrack(EntityPlayerMP player)
    {
        if(player != null)
            this.players.remove(player);

        if(this.players.isEmpty())
            this.stopTrack();
    }

    public void stopTrack()
    {
        VChat.instance.getTrackHandler().stopTrack(this);
    }

    public void play()
    {
        if(this.timeout > 0)
        {
            this.timeout--;
        }
        else
        {
            if(this.pointer >= this.elements.length)
            {
                this.stopTrack();
                return;
            }

            TrackElement element = this.elements[this.pointer];

            if(!(element instanceof TrackElement.ElementRepeat))
                this.timeout = 1;
            else
                this.timeout = 0;

            this.pointer++;
            element.execute();
            this.timeout *= 5;
        }
    }

    public void wait(int duration)
    {
        this.timeout = duration;
    }

    public void jumpToElement(int pointer)
    {
        this.pointer = pointer;
    }

    public void decreasePointer()
    {
        this.pointer--;
    }

    public String getName()
    {
        return this.name;
    }

    public ArrayList<EntityPlayerMP> getPlayers()
    {
        return this.players;
    }

    public static class ParseException extends RuntimeException
    {
        public ParseException(String cause)
        {
            super(cause);
        }
    }
}
