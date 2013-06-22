/*
 License:

 blueprint-sdk is licensed under the terms of Eclipse Public License(EPL) v1.0
 (http://www.eclipse.org/legal/epl-v10.html)


 Distribution:

 International - http://code.google.com/p/blueprint-sdk
 South Korea - http://lempel.egloos.com


 Background:

 blueprint-sdk is a java software development kit to protect other open source
 softwares' licenses. It's intended to provide light weight APIs for blueprints.
 Well... at least trying to.

 There are so many great open source projects now. Back in year 2000, there
 were not much to use. Even JDBC drivers were rare back then. Naturally, I have
 to implement many things by myself. Especially dynamic class loading, networking,
 scripting, logging and database interactions. It was time consuming. Now I can
 take my picks from open source projects.

 But I still need my own APIs. Most of my clients just don't understand open
 source licenses. They always want to have their own versions of open source
 projects but don't want to publish derivative works. They shouldn't use open
 source projects in the first place. So I need to have my own open source project
 to be free from derivation terms and also as a mediator between other open
 source projects and my client's requirements.

 Primary purpose of blueprint-sdk is not to violate other open source project's
 license terms.


 To commiters:

 License terms of the other software used by your source code should not be
 violated by using your source code. That's why blueprint-sdk is made for.
 Without that, all your contributions are welcomed and appreciated.
 */
package lempel.blueprint.base.aio.session;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

import lempel.blueprint.base.aio.SelectorLoadBalancer;
import lempel.blueprint.base.aio.protocol.EchoProtocol;

/**
 * Echo Session
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2008. 12. 1.
 * @last $Date$
 */
public class EchoSession extends Session {
	public EchoSession(final SocketChannel channel, final ConcurrentHashMap<Integer, Session> map,
			final Integer bufferSize, final SelectorLoadBalancer selectorLB) throws IOException {
		super(channel, map, bufferSize, selectorLB);

		protocol = new EchoProtocol(wrapper, selectorLB);
	}

	public void process() throws IOException {
		// read
		byte[] request = protocol.read(readBuffer);

		// write
		// don't have call protocol.write but not prohibited.
		write(request);
	}
}
