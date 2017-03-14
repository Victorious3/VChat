package moe.nightfall.vic.chat.api;

import net.minecraft.util.text.TextComponentString;

public interface IChatFormatter 
{
    void apply(TextComponentString text);

    boolean isMatched();
}
