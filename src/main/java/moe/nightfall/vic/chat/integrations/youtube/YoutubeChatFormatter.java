package moe.nightfall.vic.chat.integrations.youtube;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moe.nightfall.vic.chat.Misc;
import moe.nightfall.vic.chat.VChat;
import moe.nightfall.vic.chat.integrations.ChatFormatter;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Pattern;

public class YoutubeChatFormatter extends ChatFormatter
{
    private static final Pattern PATTERN = Pattern.compile("(http:|https:)?//(www\\.)?(youtube.com|youtu.be)/(watch)?(\\?v=)?(\\S+)?");
    private static final String API_KEY = "AIzaSyBvlMpa7lLAuuqDY-FhNQhIauu0__-qTg0";

    public YoutubeChatFormatter(VChat instance)
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
        ChatComponentText text = new ChatComponentText("[Youtube - ");

        ChatStyle style = new ChatStyle();
        style.setColor(EnumChatFormatting.RED);

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

                ChatComponentText toolTipText = new ChatComponentText(BULLET + " YouTube\n\n");
                toolTipText.getChatStyle().setColor(EnumChatFormatting.RED);
                toolTipText.appendText(EnumChatFormatting.RED + "Title: " + EnumChatFormatting.WHITE + youtubeVideo.getTitle() + "\n");
                toolTipText.appendText(EnumChatFormatting.RED + "Publisher: " + EnumChatFormatting.WHITE + youtubeVideo.getChannelTitle() + "\n");
                toolTipText.appendText(EnumChatFormatting.RED + "Duration: " + (hours > 0 ? EnumChatFormatting.RED + "" + hours + EnumChatFormatting.WHITE + " hours, " : "") + EnumChatFormatting.RED + minutes + EnumChatFormatting.WHITE + " minutes and " + EnumChatFormatting.RED + seconds + EnumChatFormatting.WHITE + " seconds");

                style.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));
                style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, toolTipText));

                ChatComponentText title = new ChatComponentText("\"" + youtubeVideo.getTitle() + "\"");
                title.getChatStyle().setColor(EnumChatFormatting.WHITE);

                text.appendSibling(title);
                text.appendText(" by ");

                ChatComponentText publisher = new ChatComponentText(youtubeVideo.getChannelTitle());
                publisher.getChatStyle().setColor(EnumChatFormatting.WHITE);

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

            style.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));

            ChatComponentText toolTipText = new ChatComponentText("Click to open this YouTube video (Failed to retrieve video's data)");
            toolTipText.getChatStyle().setColor(EnumChatFormatting.RED);

            style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, toolTipText));

            ChatComponentText link = new ChatComponentText(match);
            link.getChatStyle().setColor(EnumChatFormatting.WHITE);

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
