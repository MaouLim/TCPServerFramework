package bupt.networks.tcp.behaviors;

/*
 * Created by Maou Lim on 2017/7/14.
 */
public interface ExceptionHandler {

    void handleException(Throwable throwable, Object sender);
}
