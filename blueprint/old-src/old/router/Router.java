package lempel.old.router;

import lempel.blueprint.log.*;
import lempel.old.framework.*;


/**
 * Router Process
 * 
 * @author Sang-min Lee
 * @since 2005.5.20.
 * @version 2005.5.20.
 */
public class Router {
	public static void main(String[] args) {
		Logger log = null;

		if (args.length != 1) {
			System.out
					.println("Usage: java com.bluePrint.router.Router <property XML file>");
			System.exit(1);
		}

		XMLDocument doc = new XMLDocument();
		try {
			doc.load(args[0]);
		} catch (Exception ex) {
			System.out.println("Can't read property XML file - " + ex);
			ex.printStackTrace();
			System.exit(2);
		}

		log = Logger.getInstance(doc);

		try {
			RouterAcceptor acceptor = new RouterAcceptor(doc);
			acceptor.start();
		} catch (Exception ex) {
			log.println(LogLevel.ERR, "Can't start process - " + ex);
			ex.printStackTrace();
		}
	}
}
