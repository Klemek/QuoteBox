package fr.klemek.quotetube.youtube;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import fr.klemek.quotetube.R;
import fr.klemek.quotetube.utils.Constants;
import fr.klemek.quotetube.utils.Utils;

/**
 * Created by klemek on 19/03/17.
 */

public class YoutubeVideo extends YoutubeElement{

    private String videoId;
    private String videoTitle;
    private int views;
    private int upvotes;
    private int downvotes;
    private Date publishedAt;
    private long duration;

    public YoutubeVideo(String videoId) {
        super(YoutubeElementType.VIDEO);
        this.videoId = videoId;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public String getPublishedAt(Context c) {
        return DateFormat.getDateFormat(c).format(publishedAt);
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getViews(Context c) {
        return c.getResources().getString(R.string.video_views,Constants.numFormatter.format(views));
    }

    public void setViews(int views) {
        this.views = views;
    }

    public String getUpvotes(Context c) {
        return c.getResources().getString(R.string.video_upvotes,Constants.numFormatter.format(upvotes));
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public String getDownvotes(Context c) {
        return c.getResources().getString(R.string.video_downvotes,Constants.numFormatter.format(downvotes));
    }

    public void setDownvotes(int downvotes) {
        this.downvotes = downvotes;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getDuration() {
        return Utils.formatDuration(duration);
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
