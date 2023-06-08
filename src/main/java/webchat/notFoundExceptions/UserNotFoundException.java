package webchat.notFoundExceptions;

public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException(long userId){super("User was not found, id = " + userId);}
    public UserNotFoundException(String userName){super("Username was not found, name = " + userName);}
    public UserNotFoundException(String userName, String uniqueMessage){super("User " + userName + " " + uniqueMessage);}
}

