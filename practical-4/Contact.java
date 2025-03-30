import java.io.Serializable;

public class Contact implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String surname;
    private final String number;
    private final byte[] image;

    public Contact(String name, String surname, String number, byte[] image) {
        this.name = name;
        this.surname = surname;
        this.number = number;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getNumber() {
        return number;
    }

    public byte[] getImage() {
        return image;
    }
}
