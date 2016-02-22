package moe.nightfall.vic.chat.integrations.soundcloud;

public class SoundCloudTrack
{
    private SoundCloudUser user;
    private Integer duration;
    private String description;
    private String title;
    private Integer playback_count;

    public String getDescription()
    {
        return this.description;
    }

    public String getTitle()
    {
        return this.title;
    }

    public SoundCloudUser getUser()
    {
        return this.user;
    }

    public Integer getDuration()
    {
        return this.duration;
    }

    public Integer getPlaybackCount()
    {
        return this.playback_count;
    }
}