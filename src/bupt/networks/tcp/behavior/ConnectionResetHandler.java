package bupt.networks.tcp.behavior;

import java.net.Socket;

/*
 * Created by Maou Lim on 2017/7/11.
 */
public interface ConnectionResetHandler {

    void handleConnectionReset(Socket    socket,
                               Throwable throwable,
                               Object    sender);
}
