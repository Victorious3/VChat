package moe.nightfall.vic.chat.commands.channels;

import moe.nightfall.vic.chat.commands.CommandOverrideAccess;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandGlobal extends CommandOverrideAccess
{
    @Override
    public String getName()
    {
        return "global";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/global <message>";
    }

    @Override
    public List<String> getAliases()
    {
        return Collections.singletonList("g");
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws WrongUsageException
    {
        if(args.length < 1)
            throw new WrongUsageException(getUsage(sender));

        server.getCommandManager().executeCommand(sender, "/channel msg global " + StringUtils.join(Arrays.asList(args), " "));
    }
}
