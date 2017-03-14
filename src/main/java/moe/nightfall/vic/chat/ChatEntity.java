package moe.nightfall.vic.chat;

import moe.nightfall.vic.chat.util.Misc;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import moe.nightfall.vic.chat.api.bot.IChatEntity;

public class ChatEntity implements IChatEntity
{
    public static final ChatEntity SERVER = new ChatEntity((String)null);

    private final String username;
    private boolean isBot = false;

    public ChatEntity(String username, boolean isBot)
    {
        this.isBot = isBot;
        this.username = username;
    }

    public ChatEntity(String username)
    {
        this(username, false);
    }

    public ChatEntity(EntityPlayerMP player)
    {
        this(player.getName());
    }

    public ChatEntity(Object obj)
    {
        if(obj instanceof EntityPlayerMP)
        {
            this.username = ((EntityPlayerMP) obj).getName();
        }
        else if(obj instanceof String)
        {
            if(Misc.getPlayer((String) obj) != null)
            {
                this.username = (String) obj;
            }
            else if(Config.nickEnabled)
            {
                String player = VChat.instance.getNickHandler().getPlayerFromNick((String) obj);

                if(player != null)
                    this.username = player;
                else
                    this.username = (String) obj;
            }
            else
            {
                this.username = (String) obj;
            }
        }
        else
        {
            this.username = null;
        }
    }

    public EntityPlayerMP toPlayer()
    {
        return Misc.getPlayer(this.username);
    }

    @Override
    public String getUsername()
    {
        return this.username;
    }

    @Override
    public String getNickname()
    {
        if(Config.nickEnabled)
        {
            return this.isBot ? this.username : VChat.instance.getNickHandler().getNickRegistry().get(this.username);
        }
        else
        {
            return null;
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null)
            return false;

        if(this.username == null)
        {
            return obj == SERVER;
        }
        else if(obj instanceof ChatEntity)
        {
            return this.username.equalsIgnoreCase(((ChatEntity) obj).username);
        }
        else if(obj instanceof EntityPlayerMP)
        {
            return this.username.equalsIgnoreCase(((EntityPlayerMP) obj).getName());
        }
        else if(obj instanceof String)
        {
            if(((String) obj).equalsIgnoreCase(this.username))
            {
                return true;
            }
            else if(Config.nickEnabled)
            {
                String nick = getNickname();

                if(nick != null && nick.equalsIgnoreCase((String) obj))
                    return true;
            }
        }

        return false;
    }

    @Override
    public boolean isServer()
    {
        return this == SERVER;
    }

    @Override
    public boolean isBot()
    {
        return this.isBot;
    }

    @Override
    public boolean isOperator()
    {
        if (this.isBot() || this.isServer())
            return true;

        EntityPlayerMP player = toPlayer();
        return player != null && player.canUseCommand(player.getServer().getOpPermissionLevel(), null);
    }

    @Override
    public String toString()
    {
        return this.username == null ? "SERVER" : this.username;
    }

    @Override
    public int hashCode()
    {
        return this.username.hashCode();
    }

    @Override
    public String getDisplayName()
    {
        String nick = this.getNickname();
        return nick != null ? nick : this.toString();
    }
}
