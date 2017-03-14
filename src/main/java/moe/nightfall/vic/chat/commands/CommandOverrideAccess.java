package moe.nightfall.vic.chat.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public abstract class CommandOverrideAccess extends CommandBase
{
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return this.getRequiredPermissionLevel() == 0 || sender.canUseCommand(this.getRequiredPermissionLevel(), this.getName());
    }
}
