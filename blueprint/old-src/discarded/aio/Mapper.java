package lempel.blueprint.aio;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Vector;

/**
 * SocketChannel(hashcode)/ByteBuffer Mapper<br>
 * To avoid using SelectionKey.attach(Object)<br>
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 10. 01
 * @since 1.5
 * @last $Date$
 * @see
 */
public class Mapper {
	/** Eager Singleton */
	private static Mapper a = new Mapper();

	/** channel/buffer map */
	private HashMap<Long, Vector<ByteBuffer>> m;

	/**
	 * Constructor
	 */
	private Mapper() {
		m = new HashMap<Long, Vector<ByteBuffer>>();
	}

	/**
	 * return singleton
	 * 
	 * @return
	 */
	public static Mapper getInstance() {
		return a;
	}

	/**
	 * add buffer to current key's queue
	 * 
	 * @param k
	 *            channel's hashcode
	 * @param b
	 */
	public void put(long k, ByteBuffer b) {
		Vector<ByteBuffer> v;
		synchronized (m) {
			if (!m.containsKey(new Long(k))) {
				m.put(new Long(k), new Vector<ByteBuffer>(10, 10));
			}
			v = m.get(new Long(k));
		}
		v.add(b);
	}

	/**
	 * get queue
	 * 
	 * @param k
	 *            channel's hashcode
	 * @return
	 */
	public Vector<ByteBuffer> get(long k) {
		return m.get(new Long(k));
	}

	/**
	 * remove queue
	 * 
	 * @param k
	 *            channel's hashcode
	 */
	public void remove(long k) {
		m.remove(new Long(k));
	}
}
