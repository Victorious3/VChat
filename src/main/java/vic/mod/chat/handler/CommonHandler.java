package vic.mod.chat.handler;

import net.minecraft.command.server.CommandEmote;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import vic.mod.chat.ChatEntity;
import vic.mod.chat.ChatFormatter;
import vic.mod.chat.Config;
import vic.mod.chat.IChannel;
import vic.mod.chat.Misc;
import vic.mod.chat.VChat;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

public class CommonHandler implements IChatHandler
{
	public CommonHandler()
	{
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event)
	{
		if(Config.modtEnabled)
			for(String s : Misc.parseModt(Config.modt, (EntityPlayerMP)event.player))
				event.player.addChatComponentMessage(new ChatComponentText(s));
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onChat(ServerChatEvent event)
	{
		ChatEntity entity = new ChatEntity(event.player);
		IChannel channel = ChannelHandler.getActiveChannel(entity);
		if(channel == null)
		{
			ChatComponentText text = new ChatComponentText("You have to join a channel to use the chat!");
			text.getChatStyle().setColor(EnumChatFormatting.RED);
			event.player.addChatComponentMessage(text);
			event.setCanceled(true);
			return;
		}
		else if(channel.isMuted(entity)) 
		{
			ChatComponentText text = new ChatComponentText("You are muted on this channel!");
			text.getChatStyle().setColor(EnumChatFormatting.RED);
			ChannelHandler.privateMessageOnChannel(channel, ChatEntity.SERVER, entity, text);
			event.setCanceled(true);
			return;
		}
		
		String message = event.message;
		
		boolean applyFormat = true;
		if(message.startsWith("#"))
		{
			message = message.replaceFirst("#", "");
			applyFormat = false;
		}
		
		ChatComponentText computed = new ChatComponentText("");
		computed.appendSibling(new ChatComponentText(message));
		computed.getChatStyle().setColor(channel.getColor());
		
		if(applyFormat && Config.urlEnabled)
			if(Config.urlPermissionLevel == 0 || event.player.canCommandSenderUseCommand(Config.urlPermissionLevel, null))
			{
				new ChatFormatter.ChatFormatterYoutube().apply(computed);
				if(Config.urlEnabledYoutube) new ChatFormatter.ChatFormatterURL().apply(computed);
			}		
		if(Config.colorPermissionLevel == 0 || event.player.canCommandSenderUseCommand(Config.colorPermissionLevel, null)) new ChatFormatter.ChatFormatterColor().apply(computed);

		ChatComponentText componentName = Misc.getComponent(entity);
		
		for(Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList)
		{
			ChatComponentText computed2 = computed.createCopy();
			ChatEntity receiver = new ChatEntity(obj);		
			if(applyFormat) 
			{
				for(Object obj2 : MinecraftServer.getServer().getConfigurationManager().playerEntityList)
				{
					ChatEntity player = new ChatEntity(obj2);
					new ChatFormatter.ChatFormatterUsername(player, receiver, false).apply(computed2);	
				}
				if(Config.nickEnabled)
				{
					for(Object obj2 : MinecraftServer.getServer().getConfigurationManager().playerEntityList)
					{
						ChatEntity player = new ChatEntity(obj2);
						new ChatFormatter.ChatFormatterUsername(player, receiver, true).apply(computed2);	
					}
				}	
			}
			VChat.channelHandler.privateMessageOnChannel(channel, entity, receiver, new ChatComponentTranslation("chat.type.text", componentName, computed2));
		}
		MinecraftServer.getServer().addChatMessage(new ChatComponentTranslation("chat.type.text", componentName, computed));
		event.setCanceled(true);
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onCommand(CommandEvent event)
	{
		if(event.command instanceof CommandEmote)
		{
			if(event.sender.canCommandSenderUseCommand(Config.colorPermissionLevel, null))
			{
				if(event.parameters.length < 2) return;
				for(int i = 0; i < event.parameters.length; i++) event.parameters[i] = event.parameters[i].replaceAll("&", "\u00A7");
			}
		}
	}

	@Override public void onServerLoad(FMLServerStartingEvent event) {}
	@Override public void onServerUnload(FMLServerStoppingEvent event) {}
}