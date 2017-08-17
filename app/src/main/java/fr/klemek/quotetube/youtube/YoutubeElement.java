package fr.klemek.quotetube.youtube;

import java.util.Date;

/**
 * Created by klemek on 19/03/17.
 */

public abstract class YoutubeElement {

    public enum YoutubeElementType{
        VIDEO,
        CHANNEL
    }

    private String channelId;
    private String channelTitle;
    private String description;
    private String thumbURL;
    private YoutubeElementType type;

    public YoutubeElement(YoutubeElementType type) {
        this.type = type;
    }

    public YoutubeElementType getType() {
        return type;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }

    public String getThumbURL() {
        return thumbURL;
    }

    public void setThumbURL(String thumbURL) {
        this.thumbURL = thumbURL;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}