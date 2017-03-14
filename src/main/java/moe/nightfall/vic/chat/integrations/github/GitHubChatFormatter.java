package moe.nightfall.vic.chat.integrations.github;

import moe.nightfall.vic.chat.VChat;
import moe.nightfall.vic.chat.integrations.ChatFormatter;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

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
    public void apply(TextComponentString text)
    {
        super.apply(text, PATTERN);
    }

    @Override
    protected TextComponentString getComponentReplacement(String match)
    {
        TextComponentString text = new TextComponentString("[GitHub - ");

        Style style = new Style();
        style.setColor(TextFormatting.DARK_GRAY);

        try
        {
            URL url = new URL(match);

            String path = url.getPath();
            path = path.startsWith("/") ? path.substring(1) : path;

            String subtitle = "Unknown page";

            TextComponentString toolTipText = new TextComponentString(BULLET + " GitHub\n\n");
            toolTipText.getStyle().setColor(TextFormatting.DARK_GRAY);

            if (match.endsWith("github.com") || match.endsWith("github.com/"))
            {
                subtitle = "Home page";
                toolTipText.appendText(TextFormatting.GRAY + "Home page");
            }
            else
            {
                String[] pages = path.split("/");

                if (pages.length == 1)
                {
                    subtitle = "Profile of " + TextFormatting.GRAY + pages[0];
                    toolTipText.appendText(TextFormatting.GRAY + "Profile of: " + TextFormatting.WHITE + pages[0]);
                }
                else if (pages.length > 1)
                {
                    subtitle = "Project " + TextFormatting.GRAY  + "\"" + pages[1] + "\"" + TextFormatting.WHITE + " of " + TextFormatting.GRAY + pages[0];
                    toolTipText.appendText(TextFormatting.GRAY + "Project: " + TextFormatting.WHITE + pages[1] + "\n");
                    toolTipText.appendText(TextFormatting.GRAY + "Creator: " + TextFormatting.WHITE + pages[0]);
                }
            }

            style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, toolTipText));

            TextComponentString title = new TextComponentString(subtitle);
            title.getStyle().setColor(TextFormatting.WHITE);

            text.appendSibling(title);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            this.instance.getLogger().warn("Failed to retrieve the GitHub page's data of '" + match + "' (" + e.getMessage() + ")");

            style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));

            TextComponentString toolTipText = new TextComponentString("Click to open this GitHub page (Failed to retrieve page's data)");
            toolTipText.getStyle().setColor(TextFormatting.GRAY);

            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, toolTipText));

            TextComponentString link = new TextComponentString(match);
            link.getStyle().setColor(TextFormatting.GRAY);

            text.appendSibling(link);
        }

        text.appendText("]");
        text.setStyle(style);

        return text;
    }
}
