package lempel.blueprint.app.tracer;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

public class TracingProperties {
	private boolean debug = false;
	private static final String propsFileName = "trace.properties";
	private boolean instrument = false;
	private boolean trace = false;
	private boolean fastTrace = false;
	private boolean realoadProperties = false;
	//private boolean propsRead = false;
	private String logFileName = "trace.txt";
	private ArrayList<String> classList = new ArrayList<String>();
	private ArrayList<String> excludeClassList = new ArrayList<String>();

	private static Object lock = new Object();
	private static TracingProperties inst;

	// once object is instantiated, props WILL have been read and there will
	// be a thead reloading them every 4 seconds.
	private TracingProperties() {
		readProps();
		if (realoadProperties) {
			new AutoReloaderThread().start();
		}
	}

	// this thread is solely for the purpose of reloading this properties
	// file every 4 seconds, so that it can be edited and then the logging
	// will pick up changes without user having to terminate the entire
	// debugging session or VM that they are tracing, in order to be able
	// to toggle tracing on and off. I did NOT want to drag any GUI code into
	// this logger module, so polling the properties file IS THE IDEAL thing to
	// do here even though in almost every other case it would be a bad idea.
	public class AutoReloaderThread extends Thread {
		public AutoReloaderThread() {
			setDaemon(true);
		}

		public void run() {
			while (true) {
				readDelay();
				readProps();
			}
		}
	}

	private void readDelay() {
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static TracingProperties getInstance() {
		synchronized (lock) {
			if (inst == null) {
				inst = new TracingProperties();
			}
		}
		return (inst);
	}

	private void readProps() {
		synchronized (lock) {
			Properties properties = new Properties();
			try {
				if (debug)
					System.out.println("reading trace properties.");

				File file = new File(propsFileName);
				if (!file.isFile()) {
					setDisabled();
					return;
				}
				System.out.println("Reading properties: "
						+ file.getAbsolutePath());
				FileInputStream is = new FileInputStream(propsFileName);
				properties.load(is);
				is.close();

				String instrumentProp = properties.getProperty("instrument");
				logFileName = properties.getProperty("logFile");
				instrument = (instrumentProp != null && instrumentProp
						.equalsIgnoreCase("true"));

				String traceProp = properties.getProperty("trace");
				trace = (traceProp != null && traceProp
						.equalsIgnoreCase("true"));

				String fastTraceProp = properties.getProperty("fasttrace");
				fastTrace = (fastTraceProp != null && fastTraceProp
						.equalsIgnoreCase("true"));

				String realoadPropertiesProp = properties
						.getProperty("realoadProperties");
				realoadProperties = (realoadPropertiesProp != null && realoadPropertiesProp
						.equalsIgnoreCase("true"));

				String classListProp = properties.getProperty("classList");
				parseList(classList, classListProp);

				String exClassListProp = properties
						.getProperty("excludeClassList");
				parseList(excludeClassList, exClassListProp);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("property read failed.");
				setDisabled();
			}
		}
	}

	private void parseList(ArrayList<String> aList, String inputList) {
		// 히발놈 이정도 체크는 해줘야지 ㅡㅡ
		if (inputList == null) {
			return;
		}

		// remove old content if there is any
		aList.clear();

		ArrayList<String> list = makeArrayList(inputList, " ");
		for (int i = 0; i < list.size(); i++) {
			String name = list.get(i);
			name = name.replace("\n", "");
			name = name.replace("\r", "");
			name = name.replace("\t", "");
			name = name.replace(".", "/");

			if (name.length() > 0) {
				aList.add(name);
			}
		}
	}

	// duplicated from XString class to avoid dependency
	private static ArrayList<String> makeArrayList(String input,
			String strDelimiter) {
		ArrayList<String> v = new ArrayList<String>();
		StringTokenizer t = new StringTokenizer(input, strDelimiter, true);
		String strToken;
		boolean bTokFlag = true;
		while (t.hasMoreTokens()) {
			strToken = t.nextToken();
			if (!strToken.equals(strDelimiter)) {
				v.add(strToken);
				bTokFlag = false;
			} else {
				if (bTokFlag)
					v.add("");
				bTokFlag = true;
			}
		}
		if (bTokFlag)
			v.add("");
		return (v);
	}

	public void setDisabled() {
		instrument = false;
		trace = false;
		fastTrace = false;
	}

	public boolean shouldTraceClass(String className) {
		synchronized (lock) {
			boolean trace = false;
			for (int i = 0; i < classList.size(); i++) {
				if (className.startsWith(classList.get(i))) {
					trace = true;
					break;
				}
			}

			// if the class matched one to trace, then make sure it also does
			// NOT match one in the exclusions list.
			if (trace) {
				for (int i = 0; i < excludeClassList.size(); i++) {
					if (className.startsWith(excludeClassList.get(i))) {
						trace = false;
						break;
					}
				}
			}
			return (trace);
		}
	}

	public boolean isInstrument() {
		return (instrument);
	}

	public boolean isTrace() {
		return trace;
	}

	public boolean isFastTrace() {
		return fastTrace;
	}

	public String getLogFileName() {
		return logFileName;
	}

	public void setLogFileName(String logFileName) {
		this.logFileName = logFileName;
	}
}
