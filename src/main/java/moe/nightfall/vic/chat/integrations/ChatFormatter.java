package moe.nightfall.vic.chat.integrations;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import moe.nightfall.vic.chat.*;

import moe.nightfall.vic.chat.api.IChatFormatter;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.event.HoverEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

public abstract class ChatFormatter implements IChatFormatter
{
    protected static final String BULLET = "\u25c8";

    protected final VChat instance;
    protected boolean matched;

    public ChatFormatter(VChat instance)
    {
        this.instance = instance;
        this.matched = false;
    }

    public void apply(ChatComponentText component, Pattern pattern)
    {
        for(int i = 0; i < component.getSiblings().size(); i++)
        {
            Object obj = component.getSiblings().get(i);

            if(obj instanceof ChatComponentText && ((ChatComponentText)obj).getChatStyle().isEmpty())
            {
                ChatComponentText baseComponent = (ChatComponentText)obj;
                String text = baseComponent.getUnformattedTextForChat();
                Matcher matcher = pattern.matcher(text);

                this.matched = matcher.find();

                matcher.reset();

                if(appliesCustomStyle())
                {
                    component.getSiblings().remove(i);

                    int index = i;
                    int start = 0;
                    int end = text.length();

                    while(start < text.length())
                    {
                        boolean find = matcher.find();

                        if(find) end = matcher.start();

                        if(start != end)
                        {
                            component.getSiblings().add(index, new ChatComponentText(text.substring(start, end)));
                            index++;
                        }

                        if(find)
                        {
                            String matched = text.substring(matcher.start(), matcher.end());
                            ChatComponentText comp = getComponentReplacement(matched);
                            component.getSiblings().add(index, comp);
                            start = matcher.end();
                            end = text.length();
                            index++;
                        }
                        else start = end + 1;
                    }
                }
                else
                {
                    component.getSiblings().remove(i);
                    component.getSiblings().add(i, new ChatComponentText(matcher.replaceAll(getReplacement())));
                }
            }
        }
    }

    @Override
    public boolean isMatched()
    {
        return this.matched;
    }

    protected ChatComponentText getComponentReplacement(String match)
    {
        return new ChatComponentText(match);
    }

    protected boolean appliesCustomStyle()
    {
        return true;
    }

    protected String getReplacement()
    {
        return "";
    }

    public static class ChatFormatterURL extends ChatFormatter
    {
        private static final Pattern PATTERN = Pattern.compile("\\b(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

        public ChatFormatterURL(VChat instance)
        {
            super(instance);
        }

        @Override
        protected ChatComponentText getComponentReplacement(String match)
        {
            ChatComponentText text = new ChatComponentText(match);
            ChatStyle style = new ChatStyle();
            style.setColor(EnumChatFormatting.BLUE);
            style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to open URL")));
            style.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, match));
            text.setChatStyle(style);
            return text;
        }

        @Override
        public void apply(ChatComponentText text)
        {
            super.apply(text, PATTERN);
        }
    }

    public static class ChatFormatterColor extends ChatFormatter
    {
        private static Pattern pattern = Pattern.compile("[&]");

        public ChatFormatterColor(VChat instance)
        {
            super(instance);
        }

        @Override
        public void apply(ChatComponentText text)
        {
            super.apply(text, pattern);
        }

        @Override
        protected boolean appliesCustomStyle()
        {
            return false;
        }

        @Override
        protected String getReplacement()
        {
            return "\u00A7";
        }
    }

    public static class ChatFormatterUsername extends ChatFormatter
    {
        private Pattern pattern;
        private ChatEntity player;
        private ArrayList<EntityPlayerMP> mentioned;
        private boolean replaceNick;
        private boolean canMatch = true;
        private boolean isSelf = false;

        public ChatFormatterUsername(VChat instance, ChatEntity player, ChatEntity receiver, boolean replaceNick, ArrayList<EntityPlayerMP> mentioned)
        {
            super(instance);

            String nickname = player.getNickname();

            if(replaceNick && nickname == null)
                this.canMatch = false;
            else
                this.pattern = Pattern.compile("(?<![0-9A-z_])(?i)" + (replaceNick ? nickname : player.getUsername()) + "(?![0-9A-z_])");

            this.player = player;
            this.mentioned = mentioned;
            this.replaceNick = replaceNick;

            if(player.equals(receiver))
                this.isSelf = true;
        }

        @Override
        public void apply(ChatComponentText text)
        {
            if(this.canMatch)
                super.apply(text, this.pattern);
        }

        @Override
        protected ChatComponentText getComponentReplacement(String match)
        {
            if(!this.isSelf)
            {
                EntityPlayerMP playerEntity = this.player.toPlayer();

                if(playerEntity != null)
                    this.mentioned.add(playerEntity);
            }

            ChatComponentText text = new ChatComponentText(this.replaceNick ? this.player.getNickname() : this.player.getUsername());
            ChatStyle style = new ChatStyle();
            boolean afk = Config.afkEnabled && this.instance.getAfkHandler().isAFK(player);

            if(this.isSelf)
                style.setColor(Config.colorHighlightSelf);
            else if(afk)
                style.setColor(EnumChatFormatting.GRAY);
            else
                style.setColor(Config.colorHighlight);

            if(!this.isSelf)
            {
                if(afk)
                    style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(this.player.getUsername() + " (AFK - " + this.instance.getAfkHandler().getReason(this.player) + ")")));
                else if(this.replaceNick)
                    style.setChatHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ChatComponentText(this.player.getUsername())));
                if(!afk)
                    style.setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + this.player.getUsername()));
            }

            text.setChatStyle(style);

            return text;
        }
    }
}
