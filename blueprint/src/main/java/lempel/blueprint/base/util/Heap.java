package lempel.blueprint.base.util;

import java.util.Comparator;

/**
 * A heap-based priority queue, without any concurrency control
 * 
 * @author Doug Lea
 * @since 1998.6.11.
 */
public class Heap {
	protected Object[] nodes; // the tree nodes, packed into an array

	protected int count = 0; // number of used slots

	protected final Comparator<Object> cmp; // for ordering

	/**
	 * Create a Heap with the given initial capacity and comparator
	 * 
	 * @exception IllegalArgumentException
	 *                if capacity less or equal to zero
	 */

	public Heap(final int capacity, final Comparator<Object> cmp) throws IllegalArgumentException {
		if (capacity <= 0) {
			throw new IllegalArgumentException();
		}
		this.nodes = new Object[capacity];
		this.cmp = cmp;
	}

	/**
	 * Create a Heap with the given capacity, and relying on natural ordering.
	 */

	public Heap(final int capacity) {
		this(capacity, null);
	}

	/** perform element comparisons using comparator or natural ordering * */
	@SuppressWarnings("unchecked")
	protected int compare(final Object objA, final Object pbjB) {
		int result;
		if (cmp == null) {
			result = ((Comparable) objA).compareTo(pbjB);
		} else {
			result = cmp.compare(objA, pbjB);
		}
		return result;
	}

	// indexes of heap parents and children
	protected final int parent(final int val) {
		return (val - 1) / 2;
	}

	protected final int left(final int val) {
		return 2 * val + 1;
	}

	protected final int right(final int val) {
		return 2 * (val + 1);
	}

	/**
	 * insert an element, resize if necessary
	 */
	public void insert(final Object obj) {
		synchronized (this) {
			if (count >= nodes.length) {
				int newcap = 3 * nodes.length / 2 + 1;
				Object[] newnodes = new Object[newcap];
				System.arraycopy(nodes, 0, newnodes, 0, nodes.length);
				nodes = newnodes;
			}

			int cnt = count;
			++count;
			while (cnt > 0) {
				int par = parent(cnt);
				if (compare(obj, nodes[par]) < 0) {
					nodes[cnt] = nodes[par];
					cnt = par;
				} else {
					break;
				}
			}
			nodes[cnt] = obj;
		}
	}

	/**
	 * Return and remove least element, or null if empty
	 */

	public Object extract() {
		Object result;
		synchronized (this) {
			if (count < 1) {
				result = null;
			} else {
				int kkk = 0; // take element at root;
				result = nodes[kkk];
				--count;
				Object xxx = nodes[count];
				nodes[count] = null;
				for (;;) {
					int lll = left(kkk);
					if (lll >= count) {
						break;
					} else {
						int rrr = right(kkk);
						int child = (rrr >= count || compare(nodes[lll], nodes[rrr]) < 0) ? lll : rrr;
						if (compare(xxx, nodes[child]) > 0) {
							nodes[kkk] = nodes[child];
							kkk = child;
						} else {
							break;
						}
					}
				}
				nodes[kkk] = xxx;
			}
		}
		return result;
	}

	/** Return least element without removing it, or null if empty * */
	public Object peek() {
		synchronized (this) {
			return (count > 0) ? nodes[0] : null;
		}
	}

	/** Return number of elements * */
	public int size() {
		synchronized (this) {
			return count;
		}
	}

	/** remove all elements * */
	public void clear() {
		synchronized (this) {
			for (int i = 0; i < count; ++i) {
				nodes[i] = null;
			}

			count = 0;
		}
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		nodes = null;

		super.finalize();
	}
}