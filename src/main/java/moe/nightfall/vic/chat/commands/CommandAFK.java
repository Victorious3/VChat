package moe.nightfall.vic.chat.commands;

import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.VChat;
import moe.nightfall.vic.chat.handlers.AFKHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.StringUtils;

public class CommandAFK extends CommandOverrideAccess
{
    private final AFKHandler afkHandler;

    public CommandAFK(AFKHandler afkHandler)
    {
        this.afkHandler = afkHandler;
    }

    @Override
    public String getName()
    {
        return "afk";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/afk [reason]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if(!(sender instanceof EntityPlayerMP))
            return;

        String reason = args.length > 0 ? StringUtils.join(args, " ") : "AFK";
        ChatEntity entity = new ChatEntity(sender);

        if(this.afkHandler.isAFK(entity))
        {
            this.afkHandler.removeAFK(entity);

            if(Config.autoAfkEnabled)
                VChat.instance.getAutoAFKHandler().onAFKRemoved((EntityPlayerMP)sender);
        }
        else
        {
            this.afkHandler.setAFK(entity, reason);
        }
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }
}
