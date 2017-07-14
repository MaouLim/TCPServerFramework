package bupt.networks.tcp.exceptions;

/*
 * Created by Maou Lim on 2017/7/14.
 */
public class ComponentInitFailedException extends Exception {

    public ComponentInitFailedException(String message) {
        super(message);
    }

    public ComponentInitFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
