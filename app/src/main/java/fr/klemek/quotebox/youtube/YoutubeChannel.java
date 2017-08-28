package fr.klemek.quotebox.youtube;

/**
 * Created by klemek on 19/03/17 !
 */

class YoutubeChannel extends YoutubeElement{

    public YoutubeChannel(String id){
        super(YoutubeElementType.CHANNEL);
        setChannelId(id);
    }


}
