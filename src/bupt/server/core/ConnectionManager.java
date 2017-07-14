package bupt.server.core;

import bupt.networks.tcp.Communicator;
import bupt.networks.tcp.Listener;
import bupt.networks.tcp.TCPHelper;
import bupt.networks.tcp.exceptions.ComponentInitFailedException;
import bupt.server.protocol.MessageBase;
import bupt.server.protocol.MessageBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Created by Maou Lim on 2017/7/11.
 */
public abstract class ConnectionManager
        extends Listener implements MessageBuilder, MessageProcessor {

    private ConcurrentHashMap<String, Communicator> communicatorMap = null;

    public ConnectionManager(int localPort, int backlog) throws ComponentInitFailedException {
        super(localPort, backlog);
        this.communicatorMap = new ConcurrentHashMap<>();
    }

    public void dispatch(String targetCommunicatorId, MessageBase message) {
        communicatorMap.computeIfPresent(
                targetCommunicatorId,
                (id, communicator) -> {
                    communicator.send(message.toString());
                    return communicator;
                }
        );
    }

    @Override
    public void handleConnectionEstablished(Socket socket, Object sender) {
        try {
            Communicator communicator = new Communicator(socket) {

                private static final int ONE_MS = 1;

                @Override
                public void handleTimeout(Socket socket, Object sender) {
                    try {
                        Thread.sleep(ONE_MS);
                    }
                    catch (InterruptedException ex) { }

                    System.err.println("receiver timeout, restart receiving");
                }

                @Override
                public void handleConnectionReset(Socket    socket,
                                                  Throwable throwable,
                                                  Object    sender) {
                    System.err.println("ConnectionReset reason: " + Arrays.toString(throwable.getStackTrace()));

                    String identifier = socket.getRemoteSocketAddress().toString();

                    Communicator removed = communicatorMap.remove(identifier);
                    if (null == removed) {
                        return;
                    }
                    removed.close();
                    System.err.println("connection<" + identifier + "> has been removed");
                }

                @Override
                public void handleMessageArrived(String content, Object sender) {
                    MessageBase message = parse(content);

                    if (null == message) {
                        return;
                    }

                    message.setSourceCommunicatorId(
                            ((Communicator) sender).getRemoteSocketAddress().toString()
                    );

                    onMessage(message, this);
                }
            };

            communicator.start();
        }

        catch (ComponentInitFailedException ex) {
            ex.printStackTrace();
        }
    }
}


