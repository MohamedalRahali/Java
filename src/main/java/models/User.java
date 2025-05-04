package models;

import javafx.beans.property.*;
import java.sql.Date;
import java.util.List;

public class User {
    private int id;
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty lastname = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();
    private List<String> roles;
    private final ObjectProperty<Date> dateOfBirth = new SimpleObjectProperty<>();
    private final BooleanProperty isBlocked = new SimpleBooleanProperty();
    private String username;

    public User() {}

    public User(String name, String lastname, String email, String password,
                List<String> roles, Date dateOfBirth, boolean isBlocked, String username) {
        setName(name);
        setLastname(lastname);
        setEmail(email);
        setPassword(password);
        setRoles(roles);
        setDateOfBirth(dateOfBirth);
        setIsBlocked(isBlocked);
        setUsername(username);
    }

    // Getters and setters for regular fields
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    // Property accessors
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    public String getLastname() { return lastname.get(); }
    public void setLastname(String lastname) { this.lastname.set(lastname); }
    public StringProperty lastnameProperty() { return lastname; }

    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }
    public StringProperty emailProperty() { return email; }

    public String getPassword() { return password.get(); }
    public void setPassword(String password) { this.password.set(password); }
    public StringProperty passwordProperty() { return password; }

    public Date getDateOfBirth() { return dateOfBirth.get(); }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth.set(dateOfBirth); }
    public ObjectProperty<Date> dateOfBirthProperty() { return dateOfBirth; }

    public boolean getIsBlocked() { return isBlocked.get(); }
    public void setIsBlocked(boolean isBlocked) { this.isBlocked.set(isBlocked); }
    public BooleanProperty isBlockedProperty() { return isBlocked; }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // For date display in TableView
    public String getDateOfBirthString() {
        return dateOfBirth.get() != null ? dateOfBirth.get().toString() : "";
    }
}
