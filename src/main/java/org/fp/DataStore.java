package org.fp;

public class DataStore {
    public LibraryUsers users;
    public LibraryModel model;

    public DataStore() {
        this.users = new LibraryUsers();
        this.model = new LibraryModel();
    }
}