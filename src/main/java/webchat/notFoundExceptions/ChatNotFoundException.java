package webchat.notFoundExceptions;

public class ChatNotFoundException extends RuntimeException{

    public ChatNotFoundException(long chatId){super("Chat was not found, id = " + chatId);}
}
