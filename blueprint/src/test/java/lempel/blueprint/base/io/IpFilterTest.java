/*
 * Copyright 2008 Sangmin Lee, all rights reserved.
 */
package lempel.blueprint.base.io;

import lempel.blueprint.base.log.Logger;

/**
 * IpFilter Test
 * 
 * @author Sangmin Lee
 * @version $Revision$
 * @since 2008. 12. 12.
 * @last $Date$
 */
public class IpFilterTest {
    private static final Logger LOGGER = Logger.getInstance();

    public static void main(final String[] args) {
	LOGGER.setTracing(true);

	IpFilter ipf1 = new IpFilter();

	String ip1 = "127.0.0.2";
	LOGGER.println(ipf1.isAllowed(ip1));
	LOGGER.println(ipf1.isBanned(ip1));

	String ip2 = "127.0.0.1";
	ipf1.allow(ip2);
	LOGGER.println(ipf1.isAllowed(ip2));
	LOGGER.println(ipf1.isBanned(ip2));
	LOGGER.println(ipf1.isAllowed(ip1));
	LOGGER.println(ipf1.isBanned(ip1));

	ipf1.allow("128.128.128.*");
	LOGGER.println(ipf1.isAllowed("128.128.128.1"));
	LOGGER.println(ipf1.isBanned("128.128.128.255"));

	ipf1.ban("128.128.1.*");
	LOGGER.println(ipf1.isAllowed("128.128.1.1"));
	LOGGER.println(ipf1.isBanned("128.128.1.255"));

	ipf1.ban("*");
	LOGGER.println(ipf1.isAllowed("128.128.1.1"));
	LOGGER.println(ipf1.isBanned("123.123.1.1"));
    }
}
