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
package lempel.blueprint.base.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Recycle byte arrays as many as possible to reduce memory allocations.<br>
 * <b>BEWARE: </b>This pool reduces memory allocations & heap usage variations
 * but total performance could be decreased (due to inevitable locks).<br>
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2009. 1. 20.
 * @last $Date$
 */
public class ByteArrayPool {
	private final int arraySize;
	private final int maxArrays;
	private final List<byte[]> pool;

	/**
	 * Constructor
	 * 
	 * @param arraySize
	 *            each array's size
	 * @param maxArrays
	 *            maximum number of arrays to preserve
	 */
	protected ByteArrayPool(final int arraySize, final int maxArrays) {
		this.arraySize = arraySize;
		this.maxArrays = maxArrays;
		pool = new ArrayList<byte[]>(maxArrays);
	}

	public byte[] newArray() {
		byte[] result;

		synchronized (pool) {
			if (pool.isEmpty()) {
				result = new byte[arraySize];
			} else {
				result = pool.remove(0);
			}
		}

		return result;
	}

	/**
	 * Release un-necessary array to pool.
	 * 
	 * @param arr
	 */
	public void release(final byte[] arr) {
		synchronized (pool) {
			if (pool.size() < maxArrays) {
				pool.add(arr);
			}
		}
	}
}
