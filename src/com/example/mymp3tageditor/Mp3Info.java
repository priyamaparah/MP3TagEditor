package com.example.mymp3tageditor;



public class Mp3Info {

	public String albumName;
	public String songTitle;
    public String songDuration;
    public String artistName;
	
	public Mp3Info(){}
	
	public Mp3Info(String albumName, String SongTitle) {
		this.albumName = albumName;
		this.songTitle = SongTitle;
	}
}
