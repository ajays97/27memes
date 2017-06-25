package com.techurity.a27memes.model;

/**
 * Created by Ajay Srinivas on 6/25/2017.
 */

public class Comment {

    private String creator;
    private String message;

    public Comment() {
    }

    public Comment(String creator, String message) {
        this.creator = creator;
        this.message = message;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
