/*
 * Copyright 2009 Sangmin Lee, all rights reserved.
 */
package lempel.blueprint.base.util;

import blueprint.sdk.logger.Logger;
import blueprint.sdk.util.ByteArrayFactory;
import blueprint.sdk.util.ByteArrayPool;
import blueprint.sdk.util.StringUtil;

/**
 * ByteArrayFactory Test
 *
 * @author Sangmin Lee
 * @since 2009. 1. 20.
 */
public class ByteArrayFactoryTest {
    private static final Logger LOGGER = Logger.getInstance();

    public static void main(final String[] args) {
        ByteArrayPool pool = ByteArrayFactory.getInstance(10, 10);
        byte[][] arr = new byte[100][];
        for (int i = 0; i < 100; i++) {
            arr[i] = pool.newArray();
            arr[i] = StringUtil.lpadSpace(Integer.toString(i), 10).getBytes();
        }
        for (byte[] a : arr) {
            pool.release(a);
        }

        for (int i = 0; i < 10; i++) {
            LOGGER.debug(new String(pool.newArray()));
        }
    }
}
