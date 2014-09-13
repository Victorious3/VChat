package vic.mod.chat.handler;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;
import vic.mod.chat.ChatEntity;
import vic.mod.chat.Config;
import vic.mod.chat.VChat;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class AutoAFKHandler extends ChatHandlerImpl
{	
	private HashMap<EntityPlayerMP, TrackedPosition> tracked = new HashMap<EntityPlayerMP, TrackedPosition>();
	
	public AutoAFKHandler()
	{
		FMLCommonHandler.instance().bus().register(this);
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
				ChatEntity entity = new ChatEntity(pos.player);
				
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
		tracked.put(entity, new TrackedPosition(entity));
	}
	
	@SubscribeEvent()
	public void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event)
	{
		tracked.put((EntityPlayerMP)event.player, new TrackedPosition((EntityPlayerMP)event.player));
	}
	
	@SubscribeEvent()
	public void onPlayerLeft(PlayerEvent.PlayerLoggedOutEvent event)
	{
		tracked.remove(event.player);
	}
	
	@Override 
	public void onServerLoad(FMLServerStartingEvent event) 
	{
		tracked.clear();
	}
	
	public static class TrackedPosition
	{
		private boolean autoAfk;
		private int cooldown = Config.autoAfkTime;
		private Vec3 position;
		private EntityPlayerMP player;
		
		public TrackedPosition(EntityPlayerMP player)
		{
			this.player = player;
			position = Vec3.createVectorHelper(player.posX, player.posY, player.posZ);
		}
		
		public void update()
		{
			Vec3 npos = Vec3.createVectorHelper(player.posX, player.posY, player.posZ);

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
	}
}
