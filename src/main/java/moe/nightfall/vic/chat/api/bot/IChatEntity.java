package moe.nightfall.vic.chat.api.bot;

public interface IChatEntity 
{
    String getUsername();

    String getNickname();

    String getDisplayName();

    boolean isServer();

    boolean isBot();

    /** Will return true for opped players, bots and the server entity. **/
    boolean isOperator();
}
