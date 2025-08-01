package com.example.storysphere_appbar;

public class WritingItem {
    private int id;
    private String title;
    private String tagline;
    private String tag;
    private String category;
    private String imagePath;

    // Constructor
    public WritingItem(int id, String title, String tagline, String tag, String category, String imagePath) {
        this.id = id;
        this.title = title;
        this.tagline = tagline;
        this.tag = tag;
        this.category = category;
        this.imagePath = imagePath;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getTagline() {
        return tagline;
    }

    public String getTag() {
        return tag;
    }

    public String getCategory() {
        return category;
    }

    public String getImagePath() {
        return imagePath;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}