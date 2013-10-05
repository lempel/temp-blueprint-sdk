/*
 * Copyright 2009 Sangmin Lee, all rights reserved.
 */
package lempel;

import lempel.blueprint.base.log.Logger;
import lempel.blueprint.base.util.TimeStamper;

/**
 * 
 * @author Sangmin Lee
 * @version $Revision$
 * @since 2009. 2. 24.
 * @last $Date$
 */
public class ForEachTest {
    private static final Logger LOGGER = Logger.getInstance();

    public static void main(final String[] args) {
	String[] temp = new String[2];
	for (String y : temp) {
	    y = TimeStamper.getTimeStamp();
	}

	for (String y : temp) {
	    LOGGER.println(y);
	}

	byte[] data = new byte[] { 0x01, 0x02 };
	for (byte b : data) {
	    b = 0x03;
	}
	for (byte b : data) {
	    LOGGER.println(b);
	}
    }
}
