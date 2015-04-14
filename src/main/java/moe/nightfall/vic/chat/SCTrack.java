package moe.nightfall.vic.chat;

public class SCTrack 
{
	private SCUser user;
	private Integer duration;
	private String description;
	private String title;
	private Integer playback_count;
	
	public String getDescription()
	{
		return description;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public SCUser getUser()
	{
		return user;
	}
	
	public Integer getDuration()
	{
		return duration;
	}
	
	public Integer getPlaybackCount()
	{
		return playback_count;
	}
}