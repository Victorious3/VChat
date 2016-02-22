package moe.nightfall.vic.chat.commands;

import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.Misc;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

public class CommandTop extends CommandOverrideAccess
{
    @Override
    public String getCommandName()
    {
        return "top";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return Config.topPermissionLevel;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/top";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        Runtime runtime = Runtime.getRuntime();
        int proc = runtime.availableProcessors();
        long uptime = (long) MinecraftServer.getServer().getTickCounter() * 50;
        long sysmem = Misc.getDeviceMemory() / (1024 * 1024);
        long freemem = runtime.totalMemory() - runtime.freeMemory();
        long maxmem = runtime.maxMemory();
        float usedmem = (float) (freemem / (double) maxmem);
        freemem /= (1024 * 1024);
        maxmem /= (1024 * 1024);
        float cpuload = (float) Misc.getCPULoad();

        int day = (int) TimeUnit.MILLISECONDS.toHours(uptime) / 24;
        int hrs = (int) TimeUnit.MILLISECONDS.toHours(uptime) % 24;
        int min = (int) TimeUnit.MILLISECONDS.toMinutes(uptime) % 60;
        int sec = (int) TimeUnit.MILLISECONDS.toSeconds(uptime) % 60;

        ChatComponentText uptComp = new ChatComponentText(String.format("Uptime: %d:%02d:%02d:%02d", day, hrs, min, sec));
        ChatComponentText aprComp = new ChatComponentText(String.format("Available processors: %d", proc));
        ChatComponentText sysmemComp = new ChatComponentText(String.format("System memory: %d MB", sysmem));

        ChatComponentText memComp = new ChatComponentText("RAM: ");
        memComp.appendSibling(createBar(usedmem, freemem + "/" + maxmem + "MB", 50));

        ChatComponentText cpuComp = new ChatComponentText("CPU: ");
        cpuComp.appendSibling(createBar(cpuload, (int)(cpuload * 100) + "%", 50));

        sender.addChatMessage(uptComp);
        sender.addChatMessage(aprComp);
        sender.addChatMessage(sysmemComp);
        sender.addChatMessage(memComp);
        sender.addChatMessage(cpuComp);
    }

    private static ChatComponentText createBar(float value, String inlay, int maxLength)
    {
        value = MathHelper.clamp_float(value, 0, 1);
        maxLength -= 2;

        int length = (int) (value * maxLength);

        String out = StringUtils.repeat('#', MathHelper.clamp_int(length, 0, maxLength - inlay.length()));
        out += StringUtils.repeat('_', MathHelper.clamp_int(maxLength - length - inlay.length(), 0, maxLength - inlay.length()));

        if(length > maxLength - inlay.length())
        {
            int off = inlay.length() - (maxLength - length);
            out += inlay.substring(0, off);
            inlay = inlay.substring(off, inlay.length());
        }

        length = out.length();
        out += StringUtils.repeat('_', maxLength - out.length());

        ChatComponentText comp = new ChatComponentText("[");

        int i1 = MathHelper.clamp_int(maxLength / 3, 0, length);
        ChatComponentText c1 = new ChatComponentText(out.substring(0, i1));
        int i2 = MathHelper.clamp_int(i1 + maxLength / 3, 0, length);
        ChatComponentText c2 = new ChatComponentText(out.substring(i1, i2));
        ChatComponentText c3 = new ChatComponentText(out.substring(i2, length));

        c1.getChatStyle().setColor(EnumChatFormatting.GREEN);
        c2.getChatStyle().setColor(EnumChatFormatting.YELLOW);
        c3.getChatStyle().setColor(EnumChatFormatting.RED);

        comp.appendSibling(c1);
        comp.appendSibling(c2);
        comp.appendSibling(c3);
        comp.appendText(inlay + "]");

        return comp;
    }
}
