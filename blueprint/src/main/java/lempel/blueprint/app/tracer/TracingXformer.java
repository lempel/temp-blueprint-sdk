package lempel.blueprint.app.tracer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.RETURN;
import org.apache.bcel.generic.Type;

public class TracingXformer implements ClassFileTransformer {
	public static Object lock = new Object();
	static boolean debug = false;

	@SuppressWarnings("unchecked")
	public byte[] transform(ClassLoader loader, String className, Class redefiningClass, ProtectionDomain domain,
			byte[] bytes) throws IllegalClassFormatException {
		synchronized (lock) {
			TracingProperties props = TracingProperties.getInstance();

			if (props.isInstrument() && !className.startsWith("com/raventools/trace/")
					&& !className.startsWith("com/raventools/logging/") && !className.startsWith("java/")
					&& !className.startsWith("javax/") && props.shouldTraceClass(className)) {
				if (debug)
					MethodLogger.log("TracingXformer processing class: " + className);

				ByteArrayInputStream is = new ByteArrayInputStream(bytes);
				JavaClass clazz = addTracing(is, className);

				if (clazz == null) {
					MethodLogger.log("tracing injection failed: " + className);
					return null;
				}

				byte[] b = clazz.getBytes();
				return b;
			} else {
				if (debug)
					MethodLogger.log("TracingXformer ignoring class: " + className);

				return (null); // no mods made - return null.
			}
		}
	}

