package bupt.networks.tcp.behaviors;

import java.net.Socket;

/*
 * Created by Maou Lim on 2017/7/14.
 */
public interface TimeoutHandler {

    void handleTimeout(Socket socket, Object sender);
}
