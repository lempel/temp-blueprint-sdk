/*
 * Copyright 2008 Simon Lee, all rights reserved.
 */
package lempel.blueprint.experimental.aio.sio;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import lempel.blueprint.base.log.Logger;
import uka.transport.MemoryOutputStream;

/**
 * Client Session for comparison
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2008. 12. 3.
 * @since 1.5
 * @last $Date$
 * @see
 */
public class Session extends Thread {
    private static final Logger LOGGER = Logger.getInstance();

    private transient final Socket sock;
    private transient final DataInputStream dis;
    private transient final DataOutputStream dos;

    public Session(final Socket sock) throws IOException {
	super();
	this.sock = sock;
	this.sock.setSoTimeout(5000);
	dis = new DataInputStream(sock.getInputStream());
	dos = new DataOutputStream(sock.getOutputStream());
    }

    public void run() {
	MemoryOutputStream mos = new MemoryOutputStream();
	for (;;) {
	    try {
		byte[] data = new byte[dis.available()];
		if (data.length == 0) {
		    try {
			Thread.sleep(10);
		    } catch (InterruptedException ignored) {
		    }
		} else {
		    dis.readFully(data);
		    mos.write(data);

		    if (isEOF(mos.getBuffer())) {
			String msg = "OK. I got It. - " + System.currentTimeMillis();
			dos.write(msg.getBytes());
			dos.flush();

			try {
			    sock.close();
			} catch (IOException ignored) {
			}

			return;
		    }
		}
	    } catch (IOException e) {
		LOGGER.error(e);
		LOGGER.trace(e);
	    }
	}
    }

    /**
     * HTTP 요청을 다 읽어 냈는가 확인<br>
     * (마지막으로 읽은 내용이 연속된 두개의 new line이면 true)<br>
     * 
     * @param data
     * @return
     */
    protected boolean isEOF(final byte[] data) {
	boolean result = false;
	int length = data.length;

	if (length >= 2 && (data[length - 2] == 0x0a && data[length - 1] == 0x0a)) {
	    result = true;
	}
	if (length >= 4
		&& (data[length - 4] == 0x0d && data[length - 3] == 0x0a
			&& data[length - 2] == 0x0d && data[length - 1] == 0x0a)) {
	    result = true;
	}

	return result;
    }
}
