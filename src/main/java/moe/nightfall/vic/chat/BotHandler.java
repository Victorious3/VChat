package moe.nightfall.vic.chat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;

import moe.nightfall.vic.chat.api.IChannel;
import moe.nightfall.vic.chat.api.bot.IBotHandler;
import moe.nightfall.vic.chat.api.bot.IChannelBase;
import moe.nightfall.vic.chat.api.bot.IChatBot;
import moe.nightfall.vic.chat.api.bot.IChatEntity;
import moe.nightfall.vic.chat.api.bot.LogLevel;
import moe.nightfall.vic.chat.handler.ChannelHandler;
import net.minecraft.command.CommandResultStats.Type;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

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
		MinecraftServer.getServer().addChatMessage(text);
	}

	@Override
	public void sendMessage(IChannelBase channel, String message) 
	{
		ChatComponentText text = new ChatComponentText("");
		text.appendSibling(Misc.getComponent(botEntity));
		text.appendText(": " + message);
		ChannelHandler.broadcastOnChannel((IChannel)channel, botEntity, text);
		MinecraftServer.getServer().addChatMessage(text);
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
		return new File("vChat/bots");
	}

	@Override
	public void sendCommand(String command, String[] args) 
	{
		botSender.activeCommand = command;
		botSender.activeArgs = args;
		try {
			MinecraftServer.getServer().getCommandManager().executeCommand(botSender, command + " " + StringUtils.join(Arrays.asList(args), " "));
		} catch (Exception e) {
			botSender.addChatMessage(new ChatComponentText("$COMMANDEXECFAILED " + e.getClass().getSimpleName() + ": " + e.getMessage()));
		}
	}
	
	public class BotCommandSender implements ICommandSender
	{
		public String activeCommand;
		public String[] activeArgs;
		
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
		public World getEntityWorld() 
		{
			return MinecraftServer.getServer().getEntityWorld();
		}

		@Override
		public String getName()
		{
			return botEntity.getUsername();

		}

		@Override
		public IChatComponent getDisplayName()
		{
			return MinecraftServer.getServer().getDisplayName();
		}

		@Override
		public BlockPos getPosition()
		{
			return MinecraftServer.getServer().getPosition();
		}

		@Override
		public Vec3 getPositionVector()
		{
			return null;
		}

		@Override
		public Entity getCommandSenderEntity()
		{
			return null;
		}

		@Override
		public boolean sendCommandFeedback()
		{
			return false;
		}

		@Override
		public void setCommandStat(Type type, int amount)
		{
			// TODO Auto-generated method stub
			
		}	
	}

	@Override
	public void log(LogLevel level, String message) 
	{
		message = "[" + owningBot.getName() + "]: " + message;
		VChat.logger.log(Level.toLevel(level.name(), Level.INFO), message);
	}

	@Override
	public void log(String message) 
	{
		message = "[" + owningBot.getName() + "]: " + message;
		VChat.logger.log(Level.INFO, message);
	}

	@Override
	public void logf(LogLevel level, String message, Object... args)
	{
		message = "[" + owningBot.getName() + "]: " + message;
		VChat.logger.log(Level.toLevel(level.name(), Level.INFO), message, args);
	}

	@Override
	public void logf(String message, Object... args) 
	{
		message = "[" + owningBot.getName() + "]: " + message;
		VChat.logger.log(Level.INFO, message, args);
	}

	@Override
	public List<IChatEntity> getAllChatEntities() 
	{
		List<IChatEntity> list = new ArrayList<IChatEntity>();
		list.addAll(Misc.getOnlinePlayersAsEntity());
		for(BotHandler bot : VChat.botLoader.bots.values())
			list.add(bot.botEntity);
		return list;
	}

	@Override
	public IChatBot getBotForName(String name) 
	{
		return VChat.botLoader.getBot(name).owningBot;
	}
}
