package vic.mod.chat.transformer;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.launchwrapper.IClassTransformer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class VchatClassTransformater implements IClassTransformer {
	
	private BiMap<String, String> classToPatch = HashBiMap.create(new HashMap<String, String>());
	private HashMap<String, ITransformHandler> handlers = new HashMap<String, ITransformHandler>();
	public VchatClassTransformater() 
	{
		classToPatch.put("net.minecraft.network.play.server.S38PacketPlayerListItem", "ho");
		classToPatch.put("net.minecraft.network.play.server.S3CPacketUpdateScore", "ie");
		classToPatch.put("net.minecraft.network.play.server.S0CPacketSpawnPlayer", "gb");
		handlers.put("net.minecraft.network.play.server.S38PacketPlayerListItem", new PlayerListItemTransformer());
		handlers.put("net.minecraft.network.play.server.S3CPacketUpdateScore", new UpdateScoreTransformer());
		handlers.put("net.minecraft.network.play.server.S0CPacketSpawnPlayer", new SpawnPlayerTransformer());
	}
	
	@Override
	public byte[] transform(String contextName, String transformedName, byte[] bytes) 
	{
		//System.out.println(transformedName);
		boolean isKey = classToPatch.containsKey(contextName);
		boolean isValue = classToPatch.containsValue(contextName);
		
		if(isKey || isValue)
		{
			String name = contextName;
			if(isValue) name = classToPatch.inverse().get(name);
			if(handlers.get(name) == null) return bytes;
			return handlers.get(name).transform(contextName, bytes);
			//return patchClass(name, bytes, VchatLoadingPlugin.location);
		}
		
		return bytes;
	}

	public byte[] patchClass(String name, byte[] bytes, File location) 
	{
		try {
			ZipFile zip = new ZipFile(location);
			ZipEntry entry = zip.getEntry(name.replace('.', '/') + ".class");
			if (entry == null) {
				System.out.println("[Vchat]:" + name + " not found in "
						+ location.getName());
			} else {
				// serialize the class file into the bytes array
				InputStream zin = zip.getInputStream(entry);
				bytes = new byte[(int) entry.getSize()];
				zin.read(bytes);
				zin.close();
				System.out.println("[Vchat]: " + "Class " + name + " patched!");
			}
			zip.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bytes;
	}

}
