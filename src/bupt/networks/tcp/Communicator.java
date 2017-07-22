package bupt.networks.tcp;

/*
 * Created by Maou on 2017/7/5.
 */

import bupt.networks.tcp.behaviors.ConnectionResetHandler;
import bupt.networks.tcp.behaviors.MessageArrivedHandler;
import bupt.networks.tcp.behaviors.TimeoutHandler;
import bupt.networks.tcp.exceptions.ComponentInitFailedException;
import bupt.util.ArrayUtil;
import bupt.util.BytesUtil;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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

	public static final int     RECEIVE_BUFF_SIZE = 1024;
	public static final int     DEFAULT_TIMEOUT   = 1000;
	public static final Charset DEFAULT_CHARSET   = StandardCharsets.UTF_8;

	private Socket  socket    = null;
	private boolean available = false;
	private Charset charset   = DEFAULT_CHARSET;

	private DataInputStream  inputStream  = null;
	private DataOutputStream outputStream = null;

	public Communicator(@NotNull Socket socket, int timeout, Charset charset)
			throws ComponentInitFailedException {

		if (socket.isClosed() || !socket.isConnected()) {
			throw new ComponentInitFailedException("closed or not connected socket.");
		}

		if (timeout < 0) {
			throw new ComponentInitFailedException("timeout could't be less than zero.");
		}

		this.socket = socket;
		this.charset = charset;

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

	public Communicator(@NotNull Socket socket, int timeout)
			throws ComponentInitFailedException {
		this(socket, timeout, DEFAULT_CHARSET);
	}

	public Communicator(@NotNull Socket socket, Charset charset)
			throws ComponentInitFailedException {
		this(socket, DEFAULT_TIMEOUT, charset);
	}

	public Communicator(@NotNull Socket socket)
			throws ComponentInitFailedException {
		this(socket, DEFAULT_TIMEOUT, DEFAULT_CHARSET);
	}

	public boolean isAvailable() {
		return available;
	}

	public boolean send(String message) {
		if (!available) {
			return false;
		}

		try {
			byte[] contentBytes = message.getBytes(charset);
			byte[] lenBytes = BytesUtil.getBytes(contentBytes.length);

			byte[] buff = ArrayUtil.concat(lenBytes, contentBytes);
			/* then write the buff to the output stream */
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
			if (null != socket && !socket.isClosed()) {
				socket.close();
			}
		}
		catch (IOException ex) { }

		try {
			if (null != inputStream) {
				inputStream.close();
			}
		}
		catch (IOException ex) { }

		try {
			if (null == outputStream) {
				outputStream.close();
			}
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
					builder.append(new String(buff, 0, readSize, charset));
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
		if (!available || null == socket || socket.isClosed()) {
			return null;
		}
		return socket.getRemoteSocketAddress();
	}

	@Nullable
	public SocketAddress getLocalSocketAddress() {
		if (!available || null == socket || socket.isClosed()) {
			return null;
		}

		return socket.getLocalSocketAddress();
	}

	public Charset getCharset() {
		return charset;
	}
}
