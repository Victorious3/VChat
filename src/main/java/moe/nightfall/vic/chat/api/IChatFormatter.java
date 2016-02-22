package moe.nightfall.vic.chat.api;

import net.minecraft.util.ChatComponentText;

public interface IChatFormatter 
{
    void apply(ChatComponentText text);

    boolean isMatched();
}
