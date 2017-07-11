package bupt.server.protocol;

/*
 * Created by Maou Lim on 2017/7/11.
 */
public interface MessageBuilder {

    MessageBase parse(String content);
}
