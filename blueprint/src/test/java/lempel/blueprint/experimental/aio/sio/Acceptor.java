/*
 * Copyright 2008 Sangmin Lee, all rights reserved.
 */
package lempel.blueprint.experimental.aio.sio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import lempel.blueprint.base.log.Logger;

/**
 * SIO Acceptor for comparison
 * 
 * @author Sangmin Lee
 * @version $Revision$
 * @create 2008. 12. 3.
 * @since 1.5
 * @last $Date$
 * @see
 */
public class Acceptor extends Thread {
    private static final Logger LOGGER = Logger.getInstance();
    private final transient ServerSocket ssock;

    public Acceptor(final int port) throws IOException {
	super();
	ssock = new ServerSocket(port);
    }

    public void run() {
	for (;;) {
	    try {
		Socket sock = ssock.accept();
		new Session(sock).start();
	    } catch (IOException e) {
		LOGGER.error(e);
		LOGGER.trace(e);
	    }
	}
    }
}
