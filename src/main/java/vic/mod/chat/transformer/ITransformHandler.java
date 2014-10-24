package vic.mod.chat.transformer;

public interface ITransformHandler 
{
	byte[] transform(String className, byte[] buffer);	
}
