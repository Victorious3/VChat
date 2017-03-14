package moe.nightfall.vic.chat.channels;

import java.util.ArrayList;
import java.util.Collection;

import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.VChat;
import moe.nightfall.vic.chat.api.IChannel;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public abstract class ChannelBase implements IChannel
{
    protected VChat instance;

    protected ArrayList<ChatEntity> joined;
    protected ArrayList<ChatEntity> muted;
    protected ArrayList<ChatEntity> banned;

    public ChannelBase(VChat instance)
    {
        this.instance = instance;

        this.joined = new ArrayList<ChatEntity>();
        this.muted = new ArrayList<ChatEntity>();
        this.banned = new ArrayList<ChatEntity>();
    }

    @Override
    public ITextComponent formatChat(ChatEntity sender, ChatEntity receiver, ITextComponent message)
    {
        if(sender.isServer())
        {
            TextComponentString text = new TextComponentString("*");
            text.getStyle().setItalic(true);
            text.getStyle().setColor(TextFormatting.GRAY);
            text.appendSibling(message);

            return text;
        }

        return message;
    }

    @Override
    public void onJoin(ChatEntity player, boolean initial)
    {
        if(!this.joined.contains(player))
        {
            if(!initial)
                this.instance.getChannelHandler().broadcastOnChannel(this, ChatEntity.SERVER, new TextComponentString(player + " joined the channel."));

            this.joined.add(player);
        }
    }

    @Override
    public void onLeave(ChatEntity player, boolean initial)
    {
        if(!this.joined.contains(player))
            return;

        this.joined.remove(player);

        if(!initial)
            this.instance.getChannelHandler().broadcastOnChannel(this, ChatEntity.SERVER, new TextComponentString(player + " left the channel."));
    }

    @Override
    public void mute(ChatEntity issuer, ChatEntity player, boolean unmute)
    {
        this.instance.getChannelHandler().broadcastOnChannel(this, ChatEntity.SERVER, new TextComponentString(player + " was " + (unmute ? "unmuted" : "muted") +" by " + issuer));

        if(unmute)
            this.muted.remove(player);
        else
            this.muted.add(player);
    }

    @Override
    public void kick(ChatEntity issuer, ChatEntity player)
    {
        if(!this.joined.contains(player))
        {
            this.instance.getChannelHandler().privateMessageOnChannel(this, ChatEntity.SERVER, issuer, new TextComponentString("There is no player called \"" + player.getUsername() + "\" on the channel!"));
            return;
        }

        this.instance.getChannelHandler().broadcastOnChannel(this, ChatEntity.SERVER, new TextComponentString(player + " was kicked from the channel by " + issuer));
        this.instance.getChannelHandler().leaveChannel(player, this);
    }

    @Override
    public void ban(ChatEntity issuer, ChatEntity player, boolean unban)
    {
        if(unban)
        {
            if(this.banned.contains(player))
            {
                this.banned.remove(player);

                if(this.isWhitelisted())
                    this.instance.getChannelHandler().privateMessageOnChannel(this, ChatEntity.SERVER, issuer, new TextComponentString("You removed " + player.getUsername() + " from the whitelist."));
                else
                    this.instance.getChannelHandler().privateMessageOnChannel(this, ChatEntity.SERVER, issuer, new TextComponentString("You unbanned " + player.getUsername() + " from the channel."));

                if(this.isWhitelisted() && this.joined.contains(player))
                    this.instance.getChannelHandler().leaveChannel(player, this);
            }
            else if(this.isWhitelisted())
            {
                this.instance.getChannelHandler().privateMessageOnChannel(this, ChatEntity.SERVER, issuer, new TextComponentString("You haven't added " + player.getUsername() + " to the whitelist!"));
            }
            else
            {
                this.instance.getChannelHandler().privateMessageOnChannel(this, ChatEntity.SERVER, issuer, new TextComponentString("You haven't banned " + player.getUsername() + " from the channel!"));
            }
        }
        else
        {
            if(!this.banned.contains(player))
            {
                this.banned.add(player);

                if(this.isWhitelisted())
                    this.instance.getChannelHandler().broadcastOnChannel(this, ChatEntity.SERVER, new TextComponentString(player + " was added to the whitelist by " + issuer));
                else
                    this.instance.getChannelHandler().broadcastOnChannel(this, ChatEntity.SERVER, new TextComponentString(player + " was banned from the channel by " + issuer));

                if(!this.isWhitelisted() && this.joined.contains(player))
                    this.instance.getChannelHandler().leaveChannel(player, this);
            }
            else if(this.isWhitelisted())
            {
                this.instance.getChannelHandler().privateMessageOnChannel(this, ChatEntity.SERVER, issuer, new TextComponentString("You already added " + player.getUsername() + " to the whitelist!"));
            }
            else
            {
                this.instance.getChannelHandler().privateMessageOnChannel(this, ChatEntity.SERVER, issuer, new TextComponentString("You already banned " + player.getUsername() + " from the channel!"));
            }
        }
    }

    @Override
    public void write(JsonObject obj)
    {
        JsonArray muted = new JsonArray();

        for (ChatEntity aMuted : this.muted)
            muted.add(new JsonPrimitive(aMuted.getUsername()));

        JsonArray banned = new JsonArray();

        for (ChatEntity aBanned : this.banned)
            banned.add(new JsonPrimitive(aBanned.getUsername()));

        obj.add("muted", muted);
        obj.add("banned", banned);
    }

    @Override
    public void read(JsonObject obj)
    {
        if(obj.has("muted"))
        {
            JsonArray muted = obj.getAsJsonArray("muted");

            for(int i = 0; i < muted.size(); i++)
                this.muted.add(new ChatEntity(muted.get(i).getAsString()));
        }

        if(obj.has("banned"))
        {
            JsonArray banned = obj.getAsJsonArray("banned");

            for(int i = 0; i < banned.size(); i++)
                this.banned.add(new ChatEntity(banned.get(i).getAsString()));
        }
    }

    @Override
    public Collection<ChatEntity> getMembers()
    {
        return this.joined;
    }

    @Override
    public boolean autoJoin(ChatEntity player)
    {
        return true;
    }

    @Override
    public boolean isOnChannel(ChatEntity player)
    {
        return this.joined.contains(player);
    }

    @Override
    public boolean isMuted(ChatEntity player)
    {
        return this.muted.contains(player);
    }

    @Override
    public boolean isWhitelisted()
    {
        return false;
    }

    @Override
    public boolean canReceiveChat(ChatEntity sender, ChatEntity receiver, ITextComponent message)
    {
        return true;
    }

    @Override
    public boolean canJoin(ChatEntity player)
    {
        return !this.banned.contains(player);
    }
}
