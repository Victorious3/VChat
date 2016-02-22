package moe.nightfall.vic.chat.integrations.youtube;

public class YoutubeVideo
{
    private final String title;
    private final String channelTitle;
    private final String duration;

    public YoutubeVideo(String title, String channelTitle, String duration)
    {
        this.title = title;
        this.channelTitle = channelTitle;
        this.duration = duration;
    }

    public String getTitle()
    {
        return this.title;
    }

    public String getChannelTitle()
    {
        return this.channelTitle;
    }

    public String getDuration()
    {
        return this.duration;
    }
}
