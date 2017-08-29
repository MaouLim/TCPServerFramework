package bupt.server.core;

import bupt.networks.tcp.Communicator;
import bupt.networks.tcp.Listener;
import bupt.networks.tcp.exceptions.ComponentInitFailedException;
import bupt.server.protocol.primitive.ProtocolUnit;
import bupt.server.protocol.primitive.ProtocolUnitDecoder;
import bupt.server.protocol.primitive.ProtocolUnitEncoder;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Created by Maou Lim on 2017/7/11.
 */
public abstract class ConnectionManager
        extends Listener
        implements ProtocolUnitDecoder,
                   ProtocolUnitEncoder,
                   ProtocolUnitProcessor {

    private ConcurrentHashMap<String, Communicator> communicatorMap = null;

    public ConnectionManager(int localPort, int backlog) throws ComponentInitFailedException {
        super(localPort, backlog);
        this.communicatorMap = new ConcurrentHashMap<>();
    }

    /* dispatch pu to the specific communicators */
    public void dispatch(ProtocolUnit unit) {
        List<String> targets = unit.getTargetCommunicators();

        for (String target : targets) {
            communicatorMap.computeIfPresent(
                    target,
                    (id, communicator) -> {
                        communicator.send(encode(unit));
                        return communicator;
                    }
            );
        }
    }

    public boolean remove(String communicatorId) {
        Communicator removed = communicatorMap.remove(communicatorId);
        if (null == removed) {
            return false;
        }
        removed.close();
        return true;
    }

    @Override
    public void close() {
        super.close();
        communicatorMap.forEach((id, communicator) -> communicator.close());
        communicatorMap.clear();
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
                    catch (InterruptedException ignored) { }

                    System.err.println("receiver timeout, restart receiving task");
                }

                @Override
                public void handleConnectionReset(Socket    socket,
                                                  Throwable throwable,
                                                  Object    sender) {
                    System.err.println("ConnectionReset reason: " + Arrays.toString(throwable.getStackTrace()));

                    String identifier = socket.getRemoteSocketAddress().toString();
                    if (!remove(identifier)) {
                        return;
                    }

                    System.err.println("connection<" + identifier + "> has been removed");
                }

                @Override
                public void handleMessageArrived(String content, Object sender) {
                    ProtocolUnit unit = decode(content);

                    if (null == unit) {
                        return;
                    }

                    unit.setSourceCommunicator(
                            ((Communicator) sender).getRemoteSocketAddress().toString()
                    );

                    onMessage(unit, this);
                }
            };

            if (!communicator.isAvailable()) {
                communicator.close();
                return;
            }

            communicatorMap.put(
                    communicator.getRemoteSocketAddress().toString(),
                    communicator
            );

            communicator.start();
        }

        catch (ComponentInitFailedException ex) {
            ex.printStackTrace();

            try {
                if (null != socket && !socket.isClosed()) {
                    socket.close();
                }
            }
            catch (IOException ignored) { }
        }
    }
}


