package moe.nightfall.vic.chat.commands;

import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.Misc;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

import java.util.Collections;
import java.util.List;

public class CommandPos extends CommandOverrideAccess
{
    @Override
    public String getCommandName()
    {
        return "checkpos";
    }

    @Override
    public List<String> getCommandAliases()
    {
        return Collections.singletonList("pos");
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return Config.posPermissionLevel;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/checkpos <player>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws WrongUsageException, PlayerNotFoundException
    {
        if(args.length < 1)
            throw new WrongUsageException(getCommandUsage(sender));

        EntityPlayerMP player = Misc.getPlayer(args[0]);

        if(player == null)
            throw new PlayerNotFoundException();

        sender.addChatMessage(new ChatComponentText(player.getCommandSenderName() + ": [" + (int)player.posX + ", " + (int)player.posY + ", " + (int)player.posZ + "]"));
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 0;
    }
}
