package vic.mod.chat;

import java.io.File;
import java.util.Arrays;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

import org.apache.commons.lang3.StringUtils;

import vic.mod.chat.api.IChannel;
import vic.mod.chat.api.bot.IBotHandler;
import vic.mod.chat.api.bot.IChannelBase;
import vic.mod.chat.api.bot.IChatBot;
import vic.mod.chat.api.bot.IChatEntity;
import vic.mod.chat.handler.ChannelHandler;

public class BotHandler implements IBotHandler
{
	public IChatBot owningBot;
	public ChatEntity botEntity;
	public BotCommandSender botSender;
	
	public BotHandler(IChatBot owningBot) 
	{
		this.owningBot = owningBot;
		botEntity = new ChatEntity(owningBot.getName(), true);
		botSender = new BotCommandSender();
	}
	
	@Override
	public IChannelBase getChannelForName(String name) 
	{
		return ChannelHandler.getChannel(name);
	}

	@Override
	public IChannelBase getDefaultChannel() 
	{
		return ChannelHandler.getChannel("global");
	}

	@Override
	public IChatEntity getChatEntityForName(String name) 
	{
		return new ChatEntity(Misc.getPlayer(name));
	}

	@Override
	public IChatEntity getServer() 
	{
		return ChatEntity.SERVER;
	}

	@Override
	public boolean isPlayerOnline(String name) 
	{
		return getChannelForName(name) != null;
	}

	@Override
	public boolean isOnChannel(IChatEntity entity, IChannelBase channel) 
	{
		if(entity == null) return false;
		return ChannelHandler.getJoinedChannels(entity).contains(channel);
	}

	@Override
	public void sendGlobalMessage(String message) 
	{
		ChatComponentText text = new ChatComponentText("");
		text.appendSibling(Misc.getComponent(botEntity));
		text.appendText(": " + message);
		ChannelHandler.broadcast(text, botEntity);
	}

	@Override
	public void sendMessage(IChannelBase channel, String message) 
	{
		ChatComponentText text = new ChatComponentText("");
		text.appendSibling(Misc.getComponent(botEntity));
		text.appendText(": " + message);
		ChannelHandler.broadcastOnChannel((IChannel)channel, botEntity, text);
	}

	@Override
	public void sendPrivateMessage(IChatEntity entity, String message) 
	{
		ChatComponentText text = new ChatComponentText("");
		text.appendSibling(Misc.getComponent(botEntity));
		ChatComponentText mcomp = new ChatComponentText(" whispers to you: " + message);
		mcomp.getChatStyle().setItalic(true);
		mcomp.getChatStyle().setColor(EnumChatFormatting.GRAY);
		text.appendSibling(mcomp);
		
		ChannelHandler.privateMessageTo(botEntity, (ChatEntity)entity, text);
	}

	@Override
	public File getBotDir()
	{
		return new File("/vBots");
	}

	@Override
	public void sendCommand(String command, String[] args) 
	{
		botSender.activeCommand = command;
		botSender.activeArgs = args;
		MinecraftServer.getServer().getCommandManager().executeCommand(MinecraftServer.getServer(), command + StringUtils.join(Arrays.asList(args), " "));
	}
	
	public class BotCommandSender implements ICommandSender
	{
		public String activeCommand;
		public String[] activeArgs;
		
		@Override
		public String getCommandSenderName() 
		{
			return botEntity.getUsername();
		}

		@Override
		public IChatComponent func_145748_c_() 
		{
			return MinecraftServer.getServer().func_145748_c_();
		}

		@Override
		public void addChatMessage(IChatComponent comp)
		{
			owningBot.onCommandMessage(activeCommand, activeArgs, comp.getUnformattedText());
		}

		@Override
		public boolean canCommandSenderUseCommand(int par1, String par2) 
		{
			return true;
		}

		@Override
		public ChunkCoordinates getPlayerCoordinates() 
		{
			return MinecraftServer.getServer().getPlayerCoordinates();
		}

		@Override
		public World getEntityWorld() 
		{
			return MinecraftServer.getServer().getEntityWorld();
		}	
	}
}
