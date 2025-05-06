package model;

import java.util.Date;

public class Comment {
    private int id;
    private String contenu;
    private Date dateCreation;
    private int blogId;
    private int userId;
    private boolean isReported;
    private int reportCount;

    public Comment() {
        this.dateCreation = new Date();
        this.isReported = false;
        this.reportCount = 0;
    }

    public Comment(String contenu, int blogId, int userId) {
        this();
        this.contenu = contenu;
        this.blogId = blogId;
        this.userId = userId;
    }

    public Comment(int id, String contenu, Date dateCreation, int blogId, int userId) {
        this.id = id;
        this.contenu = contenu;
        this.dateCreation = dateCreation;
        this.blogId = blogId;
        this.userId = userId;
    }

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

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
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

    public boolean isReported() {
        return isReported;
    }

    public void setReported(boolean reported) {
        isReported = reported;
    }

    public int getReportCount() {
        return reportCount;
    }

    public void setReportCount(int reportCount) {
        this.reportCount = reportCount;
    }
} 