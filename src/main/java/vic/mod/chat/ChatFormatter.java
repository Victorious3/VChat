package vic.mod.chat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

public abstract class ChatFormatter implements IChatFormatter
{
	private boolean matched = false;
	
	private void apply(ChatComponentText component, Pattern pattern)
	{
		for(int i = 0; i < component.getSiblings().size(); i++)
		{
			Object obj = component.getSiblings().get(i);
			if(obj instanceof ChatComponentText && ((ChatComponentText)obj).getChatStyle().isEmpty())
			{
				ChatComponentText baseComponent = (ChatComponentText)obj;
				String text = baseComponent.getUnformattedTextForChat();
				Matcher matcher = pattern.matcher(text);
				matched = matcher.find();
				matcher.reset();
				
				if(appliesCustomStyle())
				{
					component.getSiblings().remove(i);
					int index = i;
					int start = 0;
					int end = text.length();
					while(start < text.length())
					{
						boolean find = matcher.find();
						if(find) end = matcher.start();
						if(start != end) 
						{
							component.getSiblings().add(index, new ChatComponentText(text.substring(start, end)));
							index++;
						}
						if(find)
						{
							String matched = text.substring(matcher.start(), matcher.end());
							ChatComponentText comp = new ChatComponentText(getReplacement(matched));
							comp.setChatStyle(getStyle(matched));
							component.getSiblings().add(index, comp);
							start = matcher.end();
							end = text.length();
							index++;
						}
						else start = end + 1;
					}
				}
				else
				{
					component.getSiblings().remove(i);
					component.getSiblings().add(i, new ChatComponentText(matcher.replaceAll(getReplacement(null))));
				}
			}
		}
	}
	
	@Override
	public boolean matched() 
	{
		return matched;
	}

	protected ChatStyle getStyle(String match)
	{
		return new ChatStyle();
	}
	
	protected boolean appliesCustomStyle()
	{
		return true;
	}
	
	protected String getReplacement(String match)
	{
		return match;
	}
	
	public static class ChatFormatterURL extends ChatFormatter
	{
		@Override
		protected ChatStyle getStyle(String match) 
		{
			ChatStyle style = super.getStyle(match);
			style.setUnderlined(true);
			style.setColor(EnumChatFormatting.BLUE);
			style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to open URL")));
			style.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));
			return style;
		}

		@Override
		public void apply(ChatComponentText text) 
		{
			super.apply(text, Misc.urlPattern);
		}
	}
	
	public static class ChatFormatterColor extends ChatFormatter
	{
		private static Pattern pattern = Pattern.compile("[\\&]");
		
		@Override
		public void apply(ChatComponentText text) 
		{
			super.apply(text, pattern);
		}

		@Override
		protected boolean appliesCustomStyle() 
		{
			return false;
		}

		@Override
		protected String getReplacement(String match) 
		{
			return "\u00A7";
		}
	}
	
	public static class ChatFormatterUsername extends ChatFormatter
	{
		private Pattern pattern;
		private boolean canMatch = true;
		private boolean isSelf = false;
		private ChatEntity player;
		private boolean replaceNick;
		
		public ChatFormatterUsername(ChatEntity player, ChatEntity receiver, boolean replaceNick)
		{
			String nickname = player.getNickname();
			if(replaceNick && nickname == null) canMatch = false;
			else pattern = Pattern.compile("(?<![0-9A-z_])(?i)" + (replaceNick ? nickname : player.getUsername()) + "(?![0-9A-z_])");
			if(player.equals(receiver)) isSelf = true;
			this.player = player;
			this.replaceNick = replaceNick;
		}

		@Override
		public void apply(ChatComponentText text) 
		{
			if(canMatch) super.apply(text, pattern);
		}

		@Override
		protected String getReplacement(String match)
		{
			return replaceNick ? player.getNickname() : player.getUsername();
		}

		@Override
		protected ChatStyle getStyle(String match) 
		{
			ChatStyle style = super.getStyle(match);
			if(isSelf) style.setColor(Config.colorHighlightSelf);
			else style.setColor(Config.colorHighlight);
			if(replaceNick) style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(player.getUsername())));
			return style;
		}
	}
}
