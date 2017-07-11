package bupt.networks.tcp;

/*
 * Created by Maou on 2017/7/5.
 */

//import android.bupt.util.Log;

import bupt.networks.tcp.behavior.ConnectionResetHandler;
import bupt.networks.tcp.behavior.MessageArrivedHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

/*
 * this class is to manage a connection with a tcp-connected remote endpoint.
 * user has to implement handleMessageArrived() to tell how to handle the
 * message from remote. and it must be run on a separate thread. use send() to
 * send the message to remote endpoint.
 */
public abstract class Communicator
		implements Runnable, ConnectionResetHandler, MessageArrivedHandler {

	public static final String TAG = "Communicator";

	public static final int RECEIVE_BUFF_SIZE = 1024;

	private Socket socket = null;
	private boolean available = false;

	private DataInputStream inputStream = null;
	private DataOutputStream outputStream = null;

	public Communicator(Socket socket) {
		this.socket = socket;

		try {
			inputStream = new DataInputStream(this.socket.getInputStream());
			outputStream = new DataOutputStream(this.socket.getOutputStream());
			available = true;
		}
		catch (Exception ex) {
			//Log.e(TAG, "exception in ctor", ex);
		}
	}

	public void send(String message) {
		if (!available) {
			return;
		}

		try {
			byte[] buff = message.getBytes();
			/* send the length of the message */
			outputStream.writeInt(buff.length);
			/* then send the content */
			outputStream.write(buff, 0, buff.length);
		}

		catch (Exception ex) {
			//Log.e(TAG, "exception in send", ex);
		}
	}

	public void close() {
		available = false;

		try {
			inputStream.close();
		}
		catch (IOException ex) {
			//Log.e(TAG, "exception in close", ex);
		}

		try {
			outputStream.close();
		}
		catch (IOException ex) {
			//Log.e(TAG, "exception in close", ex);
		}
	}

	@Override
	public void run() {

		byte[] buff = new byte[RECEIVE_BUFF_SIZE];

		while (available) {

			StringBuilder builder = new StringBuilder();

			try {
				int messageSize = inputStream.readInt();
				if (messageSize <= 0) {
					throw new IOException("Remote socket has been closed.");
				}

				while (0 < messageSize) {
					int readSize = inputStream.read(buff, 0, Math.min(RECEIVE_BUFF_SIZE, messageSize));
					if (readSize <= 0) {
						throw new IOException("Remote socket has been closed.");
					}
					builder.append(new String(buff, 0, readSize));
					messageSize -= readSize;
				}

				handleMessageArrived(builder.toString(), this);
			}
			catch (IOException ex) {
				handleConnectionReset(socket, ex, this);
				//Log.e(TAG, "exception in run", ex);
				available = false;
			}
		}

		try {
			inputStream.close();
		}
		catch (IOException ex) {
			//Log.e(TAG, "exception in close", ex);
		}

		try {
			outputStream.close();
		}
		catch (IOException ex) {
			//Log.e(TAG, "exception in close", ex);
		}

		//Log.d(TAG, "communicator finalized.");
	}

	public SocketAddress getRemoteSocketAddress() {
		if (!available || socket.isClosed()) {
			return null;
		}
		return socket.getRemoteSocketAddress();
	}
}
