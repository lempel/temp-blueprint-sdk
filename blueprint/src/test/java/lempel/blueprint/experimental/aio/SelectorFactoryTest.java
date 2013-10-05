/*
 * Copyright 2008 Sangmin Lee, all rights reserved.
 */
package lempel.blueprint.experimental.aio;

import java.io.IOException;
import java.nio.channels.Selector;

import lempel.blueprint.base.aio.SelectorFactory;
import lempel.blueprint.base.log.Logger;

/**
 * Tests Selector Factory
 * 
 * @author Sangmin Lee
 * @version $Revision$
 * @create 2008. 12. 5.
 * @since 1.5
 * @last $Date$
 * @see
 */
public class SelectorFactoryTest {
    public static void main(final String[] args) throws IOException {
	final Logger logger = Logger.getInstance();
	long start;
	long end;

	int volume = 1000;
	for (int x = 0; x < 20; x++) {
	    start = System.nanoTime();
	    Selector[] sels = new Selector[volume];
	    for (int i = 0; i < volume; i++) {
		sels[i] = SelectorFactory.get();
	    }
	    for (Selector sel : sels) {
		SelectorFactory.release(sel);
	    }
	    end = System.nanoTime();
	    logger.debug(end - start + " : " + SelectorFactory.size());
	}
    }
}