	public static JavaClass addTracing(InputStream is, String className) {
		try {
			JavaClass clazz = BcelHelper.lookupClass(is, className);
			if (clazz == null) {
				MethodLogger.log("Repository.lookupClass failed on: " + className);
				return (null);
			}

			JavaClass traced_class = addTracing(clazz);
			return traced_class;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return (null);
	}

	public static JavaClass addTracing(JavaClass clazz) {
		if (clazz == null)
			return (null);

		try {
			ClassGen cg = new ClassGen(clazz);
			Method[] methods = cg.getMethods();

			if (debug)
				System.out.println("scanning methods...");

			int methodNum = methods.length;
			for (int i = 0; i < methodNum; i++) {
				if (debug)
					System.out.println("    instrumenting: " + methods[i].getName());

				addMethodWithTracing(cg, i);
			}

			clazz = cg.getJavaClass();
			return clazz;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (null);
	}

	private static void addMethodWithTracing(ClassGen cg, int i) {
		ConstantPoolGen cp = cg.getConstantPool();

		int staticThreadEnter = cp.addMethodref("com.raventools.trace.MethodLogger", "staticMethodEnter",
				"(Ljava/lang/String;Ljava/lang/String;)V");
		int instanceThreadEnter = cp.addMethodref("com.raventools.trace.MethodLogger", "instanceMethodEnter",
				"(Ljava/lang/Object;Ljava/lang/String;)V");
		int threadExit = cp.addMethodref("com.raventools.trace.MethodLogger", "methodExit", "(Ljava/lang/String;)V");

		Method m = cg.getMethodAt(i);

		Code code = m.getCode();
		int flags = m.getAccessFlags();
		String name = m.getName();

		// This will avoid instrumenting the toString methods. IT IS VERY
		// IMPORTANT that you avoid toSting, because
		// the instrumentation code itself can call toString(), so if you have
		// it instrumented it will be an
		// infinite recursion and will cause stack overflow.
		if (name.indexOf("toString") != -1)
			return;

		String signature = m.getSignature();

		if (name.equals("<init>") || name.equals("<clinit>") || code == null) {
			return;
		}

		String hiddenName = "hidden_" + name;
		int nameIndex = cp.addUtf8(hiddenName);
		m.setNameIndex(nameIndex);

		if (m.isPublic()) {
			int newFlags = (flags & ~Constants.ACC_PUBLIC) | Constants.ACC_PRIVATE;
			m.setAccessFlags(newFlags);
		}

		cg.setMethodAt(m, i);

		int hiddenMethodIndex = cp.addMethodref(cg.getClassName(), hiddenName, signature);
		InstructionList il = new InstructionList();

		Type[] argTypes = Type.getArgumentTypes(signature);
		Type returnType = Type.getReturnType(signature);
		int argNum = argTypes.length;

		int argOffset = 0;
		MethodGen newMethod = new MethodGen(flags, returnType, argTypes, null, name, cg.getClassName(), il, cp);

		if (!m.isStatic()) {
			argOffset++;
			il.append(new ALOAD(0));
			il.append(new PUSH(cp, name));
			il.append(new INVOKESTATIC(instanceThreadEnter));
		} else {
			il.append(new PUSH(cp, (String) null)); // don't know how to get
													// class name here.
			il.append(new PUSH(cp, name));
			il.append(new INVOKESTATIC(staticThreadEnter));
		}

		if (!m.isStatic()) {
			il.append(new ALOAD(0));
			argOffset = 1;
		}

		for (int iarg = 0; iarg < argNum; iarg++) {
			Instruction instr = InstructionFactory.createLoad(argTypes[iarg], slotOfArg(iarg, argTypes) + argOffset);
			il.append(instr);
		}

		if (m.isStatic()) {
			il.append(new INVOKESTATIC(hiddenMethodIndex));
		} else {
			il.append(new INVOKESPECIAL(hiddenMethodIndex));
		}

		// lempel --->
		{
			int myTraceO = cp.addMethodref("com.raventools.trace.LempelLogger", "trace", "(ILjava/lang/Object;)V");
			int myTraceZ = cp.addMethodref("com.raventools.trace.LempelLogger", "trace", "(IZ)V");
			int myTraceB = cp.addMethodref("com.raventools.trace.LempelLogger", "trace", "(IB)V");
			int myTraceC = cp.addMethodref("com.raventools.trace.LempelLogger", "trace", "(IC)V");
			int myTraceD = cp.addMethodref("com.raventools.trace.LempelLogger", "trace", "(ID)V");
			int myTraceF = cp.addMethodref("com.raventools.trace.LempelLogger", "trace", "(IF)V");
			int myTraceI = cp.addMethodref("com.raventools.trace.LempelLogger", "trace", "(II)V");
			int myTraceS = cp.addMethodref("com.raventools.trace.LempelLogger", "trace", "(IS)V");
			int myTraceJ = cp.addMethodref("com.raventools.trace.LempelLogger", "trace", "(IJ)V");

			for (int iarg = 0; iarg < argNum; iarg++) {
				if (Type.NO_ARGS.equals(argTypes[iarg])) {
					continue;
				}

				il.append(new PUSH(cp, iarg));
				if (Type.NULL.equals(argTypes[iarg])) {
					il.append(new PUSH(cp, "<null>"));
				} else {
					Instruction instr = InstructionFactory.createLoad(argTypes[iarg], slotOfArg(iarg, argTypes)
							+ argOffset);
					il.append(instr);
				}

				if (Type.BOOLEAN.equals(argTypes[iarg])) {
					il.append(new INVOKESTATIC(myTraceZ));
				} else if (Type.BYTE.equals(argTypes[iarg])) {
					il.append(new INVOKESTATIC(myTraceB));
				} else if (Type.CHAR.equals(argTypes[iarg])) {
					il.append(new INVOKESTATIC(myTraceC));
				} else if (Type.DOUBLE.equals(argTypes[iarg])) {
					il.append(new INVOKESTATIC(myTraceD));
				} else if (Type.FLOAT.equals(argTypes[iarg])) {
					il.append(new INVOKESTATIC(myTraceF));
				} else if (Type.INT.equals(argTypes[iarg])) {
					il.append(new INVOKESTATIC(myTraceI));
				} else if (Type.SHORT.equals(argTypes[iarg])) {
					il.append(new INVOKESTATIC(myTraceS));
				} else if (Type.LONG.equals(argTypes[iarg])) {
					il.append(new INVOKESTATIC(myTraceJ));
				} else {
					il.append(new INVOKESTATIC(myTraceO));
				}
			}
		}
		// <--- lempel

		if (returnType != Type.VOID) {
			il.append(InstructionFactory.createStore(returnType, slotOfArg(argNum, argTypes) + argOffset));

			il.append(new PUSH(cp, name));
			il.append(new INVOKESTATIC(threadExit));
			il.append(InstructionFactory.createLoad(returnType, slotOfArg(argNum, argTypes) + argOffset));
			il.append(InstructionFactory.createReturn(Type.getReturnType(signature)));
		} else {
			il.append(new PUSH(cp, name));
			il.append(new INVOKESTATIC(threadExit));

			il.append(new RETURN());
		}

		il.append(InstructionFactory.createStore(Type.OBJECT, slotOfArg(argNum, argTypes) + argOffset));
		il.append(InstructionFactory.createLoad(Type.OBJECT, slotOfArg(argNum, argTypes) + argOffset));
		il.append(new ATHROW());

		newMethod.setInstructionList(il);
		newMethod.setMaxLocals();
		newMethod.setMaxStack();

		cg.addMethod(newMethod.getMethod());
		il.dispose();
	}

	private static int slotOfArg(int n, Type[] types) {
		int s = 0;
		for (int i = 0; i < n; i++) {
			if (types[i] == Type.LONG || types[i] == Type.DOUBLE) {
				s += 2;
			} else {
				s++;
			}
		}
		return s;
	}
}
