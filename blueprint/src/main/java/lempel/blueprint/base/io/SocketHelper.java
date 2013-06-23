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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import blueprint.sdk.logger.Logger;


/**
 * Some helpful APIs for Socket handling
 * 
 * @author Simon Lee
 * @since 2012. 10. 11.
 */
public class SocketHelper {
	private static Logger logger = Logger.getInstance();

	private static boolean lingerFlag = true;
	private static int lingerValue = 3;
	private static boolean trace = false;

	/** associated socket */
	public Socket socket;
	/** input stream from socket */
	public DataInputStream input;
	/** output stream for socket */
	public DataOutputStream output;

	private SocketHelper() {
		super();
	}

	public static SocketHelper newInstance(Socket sock) throws IOException {
		SocketHelper result = new SocketHelper();
		result.socket = sock;
		result.input = new DataInputStream(sock.getInputStream());
		result.output = new DataOutputStream(sock.getOutputStream());

		result.setThroughputMode();

		return result;
	}

	/**
	 * Close all resources
	 * 
	 * @param holder
	 */
	public void close() {
		close(input, output, socket);
	}

	/**
	 * Close given resources
	 * 
	 * @param input
	 * @param output
	 * @param socket
	 */
	public static void close(InputStream input, OutputStream output, Socket socket) {
		if (input != null) {
			try {
				input.close();
			} catch (IOException ignored) {
			}
		}

		if (output != null) {
			try {
				output.flush();
			} catch (IOException ignored) {
			}
			try {
				output.close();
			} catch (IOException ignored) {
			}
		}

		if (socket != null && !socket.isClosed()) {
			try {
				socket.close();
			} catch (IOException ignored) {
			}
		}
	}

	/**
	 * Set linger flag and value
	 * 
	 * @throws IOException
	 */
	public void setLinger() throws IOException {
		setLinger(socket);
	}

	/**
	 * Call socket's setSoLinger
	 * 
	 * @param flag
	 * @param value
	 * @throws IOException
	 */
	public void setSoLinger(boolean flag, int value) throws IOException {
		socket.setSoLinger(flag, value);
	}

	/**
	 * @return set true to trace I/O of socket
	 */
	public static boolean isTrace() {
		return trace;
	}

	/**
	 * @param trace
	 *            true to trace I/O of socket
	 */
	public static void setTrace(boolean trace) {
		SocketHelper.trace = trace;
	}

	/**
	 * Set linger flag and value to give Socket
	 * 
	 * @param sock
	 * @throws IOException
	 */
	public static void setLinger(Socket sock) throws IOException {
		sock.setSoLinger(lingerFlag, lingerValue);
	}

	/**
	 * Set socket's properties for less cost
	 * 
	 * @throws IOException
	 */
	public void setLowCostMode() throws IOException {
		setLowCostMode(socket);
	}

	/**
	 * Set socket's properties for less cost
	 * 
	 * @param sock
	 * @throws IOException
	 */
	public static void setLowCostMode(Socket sock) throws IOException {
		sock.setTrafficClass(0x02);
	}

	/**
	 * Set socket's properties for more reliability
	 * 
	 * @throws IOException
	 */
	public void setReliabilityMode() throws IOException {
		setReliabilityMode(socket);
	}

	/**
	 * Set socket's properties for more reliability
	 * 
	 * @param sock
	 * @throws IOException
	 */
	public static void setReliabilityMode(Socket sock) throws IOException {
		sock.setTrafficClass(0x04);
	}

	/**
	 * Set socket's properties for less delay
	 * 
	 * @throws IOException
	 */
	public void setLowDelayMode() throws IOException {
		setLowDelayMode(socket);
	}

	/**
	 * Set socket's properties for less delay
	 * 
	 * @param sock
	 * @throws IOException
	 */
	public static void setLowDelayMode(Socket sock) throws IOException {
		sock.setTrafficClass(0x10);
	}

	/**
	 * Set socket's properties for more throughput (low cost + low delay)
	 * 
	 * @throws IOException
	 */
	public void setThroughputMode() throws IOException {
		setThroughputMode(socket);
	}

