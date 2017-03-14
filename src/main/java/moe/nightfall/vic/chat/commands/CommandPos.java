package moe.nightfall.vic.chat.commands;

import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.util.Misc;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.util.Collections;
import java.util.List;

public class CommandPos extends CommandOverrideAccess
{
    @Override
    public String getName()
    {
        return "checkpos";
    }

    @Override
    public List<String> getAliases()
    {
        return Collections.singletonList("pos");
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return Config.posPermissionLevel;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/checkpos <player>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws WrongUsageException, PlayerNotFoundException
    {
        if(args.length < 1)
            throw new WrongUsageException(getUsage(sender));

        EntityPlayerMP player = Misc.getPlayer(args[0]);

        if(player == null)
            throw new PlayerNotFoundException(args[0]);

        sender.sendMessage(new TextComponentString(player.getName() + ": [" + (int)player.posX + ", " + (int)player.posY + ", " + (int)player.posZ + "]"));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        return getListOfStringsMatchingLastWord(args, server.getPlayerList().getOnlinePlayerNames());
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 0;
    }
}
