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
package lempel.blueprint.base.io;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * If incremental is set, buffer's size will be increase automatically.<br>
 * If incremental is not set and overflowCheck is set, throws OverflowException.<br>
 * And both adaptability and overflowCheck is not set, trimmed tail will be
 * stored.<br>
 * <br>
 * This happens when you put something to buffer. Buffer itself is circular.<br>
 * <br>
 * <b>This class is thread safe.</b><br>
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2008. 11. 24.
 * @last $Date$
 */
public class CircularByteBuffer {
	private final boolean incremental;
	private final boolean overflowCheck;
	private ByteBuffer buffer = null;
	private transient ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public CircularByteBuffer(final int capacity, final boolean incremental, final boolean overflowCheck) {
		this.incremental = incremental;
		allocate(capacity);
		this.overflowCheck = overflowCheck;
	}

	private void allocate(final int capacity) {
		buffer = ByteBuffer.allocate(capacity);
	}

	/**
	 * push byte[] to buffer
	 * 
	 * @param data
	 */
	public void push(final byte[] data) {
		lock.writeLock().lock();
		try {
			if (buffer.remaining() >= data.length) {
				buffer.put(data);
			} else {
				if (buffer.capacity() < data.length) {
					if (incremental) {
						// incremental mode
						resize(buffer.capacity() + (data.length - buffer.remaining()));
					} else if (overflowCheck) {
						// non-incremental mode + overflow check
						throw new OverflowException(data.length);
					} else {
						// non-incremental mode + w/o overflow check
						buffer.clear();
					}
					buffer.put(data, data.length - buffer.capacity(), buffer.capacity());
				} else {
					buffer.position(data.length);
					buffer.compact();
					buffer.put(data);
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Pops all byte[] from buffer
	 * 
	 * @return
	 */
	public byte[] pop() {
		byte[] result = array(true);

		return result;
	}

	/**
	 * Unlike pop, just returns buffer's content.
	 * 
	 * @return
	 */
	public byte[] array() {
		return array(false);
	}

	private byte[] array(final boolean clear) {
		byte[] result;

		lock.readLock().lock();
		try {
			if (buffer.remaining() > 0) {
				result = new byte[buffer.position()];
				for (int i = 0; i < result.length; i++) {
					result[i] = buffer.get(i);
				}
			} else {
				result = buffer.array();
			}

			if (clear) {
				buffer.clear();
			}
		} finally {
			lock.readLock().unlock();
		}

		return result;
	}

	public int remaining() {
		return buffer.remaining();
	}

	public void clear() {
		lock.writeLock().lock();
		try {
			buffer.clear();
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Buffer can't be shinked. Use bigger capatity
	 * 
	 * @param capacity
	 */
	public void resize(final int capacity) {
		lock.writeLock().lock();

		try {
			if (capacity > buffer.capacity()) {
				byte[] data = array(true);
				allocate(capacity);
				push(data);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public int getCapacity() {
		return buffer.capacity();
	}

	@Override
	protected void finalize() throws Throwable {
		buffer.clear();
		buffer = null;
		while (lock.getWriteHoldCount() > 0) {
			lock.writeLock().unlock();
		}
		while (lock.getReadLockCount() > 0) {
			lock.readLock().unlock();
		}
		lock = null;

		super.finalize();
	}
}
