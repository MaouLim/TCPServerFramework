package bupt.networks.tcp;

/*
 * Created by Maou on 2017/7/5.
 */

import bupt.networks.tcp.behaviors.ConnectionEstablishedHandler;
import bupt.networks.tcp.behaviors.FailedToConnectHandler;
import bupt.networks.tcp.exceptions.ComponentInitFailedException;
import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/*
 * this class is to establish a tcp connection with a specified remote endpoint.
 * it needs to be run on a separate thread, and user has to implement
 * handleConnectionEstablished() to tell how to handle the connection when established
 */
public abstract class Connector
		implements Runnable, ConnectionEstablishedHandler, FailedToConnectHandler {

	public static final String TAG = "Connector";

	public static final int DEFAULT_REMOTE_PORT = 8787;
	public static final int DEFAULT_TIMEOUT     = 10000;

	private InetAddress remoteAddress = null;
	private int         remotePort    = DEFAULT_REMOTE_PORT;
	private int  		timeout       = DEFAULT_TIMEOUT;

	public Connector(@NotNull InetAddress remoteAddress, int remotePort, int timeout)
			throws ComponentInitFailedException {
		if (remotePort < 0 || 65536 <= remotePort) {
			throw new ComponentInitFailedException("port is invalid.");
		}
		if (timeout < 0) {
			throw new ComponentInitFailedException("timeout could't be less than zero.");
		}

		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
		this.timeout = timeout;
	}

	public Connector(@NotNull InetAddress remoteAddress) throws ComponentInitFailedException {
		this(remoteAddress, DEFAULT_REMOTE_PORT, DEFAULT_TIMEOUT);
	}

	public void handleFailedToConnect(Socket socket, Throwable throwable, Object sender) {

		/* print the reason of the failure */
		System.err.println("failed to connect to remote endpoint. reason: " + throwable.getStackTrace());

		/* try to close the socket */
		try {
			if (null != socket && !socket.isClosed()) {
				socket.close();
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void start() {
		TCPHelper.startConnector(this);
	}

	@Override
	public void run() {
		Socket socket = new Socket();

		try {
			socket.connect(
				new InetSocketAddress(remoteAddress, remotePort),
				timeout
			);

			handleConnectionEstablished(socket, this);
		}

		catch (IOException ex) {
			handleFailedToConnect(socket, ex, this);
		}

		finally {
			if (!socket.isClosed()) {
				try {
					socket.close();
				}
				catch (IOException ignored) { }
			}
		}
	}
}
