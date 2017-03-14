package moe.nightfall.vic.chat.integrations.soundcloud;

import com.google.gson.GsonBuilder;
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

public class SoundCloudChatFormatter extends ChatFormatter
{
    private static final Pattern PATTERN = Pattern.compile("(http|https)?://(www\\.)?soundcloud\\.com/\\S*");
    private static final String API_KEY = "00efa1907d5fb9571f5776add950b623";

    public SoundCloudChatFormatter(VChat instance)
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
        TextComponentString text = new TextComponentString("[SoundCloud - ");

        Style style = new Style();
        style.setColor(TextFormatting.GOLD);

        try
        {
            URL apiURL = new URL("http://api.soundcloud.com/resolve.json?url=" + match + "&client_id=" + API_KEY);
            HashMap<String, String> move = new GsonBuilder().create().fromJson(new BufferedReader(new InputStreamReader(apiURL.openStream(), "UTF-8")), HashMap.class);
            URL trackURL = new URL(move.get("location"));
            SoundCloudTrack track = new GsonBuilder().create().fromJson(new BufferedReader(new InputStreamReader(trackURL.openStream(), "UTF-8")), SoundCloudTrack.class);

            int seconds = track.getDuration() / 1000;
            int minutes = seconds / 60;
            seconds = seconds % 60;

            TextComponentString toolTipText = new TextComponentString(BULLET + " SoundCloud\n\n");
            toolTipText.getStyle().setColor(TextFormatting.GOLD);
            toolTipText.appendText(TextFormatting.YELLOW + "Title: " + TextFormatting.WHITE + track.getTitle() + " (" + TextFormatting.GOLD + track.getPlaybackCount() + TextFormatting.WHITE + " plays)\n");
            toolTipText.appendText(TextFormatting.YELLOW + "Publisher: " + TextFormatting.WHITE + track.getUser().getUsername() + "\n");
            toolTipText.appendText(TextFormatting.YELLOW + "Duration: " + TextFormatting.GOLD + minutes + TextFormatting.WHITE + " minutes and " + TextFormatting.GOLD + seconds + TextFormatting.WHITE + " seconds");

            style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, toolTipText));

            TextComponentString title = new TextComponentString("\"" + track.getTitle() + "\"");
            title.getStyle().setColor(TextFormatting.YELLOW);

            text.appendSibling(title);
            text.appendText(" by ");

            TextComponentString publisher = new TextComponentString(track.getUser().getUsername());
            publisher.getStyle().setColor(TextFormatting.YELLOW);

            text.appendSibling(publisher);
        }
        catch(Exception e)
        {
            this.instance.getLogger().warn("Failed to retrieve the SoundCloud track's data of '" + match + "' (" + e.getMessage() + ")");

            style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));

            TextComponentString toolTipText = new TextComponentString("Click to open this SoundCloud track (Failed to retrieve track's data)");
            toolTipText.getStyle().setColor(TextFormatting.GOLD);

            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, toolTipText));

            TextComponentString link = new TextComponentString(match);
            link.getStyle().setColor(TextFormatting.YELLOW);

            text.appendSibling(link);
        }

        text.appendText("]");
        text.setStyle(style);

        return text;
    }
}
