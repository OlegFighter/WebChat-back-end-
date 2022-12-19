package webchat.controller;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import webchat.model.Chat;
import webchat.model.Message;
import webchat.model.User;
import webchat.notFoundExceptions.ChatNotFoundException;
import webchat.notFoundExceptions.UserNotFoundException;
import webchat.repository.ChatRepository;
import webchat.repository.MessageRepository;
import webchat.repository.UserRepository;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;

//Выбрасывать исключение при вводе пустого имени!


@RestController
public class UserController {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    public UserController(ChatRepository chatRepository, UserRepository userRepository, MessageRepository messageRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        //this.passwordEncoder = passwordEncoder;
    }


    /*@PostMapping("/sign_up")
    AuthRequestBody createUser(@RequestBody NewAuthUserRequestBody newUser){
        User temp = new User(newUser.userName, passwordEncoder.encode(newUser.password));
        this.userRepository.save(temp);
        return new AuthRequestBody(temp.getUserId(), temp.getName());
    }*/

    @PostMapping("/new_user")
    UserRequestBody createUserWithoutAuthorization(@RequestBody NewUserRequestBody newUserRequestBody){
        User temp = new User(newUserRequestBody.userName);
        this.userRepository.save(temp);
        return new UserRequestBody(temp.getName(), temp.getUserId());
    }

    @PostMapping("/send")
    MessageResponseBody sendMessageToAChat(@RequestBody MessageRequestBody messageRequestBody){
        Chat changeableChat = chatRepository.findById(messageRequestBody.chatId).
                orElseThrow( () -> new ChatNotFoundException(messageRequestBody.chatId));
        User sender = userRepository.findById(messageRequestBody.senderId).
                orElseThrow(() -> new UserNotFoundException(messageRequestBody.senderId));
        LocalDateTime sendingDate = LocalDateTime.now();
        Message sending = new Message(messageRequestBody.textOfMessage, sendingDate, sender.getUserId(), sender.getName());
        changeableChat.addMessage(sending);
        messageRepository.save(sending);
        chatRepository.save(changeableChat);
        return new MessageResponseBody(sending.getMessageId());
    }

    @DeleteMapping("/delete_chat")
    String deleteChat(@RequestBody DeleteChatRequestBody deleteChatRequestBody){
        Chat chatToDelete = chatRepository.findById(deleteChatRequestBody.chatId).
                orElseThrow( () -> new ChatNotFoundException(deleteChatRequestBody.chatId));
        if(deleteChatRequestBody.userId != chatToDelete.getCreatorId()){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the creator can delete a chat!");
        }
        chatToDelete.getUsers().clear();
        chatRepository.save(chatToDelete);
        chatRepository.deleteById(chatToDelete.getChatId());
        messageRepository.deleteAll(chatToDelete.getMessagesInTheChat());
        return "Chat with id = " + chatToDelete.getChatId() + " was deleted.";
    }

    @PostMapping("/user_chats")
    Set<VisualChat> getUsersChats(@RequestBody getUsersChatsRequest request){
        User thisUser = userRepository.findById(request.userId).orElseThrow(() -> new UserNotFoundException(request.userId));
        Set<Chat> usersChats = chatRepository.findByUser(thisUser);
        Set<VisualChat> response = new java.util.HashSet<VisualChat>(Set.of());
        Iterator<Chat> iterator = usersChats.iterator();
        Chat tempChat = new Chat();
        while (iterator.hasNext()) {
            VisualChat temp = new VisualChat();
            tempChat = iterator.next();
            temp.setChatId(tempChat.getChatId());
            temp.setChatName(tempChat.getChatName());
            response.add(temp);
        }
        return response;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class getUsersChatsRequest implements Serializable{
        Long userId;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class VisualChat implements Serializable{
        String chatName;
        Long chatId;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class UserRequestBody implements Serializable {
        String userName;
        Long id;
    }

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
    public static class MessageResponseBody implements Serializable {
        Long messageId;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class DeleteChatRequestBody implements Serializable {
        Long chatId;
        Long userId;
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
}
