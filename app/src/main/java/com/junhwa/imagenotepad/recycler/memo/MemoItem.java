package com.junhwa.imagenotepad.recycler.memo;

import android.graphics.Bitmap;

public class MemoItem {
    private int id;
    private String title;
    private String contents;
    private Bitmap picture;

    public MemoItem(int id, String title, String contents, Bitmap picture) {
        this.id = id;
        this.title = title;
        this.contents = contents;
        this.picture = picture;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }
}
