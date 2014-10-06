package vic.mod.chat;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.util.ChatComponentText;
import vic.mod.chat.Track.Element.ElementRepeat;

import com.google.common.collect.Lists;

public class Track 
{
	private Element[] elements;
	private int pointer;
	private int timeout;
	private List<EntityPlayerMP> players;
	public String name;
	
	private Track() {}
	
	public static Track parseTrack(String name, String track)
	{
		if(track == null) throw new NullPointerException();
		if(!track.matches("[A-Z0-9\\[\\]:#?\\s]+")) throw new ParseException("Track contains illegal characters.");
		
		int level = 0;
		int index = 0;
		int curType = 0;
		
		for(char ch : track.toCharArray())
		{
			if(ch == '[') level++;
			else if(ch == ']') level--;
		}
		if(level != 0) throw new ParseException("Track contains non enclosing brackets.");
		
		List<Element> list = Lists.newArrayList();
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
				if(type > 6) throw new ParseException("Instrument modifier out of bounds (0-6) at index " + index + ".");
				
				String sound;		
				switch (type) {
				case 0 : sound = "note.bass"; break;
				case 1 : sound = "note.bassattack"; break;
				case 2 : sound = "note.bd"; break;
				case 3 : sound = "note.harp"; break;
				case 4 : sound = "note.bat"; break;
				case 5 : sound = "note.pling"; break;
				default : sound = "note.snare"; break;
				}
				list.add(new Element.ElementNote(ret, duration, pitch, sound));
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
				list.add(new Element.ElementPause(ret, duration));
			}
			else if(ch == '[') jumper.add(list.size());
			else if(ch == ':')
			{
				int times = 1;
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
					if(ch1 != ']') throw new ParseException("No enclosing ']' found to ':' at index " + index + ".");
					index++;
				}
				list.add(new Element.ElementRepeat(ret, times, jumper.remove(jumper.size() - 1)));
			}
			else if(ch == ']')
			{
				list.add(new Element.ElementRepeat(ret, 1, jumper.remove(jumper.size() - 1)));
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
		ret.elements = list.toArray(new Element[list.size()]);
		ret.name = name;
		return ret;
	}
	
	protected void wait(int duration) 
	{
		timeout = duration;
	}
	
	protected void jumpToElement(int pointer)
	{
		this.pointer = pointer;
	}
	
	public void start(List players)
	{
		if(players.isEmpty() || elements.length == 0) return;
		this.players = players;
		for(EntityPlayerMP player : this.players)
			player.addChatComponentMessage(new ChatComponentText("\u25B6 Now playing: \"" + name + "\""));
		timeout = 0;
		for(Element element : elements) element.reset();
		VChat.trackHandler.startTrack(this);
	}
	
	public void stopTrack()
	{
		VChat.trackHandler.stopTrack(this);
	}
	
	public void play()
	{
		if(timeout > 0) timeout--;
		else
		{
			Element element = elements[pointer];
			if(!(element instanceof ElementRepeat)) timeout = 1;
			else timeout = 0;
			pointer++;
			element.execute();
			if(pointer >= elements.length) stopTrack();
			timeout *= 5;
		}	
	}
	
	public static class ParseException extends RuntimeException
	{
		public ParseException(String cause)
		{
			super(cause);
		}
	}
	
	public static abstract class Element
	{	
		public Track parent;
		
		private Element(Track parent)
		{
			this.parent = parent;
		}
		
		public abstract void execute();
		
		public void reset() {};
		
		public static class ElementPause extends Element
		{
			private int duration;
			
			private ElementPause(Track parent, int duration)
			{
				super(parent);
				this.duration = duration;
			}
			
			@Override
			public void execute() 
			{
				parent.wait(duration);
			}
		}
		
		public static class ElementNote extends Element
		{
			private int times, curTimes;
			private float pitch;
			private String sound;
			
			private ElementNote(Track parent, int duration, float pitch, String sound)
			{
				super(parent);
				this.times = duration;
				this.curTimes = duration;
				this.pitch = pitch;
				this.sound = sound;
			}
			
			@Override
			public void execute() 
			{
				if(curTimes > 0)
				{
					curTimes--;
					parent.pointer--;
				}
				else curTimes = times;
				for(EntityPlayerMP player : parent.players) 
					player.playerNetServerHandler.sendPacket(new S29PacketSoundEffect(sound, player.posX, player.posY, player.posZ, 1F, pitch));
			}
		}
		
		public static class ElementRepeat extends Element
		{
			private int times, curTimes, pointer;
			
			private ElementRepeat(Track parent, int duration, int pointer) 
			{
				super(parent);
				this.times = duration;
				this.curTimes = duration;
				this.pointer = pointer;
			}

			@Override
			public void execute() 
			{
				if(curTimes > 0)
				{
					curTimes--;
					parent.jumpToElement(pointer);
				}
				else curTimes = times;
			}

			@Override
			public void reset() 
			{
				curTimes = times;
			}
		}
	}
}
