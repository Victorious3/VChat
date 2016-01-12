package moe.nightfall.vic.chat.handler;

import java.util.HashMap;

import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.Misc;
import moe.nightfall.vic.chat.VChat;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutoAFKHandler extends ChatHandlerImpl
{
	private HashMap<String, TrackedPosition> tracked = new HashMap<String, TrackedPosition>();

	public AutoAFKHandler()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	long lastAction = System.currentTimeMillis();
	
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event)
	{
		long time = System.currentTimeMillis();
		if(time - lastAction >= 1000)
		{
			for(TrackedPosition pos : tracked.values())
			{
				pos.update();
				ChatEntity entity = new ChatEntity(pos.getPlayer());
				
				if(pos.hitCooldown() && !VChat.afkHandler.isAfk(entity))
				{
					VChat.afkHandler.setAfk(entity, "Auto AFK");
					pos.autoAfk = true;
				}
				if(!pos.hitCooldown() && pos.autoAfk)
				{
					VChat.afkHandler.removeAfk(entity);
					pos.autoAfk = false;
				}
			}
			lastAction = time;
		}
	}
	
	/** Callback from the /afk command to remove possible auto afks **/
	public void onAFKRemoved(EntityPlayerMP entity)
	{
		updatePlayer(entity);
	}
	
	@SubscribeEvent()
	public void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event)
	{
		updatePlayer((EntityPlayerMP)event.player);
	}
	
	@SubscribeEvent()
	public void onPlayerLeft(PlayerEvent.PlayerLoggedOutEvent event)
	{
		tracked.remove(event.player.getName());
	}

	@Override
	public void onServerLoad(FMLServerStartingEvent event) 
	{
		tracked.clear();
	}

	public void updatePlayer(EntityPlayerMP player)
	{
		tracked.put(player.getName(), new TrackedPosition(player));
	}

	public void updatePlayer(String username)
	{
		updatePlayer(Misc.getPlayer(username));
	}
	
	public static class TrackedPosition
	{
		private boolean autoAfk;
		private int cooldown = Config.autoAfkTime;
		private Vec3 position;
		private String playerName;
		
		public TrackedPosition(EntityPlayerMP player)
		{
			this.playerName = player.getName();
			position = new Vec3(player.posX, player.posY, player.posZ);
		}
		
		public void update()
		{
			EntityPlayerMP player = getPlayer();
			Vec3 npos = new Vec3(player.posX, player.posY, player.posZ);

			if(position.xCoord != npos.xCoord || position.yCoord != npos.yCoord || position.zCoord != npos.zCoord) 
				cooldown = Config.autoAfkTime;
			else cooldown--;
			if(cooldown < 0) cooldown = 0;
			
			this.position = npos;
		}
		
		public boolean hitCooldown()
		{
			return cooldown == 0;
		}

		public EntityPlayerMP getPlayer()
		{
			return Misc.getPlayer(playerName);
		}
	}
}
