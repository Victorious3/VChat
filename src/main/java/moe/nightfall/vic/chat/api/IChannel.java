package moe.nightfall.vic.chat.api;

import java.util.Collection;

import moe.nightfall.vic.chat.ChatEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import moe.nightfall.vic.chat.api.bot.IChannelBase;

import com.google.gson.JsonObject;

public interface IChannel extends IChannelBase
{
    IChatComponent formatChat(ChatEntity sender, ChatEntity receiver, IChatComponent message);

    @Override
    String getPrefix();

    @Override
    String getName();

    void onJoin(ChatEntity player, boolean initial);

    void onLeave(ChatEntity player, boolean initial);

    void mute(ChatEntity issuer, ChatEntity player, boolean unmute);

    void kick(ChatEntity issuer, ChatEntity player);

    void ban(ChatEntity issuer, ChatEntity player, boolean unban);

    void write(JsonObject obj);

    void read(JsonObject obj);

    EnumChatFormatting getColor();

    Collection<ChatEntity> getMembers();

    boolean autoJoin(ChatEntity player);

    boolean isOnChannel(ChatEntity player);

    boolean isMuted(ChatEntity player);

    @Override
    boolean isWhitelisted();

    boolean canReceiveChat(ChatEntity sender, ChatEntity receiver, IChatComponent message);

    boolean canJoin(ChatEntity player);
}
