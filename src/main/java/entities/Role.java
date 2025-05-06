package esprit.tn.entities;

public enum Role {
    ADMIN("Administrateur"),
    ARTISTE("Artiste"),
    CLIENT("Client");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}