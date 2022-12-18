package webchat.controller;

import lombok.*;
import webchat.model.Chat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import webchat.model.User;
import webchat.notFoundExceptions.ChatNotFoundException;
import webchat.notFoundExceptions.UserNotFoundException;
import webchat.repository.ChatRepository;
import webchat.repository.MessageRepository;
import webchat.repository.UserRepository;

import java.io.Serializable;
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

    /*@PostMapping("/new_chat") //НЕ ТРОГАТЬ! Служебный метод для бэкэнда
    newChatResponseBody createChat(@RequestBody NewChatRequest newChatRequest){
        User temp = userRepository.findById(newChatRequest.usersId.get(0)).orElseThrow();
        Chat tempChat = new Chat(newChatRequest.chatName, Set.of(temp), );
        chatRepository.save(tempChat);
        return new newChatResponseBody(tempChat.getChatName(),tempChat.getChatId(), tempChat.getUsers());
    }*/
    @PostMapping("/create_chat") //problems with response!
    NewChatFromUserResponse creatingChatFromUser(@RequestBody NewChatFromUserRequestBody newChatFromUserRequestBody){
        User creator = userRepository.findById(newChatFromUserRequestBody.idOfCreator).
                orElseThrow(() -> new UserNotFoundException(newChatFromUserRequestBody.idOfCreator));
        User requestedUser = userRepository.findById(newChatFromUserRequestBody.idOfRequested).
                orElseThrow(() -> new UserNotFoundException(newChatFromUserRequestBody.idOfRequested));
        Set<User> usersOfTheChat = Set.of(creator, requestedUser);
        Chat newChat =
                new Chat(newChatFromUserRequestBody.chatName, usersOfTheChat, newChatFromUserRequestBody.idOfCreator);
        chatRepository.save(newChat);
        userRepository.save(creator);
        userRepository.save(requestedUser);
        return new NewChatFromUserResponse(newChat.getChatId());
    }

    @PostMapping("/add_to_chat") //problems with response!
    AddToChatResponse addingANewUserToTheChat(@RequestBody AddToChatRequest addToChatRequest){
        User requestedUser = userRepository.findById(addToChatRequest.requestedUserId).
                orElseThrow( () -> new UserNotFoundException(addToChatRequest.requestedUserId));
        Chat changeableChat = chatRepository.findById(addToChatRequest.chatId).
                orElseThrow( () -> new ChatNotFoundException(addToChatRequest.chatId));
        changeableChat.addUser(requestedUser);
        chatRepository.save(changeableChat);
        userRepository.save(requestedUser);
        return new AddToChatResponse(changeableChat.getChatId(), changeableChat.getUsers(), requestedUser.getUserId());
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    private static class newChatResponseBody implements Serializable{
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
    public static class NewChatFromUserResponse implements Serializable{
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
    public static class AddToChatRequest implements Serializable{
        Long requestedUserId;
        Long chatId;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class AddToChatResponse implements Serializable{
        Long chatId;
        Set<User> usersOfTheChat;
        Long whoWasAddedId;
    }
}
