package vic.mod.chat;

import vic.mod.chat.api.bot.IBotHandler;
import vic.mod.chat.api.bot.IChannelBase;
import vic.mod.chat.api.bot.IChatBot;
import vic.mod.chat.api.bot.IChatEntity;
import vic.mod.chat.api.bot.LogLevel;
import vic.mod.chat.api.bot.Version;

@Version(version = "1.2")
public class TestBot implements IChatBot
{

	@Override
	public void onLoad(IBotHandler handler) {
		// TODO Auto-generated method stub
		handler.log(LogLevel.FATAL, "ERR÷÷÷÷R");
	}

	@Override
	public void onServerLoad() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onServerUnload() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return "SPECIAL TEST BOT";
	}

	@Override
	public void onMessage(String message, IChatEntity sender,
			IChannelBase channel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPrivateMessage(String message, IChatEntity sender) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCommandMessage(String command, String[] args, String message) {
		// TODO Auto-generated method stub
		
	}

}
