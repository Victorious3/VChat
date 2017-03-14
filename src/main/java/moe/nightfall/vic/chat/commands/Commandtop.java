package moe.nightfall.vic.chat.commands;

import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.Misc;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

public class CommandTop extends CommandOverrideAccess
{
    @Override
    public String getName()
    {
        return "top";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return Config.topPermissionLevel;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/top";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        Runtime runtime = Runtime.getRuntime();
        int proc = runtime.availableProcessors();
        long uptime = (long) server.getTickCounter() * 50;
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

        TextComponentString uptComp = new TextComponentString(String.format("Uptime: %d:%02d:%02d:%02d", day, hrs, min, sec));
        TextComponentString aprComp = new TextComponentString(String.format("Available processors: %d", proc));
        TextComponentString sysmemComp = new TextComponentString(String.format("System memory: %d MB", sysmem));

        TextComponentString memComp = new TextComponentString("RAM: ");
        memComp.appendSibling(createBar(usedmem, freemem + "/" + maxmem + "MB", 50));

        TextComponentString cpuComp = new TextComponentString("CPU: ");
        cpuComp.appendSibling(createBar(cpuload, (int)(cpuload * 100) + "%", 50));

        sender.sendMessage(uptComp);
        sender.sendMessage(aprComp);
        sender.sendMessage(sysmemComp);
        sender.sendMessage(memComp);
        sender.sendMessage(cpuComp);
    }

    private static TextComponentString createBar(float value, String inlay, int maxLength)
    {
        value = MathHelper.clamp(value, 0, 1);
        maxLength -= 2;

        int length = (int) (value * maxLength);

        String out = StringUtils.repeat('#', MathHelper.clamp(length, 0, maxLength - inlay.length()));
        out += StringUtils.repeat('_', MathHelper.clamp(maxLength - length - inlay.length(), 0, maxLength - inlay.length()));

        if(length > maxLength - inlay.length())
        {
            int off = inlay.length() - (maxLength - length);
            out += inlay.substring(0, off);
            inlay = inlay.substring(off, inlay.length());
        }

        length = out.length();
        out += StringUtils.repeat('_', maxLength - out.length());

        TextComponentString comp = new TextComponentString("[");

        int i1 = MathHelper.clamp(maxLength / 3, 0, length);
        TextComponentString c1 = new TextComponentString(out.substring(0, i1));
        int i2 = MathHelper.clamp(i1 + maxLength / 3, 0, length);
        TextComponentString c2 = new TextComponentString(out.substring(i1, i2));
        TextComponentString c3 = new TextComponentString(out.substring(i2, length));

        c1.getStyle().setColor(TextFormatting.GREEN);
        c2.getStyle().setColor(TextFormatting.YELLOW);
        c3.getStyle().setColor(TextFormatting.RED);

        comp.appendSibling(c1);
        comp.appendSibling(c2);
        comp.appendSibling(c3);
        comp.appendText(inlay + "]");

        return comp;
    }
}
