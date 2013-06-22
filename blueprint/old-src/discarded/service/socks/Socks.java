package lempel.blueprint.service.socks;

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.xml.parsers.ParserConfigurationException;

import lempel.blueprint.aio.ProactorHelper;
import lempel.blueprint.config.XmlConfig;
import lempel.blueprint.log.Logger;
import lempel.blueprint.util.GlobalContext;

import org.xml.sax.SAXException;

/**
 * Socks Server<br>
 * (current version is 5)<br>
 * *주의* 현재는 read selector를 1개만 사용하는 관계로 bandwidth를 많이 사용하는 connection이 있는 경우 다른
 * connection의 속도가 저하되는 문제가 있다.<br>
 * ex) 웹페이지의 Streaming Media<br>
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 10. 29
 * @since 1.5
 * @last $Date$
 * @see
 */
public class Socks {
	/** logger */
	private final Logger logger = Logger.getInstance();

	/**
	 * Entry Point
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			XmlConfig config = new XmlConfig("socks.xml");
			Class<?> reactor = Class.forName(config
					.getString("/socks/reactor/@class"));
			int workerCount = config.getInt("/socks/worker/@count");
			String address = config.getString("/socks/bind/@address");
			int port = config.getInt("/socks/bind/@port");

			GlobalContext gctx = GlobalContext.getInstance();
			gctx.put(XmlConfig.class.getName(), config);

			InetSocketAddress inetAddress = null;
			if ("*".equals(address)) {
				inetAddress = new InetSocketAddress(port);
			} else {
				inetAddress = new InetSocketAddress(address, port);
			}

			Connector con = new Connector(workerCount);
			gctx.put(Connector.class.getName(), con);

			ProactorHelper helper = new ProactorHelper(null, 0, reactor,
					inetAddress);
			helper.start();
		} catch (NoSuchMethodException e) {
			logger.error(e.toString());
		} catch (IOException e) {
			logger.error(e.toString());
			logger.trace(e);
		} catch (ClassNotFoundException e) {
			logger.error(e.toString());
		} catch (ParserConfigurationException e) {
			logger.error(e.toString());
		} catch (SAXException e) {
			logger.error(e.toString());
		}
	}
}
