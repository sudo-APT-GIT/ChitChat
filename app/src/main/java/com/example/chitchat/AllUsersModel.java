package com.example.chitchat;

public class AllUsersModel {
    public String name;
    public String image;
    public String status;

    public AllUsersModel(String name, String image, String status) {
        this.name = name;
        this.image = image;
        this.status = status;
    }

    public AllUsersModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
