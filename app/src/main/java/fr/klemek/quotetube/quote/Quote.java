package fr.klemek.quotetube.quote;

import java.io.File;
import java.io.Serializable;

import fr.klemek.quotetube.utils.Constants;

/**
 * Created by klemek on 16/03/17 !
 */

public class Quote implements Serializable{

    private int color;
    private String name;
    private String soundFile;

    private String[] videoInfo;

    public Quote(int color, String name, String soundFile, String[] videoInfo) {
        this.color = color;
        this.name = name;
        this.soundFile = soundFile;
        this.videoInfo = videoInfo;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getFile(){
        return new File(Constants.DIR_QUOTES +soundFile);
    }

    public String[] getVideoInfo() {
        return videoInfo;
    }

    public void setVideoInfo(String[] videoInfo) {
        this.videoInfo = videoInfo;
    }
}
