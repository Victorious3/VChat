package moe.nightfall.vic.chat.commands;

import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.VChat;
import moe.nightfall.vic.chat.handlers.NickHandler;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

import java.util.List;

public class CommandNick extends CommandOverrideAccess
{
    private final NickHandler nickHandler;

    public CommandNick(NickHandler nickHandler)
    {
        this.nickHandler = nickHandler;
    }

    @Override
    public String getCommandName()
    {
        return "nick";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/nick <player> [nickname]";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return Config.nickPermissionLevel;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if(args.length > 0 && args.length < 3)
        {
            EntityPlayerMP player = null;
            try
            {
                player = getPlayer(sender, args[0]);
            }
            catch (PlayerNotFoundException e)
            {
                VChat.instance.getLogger().error("Failed to get the command sender of the last /nick command!");
                e.printStackTrace();
            }

            if(args.length == 1)
            {
                if(!this.nickHandler.getNickRegistry().containsKey(args[0]))
                    throw new CommandException("The given player has no nickname!", sender);

                this.nickHandler.getNickRegistry().remove(args[0]);

                if(player != null)
                    player.refreshDisplayName();

                sender.addChatMessage(new ChatComponentText("Removed nickname from player \"" + args[0] + "\"."));
            }
            else
            {
                if(args[1].contains("\u00A7"))
                    throw new CommandException("You can not use color codes inside nicknames.", sender);

                String playername = this.nickHandler.getPlayerFromNick(args[1]);

                if(playername == null)
                {
                    if(args[0].equals(args[1]))
                        throw new CommandException("You can not create a nickname which is equivalent to the username!", sender);

                    if(args[1].length() < Config.nickMin)
                        throw new CommandException("You can not create a nickname which has less than " + Config.nickMin + " characters!", sender);

                    if(args[1].length() > Config.nickMax)
                        throw new CommandException("You can not create a nickname which has more than " + Config.nickMax + " characters!", sender);

                    if(!args[1].matches("[A-z0-9_]+"))
                        throw new CommandException("A nickname can only contain alphanumeric characters and underscores.");

                    this.nickHandler.getNickRegistry().put(args[0], args[1]);

                    if(player != null)
                        player.refreshDisplayName();

                    sender.addChatMessage(new ChatComponentText("Added nickname \"" + args[1] + "\" to player \"" + args[0] + "\"."));
                }
                else
                {
                    throw new CommandException("The given nickname is already taken by player \"" + playername + "\". You can remove it with /nick " + playername + ".", sender);
                }
            }
        }
        else throw new WrongUsageException(getCommandUsage(sender));
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : null;
    }
}
