package webchat.subModels;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class Contact implements Serializable, Comparable<Contact>{
    String contactName;


    @Override
    public int compareTo(Contact o) {
        return contactName.compareTo(o.contactName);
    }
}
