package com.example.docs.ot.model;

public class Operation {
    public enum Type { INSERT, DELETE }
    Type type;
    int position;
    String character;
    int version;
}
