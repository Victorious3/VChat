package moe.nightfall.vic.chat.integrations.github;

import moe.nightfall.vic.chat.Misc;
import moe.nightfall.vic.chat.VChat;
import moe.nightfall.vic.chat.integrations.ChatFormatter;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubChatFormatter extends ChatFormatter
{
    private static final Pattern PATTERN = Pattern.compile("(http:|https)?://github\\.com/\\S*");
    private static final Pattern PROFILE_PATTERN = Pattern.compile("(http:|https)?://github\\.com/\\S*/");
    private static final Pattern PROJECT_PATTERN = Pattern.compile("(http:|https)?://github\\.com/\\S*/\\S*/");

    public GitHubChatFormatter(VChat instance)
    {
        super(instance);
    }

    @Override
    public void apply(ChatComponentText text)
    {
        super.apply(text, PATTERN);
    }

    @Override
    protected ChatComponentText getComponentReplacement(String match)
    {
        ChatComponentText text = new ChatComponentText("[GitHub - ");

        ChatStyle style = new ChatStyle();
        style.setColor(EnumChatFormatting.GRAY);

        try
        {
            URL url = new URL(match);
            String pageTitle = Misc.getPageTitle(url);

            Matcher profileMatcher = PROFILE_PATTERN.matcher(match);
            Matcher projectMatcher = PROJECT_PATTERN.matcher(match);

            ChatComponentText toolTipText = new ChatComponentText(BULLET + " GitHub\n\n");
            toolTipText.getChatStyle().setColor(EnumChatFormatting.GRAY);

            if (profileMatcher.matches())
            {
                toolTipText.appendText(EnumChatFormatting.YELLOW + "Profile: " + EnumChatFormatting.WHITE + track.getTitle() + "\n");
            }

            toolTipText.appendText(EnumChatFormatting.YELLOW + "Title: " + EnumChatFormatting.WHITE + track.getTitle() + " (" + EnumChatFormatting.GOLD + track.getPlaybackCount() + EnumChatFormatting.WHITE + " plays)\n");
            toolTipText.appendText(EnumChatFormatting.YELLOW + "Publisher: " + EnumChatFormatting.WHITE + track.getUser().getUsername() + "\n");
            toolTipText.appendText(EnumChatFormatting.YELLOW + "Duration: " + EnumChatFormatting.GOLD + minutes + EnumChatFormatting.WHITE + " minutes and " + EnumChatFormatting.GOLD + seconds + EnumChatFormatting.WHITE + " seconds");

            style.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));
            style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, toolTipText));

            ChatComponentText title = new ChatComponentText("\"" + Misc.getPageTitle(url) + "\"");
            title.getChatStyle().setColor(EnumChatFormatting.WHITE);

            text.appendSibling(publisher);
        }
        catch(Exception e)
        {
            this.instance.getLogger().warn("Failed to retrieve the SoundCloud track's data of '" + match + "' (" + e.getMessage() + ")");

            style.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));

            ChatComponentText toolTipText = new ChatComponentText("Click to open this SoundCloud track (Failed to retrieve track's data)");
            toolTipText.getChatStyle().setColor(EnumChatFormatting.GOLD);

            style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, toolTipText));

            ChatComponentText link = new ChatComponentText(match);
            link.getChatStyle().setColor(EnumChatFormatting.YELLOW);

            text.appendSibling(link);
        }

        text.appendText("]");
        text.setChatStyle(style);

        return text;
    }
}
