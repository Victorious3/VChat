package moe.nightfall.vic.chat.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import moe.nightfall.vic.chat.commands.CommandAFK;

import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.util.Misc;
import moe.nightfall.vic.chat.VChat;
import net.minecraft.command.server.CommandMessage;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.CommandEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

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
        if(event.command instanceof CommandMessage)
        {
            if(event.parameters.length > 0)
            {
                ChatEntity entity = new ChatEntity((Object)event.parameters[0]);

                if(entity.getUsername() != null && this.isAFK(entity) && !entity.equals(new ChatEntity(event.sender.getCommandSenderName())))
                {
                    ChatComponentText text = new ChatComponentText("The player you tired to message is currently AFK (Reason: " + getReason(entity) + ")");
                    text.getChatStyle().setColor(EnumChatFormatting.RED);
                    event.sender.addChatMessage(text);

                    this.afks.get(entity).messaged.add(new ChatEntity(event.sender));
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

        ChatComponentText text = new ChatComponentText("*" + nickname + " is now AFK" + (!reason.equalsIgnoreCase("AFK") ? " (" + reason + ")" : "") + ".");
        text.getChatStyle().setItalic(true);
        text.getChatStyle().setColor(EnumChatFormatting.GRAY);

        this.instance.getChannelHandler().broadcast(text, ChatEntity.SERVER);
    }

    public void removeAFK(ChatEntity entity)
    {
        String nickname = entity.getNickname();

        if(nickname == null)
            nickname = entity.getUsername();

        AFKEntry entry = this.afks.remove(entity);

        ChatComponentText text = new ChatComponentText("*" + nickname + " is no longer AFK.");
        text.getChatStyle().setItalic(true);
        text.getChatStyle().setColor(EnumChatFormatting.GRAY);

        this.instance.getChannelHandler().broadcast(text, ChatEntity.SERVER);

        if(!entry.messaged.isEmpty())
        {
            ChatComponentText text1 = new ChatComponentText("The following players tried to message you: ");
            Iterator<ChatEntity> iterator = entry.messaged.iterator();

            while(iterator.hasNext())
            {
                ChatComponentText nameComponent = Misc.getComponent(iterator.next());
                text.appendSibling(nameComponent);

                if(iterator.hasNext())
                    text1.appendText(", ");
            }

            text1.appendText(".");
            entity.toPlayer().addChatMessage(text1);
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
