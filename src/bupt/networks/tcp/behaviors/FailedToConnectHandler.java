package bupt.networks.tcp.behaviors;

import java.net.Socket;

/*
 * Created by Maou Lim on 2017/7/11.
 */
public interface FailedToConnectHandler {

    void handleFailedToConnect(Socket socket, Throwable throwable, Object sender);
}
