package moe.nightfall.vic.chat.tracks;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public abstract class TrackElement
{
    protected final Track parent;

    private TrackElement(Track parent)
    {
        this.parent = parent;
    }

    public abstract void execute();

    public void reset() {}

    public static class ElementPause extends TrackElement
    {
        private final int duration;

        public ElementPause(Track parent, int duration)
        {
            super(parent);

            this.duration = duration;
        }

        @Override
        public void execute()
        {
            this.parent.wait(this.duration);
        }
    }

    public static class ElementNote extends TrackElement
    {
        private int times, curTimes;
        private float pitch;
        private String sound;

        public ElementNote(Track parent, int duration, float pitch, String sound)
        {
            super(parent);

            this.times = duration;
            this.curTimes = duration;
            this.pitch = pitch;
            this.sound = sound;
        }

        @Override
        public void execute()
        {
            if(this.curTimes > 0)
            {
                this.curTimes--;
                this.parent.decreasePointer();
            }
            else
            {
                this.curTimes = this.times;
            }

            for(EntityPlayerMP player : this.parent.getPlayers())
                player.connection.sendPacket(new SPacketSoundEffect(SoundEvent.REGISTRY.getObject(new ResourceLocation(this.sound)), SoundCategory.MUSIC, player.posX, player.posY, player.posZ, 1F, this.pitch));
        }
    }

    public static class ElementRepeat extends TrackElement
    {
        private int times, curTimes, pointer;

        public ElementRepeat(Track parent, int duration, int pointer)
        {
            super(parent);

            this.times = duration;
            this.curTimes = duration;
            this.pointer = pointer;
        }

        @Override
        public void execute()
        {
            if(curTimes > 0)
            {
                this.curTimes--;
                this.parent.jumpToElement(this.pointer);
            }
            else
            {
                this.curTimes = this.times;
            }
        }

        @Override
        public void reset()
        {
            this.curTimes = this.times;
        }
    }
}
