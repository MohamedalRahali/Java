package model;

import java.sql.Timestamp;

public class Commentaire {
    private int id;
    private String contenu;
    private Timestamp dateCreation;
    private int blogId;
    private int userId;

    public Commentaire() {
    }

    public Commentaire(int id, String contenu, Timestamp dateCreation, int blogId, int userId) {
        this.id = id;
        this.contenu = contenu;
        this.dateCreation = dateCreation;
        this.blogId = blogId;
        this.userId = userId;
    }

    public Commentaire(String contenu, int blogId, int userId) {
        this.contenu = contenu;
        this.blogId = blogId;
        this.userId = userId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }

    public int getBlogId() {
        return blogId;
    }

    public void setBlogId(int blogId) {
        this.blogId = blogId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Commentaire{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", dateCreation=" + dateCreation +
                ", blogId=" + blogId +
                ", userId=" + userId +
                '}';
    }
} 