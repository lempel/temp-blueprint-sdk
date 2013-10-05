/*
 License:

 blueprint-sdk is licensed under the terms of Eclipse Public License(EPL) v1.0
 (http://www.eclipse.org/legal/epl-v10.html)


 Distribution:

 International - http://code.google.com/p/blueprint-sdk
 South Korea - http://lempel.egloos.com


 Background:

 blueprint-sdk is a java software development kit to protect other open source
 softwares' licenses. It's intended to provide light weight APIs for blueprints.
 Well... at least trying to.

 There are so many great open source projects now. Back in year 2000, there
 were not much to use. Even JDBC drivers were rare back then. Naturally, I have
 to implement many things by myself. Especially dynamic class loading, networking,
 scripting, logging and database interactions. It was time consuming. Now I can
 take my picks from open source projects.

 But I still need my own APIs. Most of my clients just don't understand open
 source licenses. They always want to have their own versions of open source
 projects but don't want to publish derivative works. They shouldn't use open
 source projects in the first place. So I need to have my own open source project
 to be free from derivation terms and also as a mediator between other open
 source projects and my client's requirements.

 Primary purpose of blueprint-sdk is not to violate other open source project's
 license terms.


 To commiters:

 License terms of the other software used by your source code should not be
 violated by using your source code. That's why blueprint-sdk is made for.
 Without that, all your contributions are welcomed and appreciated.
 */
package lempel.blueprint.experimental.rebuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.apache.bcel.Constants;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.ExceptionTable;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

import blueprint.sdk.logger.Logger;

/**
 * Some Serializable classes won't implements readObject/writeObject.<br>
 * Rebuilder injects readObject/writeObject methods to such classes.<br>
 * This may reduce reflection cost during serialization.<br>
 * 
 * @author Sangmin Lee
 * @version $Revision$
 * @since 2008. 03. 03
 * @last $Date$
 */
public class SerializableRebuilder {
	private static final String IOEXCEPTION = "java.io.IOException";
	private static final String REBUILD_FAILED = "rebuild failed - ";
	private static final Logger LOGGER = Logger.getInstance();
	private static final String STR_OIS_NAME = "java.io.ObjectInputStream";
	private static final String STR_OIS_SIG = "Ljava.io.ObjectInputStream;";
	private static final String STR_OOS_NAME = "java.io.ObjectOutputStream";
	private static final String STR_OOS_SIG = "Ljava.io.ObjectOutputStream;";

	private static final String FILE_SEP = System.getProperty("file.separator");

	private JavaClass serializable;
	private JavaClass externalizable;

	public static void main(final String[] args) throws ClassNotFoundException, IOException {
		if (args.length != 2 && args.length != 1) {
			LOGGER.println("Usage: java " + SerializableRebuilder.class.getName() + " <classes dir> [target dir]");
			LOGGER.println("* CAUTION *  If target dir is not set, original class files will be overwritten");
			System.exit(1);
		}

		SerializableRebuilder rebuilder = new SerializableRebuilder();
		if (args.length == 1) {
			rebuilder.rebuild(new File(args[0]), new File(args[0]));
		} else {
			rebuilder.rebuild(new File(args[0]), new File(args[1]));
		}
	}

	public SerializableRebuilder() throws ClassNotFoundException {
		setSerializable(Repository.lookupClass("java.io.Serializable"));
		setExternalizable(Repository.lookupClass("java.io.Externalizable"));
	}

