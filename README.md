[![Build Status](http://img.shields.io/jenkins/s/http/jenkins.rx14.co.uk/job/Vic/VChat.svg?style=flat-square)](http://jenkins.rx14.co.uk/job/Vic/job/VChat/)
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

How to enable the Twitter links integration
--------------------------

You need to create an application into the Twitter Developer Center to enable this. Why? Just because there is a request quota into the Twitter API. So we cannot share the same application and you have to create your own.

- Go to the Twitter Application Development Center: [Twitter Application Development Center](https://apps.twitter.com/).
- Click "Create New App".
- Fill the blanks with a name, a description and a website. ("https://github.com/Victorious3/VChat" for example)
- After the filling phase, send the form and you'll be redirected to your application "dashboard".
- Select the "Keys and Access Tokens" tab, and you'll see your "Consumer Key" and "Consumer Secret" to put into the mod configuration file.
- It's almost finished! Now scroll a little down and click the button to create an access token. After that, you'll se your "Access Token" and "Access Token Secret" to put into the mod configuration file!

Jenkins: [jenkins.rx14.co.uk](http://jenkins.rx14.co.uk)
*generously served by @RX14*
