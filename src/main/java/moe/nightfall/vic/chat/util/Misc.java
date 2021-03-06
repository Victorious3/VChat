package moe.nightfall.vic.chat.util;

import moe.nightfall.vic.chat.ChatEntity;
import moe.nightfall.vic.chat.Config;
import moe.nightfall.vic.chat.VChat;
import moe.nightfall.vic.chat.bots.BotHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Misc
{
    public static final Pattern splitPattern = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
    public static SSLContext SSL_CONTEXT = null;
    public static final HostnameVerifier HOSTNAME_VERIFIER = new HostnameVerifier()
    {
        public boolean verify(String hostname, SSLSession session)
        {
            return true;
        }
    };
    private static final Pattern TITLE_TAG = Pattern.compile("\\<title>(.*)\\</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    private static Method getSystemCpuLpad;
    private static Long deviceMemory;

    private Misc()
    {

    }

    private static SSLSocketFactory factory;

    static
    {
        try
        {
            getSystemCpuLpad = osBean.getClass().getDeclaredMethod("getSystemCpuLoad");
            getSystemCpuLpad.setAccessible(true);

            Method memSize = osBean.getClass().getDeclaredMethod("getTotalPhysicalMemorySize");
            memSize.setAccessible(true);

            deviceMemory = (Long) memSize.invoke(osBean);

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager()
                    {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws CertificateException
                        {

                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws CertificateException
                        {

                        }

                        public java.security.cert.X509Certificate[] getAcceptedIssuers()
                        {
                            return null;
                        }

                    }
            };
            SSL_CONTEXT = SSLContext.getInstance("SSL");
            SSL_CONTEXT.init(null, trustAllCerts, new java.security.SecureRandom());
            factory = new SSLSocketFactoryBridge(SSL_CONTEXT.getSocketFactory());
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static String[] parseModt(String modt, EntityPlayerMP player)
    {
        if(modt.contains("%NAME%")) modt = modt.replaceAll("%NAME%", player.getDisplayNameString());
        if(modt.contains("%TIME%")) modt = modt.replaceAll("%TIME%", new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime()));
        if(modt.contains("%ONLINE%")) modt = modt.replaceAll("%ONLINE%", String.valueOf(player.mcServer.getCurrentPlayerCount()));
        if(modt.contains("%ONLINE_MAX%")) modt = modt.replaceAll("%ONLINE_MAX%", String.valueOf(player.mcServer.getMaxPlayers()));
        if(modt.contains("%DIM%")) modt = modt.replaceAll("%DIM%", String.valueOf(player.getEntityWorld().provider.getDimension()));
        if(modt.contains("%DIM_NAME%")) modt = modt.replaceAll("%DIM_NAME%", player.getEntityWorld().provider.getDimensionType().getName());
        if(modt.contains("%MODT%")) modt = modt.replaceAll("%MODT%", player.mcServer.getMOTD());

        return modt.split("/n");
    }

    public static String[] parseArgs(String[] args)
    {
        List<String> list = new ArrayList<String>();
        Matcher m = splitPattern.matcher(StringUtils.join(Arrays.asList(args), " "));

        while (m.find())
            list.add(m.group(1).replaceAll("\"", ""));

        return list.toArray(new String[list.size()]);
    }

    public static boolean checkPermission(ICommandSender sender, int permlevel)
    {
        if(sender.canUseCommand(permlevel, null)) return true;
        TextComponentTranslation component = new TextComponentTranslation("commands.generic.permission", new Object[0]);
        component.getStyle().setColor(TextFormatting.RED);
        sender.sendMessage(component);
        return false;
    }

    public static String getDuration(long duration)
    {
        long days = duration / (1000 * 60 * 60 * 24);
        duration = duration % (1000 * 60 * 60 * 24);

        long hours = duration / (1000 * 60 * 60);
        duration = duration % (1000 * 60 * 60);

        long minutes = duration / (1000 * 60);

        return (days > 0 ? days + " day " : "") + (hours > 0 ? hours + " hrs " : "") + minutes + " min";
    }

    public static EntityPlayerMP getPlayer(String player)
    {
        for (EntityPlayerMP entity : VChat.instance.getServer().getPlayerList().getPlayers())
            if (entity.getName().equalsIgnoreCase(player))
                return entity;

        return null;
    }

    public static TextComponentString getComponent(ChatEntity entity)
    {
        String nick = entity.getNickname();

        if(entity.isBot())
        {
            BotHandler handler = VChat.instance.getBotLoader().getBot(entity.getUsername());
            nick = handler.getOwningBot().getDisplayName();
        }

        TextComponentString nameComponent = new TextComponentString(nick != null ? nick : entity.getUsername());

        if(nick != null)
        {
            if(Config.afkEnabled && VChat.instance.getAfkHandler().isAFK(entity))
            {
                nameComponent.getStyle().setColor(TextFormatting.GRAY);
                nameComponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(entity.getUsername() + " (AFK - " + VChat.instance.getAfkHandler().getReason(entity) + ")")));
            }
            else
            {
                nameComponent.getStyle().setColor(entity.isBot() ? Config.colorBot : Config.colorNickName);
                nameComponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(entity.getUsername())));
                nameComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + entity.getUsername()));
            }
        }
        else
        {
            nameComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + entity.getUsername()));
        }

        return nameComponent;
    }

    public static double getCPULoad()
    {
        try
        {
            double d = (Double) getSystemCpuLpad.invoke(osBean);
            return d < 0 ? 0.0D : d;
        }
        catch (Exception ignored) {}

        return -1.0D;
    }

    public static long getDeviceMemory()
    {
        return deviceMemory != null ? deviceMemory : -1;
    }

    public static HashMap<String, String> getQueryMap(URL url)
    {
        if (url.getQuery() == null)
            return new HashMap<String, String>();

        String[] params = url.getQuery().split("&");
        HashMap<String, String> map = new HashMap<String, String>();

        for (String param : params)
        {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }

        return map;
    }

    public static String getPageTitle(URL url)
    {
        try
        {
            Scanner scanner = new Scanner(url.openStream());
            String content = "";

            while (scanner.hasNext())
                content += scanner.nextLine();

            scanner.close();

            String tagOpen = "<title>";
            String tagClose = "</title>";

            int begin = content.indexOf(tagOpen) + tagOpen.length();
            int end = content.indexOf(tagClose);

            return content.substring(begin, end);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static ArrayList<ChatEntity> getOnlinePlayersAsEntity()
    {
        ArrayList<ChatEntity> list = new ArrayList<ChatEntity>();

        for(EntityPlayerMP player : VChat.instance.getServer().getPlayerList().getPlayers())
            list.add(new ChatEntity(player));

        return list;
    }

    public static ArrayList<EntityPlayerMP> getOnlinePlayers()
    {
        return (ArrayList<EntityPlayerMP>) VChat.instance.getServer().getPlayerList().getPlayers();
    }

    public static String getPageTitle(String url) throws IOException
    {
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setConnectTimeout(2000);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Language", "en-US");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11");

        // Disable certificates checks
        if (conn instanceof HttpsURLConnection)
        {
            ((HttpsURLConnection) conn).setSSLSocketFactory(factory);
            ((HttpsURLConnection) conn).setHostnameVerifier(HOSTNAME_VERIFIER);
        }
        try
        {
            int httpCode = conn.getResponseCode();
            if (httpCode == 301 || httpCode == 302)
                return getPageTitle(conn.getHeaderField("Location"));
        } catch (IOException e)
        {
            // silence error code
        }
        InputStream stream = conn.getErrorStream();
        if (stream == null) stream = conn.getInputStream();

        ContentType contentType = getContentTypeHeader(conn);
        Charset charset = getCharset(contentType);
        if (charset == null)
            charset = Charset.defaultCharset();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset));
        int n, totalRead = 0;
        char[] buf = new char[1024];
        StringBuilder content = new StringBuilder();

        while (totalRead < 655360 && (n = reader.read(buf, 0, buf.length)) != -1)
        {
            content.append(buf, 0, n);
            totalRead += n;
        }
        reader.close();
        Matcher matcher = TITLE_TAG.matcher(content);
        if (matcher.find())
            return matcher.group(1).replaceAll("[\\s\\<>]+", " ").trim();
        return null;
    }

    public static String getDomainName(String url) throws URISyntaxException
    {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    private static ContentType getContentTypeHeader(URLConnection conn)
    {
        int i = 0;
        boolean moreHeaders = true;
        do
        {
            String headerName = conn.getHeaderFieldKey(i);
            String headerValue = conn.getHeaderField(i);
            if (headerName != null && headerName.equals("Content-Type"))
                return new ContentType(headerValue);

            i++;
            moreHeaders = headerName != null || headerValue != null;
        }
        while (moreHeaders);

        return new ContentType("text/html");
    }

    private static Charset getCharset(ContentType contentType)
    {
        if (contentType != null && contentType.charsetName != null && Charset.isSupported(contentType.charsetName))
            return Charset.forName(contentType.charsetName);
        else
            return null;
    }

    /**
     * Class that represent ContentType and Charset of a document (if present)
     */
    private static final class ContentType
    {
        private static final Pattern CHARSET_HEADER = Pattern.compile("charset=([-_a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

        private String charsetName;

        private ContentType(String headerValue)
        {
            if (headerValue == null)
                throw new IllegalArgumentException("ContentType must be constructed with a not-null headerValue");
            if (headerValue.indexOf(";") != -1)
            {
                Matcher matcher = CHARSET_HEADER.matcher(headerValue);
                if (matcher.find())
                    charsetName = matcher.group(1);
            }
        }
    }
}
