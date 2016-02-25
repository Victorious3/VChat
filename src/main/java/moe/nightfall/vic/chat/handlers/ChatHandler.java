package moe.nightfall.vic.chat.handlers;

import moe.nightfall.vic.chat.VChat;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

public class ChatHandler
{
    protected final VChat instance;

    public ChatHandler(VChat instance)
    {
        this.instance = instance;
        this.instance.registerChatHandler(this);
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
    }

    public void onServerLoad(FMLServerStartingEvent event)
    {
    }

    public void onServerUnload(FMLServerStoppingEvent event)
    {
    }
}
