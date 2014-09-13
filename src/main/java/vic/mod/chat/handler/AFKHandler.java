package vic.mod.chat.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;

import org.apache.commons.lang3.StringUtils;

import vic.mod.chat.ChatEntity;
import vic.mod.chat.Config;
import vic.mod.chat.Misc;
import vic.mod.chat.Misc.CommandOverrideAccess;
import vic.mod.chat.VChat;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

public class AFKHandler extends ChatHandlerImpl
{
	private HashMap<ChatEntity, AFKEntry> afk = new HashMap<ChatEntity, AFKEntry>();
	
	public AFKHandler()
	{
		super();
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void onServerLoad(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new AFKCommand());
	}
	
	@SubscribeEvent()
	public void onPlayerLeft(PlayerEvent.PlayerLoggedOutEvent event)
	{
		afk.remove(new ChatEntity(event.player));
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onCommand(CommandEvent event)
	{
		if(event.command instanceof CommandMessage)
		{
			if(event.parameters.length > 0)
			{
				ChatEntity entity = new ChatEntity((Object)event.parameters[0]);
				if(entity.getUsername() != null && isAfk(entity) && !entity.equals(new ChatEntity(event.sender.getCommandSenderName())))
				{
					ChatComponentText comp = new ChatComponentText("The player you tired to message is currently AFK (Reason: " + getReason(entity) + ")");
					comp.getChatStyle().setColor(EnumChatFormatting.RED);
					event.sender.addChatMessage(comp);
					afk.get(entity).messaged.add(new ChatEntity(event.sender));
				}
			}
		}
	}
	
	public class AFKCommand extends CommandOverrideAccess
	{
		@Override
		public String getCommandName() 
		{
			return "afk";
		}

		@Override
		public String getCommandUsage(ICommandSender sender) 
		{
			return "/afk [reason]";
		}

		@Override
		public void processCommand(ICommandSender sender, String[] args) 
		{
			if(!(sender instanceof EntityPlayerMP)) return;
			String reason = "AFK";
			if(args.length > 0) reason = StringUtils.join(args, " ");
			ChatEntity entity = new ChatEntity(sender);
			
			if(isAfk(entity)) 
			{
				removeAfk(entity);
				if(Config.afkEnabled) VChat.autoAfkHandler.onAFKRemoved((EntityPlayerMP)sender);
			}
			else setAfk(entity, reason);
		}

		@Override
		public int getRequiredPermissionLevel() 
		{
			return 0;
		}	
	}
	
	public boolean isAfk(ChatEntity entity)
	{
		return afk.containsKey(entity);
	}
	
	public String getReason(ChatEntity entity)
	{
		return afk.get(entity).reason;
	}
	
	public void setAfk(ChatEntity entity, String reason)
	{
		String nickname = entity.getNickname();
		if(nickname == null) nickname = entity.getUsername();
		
		afk.put(entity, new AFKEntry(reason));
		ChatComponentText text = new ChatComponentText("*" + nickname + " is now AFK" + (!reason.equalsIgnoreCase("AFK") ? " (" + reason + ")" : "") + ".");
		text.getChatStyle().setItalic(true);
		text.getChatStyle().setColor(EnumChatFormatting.GRAY);
		ChannelHandler.broadcast(text);
	}
	
	public void removeAfk(ChatEntity entity)
	{
		String nickname = entity.getNickname();
		if(nickname == null) nickname = entity.getUsername();
		
		AFKEntry entry = afk.remove(entity);
		ChatComponentText text = new ChatComponentText("*" + nickname + " is no longer AFK.");
		text.getChatStyle().setItalic(true);
		text.getChatStyle().setColor(EnumChatFormatting.GRAY);
		ChannelHandler.broadcast(text);
		
		if(!entry.messaged.isEmpty())
		{
			ChatComponentText comp = new ChatComponentText("The following players tried to message you: ");
			Iterator<ChatEntity> iterator = entry.messaged.iterator();
			while(iterator.hasNext())
			{
				ChatComponentText nameComponent = Misc.getComponent(iterator.next());
				comp.appendSibling(nameComponent);
				if(iterator.hasNext()) comp.appendText(", ");
			}
			comp.appendText(".");
			entity.toPlayer().addChatMessage(comp);
		}
	}
	
	private static class AFKEntry
	{
		public ArrayList<ChatEntity> messaged = new ArrayList<ChatEntity>();
		public String reason = "";
		
		public AFKEntry(String reason)
		{
			this.reason = reason;
		}
	}
}
