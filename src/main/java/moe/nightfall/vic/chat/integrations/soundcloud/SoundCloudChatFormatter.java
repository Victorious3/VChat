package moe.nightfall.vic.chat.integrations.soundcloud;

import com.google.gson.GsonBuilder;
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

public class SoundCloudChatFormatter extends ChatFormatter
{
    private static final Pattern PATTERN = Pattern.compile("(http:|https)?://soundcloud\\.com/\\S*");
    private static final String API_KEY = "00efa1907d5fb9571f5776add950b623";

    public SoundCloudChatFormatter(VChat instance)
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
        ChatComponentText text = new ChatComponentText("[SoundCloud - ");

        ChatStyle style = new ChatStyle();
        style.setColor(EnumChatFormatting.GOLD);

        try
        {
            URL apiURL = new URL("http://api.soundcloud.com/resolve.json?url=" + match + "&client_id=" + API_KEY);
            HashMap<String, String> move = new GsonBuilder().create().fromJson(new BufferedReader(new InputStreamReader(apiURL.openStream(), "UTF-8")), HashMap.class);
            URL trackURL = new URL(move.get("location"));
            SoundCloudTrack track = new GsonBuilder().create().fromJson(new BufferedReader(new InputStreamReader(trackURL.openStream(), "UTF-8")), SoundCloudTrack.class);

            int seconds = track.getDuration() / 1000;
            int minutes = seconds / 60;
            seconds = seconds % 60;

            ChatComponentText toolTipText = new ChatComponentText(BULLET + " SoundCloud\n\n");
            toolTipText.getChatStyle().setColor(EnumChatFormatting.GOLD);
            toolTipText.appendText(EnumChatFormatting.YELLOW + "Title: " + EnumChatFormatting.WHITE + track.getTitle() + " (" + EnumChatFormatting.GOLD + track.getPlaybackCount() + EnumChatFormatting.WHITE + " plays)\n");
            toolTipText.appendText(EnumChatFormatting.YELLOW + "Publisher: " + EnumChatFormatting.WHITE + track.getUser().getUsername() + "\n");
            toolTipText.appendText(EnumChatFormatting.YELLOW + "Duration: " + EnumChatFormatting.GOLD + minutes + EnumChatFormatting.WHITE + " minutes and " + EnumChatFormatting.GOLD + seconds + EnumChatFormatting.WHITE + " seconds");

            style.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));
            style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, toolTipText));

            ChatComponentText title = new ChatComponentText("\"" + track.getTitle() + "\"");
            title.getChatStyle().setColor(EnumChatFormatting.YELLOW);

            text.appendSibling(title);
            text.appendText(" by ");

            ChatComponentText publisher = new ChatComponentText(track.getUser().getUsername());
            publisher.getChatStyle().setColor(EnumChatFormatting.YELLOW);

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
