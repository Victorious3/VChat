package moe.nightfall.vic.chat.integrations.reddit;

import moe.nightfall.vic.chat.VChat;
import moe.nightfall.vic.chat.integrations.ChatFormatter;
import moe.nightfall.vic.chat.util.Misc;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.net.URL;
import java.util.regex.Pattern;

public class RedditChatFormatter extends ChatFormatter
{
    private static final Pattern PATTERN = Pattern.compile("(http|https)?://(www\\.)?reddit\\.com\\S*");

    public RedditChatFormatter(VChat instance)
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
        TextComponentString text = new TextComponentString("[Reddit - ");

        Style style = new Style();
        style.setColor(TextFormatting.DARK_GRAY);

        try
        {
            URL url = new URL(match);

            String path = url.getPath();
            path = path.startsWith("/") ? path.substring(1) : path;

            String subtitle = "Unknown page";

            TextComponentString toolTipText = new TextComponentString(BULLET + " Reddit\n\n");
            toolTipText.getStyle().setColor(TextFormatting.DARK_GRAY);

            if (match.endsWith("reddit.com") || match.endsWith("reddit.com/"))
            {
                subtitle = "Home page";
                toolTipText.appendText(TextFormatting.GRAY + "Home page");
            }
            else
            {
                String[] pages = path.split("/");

                if (pages.length == 2 && pages[0].equalsIgnoreCase("r"))
                {
                    subtitle = "/r/" + pages[1];
                    toolTipText.appendText(TextFormatting.GRAY + "Subreddit: " + TextFormatting.WHITE + pages[1]);
                }
                else if (pages.length > 2 && pages[0].equalsIgnoreCase("r"))
                {
                    String post = Misc.getPageTitle(url).split(" : ")[0];

                    subtitle = TextFormatting.GRAY  + "\"" + post + "\"" + TextFormatting.WHITE + " on " + TextFormatting.GRAY + "/r/" + pages[1];
                    toolTipText.appendText(TextFormatting.GRAY + "Title: " + TextFormatting.WHITE + post + "\n");
                    toolTipText.appendText(TextFormatting.GRAY + "Subreddit: " + TextFormatting.WHITE + "/r/" + pages[1]);
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
            this.instance.getLogger().warn("Failed to retrieve the Reddit page's data of '" + match + "' (" + e.getMessage() + ")");

            style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));

            TextComponentString toolTipText = new TextComponentString("Click to open this Reddit page (Failed to retrieve page's data)");
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
