package bupt.networks.tcp;

/*
 * Created by Maou on 2017/7/5.
 */


import bupt.networks.tcp.behaviors.ConnectionEstablishedHandler;
import bupt.networks.tcp.behaviors.ExceptionHandler;
import bupt.networks.tcp.exceptions.ComponentInitFailedException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

/*
 * this class is to run a listener and listen for the connecting request until
 * close() invoked, it has to be run on separate thread handleConnectionEstablished()
 * need to be implement to determinate how to handle the connection.
 */
public abstract class Listener
		implements Runnable, ConnectionEstablishedHandler, ExceptionHandler {

	public static final String TAG = "Listener";

	public static final int DEFAULT_SERVER_PORT = 8787;
	public static final int DEFAULT_BACKLOG     = -1;
	public static final int DEFAULT_TIMEOUT     = 1000; /* MS */

	private ServerSocket serverSocket = null;
	private boolean      available    = false;
	private int          backlog      = DEFAULT_BACKLOG;

	public Listener(int localPort, int backlog)
			throws ComponentInitFailedException {

		if (localPort < 0 || 65536 <= localPort) {
			throw new ComponentInitFailedException("port is invalid.");
		}

		try {
			this.serverSocket = new ServerSocket(localPort);
			this.serverSocket.setSoTimeout(DEFAULT_TIMEOUT);
			this.backlog = backlog;
			this.available = true;
		}
		catch (IOException ex) {
			throw new ComponentInitFailedException("failed to init inner components.", ex);
		}
	}

	public Listener(int localPort) throws ComponentInitFailedException {
		this(localPort, DEFAULT_BACKLOG);
	}

	public Listener() throws ComponentInitFailedException {
		this(DEFAULT_SERVER_PORT, DEFAULT_BACKLOG);
	}

	public void start() {
		if (!available) {
			return;
		}

		TCPHelper.startListener(this);
	}

	/* try to close the  */
	public void close() {
		available = false;

		try {
			if (null != serverSocket && !serverSocket.isClosed()) {
				serverSocket.close();
			}
		}
		catch (IOException ex) { }
	}

	@Override
	public void run() {
		int countConnections = 0;

		while (available && 0 < backlog && countConnections < backlog) {
			try {
				handleConnectionEstablished(serverSocket.accept(), this);
				++countConnections;
			}

			catch (SocketTimeoutException ex) {
				/* timeout to check out available */
			}

			catch (IOException ex) {
				handleException(ex, this);
				available = false;
			}
		}

		this.close();
	}
}
