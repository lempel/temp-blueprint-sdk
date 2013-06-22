package lempel.blueprint.util;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;

// $ANALYSIS-IGNORE
/**
 * Simple 'Tail' implementation
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2004. 11. 12
 * @since 1.5
 * @last $Date$
 * @see
 */
public class Tail extends Thread {
	/** Char 상수 : 'f' */
	private static final char CHAR_F = 'f';

	/** String 상수 : "Tail: Can't read target file" */
	private static final String ERR_MSG_0 = "Tail: Can't read target file";
	/** String 상수 : ": " */
	private static final String LOG_MSG_0 = ": ";
	/** String 상수 : "10" */
	private static final String TEN = "10";
	/**
	 * String 상수 : "Usage: java lempel.blueprint.util.Tail [-[line #][f]]
	 * fileName"
	 */
	private static final String USAGE = "Usage: java lempel.blueprint.util.Tail [-[line #][f]] fileName";

	/** fileName */
	protected String fileName = null;

	/** lineCount */
	protected int lineCount = 0;

	/** follow */
	protected boolean follow = false;

	/**
	 * 
	 * @param fileName
	 *            추적할 file 이름
	 */
	public Tail(String fileName) {
		this(fileName, TEN, false);
	}

	/**
	 * 
	 * @param fileName
	 *            추적할 file 이름
	 * @param lineCount
	 *            미리 읽을 line 수
	 * @param follow
	 *            f옵션 여부
	 */
	public Tail(String fileName, String lineCount, boolean follow) {
		this.fileName = fileName;
		this.lineCount = Integer.parseInt(lineCount);
		this.follow = follow;

		setDaemon(false);
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see java.lang.Thread#run()
	 */
	// $ANALYSIS-IGNORE
	public void run() {
		File file = new File(fileName);
		if (!file.exists()) {
			return;
		}

		// $ANALYSIS-IGNORE
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			Vector<String> readAhead = new Vector<String>(100, 10);

			// 미리 읽을 line이 있다면 읽어온다
			// $ANALYSIS-IGNORE
			try {
				while ((line = br.readLine()) != null) {
					if (lineCount > 0) {
						readAhead.add(line);

						if (readAhead.size() > lineCount) {
							readAhead.remove(0);
						}
					}
				}
			} catch (EOFException ignored) {
			}

			// 미리 읽은 line들을 출력
			int readAheadSize = readAhead.size();
			for (int i = 0; i < readAheadSize; i++) {
				System.out.println(StringUtil.concatString(fileName, LOG_MSG_0,
						readAhead.get(i).toString()));
			}

			while (follow) {
				// $ANALYSIS-IGNORE
				try {
					while ((line = br.readLine()) != null) {
						System.out.println(StringUtil.concatString(fileName,
								LOG_MSG_0, line));
					}
				} catch (EOFException exEof) {
				} finally {
				}

				try {
					// $ANALYSIS-IGNORE
					sleep(1000);
				} catch (InterruptedException ignored) {
				}
			}
		} catch (Exception ex) {
			System.err.println(ERR_MSG_0);
			ex.printStackTrace();
		}
	}

	/**
	 * Entry Point
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1 && args.length != 2) {
			System.out.println(USAGE);
			System.exit(1);
		}

		Tail tail = null;
		if (args.length == 1) {
			tail = new Tail(args[0]);
		} else {
			// f 옵션이 있는가 확인
			if (args[0].toLowerCase().charAt(args[0].length() - 1) == CHAR_F) {
				// 미리 읽을 line 수가 있는가 확인
				if (args[0].length() >= 3) {
					tail = new Tail(args[1], args[0].substring(1, args[0]
							.length() - 1), true);
				} else {
					tail = new Tail(args[1], TEN, true);
				}
			} else {
				tail = new Tail(args[1],
						args[0].substring(1, args[0].length()), false);
			}
		}

		tail.start();
	}
}