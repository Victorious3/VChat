package moe.nightfall.vic.chat.handlers;

import java.util.HashMap;

import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.util.Misc;
import moe.nightfall.vic.chat.VChat;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.Vec3;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class AutoAFKHandler extends ChatHandler
{
    private final HashMap<String, TrackedPosition> tracked;
    private long lastAction;

    public AutoAFKHandler(VChat instance)
    {
        super(instance);

        this.tracked = new HashMap<String, TrackedPosition>();
        this.lastAction = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        long time = System.currentTimeMillis();

        if(time - this.lastAction >= 1000)
        {
            for(TrackedPosition pos : this.tracked.values())
            {
                pos.update();
                ChatEntity entity = new ChatEntity(pos.getPlayer());

                if(pos.hitCooldown() && !VChat.instance.getAfkHandler().isAFK(entity))
                {
                    this.instance.getAfkHandler().setAFK(entity, "Auto AFK");
                    pos.autoAfk = true;
                }
                if(!pos.hitCooldown() && pos.autoAfk)
                {
                    this.instance.getAfkHandler().removeAFK(entity);
                    pos.autoAfk = false;
                }
            }

            this.lastAction = time;
        }
    }

    @Override
    public void onServerLoad(FMLServerStartingEvent event)
    {
        this.tracked.clear();
    }

    @SubscribeEvent
    public void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event)
    {
        this.updatePlayer((EntityPlayerMP) event.player);
    }

    @SubscribeEvent
    public void onPlayerLeft(PlayerEvent.PlayerLoggedOutEvent event)
    {
        this.tracked.remove(event.player.getCommandSenderName());
    }

    /** Callback from the /afk command to remove possible auto afks **/
    public void onAFKRemoved(EntityPlayerMP entity)
    {
        this.updatePlayer(entity);
    }

    public void updatePlayer(EntityPlayerMP player)
    {
        this.tracked.put(player.getCommandSenderName(), new TrackedPosition(player));
    }

    public static class TrackedPosition
    {
        private boolean autoAfk;
        private int cooldown = Config.autoAfkTime;
        private Vec3 position;
        private String playerName;

        public TrackedPosition(EntityPlayerMP player)
        {
            this.playerName = player.getCommandSenderName();
            this.position = Vec3.createVectorHelper(player.posX, player.posY, player.posZ);
        }

        public void update()
        {
            EntityPlayerMP player = getPlayer();
            Vec3 npos = Vec3.createVectorHelper(player.posX, player.posY, player.posZ);

            if(this.position.xCoord != npos.xCoord || this.position.yCoord != npos.yCoord || this.position.zCoord != npos.zCoord)
                this.cooldown = Config.autoAfkTime;
            else
                this.cooldown--;

            if(this.cooldown < 0)
                this.cooldown = 0;

            this.position = npos;
        }

        public EntityPlayerMP getPlayer()
        {
            return Misc.getPlayer(this.playerName);
        }

        public boolean hitCooldown()
        {
            return this.cooldown == 0;
        }
    }
}
