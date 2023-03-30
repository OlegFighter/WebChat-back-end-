package webchat.model;

import lombok.Getter;
import lombok.Setter;
import webchat.subModels.Contact;

import javax.persistence.*;
import java.util.Iterator;
import java.util.Set;

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
}
