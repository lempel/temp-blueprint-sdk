package lempel.blueprint.aio.example.httpserver;

import java.io.IOException;

import lempel.blueprint.aio.ProactorHelper;

/**
 * Entry Point
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 10. 04
 * @since 1.5
 * @last $Date$
 * @see
 */
public class HttpServer {
	/**
	 * Entry Point
	 * 
	 * @param args
	 * @throws IOException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public static void main(String[] args) throws IOException,
			SecurityException, NoSuchMethodException {
		ProactorHelper helper = new ProactorHelper(HttpWorker.class, 1,
				SimpleHttpReactor.class, 8080);
		helper.start();
	}
}
