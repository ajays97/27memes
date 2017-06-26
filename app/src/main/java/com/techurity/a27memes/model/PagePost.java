package com.techurity.a27memes.model;

/**
 * Created by Ajay Srinivas on 6/26/2017.
 */

public class PagePost {

    private String post_id;
    private String image_url;
    private String creator;
    private String created_at;
    private String tags;
    private String message;
    private String link;

    public PagePost() {

    }

    public PagePost(String post_id, String image_url, String creator, String created_at, String message, String tags, String link) {
        this.post_id = post_id;
        this.image_url = image_url;
        this.creator = creator;
        this.created_at = created_at;
        this.tags = tags;
        this.message = message;
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPost_id() {
        return post_id;
    }

    public String getCreator() {
        return "By: " + creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

}
