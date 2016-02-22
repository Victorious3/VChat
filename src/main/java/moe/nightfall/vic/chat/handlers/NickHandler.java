package moe.nightfall.vic.chat.handlers;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.common.collect.HashBiMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.VChat;
import moe.nightfall.vic.chat.commands.CommandNick;
import net.minecraft.command.server.CommandMessage;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NickHandler extends ChatHandler
{
    private final HashBiMap<String, String> nickRegistry;
    private final File nickFile;

    public NickHandler(VChat instance)
    {
        super(instance);

        this.nickRegistry = HashBiMap.create(new HashMap<String, String>());
        this.nickFile = new File("vChat/vchat_nicks.json");
    }

    public void loadNicks()
    {
        try
        {
            if(this.nickFile.exists())
            {
                JsonParser parser = new JsonParser();
                JsonArray nicks = (JsonArray)parser.parse(new JsonReader(new FileReader(this.nickFile)));

                for(int i = 0; i < nicks.size(); i++)
                {
                    JsonObject obj = (JsonObject)nicks.get(i);
                    this.nickRegistry.put(obj.get("username").getAsString(), obj.get("nickname").getAsString());
                }
            }
        }
        catch (Exception e)
        {
            this.instance.getLogger().error("Could not read the nick file. Maybe it's disrupted or the access is restricted. Try deleting it.");
            e.printStackTrace();
        }
    }

    public void saveNicks()
    {
        try
        {
            if(!this.nickFile.exists())
            {
                this.nickFile.getParentFile().mkdirs();
                this.nickFile.createNewFile();
            }

            JsonArray nickArray = new JsonArray();

            for(Entry<String, String> entry : this.nickRegistry.entrySet())
            {
                JsonObject obj = new JsonObject();
                obj.addProperty("username", entry.getKey());
                obj.addProperty("nickname", entry.getValue());
                nickArray.add(obj);
            }

            FileWriter writer = new FileWriter(this.nickFile);
            writer.write(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(nickArray));
            writer.flush();
            writer.close();
        }
        catch (Exception e)
        {
            this.instance.getLogger().error("Could not save the nick file. Maybe it's disrupted or the access is restricted. Try deleting it.");
            e.printStackTrace();
        }
    }

    @Override
    public void onServerLoad(FMLServerStartingEvent event)
    {
        this.nickRegistry.clear();
        this.loadNicks();

        event.registerServerCommand(new CommandNick(this));
    }

    @Override
    public void onServerUnload(FMLServerStoppingEvent event)
    {
        this.saveNicks();
    }

    @SubscribeEvent
    public void onPlayerNameFormatting(PlayerEvent.NameFormat event)
    {
        if(this.nickRegistry.containsKey(event.username))
            event.displayname = this.nickRegistry.get(event.username);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onCommand(CommandEvent event)
    {
        if(event.command instanceof CommandMessage && event.parameters.length > 0)
        {
            ChatEntity entity = new ChatEntity((Object) event.parameters[0]);

            if(entity.getUsername() != null)
                event.parameters[0] = entity.getUsername();
        }
    }

    public HashBiMap<String, String> getNickRegistry()
    {
        return this.nickRegistry;
    }

    public String getPlayerFromNick(String nick)
    {
        if(this.nickRegistry.containsValue(nick))
            return this.nickRegistry.inverse().get(nick);

        return null;
    }
}
