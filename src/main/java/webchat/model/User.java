package webchat.model;

import lombok.Getter;
import lombok.Setter;
import webchat.invalidLinkException.InvalidLinkException;
import webchat.subModels.Contact;

import javax.persistence.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User{
    @Id
    @GeneratedValue
    long userId;
    @Column(unique = true)
    String name;
    String password;
    @ManyToMany
    Set<User> contacts = Set.of();
    String avatarLink = null;
//    @ManyToMany(mappedBy = "users")
//    Set<Chat> chats;


    public User(String name){
        this.name = name;
    }
    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }
    public User() {}

    public Contact contactTransformation(){
        return new Contact(this.name);
    }

    public Set<Contact> contactsToSerializable(){
        Set<Contact> userContacts = new java.util.HashSet<Contact>(Set.of());
        Iterator<User> contactIterator = contacts.iterator();
        User tempUser = new User();
        while (contactIterator.hasNext()){
            Contact tempContact = new Contact();
            tempUser = contactIterator.next();
            tempContact.setContactName(tempUser.getName());
            userContacts.add(tempContact);
        }
        return userContacts;
    }

    public HashMap<String, byte[]> avatarsOfContacts() throws IOException {
        if (contacts == null) {
            return new HashMap<>(Map.of());
        }
        HashMap<String, byte[]> result = new HashMap<>();
        User tempUser = new User();
        Iterator<User> contactIterator = contacts.iterator();
        try{
            while (contactIterator.hasNext()) {
                tempUser = contactIterator.next();
                if(tempUser.avatarLink == null){
                    result.put(tempUser.name, null);
                }else {
                    result.put(tempUser.name, Files.readAllBytes(Path.of(tempUser.avatarLink)));
                }
            }
        }catch(IllegalArgumentException | NullPointerException | IOException | StringIndexOutOfBoundsException e){
            throw new InvalidLinkException(tempUser.avatarLink, e);
        }

        return result;
    }
}