	/**
	 * Check it's a Serializable and writeObject/readObject/writeReplace methods
	 * are ommited
	 * 
	 * @param jclass
	 * @return
	 * @throws ClassNotFoundException
	 */
	private boolean checkRebuildable(final JavaClass jclass) throws ClassNotFoundException {
		boolean result = false;

		// skip interfaces
		if (!jclass.isInterface()) {
			JavaClass[] interfaces = jclass.getAllInterfaces();
			for (JavaClass jc : interfaces) {
				if (jc.instanceOf(getExternalizable())) {
					result = false;
					break;
				} else if (jc.instanceOf(getSerializable())) {
					result = true;
				}
			}

			if (result) {
				Method[] methods = jclass.getMethods();
				for (Method m : methods) {
					if (isWriteReplace(m) || isReadObject(m) || isWriteObject(m)) {
						result = false;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Process src directory recursively and overwrites
	 * 
	 * @param src
	 *            source directory
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void rebuild(final File src) {
		File[] files = src.listFiles();
		for (File f : files) {
			String fileName = f.getName();
			if (!fileName.endsWith(".") && !fileName.endsWith("..")) {
				if (f.isDirectory()) {
					rebuild(f);
				} else if (fileName.endsWith(".class")) {
					String absName = f.getAbsolutePath();
					LOGGER.println("rebuilding " + absName);
					try {
						ClassParser parser = new ClassParser(absName);
						JavaClass jclass = parser.parse();
						rebuild(jclass, fileName);
					} catch (IOException e) {
						LOGGER.println(REBUILD_FAILED + absName);
						LOGGER.trace(e);
					} catch (ClassNotFoundException e) {
						LOGGER.println(REBUILD_FAILED + absName);
						LOGGER.trace(e);
					}
				}
			}
		}
	}

	/**
	 * Process src directory recursively and modified classes goes to tar
	 * directory
	 * 
	 * @param src
	 * @param tar
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void rebuild(final File src, final File tar) {
		if (!tar.exists()) {
			tar.mkdirs();
		}

		File[] files = src.listFiles();
		for (File f : files) {
			String fileName = f.getName();
			if (!fileName.endsWith(".") && !fileName.endsWith("..")) {
				if (f.isDirectory()) {
					rebuild(f, new File(tar.getAbsolutePath() + FILE_SEP + fileName));
				} else if (fileName.endsWith(".class")) {
					String absName = f.getAbsolutePath();
					LOGGER.println("rebuilding " + absName);
					try {
						ClassParser parser = new ClassParser(absName);
						JavaClass jclass = parser.parse();
						rebuild(jclass, tar.getAbsolutePath() + FILE_SEP + fileName);
					} catch (IOException e) {
						LOGGER.println(REBUILD_FAILED + absName);
						LOGGER.trace(e);
					} catch (ClassNotFoundException e) {
						LOGGER.println(REBUILD_FAILED + absName);
						LOGGER.trace(e);
					}
				}
			}
		}
	}

	/**
	 * actual re-build process
	 * 
	 * @param jclass
	 * @param targetName
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void rebuild(final JavaClass jclass, final String targetName) throws IOException, ClassNotFoundException {
		if (checkRebuildable(jclass)) {
			ClassGen cgen = new ClassGen(jclass);

			cgen.addMethod(createWriteObject(cgen, jclass));
			cgen.addMethod(createReadObject(cgen, jclass));

			cgen.getJavaClass().dump(targetName);
		}
	}

	private Method createWriteObject(final ClassGen cgen, final JavaClass jclass) throws ClassNotFoundException {
		ConstantPoolGen cpgen = cgen.getConstantPool();
		InstructionList ilist = new InstructionList();
		MethodGen mgen = new MethodGen(Constants.ACC_PRIVATE, Type.VOID, new Type[] { Type.getType(STR_OOS_SIG) },
				new String[] { "out" }, "writeObject", jclass.getClassName(), ilist, cpgen);
		mgen.addException(IOEXCEPTION);

		List<Field> fields = getFields(jclass);

		for (Field field : fields) {
			ilist.append(InstructionFactory.createLoad(Type.getType(STR_OOS_SIG), 1));
			ilist.append(InstructionFactory.createLoad(Type.getType("L" + jclass.getClassName() + ";"), 0));

			Type type = field.getType();
			String method = null;
			Type argType = type;
			if (Type.BOOLEAN.equals(type)) {
				method = "writeBoolean";
			} else if (Type.BYTE.equals(type)) {
				method = "writeByte";
				argType = Type.INT;
			} else if (Type.CHAR.equals(type)) {
				method = "writeChar";
				argType = Type.INT;
			} else if (Type.DOUBLE.equals(type)) {
				method = "writeDouble";
			} else if (Type.FLOAT.equals(type)) {
				method = "writeFloat";
			} else if (Type.INT.equals(type)) {
				method = "writeInt";
			} else if (Type.LONG.equals(type)) {
				method = "writeLong";
			} else if (Type.SHORT.equals(type)) {
				method = "writeShort";
				argType = Type.INT;
			} else {
				method = "writeObject";
				argType = Type.OBJECT;
			}

			InstructionFactory factory = new InstructionFactory(cgen);
			ilist.append(factory.createGetField(jclass.getClassName(), field.getName(), type));
			ilist.append(factory.createInvoke(STR_OOS_NAME, method, Type.VOID, new Type[] { argType },
					Constants.INVOKEVIRTUAL));
		}
		ilist.append(InstructionConstants.RETURN);

		mgen.setMaxLocals();
		mgen.setMaxStack();

		return mgen.getMethod();
	}

	private Method createReadObject(final ClassGen cgen, final JavaClass jclass) throws ClassNotFoundException {
		ConstantPoolGen cpgen = cgen.getConstantPool();
		InstructionList ilist = new InstructionList();
		MethodGen mgen = new MethodGen(Constants.ACC_PRIVATE, Type.VOID, new Type[] { Type.getType(STR_OIS_SIG) },
				new String[] { "in" }, "readObject", jclass.getClassName(), ilist, cpgen);
		mgen.addException(IOEXCEPTION);
		mgen.addException("java.lang.ClassNotFoundException");
		InstructionFactory factory = new InstructionFactory(cgen);

		List<Field> fields = getFields(jclass);

		for (Field field : fields) {
			ilist.append(InstructionFactory.createLoad(Type.getType("L" + jclass.getClassName() + ";"), 0));
			ilist.append(InstructionFactory.createLoad(Type.getType(STR_OIS_SIG), 1));

			Type type = field.getType();
			String method = null;
			Type argType = type;
			boolean needCast = false;
			if (Type.BOOLEAN.equals(type)) {
				method = "readBoolean";
			} else if (Type.BYTE.equals(type)) {
				method = "readByte";
			} else if (Type.CHAR.equals(type)) {
				method = "readChar";
			} else if (Type.DOUBLE.equals(type)) {
				method = "readDouble";
			} else if (Type.FLOAT.equals(type)) {
				method = "readFloat";
			} else if (Type.INT.equals(type)) {
				method = "readInt";
			} else if (Type.LONG.equals(type)) {
				method = "readLong";
			} else if (Type.SHORT.equals(type)) {
				method = "readShort";
			} else {
				method = "readObject";
				argType = Type.OBJECT;
				needCast = true;
			}

			ilist.append(factory.createInvoke(STR_OIS_NAME, method, argType, new Type[] {}, Constants.INVOKEVIRTUAL));
			if (needCast) {
				String signature = type.getSignature();

				if (signature.charAt(0) == '[') {
					byte[] sig = signature.getBytes();
					int dim = 0;
					int last = 0;
					for (int x = 0; x < sig.length; x++) {
						if (sig[x] == '[') {
							dim++;
							last = x;
						}
					}

					ilist.append(factory.createCheckCast(new ArrayType(Type.getType(signature.substring(last + 1)), dim)));
				} else {
					ilist.append(factory.createCheckCast(new ObjectType(type.toString())));
				}
			}
			ilist.append(factory.createPutField(jclass.getClassName(), field.getName(), type));
		}
		ilist.append(InstructionConstants.RETURN);

		mgen.setMaxLocals();
		mgen.setMaxStack();

		return mgen.getMethod();
	}

	// returns all fields from itself and superclasses
	private static List<Field> getFields(final JavaClass jclass) throws ClassNotFoundException {
		ArrayList<Field> result = new ArrayList<Field>();

		JavaClass workingClass = jclass;
		while (workingClass != null) {
			Field[] fields = workingClass.getFields();
			for (Field f : fields) {
				if (!f.isStatic() && !f.isTransient() && !f.isFinal()) {
					result.add(f);
				}
			}

			workingClass = workingClass.getSuperClass();
		}

		return result;
	}

	private static boolean isWriteReplace(final Method method) {
		boolean result = false;
		// is this writeRepace method with no args and throws
		// ObjectStreamException?
		if ("writeReplace".equals(method.getName()) && method.getArgumentTypes().length == 0
				&& "Ljava/lang/Object;".equals(method.getReturnType().getSignature())) {
			ExceptionTable table = method.getExceptionTable();
			if (table != null) {
				String[] exceptionNames = table.getExceptionNames();
				for (String name : exceptionNames) {
					if ("java.io.ObjectStreamException".equals(name)) {
						result = true;
					}
				}
			}
		}

		return result;
	}

	private static boolean isReadObject(final Method method) {
		boolean result = false;
		// is this readObject method with a arg which is ObjectInputStream and
		// returns nothing?
		if ("readObject".equals(method.getName()) && method.getArgumentTypes().length == 1
				&& "Ljava/io/ObjectInputStream;".equals(method.getArgumentTypes()[0].getSignature())
				&& "V".equals(method.getReturnType().getSignature())) {
			ExceptionTable table = method.getExceptionTable();
			if (table != null) {
				String[] exceptionNames = table.getExceptionNames();
				int hit = 0;
				for (String name : exceptionNames) {
					// throws IOException or ClassNotFoundException ?
					if (IOEXCEPTION.equals(name)) {
						hit++;
					} else if ("java.lang.ClassNotFoundException".equals(name)) {
						hit++;
					}
				}

				if (hit == 2) {
					result = true;
				}
			}
		}

		return result;
	}

	private static boolean isWriteObject(final Method method) {
		boolean result = false;
		// is this readObject method with a arg which is ObjectOutputStream and
		// returns nothing?
		if ("writeObject".equals(method.getName()) && method.getArgumentTypes().length == 1
				&& "Ljava/io/ObjectOutputStream;".equals(method.getArgumentTypes()[0].getSignature())
				&& "V".equals(method.getReturnType().getSignature())) {
			ExceptionTable table = method.getExceptionTable();
			if (table != null) {
				String[] exceptionNames = table.getExceptionNames();
				for (String name : exceptionNames) {
					// throws IOException?
					if (IOEXCEPTION.equals(name)) {
						result = true;
					}
				}
			}
		}

		return result;
	}

	private final JavaClass getSerializable() {
		return serializable;
	}

	private final void setSerializable(final JavaClass serializable) {
		this.serializable = serializable;
	}

	private final JavaClass getExternalizable() {
		return externalizable;
	}

	private final void setExternalizable(final JavaClass externalizable) {
		this.externalizable = externalizable;
	}
}
