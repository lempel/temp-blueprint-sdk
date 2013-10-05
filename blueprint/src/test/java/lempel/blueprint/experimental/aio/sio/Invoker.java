/*
 * Copyright 2008 Sangmin Lee, all rights reserved.
 */
package lempel.blueprint.experimental.aio.sio;

import java.io.IOException;

/**
 * Invokes SIO Acceptor
 * 
 * @author Sangmin Lee
 * @version $Revision$
 * @create 2008. 12. 3.
 * @since 1.5
 * @last $Date$
 * @see
 */
public class Invoker {
    /**
     * Entry Point
     * 
     * @param args
     * @throws IOException
     */
    public static void main(final String[] args) throws IOException {
	new Acceptor(1112).start();
    }
}
