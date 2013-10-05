/*
 * Copyright 2008 Sangmin Lee, all rights reserved.
 */
package lempel.blueprint.experimental.aio;

import lempel.blueprint.base.aio.Proactor;
import lempel.blueprint.base.aio.Reactor;
import lempel.blueprint.base.aio.Service;
import lempel.blueprint.base.aio.session.EchoSession;
import lempel.blueprint.base.aio.session.SimpleHttpSession;
import lempel.blueprint.base.log.LogLevel;
import lempel.blueprint.base.log.Logger;

/**
 * Tests Acceptor
 * 
 * @author Sangmin Lee
 * @version $Revision$
 * @create 2008. 11. 26.
 * @since 1.5
 * @last $Date$
 * @see
 */
public class AcceptorTest {
    public static void main(final String[] args) {
	Logger logger = Logger.getInstance();

	try {
	    logger.setLogLevel(LogLevel.INF);
	    logger.setTracing(true);

	    Proactor proactor1 = new Proactor(Reactor.class, 3, 1, SimpleHttpSession.class, 1024);
	    Service service1 = new Service("service 1", proactor1);
	    service1.getIpFilter().allow("127.0.0.1");
	    service1.bind("localhost", 1112, true, 5);

	    Proactor proactor2 = new Proactor(Reactor.class, 3, 1, EchoSession.class, 1024);
	    Service service2 = new Service("service 2", proactor2);
	    service1.getIpFilter().allow("127.0.0.1");
	    service2.bind("127.0.0.1", 1113, true, 5);
	} catch (Exception e) {
	    logger.trace(e);
	}
    }
}
