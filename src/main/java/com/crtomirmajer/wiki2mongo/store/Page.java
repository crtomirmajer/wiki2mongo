package com.crtomirmajer.wiki2mongo.store;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 * Created by Majer on 30.10.2016.
 */
@Entity(noClassnameStored = true)
public class Page {
    
    @Id
    private ObjectId id;
    private String   title;
    private String   titleLowerCase;
    private String   text;
    private String   redirect;
    private boolean  disambiguation;
    private int      namespace;
    
    public ObjectId getId() {
        return id;
    }
    
    public void setId(ObjectId id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getRedirect() {
        return redirect;
    }
    
    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }
    
    public boolean isDisambiguation() {
        return disambiguation;
    }
    
    public void setDisambiguation(boolean disambiguation) {
        this.disambiguation = disambiguation;
    }
    
    public void setNamespace(int namespace) {
        this.namespace = namespace;
    }
    
    public int getNamespace() {
        return namespace;
    }
    
    public String getTitleLowerCase() {
        return titleLowerCase;
    }
    
    public void setTitleLowerCase(String titleLowerCase) {
        this.titleLowerCase = titleLowerCase;
    }
}
