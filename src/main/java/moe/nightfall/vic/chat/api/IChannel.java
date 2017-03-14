package moe.nightfall.vic.chat.api;

import java.util.Collection;

import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.api.bot.IChannelBase;

import com.google.gson.JsonObject;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public interface IChannel extends IChannelBase
{
    ITextComponent formatChat(ChatEntity sender, ChatEntity receiver, ITextComponent message);

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

    TextFormatting getColor();

    Collection<ChatEntity> getMembers();

    boolean autoJoin(ChatEntity player);

    boolean isOnChannel(ChatEntity player);

    boolean isMuted(ChatEntity player);

    @Override
    boolean isWhitelisted();

    boolean canReceiveChat(ChatEntity sender, ChatEntity receiver, ITextComponent message);

    boolean canJoin(ChatEntity player);
}
