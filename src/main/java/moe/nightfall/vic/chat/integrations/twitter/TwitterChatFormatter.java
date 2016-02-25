package moe.nightfall.vic.chat.integrations.twitter;

import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.VChat;
import moe.nightfall.vic.chat.integrations.ChatFormatter;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
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
    public void apply(ChatComponentText text)
    {
        super.apply(text, PATTERN);
    }

    @Override
    protected ChatComponentText getComponentReplacement(String match)
    {
        ChatComponentText text = new ChatComponentText("[Twitter - ");

        ChatStyle style = new ChatStyle();
        style.setColor(EnumChatFormatting.AQUA);

        try
        {
            URL url = new URL(match);

            String path = url.getPath();
            path = path.startsWith("/") ? path.substring(1) : path;

            String subtitle = "Unknown page";

            ChatComponentText toolTipText = new ChatComponentText(BULLET + " Twitter\n\n");
            toolTipText.getChatStyle().setColor(EnumChatFormatting.AQUA);

            if (match.endsWith("twitter.com") || match.endsWith("twitter.com/"))
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
                else if (pages.length == 3 && pages[1].equals("status"))
                {
                    long tweetId = Long.parseLong(pages[2]);
                    Status tweet = this.twitter.tweets().showStatus(tweetId);

                    subtitle = EnumChatFormatting.DARK_AQUA + "\"" + tweet.getText() + "\"" + EnumChatFormatting.AQUA + " by " + EnumChatFormatting.DARK_AQUA + pages[0];
                    toolTipText.appendText(EnumChatFormatting.DARK_AQUA + "Tweet from: " + EnumChatFormatting.WHITE + pages[0] + " (" + EnumChatFormatting.DARK_AQUA + DATE_FORMAT.format(tweet.getCreatedAt()) + EnumChatFormatting.WHITE + ")\n");
                    toolTipText.appendText(EnumChatFormatting.DARK_AQUA + "Retweets: " + EnumChatFormatting.WHITE + tweet.getRetweetCount() + "\n");
                    toolTipText.appendText(EnumChatFormatting.DARK_AQUA + "Favorites: " + EnumChatFormatting.WHITE + tweet.getFavoriteCount());
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
            this.instance.getLogger().warn("Failed to retrieve the Tweeter tweet's data of '" + match + "' (" + e.getMessage() + ")");

            style.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));

            ChatComponentText toolTipText = new ChatComponentText("Click to open this Twitter page (Failed to retrieve tweet's data)");
            toolTipText.getChatStyle().setColor(EnumChatFormatting.AQUA);

            style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, toolTipText));

            ChatComponentText link = new ChatComponentText(match);
            link.getChatStyle().setColor(EnumChatFormatting.DARK_AQUA);

            text.appendSibling(link);
        }

        text.appendText("]");
        text.setChatStyle(style);

        return text;
    }

    private static int getDigits(String string, int charPosition)
    {
        String digits = "";

        for (int i = charPosition - 1; i >= 0; i--)
        {
            if (!Character.isDigit(string.charAt(i)))
                break;
            else
                digits = string.charAt(i) + digits;
        }

        return Integer.parseInt(digits);
    }
}
