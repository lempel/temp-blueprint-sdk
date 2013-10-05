/*
 * Copyright 2009 Sangmin Lee, all rights reserved.
 */
package lempel.blueprint.base.util;

import lempel.blueprint.base.log.Logger;

/**
 * ByteArrayFactory Test
 * 
 * @author Sangmin Lee
 * @version $Revision$
 * @since 2009. 1. 20.
 * @last $Date$
 */
public class ByteArrayFactoryTest {
    private static final Logger LOGGER = Logger.getInstance();

    public static void main(final String[] args) {
	ByteArrayPool pool = ByteArrayFactory.getInstance(10, 10);
	byte[][] arr = new byte[100][];
	for (int i = 0; i < 100; i++) {
	    arr[i] = pool.newArray();
	    arr[i] = StringUtil.lpadSapce(Integer.toString(i), 10).getBytes();
	}
	for (byte[] a : arr) {
	    pool.release(a);
	}

	for (int i = 0; i < 10; i++) {
	    LOGGER.debug(new String(pool.newArray()));
	}
    }
}
