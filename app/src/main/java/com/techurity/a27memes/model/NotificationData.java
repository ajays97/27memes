package com.techurity.a27memes.model;

/**
 * Created by Ajay Srinivas on 5/18/2017.
 */

public class NotificationData {

    public static final String TEXT = "TEXT";

    private String imageName;
    private int id;
    private String title;
    private String textMessage;
    private String sound;

    public NotificationData() {
    }

    public NotificationData(String imageName, int id, String title, String textMessage, String sound) {
        this.imageName = imageName;
        this.id = id;
        this.title = title;
        this.textMessage = textMessage;
        this.sound = sound;
    }

    public static String getTEXT() {
        return TEXT;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CharSequence getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CharSequence getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }


}
