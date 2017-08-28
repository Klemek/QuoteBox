package fr.klemek.quotebox.quote;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import fr.klemek.quotebox.R;
import fr.klemek.quotebox.utils.Utils;

/**
 * Created by klemek on 16/03/17 !
 */

public class QuoteAdapter extends BaseAdapter {

    private final Context ctx;
    private final QuoteList quotes;

    public QuoteAdapter(Context c, QuoteList q) {
        ctx = c;
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
                v = LayoutInflater.from(ctx).inflate(R.layout.quote_view, parent,false);
            } else {
                v = convertView;
            }
            ImageView img = (ImageView) v.findViewById(R.id.quote_image);
            img.clearColorFilter();
            img.setImageDrawable(ctx.getDrawable(R.drawable.rounded_rect));
            img.setColorFilter(q.getColor());
            TextView tvName = ((TextView)v.findViewById(R.id.quote_name));
            tvName.setText(q.getName());

            int lineCount = Utils.getLineCount(tvName, q.getName(), (int) ctx.getResources().getDimension(R.dimen.quote_size));
            if(lineCount > 1){
                tvName.setMinLines(2);
                tvName.setTextSize(TypedValue.COMPLEX_UNIT_PX, ctx.getResources().getDimension(R.dimen.quote_small_text));
            }else{
                tvName.setMinLines(1);
                tvName.setMaxLines(1);
            }
        }
        return v;
    }
}