	/**
	 * Set socket's properties for more throughput
	 * 
	 * @param sock
	 * @throws IOException
	 */
	public static void setThroughputMode(Socket sock) throws IOException {
		sock.setTrafficClass(0x08);
	}

	/**
	 * @return value of lingerFlag
	 */
	public static boolean isLingerFlag() {
		return lingerFlag;
	}

	/**
	 * @param lingerFlag
	 *            value for lingerFlag
	 */
	public static void setLingerFlag(boolean flag) {
		lingerFlag = flag;
	}

	/**
	 * @return value of lingerValue
	 */
	public static int getLingerValue() {
		return lingerValue;
	}

	/**
	 * @param lingerValue
	 *            value for lingerValue
	 */
	public static void setLingerValue(int value) {
		lingerValue = value;
	}

	/**
	 * Set timeout to socket
	 * 
	 * @param value
	 *            timeout value (msec)
	 * @throws SocketException
	 */
	public void setSoTimeout(int value) throws SocketException {
		socket.setSoTimeout(value);
	}

	/**
	 * Get timeout of socket
	 * 
	 * @param value
	 *            timeout value (msec)
	 * @throws SocketException
	 */
	public int getSoTimeout() throws SocketException {
		return socket.getSoTimeout();
	}

	/**
	 * Set TcpNoDelay flag
	 * 
	 * @param value
	 * @throws IOException
	 */
	public void setTcpNoDelay(boolean value) throws IOException {
		socket.setTcpNoDelay(value);
	}

	/**
	 * Get TcpNoDelay flag
	 * 
	 * @return
	 */
	public boolean getTcpNoDelay() throws IOException {
		return socket.getTcpNoDelay();
	}

	/**
	 * Call output.write
	 * 
	 * @param data
	 * @throws IOException
	 */
	public void write(byte[] data) throws IOException {
		output.write(data);
		if (trace) {
			logger.debug(this, "write {" + new String(data) + "}");
		}
	}

	/**
	 * Call output.writeBytes
	 * 
	 * @param data
	 * @throws IOException
	 */
	public void writeBytes(String data) throws IOException {
		output.writeBytes(data);
		if (trace) {
			logger.debug(this, "write {" + new String(data) + "}");
		}
	}

	/**
	 * Call output.writeBytes
	 * 
	 * @param data
	 * @param offset
	 * @param length
	 * @throws IOException
	 */
	public void write(byte[] data, int offset, int length) throws IOException {
		output.write(data, offset, length);
		if (trace) {
			logger.debug(this, "write {" + new String(data) + "}");
		}
	}

	/**
	 * Call output.flush
	 * 
	 * @throws IOException
	 */
	public void flush() throws IOException {
		output.flush();
		if (trace) {
			logger.debug(this, "flushed");
		}
	}

	/**
	 * Call input.available
	 * 
	 * @return
	 * @throws IOException
	 */
	public int available() throws IOException {
		return input.available();
	}

	/**
	 * Call input.read
	 * 
	 * @param buff
	 * @return
	 * @throws IOException
	 */
	public int read(byte[] buff) throws IOException {
		int result = input.read(buff);
		if (trace) {
			logger.debug(this, "read [" + new String(buff) + "]");
		}
		return result;
	}

	/**
	 * Call input.read
	 * 
	 * @param buff
	 * @param offset
	 * @param length
	 * @return
	 * @throws IOException
	 */
	public int read(byte[] buff, int offset, int length) throws IOException {
		int result = input.read(buff, offset, length);
		if (trace) {
			logger.debug(this, "read [" + new String(buff) + "]");
		}
		return result;
	}

	/**
	 * Call input.readFully
	 * 
	 * @param buff
	 * @throws IOException
	 */
	public void readFully(byte[] buff) throws IOException {
		input.readFully(buff);
		if (trace) {
			logger.debug(this, "read [" + new String(buff) + "]");
		}
	}

	/**
	 * Call input.readFully
	 * 
	 * @param buff
	 * @param offset
	 * @param length
	 * @throws IOException
	 */
	public void readFully(byte[] buff, int offset, int length) throws IOException {
		input.readFully(buff, offset, length);
		if (trace) {
			logger.debug(this, "read [" + new String(buff) + "]");
		}
	}
}