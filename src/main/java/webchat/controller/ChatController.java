package webchat.controller;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import webchat.model.Chat;
import webchat.model.Message;
import webchat.model.User;
import webchat.notFoundExceptions.ChatNotFoundException;
import webchat.notFoundExceptions.UserNotFoundException;
import webchat.repository.ChatRepository;
import webchat.repository.MessageRepository;
import webchat.repository.UserRepository;
import webchat.serializableClasses.Requests;
import webchat.serializableClasses.Responses;

import java.io.Serializable;
import java.util.*;

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

    @PostMapping("/create_chat_with_contact")
    Responses.NewChatWithContactResponse createChatWithContact(@RequestBody Requests.NewChatWithContactRequest newChatWithContactRequest,
                                                               @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser) {
        User creator = userRepository.findByName(currentUser.getUsername()).
                orElseThrow(() -> new UsernameNotFoundException(currentUser.getUsername()));
        User requestedUser = userRepository.findByName(newChatWithContactRequest.userName()).
                orElseThrow(() -> new UsernameNotFoundException(newChatWithContactRequest.userName()));
        Set<User> usersOfChat = Set.of(creator, requestedUser);
        Chat newChat = new Chat(newChatWithContactRequest.chatName(), usersOfChat, creator.getUserId());
        chatRepository.save(newChat);
        userRepository.save(creator);
        userRepository.save(requestedUser);
        return new Responses.NewChatWithContactResponse(newChat.getChatId());
    }


    @PostMapping("/add_contact_to_chat")
    AddToChatResponse addingAContactToTheChat(@RequestBody AddContactToChatRequest addContactToChatRequest, @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser) {
        User thisUser = userRepository.findByName(currentUser.getUsername()).
                orElseThrow(() -> new UsernameNotFoundException(currentUser.getUsername()));
        User requestedUser = userRepository.findByName(addContactToChatRequest.name).
                orElseThrow(() -> new UserNotFoundException(addContactToChatRequest.name));
        Chat changeableChat = chatRepository.findById(addContactToChatRequest.chatId).
                orElseThrow(() -> new ChatNotFoundException(addContactToChatRequest.chatId));
        if (changeableChat.getUsers().contains(requestedUser)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "This user already exist in the chat.");
        }
        changeableChat.addUser(requestedUser);
        chatRepository.save(changeableChat);
        userRepository.save(requestedUser);
        return new AddToChatResponse(requestedUser.getUserId());

    }

    @PostMapping("/chat_info")
    Responses.chatInfoResponse chatInfo(@AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser,
                                        @RequestBody Requests.chatInfoRequest chatInfoRequest){
        Chat current = chatRepository.findById(chatInfoRequest.chatId())
                .orElseThrow(() -> new ChatNotFoundException(chatInfoRequest.chatId()));
        User creator = userRepository.findById(current.getCreatorId())
                .orElseThrow(() -> new UserNotFoundException(current.getCreatorId()));
        String creatorName = creator.getName();
        Set<User> members = current.getUsers();
        Iterator<User> iter = members.iterator();
        List<String> memberName = new ArrayList<>();
        while (iter.hasNext()){
            memberName.add(iter.next().getName());
        }
        return new Responses.chatInfoResponse(creatorName, memberName, memberName.size());
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
