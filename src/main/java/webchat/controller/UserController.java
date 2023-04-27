package webchat.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
import webchat.serializableClasses.Requests;
import webchat.serializableClasses.Responses;
import webchat.subModels.Contact;
import webchat.subModels.VisualChat;

import java.time.LocalDateTime;
import java.util.*;


@RestController
public class UserController {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(PasswordEncoder passwordEncoder, ChatRepository chatRepository, UserRepository userRepository, MessageRepository messageRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @PostMapping("/sign_up")
    Requests.AuthRequestBody createUser(@RequestBody Requests.NewAuthUserRequestBody newUser) {
        User temp = new User(newUser.getUserName(), passwordEncoder.encode(newUser.getPassword()));
        try{
            this.userRepository.save(temp);
        }catch (DataIntegrityViolationException e){
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Здравствуйте! Вы уже тут есть. Что вам ещё " +
                    "надо? Два аккаунта захотел? А вот закатайте губу, пожалуйста!");
        }
        return new Requests.AuthRequestBody(temp.getUserId(), temp.getName());
    }

    @PostMapping("/new_user")
    Requests.UserRequestBody createUserWithoutAuthorization(@RequestBody Requests.NewUserRequestBody newUserRequestBody) {
        User temp = new User(newUserRequestBody.getUserName());
        this.userRepository.save(temp);
        return new Requests.UserRequestBody(temp.getName(), temp.getUserId());
    }

    @PostMapping("/send")
    Responses.MessageResponseBody sendMessageToAChat(@RequestBody Requests.MessageRequestBody messageRequestBody,
                                                     @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser) {
        Chat changeableChat = chatRepository.findById(messageRequestBody.getChatId()).
                orElseThrow(() -> new ChatNotFoundException(messageRequestBody.getChatId()));
        User sender = userRepository.findByName(currentUser.getUsername()).
                orElseThrow(() -> new UsernameNotFoundException(currentUser.getUsername()));
        LocalDateTime sendingDate = LocalDateTime.now();
        Message sending = new Message(messageRequestBody.getTextOfMessage(), sendingDate, sender.getUserId(), sender.getName());
        changeableChat.addMessage(sending);
        messageRepository.save(sending);
        chatRepository.save(changeableChat);
        return new Responses.MessageResponseBody(sending.getMessageId());
    }

    @PostMapping("/delete_chat")
    Responses.DeleteChatResponseBody deleteChat(@RequestBody Requests.DeleteChatRequestBody deleteChatRequestBody,
                      @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser) {
        Chat chatToDelete = chatRepository.findById(deleteChatRequestBody.chatId()).
                orElseThrow(() -> new ChatNotFoundException(deleteChatRequestBody.chatId()));
        User actor = userRepository.findByName(currentUser.getUsername()).
                orElseThrow(() -> new UsernameNotFoundException(currentUser.getUsername()));
        if (actor.getUserId() != chatToDelete.getCreatorId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the creator can delete a chat!");
        }
        chatToDelete.getUsers().clear();
        chatRepository.save(chatToDelete);
        chatRepository.deleteById(chatToDelete.getChatId());
        messageRepository.deleteAll(chatToDelete.getMessagesInTheChat());
        return new Responses.DeleteChatResponseBody("Chat with id = " + chatToDelete.getChatId() + " was deleted.");
    }

    @PostMapping("/delete_contact")
    Responses.DeleteContactResponseBody deleteContact(@AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser,
                                                      Requests.DeleteContactRequestBody deleteContactRequestBody){
        // Вытаскиваем обоих юзеров из репозитория
        User thisUser = userRepository.findByName(currentUser.getUsername()).
                orElseThrow(() -> new UsernameNotFoundException(currentUser.getUsername()));
        User hisContact = userRepository.findByName(deleteContactRequestBody.contactName()).
                orElseThrow(() -> new UsernameNotFoundException(deleteContactRequestBody.contactName()));

        // Изменяем список контактов у обоих юзеров
        Set<User> contactsOfThis = thisUser.getContacts();
        Set<User> contactsOfHisContact = hisContact.getContacts();
        contactsOfThis.remove(hisContact);
        contactsOfHisContact.remove(thisUser);

        // Сохраняем изменения в БД
        userRepository.save(thisUser);
        userRepository.save(hisContact);

        return new Responses.DeleteContactResponseBody("User " + hisContact.getName() + " was removed from your contacts.");
    }

