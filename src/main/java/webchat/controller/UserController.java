package webchat.controller;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import webchat.model.Chat;
import webchat.model.Message;
import webchat.model.User;
import webchat.notFoundExceptions.ChatNotFoundException;
import webchat.repository.ChatRepository;
import webchat.repository.MessageRepository;
import webchat.repository.UserRepository;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@RestController
public class UserController {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public UserController(ChatRepository chatRepository, UserRepository userRepository, MessageRepository messageRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @PostMapping("/new_user")
    UserRequestBody createUser(@RequestBody NewUserResponceBody newUserResponceBody){
        User temp = new User(newUserResponceBody.userName);
        this.userRepository.save(temp);
        return new UserRequestBody(temp.getName(), temp.getUserId());
    }

    @PostMapping("/send")
    MessageResponseBody sendMessageToAChat(@RequestBody MessageRequestBody messageRequestBody){
        Chat changeableChat = chatRepository.findById(messageRequestBody.chatId).
                orElseThrow( () -> new ChatNotFoundException(messageRequestBody.chatId));
        Date sendingDate = new Date(System.currentTimeMillis());
        Message sending = new Message(messageRequestBody.textOfMessage, sendingDate, messageRequestBody.senderId);
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
        return "Chat with id = " + chatToDelete.getChatId() + " was deleted.";
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
    public static class NewUserResponceBody implements Serializable {
        String userName;
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
}
