package lempel.blueprint.app.tracer;

public class LempelLogger {
	public static void trace(int index, Object arg) {
		StringBuilder sb = new StringBuilder();
		sb.append("	arg[");
		sb.append(index);
		sb.append("] = ");
		sb.append(arg);
		MethodLogger.log(sb.toString());
	}

	public static void trace(int index, boolean arg) {
		trace(index, Boolean.toString(arg));
	}

	public static void trace(int index, byte arg) {
		trace(index, Byte.toString(arg));
	}

	public static void trace(int index, char arg) {
		trace(index, Character.toString(arg));
	}

	public static void trace(int index, double arg) {
		trace(index, Double.toString(arg));
	}

	public static void trace(int index, float arg) {
		trace(index, Float.toString(arg));
	}

	public static void trace(int index, int arg) {
		trace(index, Integer.toString(arg));
	}

	public static void trace(int index, short arg) {
		trace(index, Short.toString(arg));
	}

	public static void trace(int index, long arg) {
		trace(index, Long.toString(arg));
	}
}