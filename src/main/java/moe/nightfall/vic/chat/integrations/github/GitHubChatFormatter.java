package moe.nightfall.vic.chat.integrations.github;

import moe.nightfall.vic.chat.VChat;
import moe.nightfall.vic.chat.integrations.ChatFormatter;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.net.URL;
import java.util.regex.Pattern;

public class GitHubChatFormatter extends ChatFormatter
{
    private static final Pattern PATTERN = Pattern.compile("(http|https)?://(www\\.)?github\\.com\\S*");

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
        style.setColor(EnumChatFormatting.DARK_GRAY);

        try
        {
            URL url = new URL(match);

            String path = url.getPath();
            path = path.startsWith("/") ? path.substring(1) : path;

            String subtitle = "Unknown page";

            ChatComponentText toolTipText = new ChatComponentText(BULLET + " GitHub\n\n");
            toolTipText.getChatStyle().setColor(EnumChatFormatting.DARK_GRAY);

            if (match.endsWith("github.com") || match.endsWith("github.com/"))
            {
                subtitle = "Home page";
                toolTipText.appendText(EnumChatFormatting.GRAY + "Home page");
            }
            else
            {
                String[] pages = path.split("/");

                if (pages.length == 1)
                {
                    subtitle = "Profile of " + EnumChatFormatting.GRAY + pages[0];
                    toolTipText.appendText(EnumChatFormatting.GRAY + "Profile of: " + EnumChatFormatting.WHITE + pages[0]);
                }
                else if (pages.length > 1)
                {
                    subtitle = "Project " + EnumChatFormatting.GRAY  + "\"" + pages[1] + "\"" + EnumChatFormatting.WHITE + " of " + EnumChatFormatting.GRAY + pages[0];
                    toolTipText.appendText(EnumChatFormatting.GRAY + "Project: " + EnumChatFormatting.WHITE + pages[1] + "\n");
                    toolTipText.appendText(EnumChatFormatting.GRAY + "Creator: " + EnumChatFormatting.WHITE + pages[0]);
                }
            }

            style.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));
            style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, toolTipText));

            ChatComponentText title = new ChatComponentText(subtitle);
            title.getChatStyle().setColor(EnumChatFormatting.WHITE);

            text.appendSibling(title);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            this.instance.getLogger().warn("Failed to retrieve the GitHub page's data of '" + match + "' (" + e.getMessage() + ")");

            style.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));

            ChatComponentText toolTipText = new ChatComponentText("Click to open this GitHub page (Failed to retrieve page's data)");
            toolTipText.getChatStyle().setColor(EnumChatFormatting.GRAY);

            style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, toolTipText));

            ChatComponentText link = new ChatComponentText(match);
            link.getChatStyle().setColor(EnumChatFormatting.GRAY);

            text.appendSibling(link);
        }

        text.appendText("]");
        text.setChatStyle(style);

        return text;
    }
}
