package vic.mod.chat.transformer;

import java.io.File;
import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;

@MCVersion("1.7.10")
public class VchatLoadingPlugin implements IFMLLoadingPlugin 
{

	public static File location;

	@Override
	public String[] getASMTransformerClass() 
	{
		return new String[]{VchatClassTransformater.class.getName()};
	}

	@Override
	public String getModContainerClass() 
	{
		return null;
	}

	@Override
	public String getSetupClass() 
	{
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) 
	{
		System.out.println(data);
		location = (File) data.get("coremodLocation");
	}

	@Override
	public String getAccessTransformerClass() 
	{
		return null;
	}

}
