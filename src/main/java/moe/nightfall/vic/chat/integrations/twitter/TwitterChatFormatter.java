package moe.nightfall.vic.chat.integrations.twitter;

import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.VChat;
import moe.nightfall.vic.chat.integrations.ChatFormatter;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class TwitterChatFormatter extends ChatFormatter
{
    private static final Pattern PATTERN = Pattern.compile("(http|https)?://(www\\.)?twitter\\.com\\S*");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss");

    private final Twitter twitter;

    public TwitterChatFormatter(VChat instance)
    {
        super(instance);

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setOAuthConsumerKey(Config.twitterConsumerKey);
        configurationBuilder.setOAuthConsumerSecret(Config.twitterConsumerSecret);
        configurationBuilder.setOAuthAccessToken(Config.twitterAccessKey);
        configurationBuilder.setOAuthAccessTokenSecret(Config.twitterAccessSecret);

        this.twitter = new TwitterFactory(configurationBuilder.build()).getInstance();
    }

    @Override
    public void apply(TextComponentString text)
    {
        super.apply(text, PATTERN);
    }

    @Override
    protected TextComponentString getComponentReplacement(String match)
    {
        TextComponentString text = new TextComponentString("[Twitter - ");

        Style style = new Style();
        style.setColor(TextFormatting.AQUA);

        try
        {
            URL url = new URL(match);

            String path = url.getPath();
            path = path.startsWith("/") ? path.substring(1) : path;

            String subtitle = "Unknown page";

            TextComponentString toolTipText = new TextComponentString(BULLET + " Twitter\n\n");
            toolTipText.getStyle().setColor(TextFormatting.AQUA);

            if (match.endsWith("twitter.com") || match.endsWith("twitter.com/"))
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
                else if (pages.length == 3 && pages[1].equals("status"))
                {
                    long tweetId = Long.parseLong(pages[2]);
                    Status tweet = this.twitter.tweets().showStatus(tweetId);

                    subtitle = TextFormatting.DARK_AQUA + "\"" + tweet.getText() + "\"" + TextFormatting.AQUA + " by " + TextFormatting.DARK_AQUA + pages[0];
                    toolTipText.appendText(TextFormatting.DARK_AQUA + "Tweet from: " + TextFormatting.WHITE + pages[0] + " (" + TextFormatting.DARK_AQUA + DATE_FORMAT.format(tweet.getCreatedAt()) + TextFormatting.WHITE + ")\n");
                    toolTipText.appendText(TextFormatting.DARK_AQUA + "Retweets: " + TextFormatting.WHITE + tweet.getRetweetCount() + "\n");
                    toolTipText.appendText(TextFormatting.DARK_AQUA + "Favorites: " + TextFormatting.WHITE + tweet.getFavoriteCount());
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
            this.instance.getLogger().warn("Failed to retrieve the Tweeter tweet's data of '" + match + "' (" + e.getMessage() + ")");

            style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));

            TextComponentString toolTipText = new TextComponentString("Click to open this Twitter page (Failed to retrieve tweet's data)");
            toolTipText.getStyle().setColor(TextFormatting.AQUA);

            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, toolTipText));

            TextComponentString link = new TextComponentString(match);
            link.getStyle().setColor(TextFormatting.DARK_AQUA);

            text.appendSibling(link);
        }

        text.appendText("]");
        text.setStyle(style);

        return text;
    }
}
