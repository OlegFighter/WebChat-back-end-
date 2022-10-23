package webchat.notFoundExceptions;

public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException(long userId){super("User was not found, id = " + userId);}
}

