package moe.nightfall.vic.chat.commands;

import moe.nightfall.vic.chat.VChat;
import moe.nightfall.vic.chat.handlers.CommonHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandList extends CommandOverrideAccess
{
    @Override
    public String getName()
    {
        return "list";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/list";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        sender.sendMessage(new TextComponentString("--Name--Playtime--Last seen--"));

        for(CommonHandler.OnlineTracker tracker : VChat.instance.getCommonHandler().getPlayerTrackers().values())
            sender.sendMessage(tracker.toChatComponent());
    }
}
