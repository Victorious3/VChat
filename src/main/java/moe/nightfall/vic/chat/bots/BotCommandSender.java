package moe.nightfall.vic.chat.bots;

import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.api.bot.IChatBot;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class BotCommandSender implements ICommandSender
{
    private final IChatBot owningBot;
    private final ChatEntity botEntity;
    private String activeCommand;
    private String[] activeArgs;

    public BotCommandSender(IChatBot owningBot, ChatEntity botEntity)
    {
        this.owningBot = owningBot;
        this.botEntity = botEntity;
    }

    @Override
    public void addChatMessage(IChatComponent comp)
    {
        this.owningBot.onCommandMessage(this.activeCommand, this.activeArgs, comp.getUnformattedText());
    }

    @Override
    public boolean sendCommandFeedback()
    {
        return false;
    }

    public void setActiveCommand(String command)
    {
        this.activeCommand = command;
    }

    public void setActiveArgs(String[] args)
    {
        this.activeArgs = args;
    }

    @Override
    public World getEntityWorld()
    {
        return MinecraftServer.getServer().getEntityWorld();
    }

    @Override
    public String getName()
    {
        return this.botEntity.getUsername();

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
    public void setCommandStat(CommandResultStats.Type type, int amount) {}

    @Override
    public boolean canCommandSenderUseCommand(int par1, String par2)
    {
        return true;
    }
}
