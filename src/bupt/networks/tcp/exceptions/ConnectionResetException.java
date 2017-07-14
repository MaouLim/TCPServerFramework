package bupt.networks.tcp.exceptions;

/*
 * Created by Maou Lim on 2017/7/14.
 */
public class ConnectionResetException extends Exception {

    public ConnectionResetException(String message) {
        super(message);
    }

    public ConnectionResetException(String message, Throwable cause) {
        super(message, cause);
    }
}
