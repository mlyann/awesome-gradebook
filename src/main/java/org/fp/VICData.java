package org.fp;

public class VICData {
    public String agentID;
    public String date;
    public String phrase;
    public String anagram;
    public String message;

    public VICData(String agentID, String date, String phrase, String anagram, String message) {
        this.agentID = agentID;
        this.date = date;
        this.phrase = phrase;
        this.anagram = anagram;
        this.message = message;
    }

    // 提供无参构造，灵活使用
    public VICData() {
    }
}
