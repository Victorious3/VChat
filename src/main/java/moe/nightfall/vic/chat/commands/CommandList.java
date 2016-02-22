package moe.nightfall.vic.chat.commands;

import moe.nightfall.vic.chat.VChat;
import moe.nightfall.vic.chat.handlers.CommonHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class CommandList extends CommandOverrideAccess
{
    @Override
    public String getCommandName()
    {
        return "list";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/list";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        sender.addChatMessage(new ChatComponentText("--Name--Playtime--Last seen--"));

        for(CommonHandler.OnlineTracker tracker : VChat.instance.getCommonHandler().getPlayerTrackers().values())
            sender.addChatMessage(tracker.toChatComponent());
    }
}