    @PostMapping("/delete_account")
    Responses.DeleteUserResponseBody deleteAccount(@AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser){
        User thisUser = userRepository.findByName(currentUser.getUsername()).
                orElseThrow(() -> new UsernameNotFoundException(currentUser.getUsername()));

        // посмотрим чаты, где он состоит
        Set<Chat> chats = chatRepository.findByUser(thisUser);
        for (Chat tempChat : chats) {
            Set<User> usersOfChat = tempChat.getUsers();
            usersOfChat.remove(thisUser);
            chatRepository.save(tempChat);
        }

        // посмотрим контакты
        Set<User> contacts  = thisUser.getContacts();
        for (User tempContact : contacts) {
            Set<User> contactsOfTemp = tempContact.getContacts();
            contactsOfTemp.remove(thisUser);
            userRepository.save(tempContact);
        }

        userRepository.delete(thisUser);
        return new Responses.DeleteUserResponseBody("Your account was deleted.");
    }


    @PostMapping("/user_chats")
    Set<VisualChat> getUsersChats(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser) {
        User thisUser = userRepository.findByName(currentUser.getUsername()).
                orElseThrow(() -> new UsernameNotFoundException(currentUser.getUsername()));
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

    @PostMapping("/user_data")
    Responses.UserDataResponseBody getUserData(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser) {
        User thisUser = userRepository.findByName(currentUser.getUsername()).
                orElseThrow(() -> new UsernameNotFoundException(currentUser.getUsername())); // нашли пользователя
        Set<Chat> usersChats = chatRepository.findByUser(thisUser); //возьмём все его чаты
        Set<VisualChat> chats = new java.util.HashSet<VisualChat>(Set.of()); //заготовим формы для ответа
        Iterator<Chat> chatIterator = usersChats.iterator(); // отсюда идёт блок копирования чатов для отправки клиенту
        Chat tempChat = new Chat();
        while (chatIterator.hasNext()) {
            VisualChat temp = new VisualChat();
            tempChat = chatIterator.next();
            temp.setChatId(tempChat.getChatId());
            temp.setChatName(tempChat.getChatName());
            chats.add(temp);
        } // закончили копирование
        return new Responses.UserDataResponseBody(chats, thisUser.contactsToSerializable());
    }

    @PostMapping("/add_contact")
    Set<Contact> addNewContact(@RequestBody Requests.addContactRequestBody addContactRequestBody,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser) {
        //"вытаскиваем" из репозитория текущего юзера и того, кого хотим добавить
        User thisUser = userRepository.findByName(currentUser.getUsername()).
                orElseThrow(()->new UsernameNotFoundException(currentUser.getUsername()));
        User requested = userRepository.findByName(addContactRequestBody.userName()).
                orElseThrow(() -> new UsernameNotFoundException(addContactRequestBody.userName()));

        // Если пользователь есть в списке контактов, то добавлять не надо
        if (thisUser.getContacts().contains(requested)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Этот пользователь уже есть среди ваших контактов!");
        }

        // У обоих(!!!) юзеров пополняем список контактов
        Set<User> contactsOfThis = thisUser.getContacts();
        Set<User> contactsOfrequested = requested.getContacts();
        contactsOfrequested.add(thisUser);
        contactsOfThis.add(requested);

        // Сохраняем изменения в БД
        userRepository.save(thisUser);
        userRepository.save(requested);

        return thisUser.contactsToSerializable();
    }


    @PostMapping("/search")
    Responses.SearchResponseBody globalUserSearch(@AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser){
        List<User> buffer = userRepository.findAll();
        List<Contact> allUsers = new ArrayList<>();
        for (User user : buffer) {
            allUsers.add(new Contact(user.getName()));
        }
        Collections.sort(allUsers);
        return new Responses.SearchResponseBody(allUsers);
    }
}
