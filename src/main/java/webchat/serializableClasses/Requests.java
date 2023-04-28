package webchat.serializableClasses;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

public class Requests {
    public record UserRequestBody(String userName, Long id) implements Serializable { }

    public record addContactRequestBody (String userName) implements Serializable { }

    public record DeleteChatRequestBody (Long chatId) implements Serializable { }
    public record DeleteContactRequestBody (String contactName) implements Serializable { }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class NewUserRequestBody implements Serializable {
        String userName;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class NewAuthUserRequestBody implements Serializable {
        String userName;
        String password;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class MessageRequestBody implements Serializable {
        Long senderId;
        Long chatId;
        String textOfMessage;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class AuthRequestBody implements Serializable {
        Long id;
        String userName;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class SingInRequestBody implements Serializable {
        String userName;
        String password;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class getUsersChatsRequest implements Serializable {
        Long userId;
    }
}
