package fr.klemek.quotetube.youtube;

/**
 * Created by klemek on 19/03/17.
 */

public class YoutubeChannel extends YoutubeElement{

    public YoutubeChannel(String id){
        super(YoutubeElementType.CHANNEL);
        setChannelId(id);
    }


}
