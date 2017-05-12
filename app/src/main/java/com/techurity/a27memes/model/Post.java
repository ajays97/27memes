package com.techurity.a27memes.model;

/**
 * Created by Ajay Srinivas on 5/11/2017.
 */

public class Post {

    private String post_id;
    private String image_url;

    public Post() {

    }

    public Post(String post_id, String image_url) {
        this.post_id = post_id;
        this.image_url = image_url;
    }

    public String getPost_id() {
        return post_id;
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
