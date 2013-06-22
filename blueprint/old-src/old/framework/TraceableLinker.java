package lempel.old.framework;

/**
 * 송/수신 내역의 trace가 가능한 linker
 * 
 * @author Sang-min Lee
 * @since 2005.1.12.
 * @version 2005.1.12.
 */
public abstract class TraceableLinker extends Linker {
	protected static TraceWorker _tracer = null;

	protected static Object _tracerMonitor = new Object();

	/**
	 * Trace정보를 전송할 worker를 설정 <br>
	 * static이므로 worker는 singleton이거나 한개의 instance만 존재해야 한다
	 * 
	 * @param tracer
	 */
	public static void setTracer(TraceWorker tracer) {
		synchronized (_tracerMonitor) {
			_tracer = tracer;
		}
	}

	/**
	 * 수신된 데이터를 tracer로 보낸다
	 * 
	 * @param header
	 *            데이터의 앞에 붙일 헤더
	 * @param value
	 *            수신된 데이터
	 */
	public static void traceRx(String header, Object value) {
		trace(header + " RX", value);
	}

	/**
	 * 송신한 데이터를 tracer로 보낸다
	 * 
	 * @param header
	 *            데이터의 앞에 붙일 헤더
	 * @param value
	 *            송신한 데이터
	 */
	public static void traceTx(String header, Object value) {
		trace(header + " TX", value);
	}

	/**
	 * tracer로 데이터를 보낸다
	 * 
	 * @param header
	 *            데이터의 앞에 붙일 헤더
	 * @param value
	 *            tracer로 보낼 데이터
	 */
	public static void trace(String header, Object value) {
		synchronized (_tracerMonitor) {
			if (_tracer != null)
				_tracer.write(header, value);
		}
	}
}