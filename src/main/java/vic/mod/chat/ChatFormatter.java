package vic.mod.chat;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
							ChatComponentText comp = getComponentReplacement(matched);
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
					component.getSiblings().add(i, new ChatComponentText(matcher.replaceAll(getReplacement())));
				}
			}
		}
	}
	
	@Override
	public boolean matched() 
	{
		return matched;
	}

	protected ChatComponentText getComponentReplacement(String match)
	{
		return new ChatComponentText(match);
	}
	
	protected boolean appliesCustomStyle()
	{
		return true;
	}
	
	protected String getReplacement()
	{
		return "";
	}
	
	public static class ChatFormatterURL extends ChatFormatter
	{
		@Override
		protected ChatComponentText getComponentReplacement(String match) 
		{
			ChatComponentText text = new ChatComponentText(match);
			ChatStyle style = new ChatStyle();
			style.setUnderlined(true);
			style.setColor(EnumChatFormatting.BLUE);
			style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to open URL")));
			style.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));
			text.setChatStyle(style);
			return text;
		}

		@Override
		public void apply(ChatComponentText text) 
		{
			super.apply(text, Misc.urlPattern);
		}
	}
	
	public static class ChatFormatterYoutube extends ChatFormatter
	{
		@Override
		protected ChatComponentText getComponentReplacement(String match) 
		{	
			try {
				URL url = new URL(match);
				List<NameValuePair> list = URLEncodedUtils.parse(url.toURI(), "UTF-8");
				String ytid = "";
				
				for(NameValuePair pair : list)
					if(pair.getName().equalsIgnoreCase("v")) ytid = pair.getValue();	
				
				if(ytid.length() == 0)
				{
					String path = url.getPath();
					if(path.startsWith("/v/")) ytid = url.getPath().substring(3);
					else ytid = url.getPath().substring(3);
				}
				
				if(ytid.length() > 0)
				{
					URL apiURL = new URL("http://gdata.youtube.com/feeds/api/videos/" + ytid + "?v=2");
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					InputStream in = apiURL.openStream();
					Document doc = db.parse(apiURL.openStream());
					String title = doc.getElementsByTagName("title").item(0).getTextContent();
					String author = ((Element)doc.getElementsByTagName("author").item(0)).getElementsByTagName("name").item(0).getTextContent();
					int seconds = Integer.parseInt((doc.getElementsByTagName("yt:duration").item(0).getAttributes().getNamedItem("seconds").getNodeValue()));
					int minutes = seconds / 60;
					seconds = seconds % 60;
					int viewcount = Integer.parseInt((doc.getElementsByTagName("yt:statistics").item(0).getAttributes().getNamedItem("viewCount").getNodeValue()));
					
					ChatComponentText c1 = new ChatComponentText("");
					c1.getChatStyle().setColor(EnumChatFormatting.WHITE);
					c1.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to open Video")));
					c1.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));
					
					c1.appendText("[");
					ChatComponentText c2 = new ChatComponentText("\"" + title + "\"");
					c2.getChatStyle().setColor(EnumChatFormatting.RED);
					c1.appendSibling(c2);
					c1.appendText(" by ");
					ChatComponentText c3 = new ChatComponentText(author);
					c3.getChatStyle().setColor(EnumChatFormatting.YELLOW);
					c1.appendSibling(c3);
					c1.appendText(" (");
					ChatComponentText c4 = new ChatComponentText(minutes + ":" + seconds);
					c4.getChatStyle().setColor(EnumChatFormatting.AQUA);
					c1.appendSibling(c4);
					c1.appendText(" - ");
					ChatComponentText c5 = new ChatComponentText(String.valueOf(viewcount));
					c5.getChatStyle().setColor(EnumChatFormatting.AQUA);
					c1.appendSibling(c5);
					c1.appendText(" views)]");
					
					return c1;
				}
			} catch (Exception e) {}
			
			ChatComponentText text = new ChatComponentText(match);
			ChatStyle style = new ChatStyle();
			style.setUnderlined(true);
			style.setColor(EnumChatFormatting.BLUE);
			style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to open URL")));
			style.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));
			text.setChatStyle(style);
			return text;
		}
		
		@Override
		public void apply(ChatComponentText text)
		{
			super.apply(text, Misc.ytVideoPattern);
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
		protected String getReplacement() 
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
		protected ChatComponentText getComponentReplacement(String match) 
		{
			ChatComponentText text = new ChatComponentText(replaceNick ? player.getNickname() : player.getUsername());
			ChatStyle style = new ChatStyle();
			if(isSelf) style.setColor(Config.colorHighlightSelf);
			else style.setColor(Config.colorHighlight);
			if(replaceNick) style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(player.getUsername())));
			text.setChatStyle(style);
			return text;
		}
	}
}
