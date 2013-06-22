package lempel.old.framework;

/**
 * ��/���� ������ trace�� ������ linker
 * 
 * @author Sang-min Lee
 * @since 2005.1.12.
 * @version 2005.1.12.
 */
public abstract class TraceableLinker extends Linker {
	protected static TraceWorker _tracer = null;

	protected static Object _tracerMonitor = new Object();

	/**
	 * Trace������ ������ worker�� ���� <br>
	 * static�̹Ƿ� worker�� singleton�̰ų� �Ѱ��� instance�� �����ؾ� �Ѵ�
	 * 
	 * @param tracer
	 */
	public static void setTracer(TraceWorker tracer) {
		synchronized (_tracerMonitor) {
			_tracer = tracer;
		}
	}

	/**
	 * ���ŵ� �����͸� tracer�� ������
	 * 
	 * @param header
	 *            �������� �տ� ���� ���
	 * @param value
	 *            ���ŵ� ������
	 */
	public static void traceRx(String header, Object value) {
		trace(header + " RX", value);
	}

	/**
	 * �۽��� �����͸� tracer�� ������
	 * 
	 * @param header
	 *            �������� �տ� ���� ���
	 * @param value
	 *            �۽��� ������
	 */
	public static void traceTx(String header, Object value) {
		trace(header + " TX", value);
	}

	/**
	 * tracer�� �����͸� ������
	 * 
	 * @param header
	 *            �������� �տ� ���� ���
	 * @param value
	 *            tracer�� ���� ������
	 */
	public static void trace(String header, Object value) {
		synchronized (_tracerMonitor) {
			if (_tracer != null)
				_tracer.write(header, value);
		}
	}
}