package lempel;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.locks.ReentrantLock;

import lempel.blueprint.base.log.Logger;

/**
 * PipedStream Test
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2008. 11. 24.
 * @since 1.5
 * @last $Date$
 * @see
 */
public class PipeTest {
    private static final Logger LOGGER = Logger.getInstance();
    private static int bufferSize = 0;
    private static ReentrantLock lock = new ReentrantLock();

    public static void main(final String[] args) throws IOException {
	final PipedInputStream pis = new PipedInputStream();

	class T1 extends Thread {
	    public void run() {
		byte[] buffer = new byte[4];
		for (;;) {
		    lock.lock();

		    try {
			pis.read(buffer);

			bufferSize -= 4;
			LOGGER.println("read : buffer size = " + bufferSize);
		    } catch (Exception e) {
			LOGGER.println("shit. read exception - " + e);
		    } finally {
			lock.unlock();
		    }

		    try {
			sleep(100);
		    } catch (InterruptedException e) {
		    }
		}
	    }
	}

	class T2 extends Thread {
	    public void run() {
		PipedOutputStream pos;
		try {
		    pos = new PipedOutputStream(pis);
		} catch (IOException e) {
		    LOGGER.trace(e);
		    return;
		}

		byte[] buffer = new byte[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
			'L', 'M' };
		while (true) {
		    lock.lock();

		    try {
			pos.write(buffer);
			pos.write(buffer);
			pos.write(buffer);
			pos.flush();

			bufferSize += buffer.length * 3;
			LOGGER.println("write : buffer size = " + bufferSize);
		    } catch (Exception e) {
			LOGGER.println("shit. write exception - " + e);
		    } finally {
			lock.unlock();
		    }
		}
	    }
	}

	new T1().start();
	new T2().start();
    }
}
