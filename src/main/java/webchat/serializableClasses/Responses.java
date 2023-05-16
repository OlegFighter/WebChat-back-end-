package webchat.serializableClasses;

import lombok.*;
import webchat.controller.UserController;
import webchat.model.User;
import webchat.subModels.Contact;
import webchat.subModels.VisualChat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Responses {

    public record SearchResponseBody(List<Contact> allUsers) implements Serializable { }
    public record UserContactsResponseBody(Set<Contact> contacts) implements Serializable { }
    public record DeleteContactResponseBody(String message) implements Serializable { }
    public record DeleteChatResponseBody(String message) implements Serializable { }
    public record DeleteUserResponseBody(String message) implements Serializable { }

    public record NewChatWithContactResponse(Long chatId) implements Serializable { }


    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class UserDataResponseBody implements Serializable {
        Set<VisualChat> userChats;
        Set<Contact> userContacts;
        HashMap<String, byte[]> avatarsOfContacts;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class MessageResponseBody implements Serializable {
        Long messageId;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class SingInResponceBody implements Serializable {
        Long id;
        String userName;
    }
}
