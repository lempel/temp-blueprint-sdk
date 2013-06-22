package lempel.blueprint.aio.example.httpserver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Map.Entry;

import lempel.blueprint.aio.Reactor;
import lempel.blueprint.concurrent.Worker;
import lempel.blueprint.log.Logger;

/**
 * HTTP client session worker
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 10. 04
 * @since 1.5
 * @last $Date$
 * @see
 */
public class HttpWorker extends Worker {
	/** Logger */
	private final Logger logger = Logger.getInstance();

	public HttpWorker(Vector<Object> jobQueue, Vector<Worker> sleepingWorkers) {
		super(jobQueue, sleepingWorkers);
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see lempel.blueprint.core.Worker#process(lempel.blueprint.aio.Reactor)
	 */
	protected void process(Object clientObject) {
		Reactor client = (Reactor) clientObject;
		try {
			String req = new String(client.receive());

			StringTokenizer st = new StringTokenizer(req, "\r\n:");
			HashMap<String, String> headers = new HashMap<String, String>();
			for (int i = 0; i < st.countTokens() / 2; i++) {
				String key = st.nextToken();
				String val = st.nextToken();
				headers.put(key, val);
			}

			StringBuffer sb = new StringBuffer(10240);

			// Write header
			sb.append("HTTP/1.0 200 OK\r\n");
			sb.append("Content-Type: text/html\r\n");
			sb.append("Server: blueprint Example\r\n");
			sb.append("\r\n");

			// Write content
			sb.append("<html><head></head><body>");
			sb.append("<h3>Request Summary : </h3>");
			sb
					.append("<table border=\"1\"><tr><th>Key</th><th>Value</th></tr>");

			Iterator<Entry<String, String>> it = headers.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> e = it.next();
				sb.append("<tr><td>").append(e.getKey()).append("</td><td>")
						.append(e.getValue()).append("</td></tr>");
			}
			sb.append("</table>");

			for (int i = 0; i < 1024; i++) {
				sb.append("this is line: ").append(i).append("<br/>");
			}

			sb.append("</body></html>");

			client.sendRawData(sb.toString().getBytes());
		} catch (Throwable e) {
			logger.error("http " + e.toString());
		}

		client.terminate();
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see lempel.blueprint.core.Terminatable#terminate()
	 */
	public void terminate() {
		setRunning(false);
	}
}
