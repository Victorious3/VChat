package moe.nightfall.vic.chat.integrations.youtube;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moe.nightfall.vic.chat.util.Misc;
import moe.nightfall.vic.chat.VChat;
import moe.nightfall.vic.chat.integrations.ChatFormatter;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Pattern;

public class YoutubeChatFormatter extends ChatFormatter
{
    private static final Pattern PATTERN = Pattern.compile("(http|https)?://(www\\.)?(youtube.com|youtu.be)/(watch)?(\\?v=)?(\\S+)?");
    private static final String API_KEY = "AIzaSyBvlMpa7lLAuuqDY-FhNQhIauu0__-qTg0";

    public YoutubeChatFormatter(VChat instance)
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
        TextComponentString text = new TextComponentString("[Youtube - ");

        Style style = new Style();
        style.setColor(TextFormatting.RED);

        try
        {
            String youtubeID = null;

            URL youtubeURL = new URL(match);
            HashMap<String, String> query = Misc.getQueryMap(youtubeURL);

            if (query.containsKey("v"))
                youtubeID = query.get("v");

            if (youtubeID == null)
            {
                String path = youtubeURL.getPath();

                if(path.startsWith("/v/"))
                    youtubeID = youtubeURL.getPath().substring(3);
                else
                    youtubeID = youtubeURL.getPath().substring(1);
            }

            URL apiURL = new URL("https://www.googleapis.com/youtube/v3/videos?id=" + youtubeID + "&key=" + API_KEY + "&part=snippet,contentDetails");
            JsonObject rootJson = new JsonParser().parse(new BufferedReader(new InputStreamReader(apiURL.openStream(), "UTF-8"))).getAsJsonObject();
            YoutubeVideo youtubeVideo = null;

            if (!rootJson.has("error"))
            {
                JsonObject item = rootJson.get("items").getAsJsonArray().get(0).getAsJsonObject();
                JsonObject snippet = item.get("snippet").getAsJsonObject();
                JsonObject contentDetails = item.get("contentDetails").getAsJsonObject();

                String title = snippet.get("title").getAsString();
                String channelTitle = snippet.get("channelTitle").getAsString();
                String duration = contentDetails.get("duration").getAsString();

                youtubeVideo = new YoutubeVideo(title, channelTitle, duration);
            }

            if (youtubeVideo != null)
            {
                String duration = youtubeVideo.getDuration().replace("PT", "");
                int hours = 0, minutes = 0, seconds = 0;

                for (int i = 0; i < duration.length(); i++)
                {
                    if (Character.isLetter(duration.charAt(i)))
                    {
                        char unit = duration.charAt(i);

                        if (unit == 'H')
                            hours = getDigits(duration, i);
                        else if (unit == 'M')
                            minutes = getDigits(duration, i);
                        else if (unit == 'S')
                            seconds = getDigits(duration, i);
                    }
                }

                TextComponentString toolTipText = new TextComponentString(BULLET + " YouTube\n\n");
                toolTipText.getStyle().setColor(TextFormatting.RED);
                toolTipText.appendText(TextFormatting.RED + "Title: " + TextFormatting.WHITE + youtubeVideo.getTitle() + "\n");
                toolTipText.appendText(TextFormatting.RED + "Publisher: " + TextFormatting.WHITE + youtubeVideo.getChannelTitle() + "\n");
                toolTipText.appendText(TextFormatting.RED + "Duration: " + (hours > 0 ? TextFormatting.RED + "" + hours + TextFormatting.WHITE + " hours, " : "") + TextFormatting.RED + minutes + TextFormatting.WHITE + " minutes and " + TextFormatting.RED + seconds + TextFormatting.WHITE + " seconds");

                style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));
                style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, toolTipText));

                TextComponentString title = new TextComponentString("\"" + youtubeVideo.getTitle() + "\"");
                title.getStyle().setColor(TextFormatting.WHITE);

                text.appendSibling(title);
                text.appendText(" by ");

                TextComponentString publisher = new TextComponentString(youtubeVideo.getChannelTitle());
                publisher.getStyle().setColor(TextFormatting.WHITE);

                text.appendSibling(publisher);
            }
            else
            {
                throw new IllegalStateException();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            this.instance.getLogger().warn("Failed to retrieve the YouTube video's data of '" + match + "' (" + e.getMessage() + ")");

            style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));

            TextComponentString toolTipText = new TextComponentString("Click to open this YouTube video (Failed to retrieve video's data)");
            toolTipText.getStyle().setColor(TextFormatting.RED);

            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, toolTipText));

            TextComponentString link = new TextComponentString(match);
            link.getStyle().setColor(TextFormatting.WHITE);

            text.appendSibling(link);
        }

        text.appendText("]");
        text.setStyle(style);

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
