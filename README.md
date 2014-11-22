[![Build Status](http://img.shields.io/jenkins/s/http/jenkins.rx14.co.uk/VChat.svg?style=flat-square)](http://jenkins.rx14.co.uk/job/VChat/)
VChat
=====

A server side mod that makes the chat more reliable. Have a look here for further explanation:
[MCForums Thread](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/wip-mods/2182328-vchat-v0-1r13-a-server-side-mod-that-makes-the)

How to create a custom Bot
--------------------------

Follow these few simple steps to create a custom bot:

- Create a new, empty, workspace. (Yes, you don't need the Minecraft source in order to create a bot, if you want extra functionality, it can help though.)
- Download the API file from the releases section or downlad the whole repo and extract the folder `src/main/java/vic/mod/chat/api/bot/` and add it to your build path. It contains everything you need.
- Create a new Class that implements "IChatBot" and fill in the gaps. You can get the IBotHandler from the `onLoad(IBotHandler handler)` method, it's used to interact with the server.
- For a really simple example on how to use the bot, have a look here: [Test Bot](https://gist.github.com/Victorious3/85a0fd97e78906886baf)
- When you are done creating your bot, build the project as an executable jar file. You can exclude the API there, but it's not necessary.
- Put your finished jar file into the folder `\vBots` or create one if there is none. (Minecraft root directory)

If you did everything right, you should get something like that:
```
[11:55:58] [Client thread/INFO] [vchat]: Attempting to load bots...
[11:55:58] [Client thread/INFO] [vchat]: Attempting to load bots from file "TestBot.jar"...
[11:55:58] [Client thread/INFO] [vchat]: Bot "TestBot" was successfully loaded and is ready for use!
[11:55:58] [Client thread/INFO] [vchat]: ...done! A total of 1 bots loaded in 12 ms
```
Your bot should respod to incoming messages now, have fun!

Jenkins: [jenkins.rx14.co.uk](http://jenkins.rx14.co.uk)
*generously served by @RX14*
