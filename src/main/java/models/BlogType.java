package models;

public class BlogType {
    private int blog_id;
    private int type_id;

    public BlogType() {}

    public BlogType(int blog_id, int type_id) {
        this.blog_id = blog_id;
        this.type_id = type_id;
    }

    public int getBlog_id() { return blog_id; }
    public void setBlog_id(int blog_id) { this.blog_id = blog_id; }
    public int getType_id() { return type_id; }
    public void setType_id(int type_id) { this.type_id = type_id; }

    @Override
    public String toString() {
        return "BlogType{" +
                "blog_id=" + blog_id +
                ", type_id=" + type_id +
                '}';
    }
}