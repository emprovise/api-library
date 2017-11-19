package com.emprovise.api.amazon.ws.database.model;

public class Music {

    private String artist;
    private String songTitle;

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    @Override
    public String toString() {
        return "Music{" +
                "artist='" + artist + '\'' +
                ", songTitle='" + songTitle + '\'' +
                '}';
    }
}
