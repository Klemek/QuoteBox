package fr.klemek.quotetube.quote;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import fr.klemek.quotetube.R;

/**
 * Created by klemek on 16/03/17 !
 */

public class QuoteAdapter extends BaseAdapter {

    private Context mContext;
    private QuoteList quotes;

    public QuoteAdapter(Context c, QuoteList q) {
        mContext = c;
        quotes = q;
    }

    public int getCount() {
        return quotes.size();
    }

    public Object getItem(int position) {
        return quotes.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = null;
        if(position<quotes.size()) {
            Quote q = quotes.get(position);
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                v = LayoutInflater.from(mContext).inflate(R.layout.quote_view, parent,false);
            } else {
                v = convertView;
            }
            ImageView img = (ImageView) v.findViewById(R.id.quote_image);
            img.clearColorFilter();
            img.setImageDrawable(mContext.getDrawable(R.drawable.rounded_rect));
            img.setColorFilter(q.getColor());
            ((TextView)v.findViewById(R.id.quote_name)).setText(q.getName());
        }
        return v;
    }
}
