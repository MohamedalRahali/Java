package model;

import java.sql.Date;

public class Blogs {
    private int id;
    private String titre;
    private String descr;
    private Date date_crea;
    private Date date_pub;
    private Type_b type;
    private int auteurId;

    public Blogs() {
        this.date_crea = Date.valueOf(java.time.LocalDate.now());
    }

    public Blogs(int id, String titre, String descr, Date date_pub, Type_b type) {
        this();
        this.id = id;
        setTitre(titre);
        setDescr(descr);
        setDate_pub(date_pub);
        setType(type);
    }

    public Blogs(String titre, String descr, Date date_pub, Type_b type) {
        this();
        setTitre(titre);
        setDescr(descr);
        setDate_pub(date_pub);
        setType(type);
    }

    public Blogs(String titre, String descr, Date date_pub) {
        this();
        setTitre(titre);
        setDescr(descr);
        setDate_pub(date_pub);
    }

    public int getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public String getDescr() {
        return descr;
    }

    public Date getDate_crea() {
        return date_crea;
    }

    public Date getDate_pub() {
        return date_pub;
    }

    public Date getDatePub() {
        return getDate_pub();
    }

    public Type_b getType() {
        return type;
    }

    public int getAuteurId() {
        return auteurId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitre(String titre) {
        if (titre == null || titre.length() < 3) {
            throw new IllegalArgumentException("Le titre doit avoir au moins 3 caractères");
        }
        this.titre = titre;
    }

    public void setDescr(String descr) {
        if (descr == null || descr.length() < 10) {
            throw new IllegalArgumentException("La description doit avoir au moins 10 caractères");
        }
        this.descr = descr;
    }

    public void setDate_pub(Date date_pub) {
        if (date_pub != null && date_pub.before(this.date_crea)) {
            throw new IllegalArgumentException("La date de publication doit être après la date de création");
        }
        this.date_pub = date_pub;
    }

    public void setType(Type_b type) {
        this.type = type;
    }

    public void setAuteurId(int auteurId) {
        this.auteurId = auteurId;
    }

    @Override
    public String toString() {
        return titre + " (Créé le: " + date_crea + ")";
    }

    public void setDate_crea(java.sql.Date dateCrea) {
    }

    public static Blogs fromDatabase(int id, String titre, String descr, Date date_pub, Date date_crea, int auteurId) {
        Blogs blog = new Blogs();
        blog.id = id;
        blog.titre = titre;
        blog.descr = descr;
        blog.date_pub = date_pub;
        blog.date_crea = date_crea;
        blog.auteurId = auteurId;
        return blog;
    }
}