package webchat.controller;

import lombok.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import webchat.model.Chat;
import webchat.model.Message;
import webchat.model.User;
import webchat.notFoundExceptions.ChatNotFoundException;
import webchat.notFoundExceptions.UserNotFoundException;
import webchat.repository.ChatRepository;
import webchat.repository.MessageRepository;
import webchat.repository.UserRepository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
public class ChatController {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    public ChatController(MessageRepository messageRepository, ChatRepository chatRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
    }

    /*@PostMapping("/new_chat") // Служебный метод
    newChatResponseBody createChat(@RequestBody NewChatRequest newChatRequest){
        User temp = userRepository.findById(newChatRequest.usersId.get(0)).orElseThrow();
        Chat tempChat = new Chat(newChatRequest.chatName, Set.of(temp), );
        chatRepository.save(tempChat);
        return new newChatResponseBody(tempChat.getChatName(),tempChat.getChatId(), tempChat.getUsers());
    }*/
    @PostMapping("/create_chat")
    NewChatFromUserResponse creatingChatFromUser(@RequestBody NewChatFromUserRequestBody newChatFromUserRequestBody,
                                                 @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser) {
        User creator = userRepository.findByName(currentUser.getUsername()).
                orElseThrow(() -> new UsernameNotFoundException(currentUser.getUsername()));
        User requestedUser = userRepository.findById(newChatFromUserRequestBody.idOfRequested).
                orElseThrow(() -> new UserNotFoundException(newChatFromUserRequestBody.idOfRequested));
        Set<User> usersOfTheChat = Set.of(creator, requestedUser);
        Chat newChat =
                new Chat(newChatFromUserRequestBody.chatName, usersOfTheChat, creator.getUserId());
        chatRepository.save(newChat);
        userRepository.save(creator);
        userRepository.save(requestedUser);
        return new NewChatFromUserResponse(newChat.getChatId());

    }


    @PostMapping("/add_to_chat")
    AddToChatResponse addingANewUserToTheChat(@RequestBody AddToChatRequest addToChatRequest,
                                              @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser) {
        User requestedUser = userRepository.findById(addToChatRequest.requestedUserId).
                orElseThrow(() -> new UserNotFoundException(addToChatRequest.requestedUserId));
        Chat changeableChat = chatRepository.findById(addToChatRequest.chatId).
                orElseThrow(() -> new ChatNotFoundException(addToChatRequest.chatId));
        if (changeableChat.getUsers().contains(requestedUser)) {
            return new AddToChatResponse(null);
        }
        changeableChat.addUser(requestedUser);
        chatRepository.save(changeableChat);
        return new AddToChatResponse(requestedUser.getUserId());
    }

    @PostMapping("/messages")
    ArrayList<Message> messagesOfTheChat(@RequestBody messagesRequest info, @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser) {
        Chat chat = chatRepository.findById(info.chatId).orElseThrow(() -> new ChatNotFoundException(info.chatId));
        Set<Message> messages = chat.getMessagesInTheChat();
        ArrayList<Message> messageList = new ArrayList<Message>(messages);
        Collections.sort(messageList);
        return messageList;
    }

    @PostMapping("/add_contact_to_chat")
    AddToChatResponse addingAContactToTheChat(@AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser,
                                              @RequestBody AddContactToChatRequest addContactToChatRequest) {
        User thisUser = userRepository.findByName(currentUser.getUsername()).
                orElseThrow(() -> new UsernameNotFoundException(currentUser.getUsername()));
        User requestedUser = userRepository.findByName(addContactToChatRequest.name).
                orElseThrow(() -> new UsernameNotFoundException(addContactToChatRequest.name));
        Chat changeableChat = chatRepository.findById(addContactToChatRequest.chatId).
                orElseThrow(() -> new ChatNotFoundException(addContactToChatRequest.chatId));
        if (changeableChat.getUsers().contains(requestedUser)) {
            return new AddToChatResponse(null);
        }
        changeableChat.addUser(requestedUser);
        chatRepository.save(changeableChat);
        userRepository.save(requestedUser);
        return new AddToChatResponse(requestedUser.getUserId());

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    private static class messagesRequest implements Serializable {
        Long chatId;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    private static class AddContactToChatRequest implements Serializable {
        String name;
        Long chatId;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    private static class newChatResponseBody implements Serializable {
        String name;
        Long chatId;
        Set<User> users;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class NewChatRequest implements Serializable {
        String chatName;
        List<Long> usersId;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class NewChatFromUserResponse implements Serializable {
        Long chatId;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class NewChatFromUserRequestBody implements Serializable {
        Long idOfRequested;
        Long idOfCreator;
        String chatName;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class AddToChatRequest implements Serializable {
        Long requestedUserId;
        Long chatId;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class AddToChatResponse implements Serializable {
        Long whoWasAddedId;
    }
}
