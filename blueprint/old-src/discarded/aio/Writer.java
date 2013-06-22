package lempel.blueprint.aio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Vector;

import lempel.blueprint.concurrent.Terminatable;
import lempel.blueprint.concurrent.Terminator;
import lempel.blueprint.log.Logger;

/**
 * Write에 사용하는 Thread
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 10. 01
 * @since 1.5
 * @last $Date$
 * @see
 */
public class Writer implements Terminatable {
	/** Logger */
	private final Logger logger = Logger.getInstance();

	/** channel/buffer mapper */
	private Mapper m;

	/** Channel Selector */
	private Selector selector = null;

	/** Thread의 지속 여부 */
	private boolean running = false;

	/**
	 * Constructor
	 * 
	 * @throws IOException
	 *             Selector 생성 실패
	 */
	public Writer() throws IOException {
		// create selector
		selector = Selector.open();

		// create channel/buffer mapper
		m = Mapper.getInstance();
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see lempel.blueprint.core.Terminatable#start()
	 */
	public void start() {
		setRunning(true);

		// Shutdown Hook에 추가
		Terminator terminator = Terminator.getInstance();
		terminator.register(this);

		Thread t = new Thread(this);
		t.setName(this.getClass().getName());
		t.setDaemon(false);
		t.start();
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see lempel.blueprint.core.Terminatable#terminate()
	 */
	public void terminate() {
		setRunning(false);

		if (selector != null) {
			try {
				selector.close();
			} catch (IOException ignored) {
			}
		}
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (isRunning()) {
			try {
				// select gains lock of selector.
				// to register channels, need to release lock periodically
				if (selector.select(10) > 0) {
					// selected when send buffer size is decreased after
					// registration
					Iterator<SelectionKey> i = selector.selectedKeys()
							.iterator();
					SelectionKey k = null;
					while (i.hasNext()) {
						k = (SelectionKey) i.next();
						i.remove();
						SocketChannel c = (SocketChannel) k.channel();

						try {
							if (k.isWritable()) {
								// get queue from mapper
								Vector<ByteBuffer> v = m.get(c.hashCode());

								// until queue went empty or send buffer became
								// full
								while (v.size() > 0) {
									// get buffer from queue
									ByteBuffer b = v.get(0);
									// try to write
									if (!tryWrite(c, b)) {
										// if send buffer is full,
										// wait for next event
										break;
									} else {
										// if sent successfully,
										// remove from queue
										v.remove(0);

										// if queue became empty,
										// cancel from selector
										if (v.size() > 0) {
											k.cancel();
										}
									}
								}
							}
						} catch (RuntimeException e) {
							m.remove(c.hashCode());
							k.cancel();
							c.close();
							logger.error(e.toString());
						} catch (IOException e) {
							m.remove(c.hashCode());
							k.cancel();
							c.close();
						}
					}
				}
			} catch (IOException e) {
				logger.error("unhandled exception");
				e.printStackTrace();
			}

			// give little time to register
			try {
				// $ANALYSIS-IGNORE
				Thread.sleep(10);
			} catch (InterruptedException e) {
				logger.error(e.toString());
			}
		}
	}

	/**
	 * register channel to write selector
	 * 
	 * @param c
	 * @throws ClosedChannelException
	 */
	private void register(SocketChannel c) throws ClosedChannelException {
		c.register(selector, SelectionKey.OP_WRITE);
	}

	/**
	 * write buffer to channel
	 * 
	 * @param c
	 * @param b
	 * @return true : successfully sent<br>
	 *         false : send buffer is full. reserved for next writable event
	 * @throws IOException
	 */
	public boolean write(SocketChannel c, ByteBuffer b) throws IOException {
		boolean result = tryWrite(c, b);

		// if failed
		if (!result) {
			// register to write selector
			register(c);
		}

		return result;
	}

	/**
	 * write buffer to channel (for internal use)
	 * 
	 * @param c
	 * @param b
	 * @return true : successfully sent<br>
	 *         false : send buffer is full
	 * @throws IOException
	 * @throws ClosedChannelException
	 */
	private boolean tryWrite(SocketChannel c, ByteBuffer b) throws IOException,
			ClosedChannelException {
		while (b.hasRemaining()) {
			// if send buffer is full
			if (c.write(b) == 0) {
				// add buffer to queue
				m.put(c.hashCode(), b);

				return false;
			}
		}

		return true;
	}

	/**
	 * Returned running to Requester
	 * 
	 * @return running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Set running
	 * 
	 * @param running
	 *            running.
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}
}
