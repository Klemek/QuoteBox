package fr.klemek.quotebox.youtube;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import fr.klemek.quotebox.R;
import fr.klemek.quotebox.utils.ConnectionUtils;

/**
 * Created by klemek on 17/03/17 !
 */

class YoutubeSearchAdapter extends BaseAdapter {

    private final ArrayList<YoutubeElement> elements;
    private String nextPageToken;
    private final Context mContext;
    private final LoadRequestListener listener;

    public YoutubeSearchAdapter(Context mContext, ArrayList<YoutubeElement> elements, LoadRequestListener listener) {
        this.elements = elements;
        this.mContext = mContext;
        this.listener = listener;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    @Override
    public int getCount() {
        return elements.size()+(nextPageToken==null?0:1);
    }

    @Override
    public Object getItem(int i) {
        return elements.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = null;
        if(i< elements.size()) {
            switch(elements.get(i).getType()){
                case VIDEO:
                    YoutubeVideo yv = (YoutubeVideo)elements.get(i);
                    if (view == null || (view.findViewById(R.id.video_title)) == null) {
                        v = LayoutInflater.from(mContext).inflate(R.layout.youtube_video_view, viewGroup,false);
                    } else {
                        v = view;
                    }
                    ImageView img = v.findViewById(R.id.video_thumbnail);
                    ConnectionUtils.loadImage(mContext,yv.getThumbURL(),R.drawable.thumbnail_placeholder,R.drawable.thumbnail_placeholder,img);
                    ((TextView)v.findViewById(R.id.video_title)).setText(yv.getVideoTitle());
                    ((TextView)v.findViewById(R.id.video_channel)).setText(yv.getChannelTitle());
                    ((TextView)v.findViewById(R.id.video_date)).setText(yv.getPublishedAt(mContext));

                    ((TextView)v.findViewById(R.id.video_views)).setText(yv.getViews(mContext));
                    ((TextView)v.findViewById(R.id.video_upvotes)).setText(yv.getUpvotes(mContext));
                    ((TextView)v.findViewById(R.id.video_downvotes)).setText(yv.getDownvotes(mContext));
                    ((TextView)v.findViewById(R.id.video_duration)).setText(yv.getDuration());
                    break;
                case CHANNEL:
                    YoutubeChannel yc = (YoutubeChannel)elements.get(i);
                    if (view == null || (view.findViewById(R.id.channel_title)) == null) {
                        v = LayoutInflater.from(mContext).inflate(R.layout.youtube_channel_view, viewGroup,false);
                    } else {
                        v = view;
                    }
                    ImageView img2 = v.findViewById(R.id.channel_thumbnail);
                    ConnectionUtils.loadImage(mContext,yc.getThumbURL(),R.drawable.thumbnail_placeholder,R.drawable.thumbnail_placeholder,img2);
                    ((TextView)v.findViewById(R.id.channel_title)).setText(yc.getChannelTitle());
                    ((TextView)v.findViewById(R.id.channel_desc)).setText(yc.getDescription());
                    break;
            }
        }else if(i== elements.size()){
            v = new ProgressBar(mContext);
            ((ProgressBar)v).setIndeterminate(true);
            listener.onLoadRequest();
        }
        return v;
    }

    public interface LoadRequestListener{
        void onLoadRequest();
    }
}
