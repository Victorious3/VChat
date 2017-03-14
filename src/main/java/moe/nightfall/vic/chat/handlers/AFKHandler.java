package moe.nightfall.vic.chat.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import moe.nightfall.vic.chat.commands.CommandAFK;

import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.Misc;
import moe.nightfall.vic.chat.VChat;
import net.minecraft.command.server.CommandMessage;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class AFKHandler extends ChatHandler
{
    private final HashMap<ChatEntity, AFKEntry> afks;

    public AFKHandler(VChat instance)
    {
        super(instance);

        this.afks = new HashMap<ChatEntity, AFKEntry>();
    }

    @Override
    public void onServerLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandAFK(this));
    }

    @SubscribeEvent
    public void onPlayerLeft(PlayerEvent.PlayerLoggedOutEvent event)
    {
        this.afks.remove(new ChatEntity(event.player));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onCommand(CommandEvent event)
    {
        if(event.getCommand() instanceof CommandMessage)
        {
            if(event.getParameters().length > 0)
            {
                ChatEntity entity = new ChatEntity((Object)event.getParameters()[0]);

                if(entity.getUsername() != null && this.isAFK(entity) && !entity.equals(new ChatEntity(event.getSender().getName())))
                {
                    TextComponentString text = new TextComponentString("The player you tired to message is currently AFK (Reason: " + getReason(entity) + ")");
                    text.getStyle().setColor(TextFormatting.RED);
                    event.getSender().sendMessage(text);

                    this.afks.get(entity).messaged.add(new ChatEntity(event.getSender()));
                }
            }
        }
    }

    public boolean isAFK(ChatEntity entity)
    {
        return this.afks.containsKey(entity);
    }

    public String getReason(ChatEntity entity)
    {
        return this.afks.get(entity).reason;
    }

    public void setAFK(ChatEntity entity, String reason)
    {
        String nickname = entity.getNickname();

        if(nickname == null)
            nickname = entity.getUsername();

        this.afks.put(entity, new AFKEntry(reason));

        TextComponentString text = new TextComponentString("*" + nickname + " is now AFK" + (!reason.equalsIgnoreCase("AFK") ? " (" + reason + ")" : "") + ".");
        text.getStyle().setItalic(true);
        text.getStyle().setColor(TextFormatting.GRAY);

        this.instance.getChannelHandler().broadcast(text, ChatEntity.SERVER);
    }

    public void removeAFK(ChatEntity entity)
    {
        String nickname = entity.getNickname();

        if(nickname == null)
            nickname = entity.getUsername();

        AFKEntry entry = this.afks.remove(entity);

        TextComponentString text = new TextComponentString("*" + nickname + " is no longer AFK.");
        text.getStyle().setItalic(true);
        text.getStyle().setColor(TextFormatting.GRAY);

        this.instance.getChannelHandler().broadcast(text, ChatEntity.SERVER);

        if(!entry.messaged.isEmpty())
        {
            TextComponentString text1 = new TextComponentString("The following players tried to message you: ");
            Iterator<ChatEntity> iterator = entry.messaged.iterator();

            while(iterator.hasNext())
            {
                TextComponentString nameComponent = Misc.getComponent(iterator.next());
                text.appendSibling(nameComponent);

                if(iterator.hasNext())
                    text1.appendText(", ");
            }

            text1.appendText(".");
            entity.toPlayer().sendMessage(text1);
        }
    }

    private static class AFKEntry
    {
        private ArrayList<ChatEntity> messaged = new ArrayList<ChatEntity>();
        private String reason = "";

        public AFKEntry(String reason)
        {
            this.reason = reason;
        }
    }
}
