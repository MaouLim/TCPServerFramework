package bupt.networks.tcp;

/*
 * Created by Maou on 2017/7/5.
 */

import bupt.networks.tcp.behaviors.ConnectionResetHandler;
import bupt.networks.tcp.behaviors.MessageArrivedHandler;
import bupt.networks.tcp.behaviors.TimeoutHandler;
import bupt.networks.tcp.exceptions.ComponentInitFailedException;
import bupt.networks.tcp.exceptions.ConnectionResetException;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/*
 * this class is to manage a connection with a tcp-connected remote endpoint.
 * user has to implement handleMessageArrived() to tell how to handle the
 * message from remote. and it must be run on a separate thread. use send() to
 * send the message to remote endpoint.
 */
public abstract class Communicator
		implements Runnable,
				   ConnectionResetHandler,
			       MessageArrivedHandler,
		  		   TimeoutHandler {

	public static final String TAG = "Communicator";

	public static final int RECEIVE_BUFF_SIZE = 1024;
	public static final int DEFAULT_TIMEOUT   = 1000;

	private Socket  socket    = null;
	private boolean available = false;

	private DataInputStream  inputStream  = null;
	private DataOutputStream outputStream = null;

	public Communicator(@NotNull Socket socket, int timeout)
			throws ComponentInitFailedException {

		if (socket.isClosed() || !socket.isConnected()) {
			throw new ComponentInitFailedException("closed or not connected socket.");
		}

		if (timeout < 0) {
			throw new ComponentInitFailedException("timeout could't be less than zero.");
		}

		this.socket = socket;

		try {
			this.socket.setSoTimeout(timeout);
			this.inputStream = new DataInputStream(this.socket.getInputStream());
			this.outputStream = new DataOutputStream(this.socket.getOutputStream());
			this.available = true;
		}
		catch (Exception ex) {
			throw new ComponentInitFailedException("failed when initializes inner components", ex);
		}
	}

	public Communicator(@NotNull Socket socket)
			throws ComponentInitFailedException {
		this(socket, DEFAULT_TIMEOUT);
	}

	public boolean isAvailable() {
		return available;
	}

	public boolean send(String message) {
		if (!available) {
			return false;
		}

		try {
			//todo merge the lenBytes and buff
			byte[] buff = message.getBytes();
			/* send the length of the message */
			outputStream.writeInt(buff.length);
			/* then send the content */
			outputStream.write(buff, 0, buff.length);

			return true;
		}

		catch (Exception ex) {
			ex.printStackTrace();
		}

		return false;
	}

	public void start() {
		if (!available) {
			return;
		}
		TCPHelper.startCommunicator(this);
	}

	public void close() {
		available = false;

		try {
			socket.close();
		}
		catch (IOException ex) { }

		try {
			inputStream.close();
		}
		catch (IOException ex) { }

		try {
			outputStream.close();
		}
		catch (IOException ex) { }
	}

	@Override
	public void run() {

		byte[] buff = new byte[RECEIVE_BUFF_SIZE];

		while (available) {

			StringBuilder builder = new StringBuilder();

			try {
				int messageSize = inputStream.readInt();
				if (messageSize <= 0) {
					throw new IOException("Remote socket has been closed actively.");
				}

				while (0 < messageSize) {
					int readSize = inputStream.read(buff, 0, Math.min(RECEIVE_BUFF_SIZE, messageSize));
					if (readSize <= 0) {
						throw new IOException("Remote socket has been closed actively.");
					}
					builder.append(new String(buff, 0, readSize));
					messageSize -= readSize;
				}

				handleMessageArrived(builder.toString(), this);
			}
			catch (SocketTimeoutException ex) {
				handleTimeout(socket, this);
			}
			catch (IOException ex) {
				handleConnectionReset(socket, ex, this);
				available = false;
			}
		}

		this.close();
	}

	@Nullable
	public SocketAddress getRemoteSocketAddress() {
		if (!available || socket.isClosed()) {
			return null;
		}
		return socket.getRemoteSocketAddress();
	}

	@Nullable
	public SocketAddress getLocalSocketAddress() {
		if (!available || socket.isClosed()) {
			return null;
		}

		return socket.getLocalSocketAddress();
	}
}
