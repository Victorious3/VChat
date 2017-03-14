package moe.nightfall.vic.chat.bots;

import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.VChat;
import moe.nightfall.vic.chat.api.bot.IChatBot;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
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
    public void sendMessage(ITextComponent comp)
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
        return this.getServer().getEntityWorld();
    }

    @Override
    public String getName()
    {
        return this.botEntity.getUsername();

    }

    @Override
    public ITextComponent getDisplayName()
    {
        return this.getServer().getDisplayName();
    }

    @Override
    public BlockPos getPosition()
    {
        return this.getServer().getPosition();
    }

    @Override
    public Vec3d getPositionVector()
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
    public boolean canUseCommand(int par1, String par2)
    {
        return true;
    }

    @Override
    public MinecraftServer getServer()
    {
        return VChat.instance.getServer();
    }
}
