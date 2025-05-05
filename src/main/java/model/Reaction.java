package model;

public class Reaction {
    private int id;
    private int blogId;
    private int userId;
    private ReactionType type;
    
    public enum ReactionType {
        LIKE("J'aime"),
        LOVE("J'adore"),
        SAD("Triste"),
        LAUGH("Rire"),
        ANGRY("Fâché");
        
        private final String label;
        
        ReactionType(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }
    
    public Reaction() {
    }
    
    public Reaction(int blogId, int userId, ReactionType type) {
        this.blogId = blogId;
        this.userId = userId;
        this.type = type;
    }

    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
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
    
    public ReactionType getType() {
        return type;
    }
    
    public void setType(ReactionType type) {
        this.type = type;
    }
} 