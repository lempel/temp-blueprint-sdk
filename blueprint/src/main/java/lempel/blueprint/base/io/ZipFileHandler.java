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
package lempel.blueprint.base.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import blueprint.sdk.util.Validator;


/**
 * Handles ZIP file
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2007. 07. 19
 * @last $Date$
 */
public class ZipFileHandler {
	private static final int BUFFER_SIZE = 10240;

	private transient File zipFile = null;

	/** true: read mode, false: write mode */
	private boolean ioMode = true;

	private transient ZipInputStream zin = null;
	private transient ZipOutputStream zout = null;

	/**
	 * @param fileName
	 *            target's path
	 * @param mode
	 *            true: read mode, false: write mode
	 * @throws FileNotFoundException
	 */
	public void open(final String fileName, final boolean mode) throws FileNotFoundException {
		zipFile = new File(fileName);
		if (mode) {
			zout = new ZipOutputStream(new FileOutputStream(zipFile));
		}

		this.ioMode = mode;
	}

	protected void open() throws FileNotFoundException {
		if (!ioMode) {
			zin = new ZipInputStream(new FileInputStream(zipFile));
		}
	}

	public void close() {
		if (ioMode) {
			try {
				if (Validator.isNotNull(zout)) {
					zout.flush();
					zout.close();
				}
			} catch (IOException ignored) {
			}
		} else {
			try {
				if (Validator.isNotNull(zin)) {
					zin.close();
				}
			} catch (IOException ignored) {
			}
		}
	}

	/**
	 * Add a file to ZIP.<br>
	 * Works for write mode only.<br>
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public void addFile(final String fileName) throws IOException {
		// write mode only
		if (!ioMode) {
			return;
		}

		ZipEntry entry = new ZipEntry(fileName);
		zout.putNextEntry(entry);

		File file = new File(fileName);
		DataInputStream din = new DataInputStream(new FileInputStream(file));
		long fileLength = file.length();
		long count = fileLength / BUFFER_SIZE;
		byte[] buffer = new byte[BUFFER_SIZE];

		for (int i = 0; i < count; i++) {
			din.readFully(buffer);
			zout.write(buffer);
			zout.flush();
		}

		long mod = fileLength - (count * BUFFER_SIZE);
		if (mod > 0) {
			buffer = new byte[(int) mod];
			din.readFully(buffer);
			zout.write(buffer);
			zout.flush();
		}

		din.close();

		zout.closeEntry();
	}

	/**
	 * Returns all file names.<br>
	 * Works for read mode only.<br>
	 * 
	 * @return
	 * @throws IOException
	 */
	public String[] getFileNames() throws IOException {
		String[] result = null;

		// read mode only
		if (ioMode) {
			open();

			ArrayList<String> list = new ArrayList<String>(1000);
			while (true) {
				ZipEntry entry = zin.getNextEntry();
				if (entry == null) {
					break;
				} else {
					list.add(entry.getName());
					zin.closeEntry();
				}
			}
			result = new String[list.size()];
			list.toArray(result);

			close();
		}

		return result;
	}

	/**
	 * Extract a file & save.<br>
	 * Works for read mode only.<bR>
	 * 
	 * @param sourcePath
	 * @param targetPath
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void getFile(final String sourcePath, final String targetPath) throws FileNotFoundException, IOException {
		// read mode only
		if (ioMode) {
			open();

			while (true) {
				ZipEntry entry = zin.getNextEntry();
				if (entry == null) {
					break;
				}

				if (sourcePath.equals(entry.getName())) {
					saveFile(entry, targetPath);
				}
				zin.closeEntry();
			}

			close();
		}
	}

	protected void saveFile(ZipEntry entry, String fileName) throws FileNotFoundException, IOException {
		// read mode only
		if (ioMode) {
			return;
		}

		String saveFileName = fileName.replace('/', System.getProperty("file.separator").charAt(0));
		File saveFile = new File(saveFileName);

		if (entry.isDirectory()) {
			saveFile.mkdirs();
			return;
		}

		DataOutputStream dout = new DataOutputStream(new FileOutputStream(saveFile, false));

		byte[] buffer = new byte[BUFFER_SIZE];

		int readLength = 0;

		while ((readLength = zin.read(buffer)) > 0) {
			dout.write(buffer, 0, readLength);
		}
		dout.flush();
		dout.close();
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (Validator.isNotNull(zin)) {
				zin.close();
			}
		} catch (Exception ignored) {
		} finally {
			zin = null;
		}
		try {
			if (Validator.isNotNull(zout)) {
				zout.close();
			}
		} catch (Exception ignored) {
		} finally {
			zout = null;
		}
		zipFile = null;

		super.finalize();
	}
}
