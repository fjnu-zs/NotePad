package com.example.demo2;

public class Note {
    private int id;
    private String title;
    private String note;
    private long modify;
    public Note(int id, String title, String note, long modify){
        this.id = id;
        this.title =title;
        this.note = note;
        this.modify = modify;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getNote() {
        return note;
    }

    public long getModify() {
        return modify;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setModify(long modify) {
        this.modify = modify;
    }

}
