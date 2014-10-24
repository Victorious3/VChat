package vic.mod.chat.transformer;

import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class SpawnPlayerTransformer implements ITransformHandler {

	@Override
	public byte[] transform(String className, byte[] buffer) {

		InsnList toInject = new InsnList();
		System.out.println("[vchat]: " + "Trying to patch SpawnPlayer...");

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(buffer);
		classReader.accept(classNode, 0);
		
		List<MethodNode> methods = classNode.methods;
		Iterator<MethodNode> iterator = methods.iterator();
		while (iterator.hasNext()) {
			MethodNode m = iterator.next();
			if (m.name.equals("<init>") && (m.desc.equals("(Lnet/minecraft/entity/player/EntityPlayer;)V") || m.desc.equals("(Lyz;)V"))) 
			{
				for (int i = 0; i < m.instructions.size(); i++) {
					AbstractInsnNode insn = m.instructions.get(i);
					
					if(insn instanceof FieldInsnNode)
					{
						
						FieldInsnNode fieldInsn = (FieldInsnNode)insn;
						boolean isOb = fieldInsn.name.equals("b");
						if((fieldInsn.name.equals("field_148955_b") || isOb) && fieldInsn.getPrevious().getOpcode() == Opcodes.INVOKEVIRTUAL)
						{
							//FIX The end of standard declaration
							toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
							toInject.add(new TypeInsnNode(Opcodes.NEW, "com/mojang/authlib/GameProfile"));
							toInject.add(new InsnNode(Opcodes.DUP));
							toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
							
							if(isOb)
								toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "yz", "bJ", "()Lcom/mojang/authlib/GameProfile;"));
							else
								toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/EntityPlayer", "getGameProfile", "()Lcom/mojang/authlib/GameProfile;"));
							
							toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/mojang/authlib/GameProfile", "getId", "()Ljava/util/UUID;"));
							toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
							
							if(isOb)
								toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "yz", "bJ", "()Lcom/mojang/authlib/GameProfile;"));
							else
								toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/EntityPlayer", "getGameProfile", "()Lcom/mojang/authlib/GameProfile;"));
							
							toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/mojang/authlib/GameProfile", "getName", "()Ljava/lang/String;"));
							toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "vic/mod/chat/handler/NickHandler", "getPlayerFromSenderName", "(Ljava/lang/String;)Ljava/lang/String;"));
							toInject.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "com/mojang/authlib/GameProfile", "<init>", "(Ljava/util/UUID;Ljava/lang/String;)V"));
							toInject.add(new FieldInsnNode(Opcodes.PUTFIELD, className.replace(".", "/"), fieldInsn.name, fieldInsn.desc));
							toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
							
							//Insert into instructions list
							m.instructions.insertBefore(m.instructions.get(i + 1), toInject);
							
							
							//Write the class and return byte array.
							ClassWriter writer = new ClassWriter(0);
							classNode.accept(writer);
							
							System.out.println("[Vchat]: SpawnPlayer patched!");
							return writer.toByteArray();
						}
					}
				}
			
			}
		}
		return buffer;
	}

}
