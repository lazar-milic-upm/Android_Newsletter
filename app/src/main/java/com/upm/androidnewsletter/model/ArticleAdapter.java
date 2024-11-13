package com.upm.androidnewsletter.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.upm.androidnewsletter.exceptions.ServerCommunicationError;
import com.upm.androidnewsletter.R;
import java.util.List;

public class ArticleAdapter extends ArrayAdapter<Article> {

    private int resourceLayout;
    private Context mContext;

    public ArticleAdapter(Context context, int resource, List<Article> articles) {
        super(context, resource, articles);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        TextView titleTextView, abstractTextView;
        ImageView articleImageView;

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(resourceLayout, parent, false);
        }

        Article article = getItem(position);

        if (article != null) {
            titleTextView = view.findViewById(R.id.textViewTitle);
            abstractTextView = view.findViewById(R.id.textViewAbstract);
            articleImageView = view.findViewById(R.id.articleImageView);

            titleTextView.setText(article.getTitleText());
            titleTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            abstractTextView.setText(article.getAbstractText().replaceAll("<[^>]*>", ""));
            try {
                articleImageView.setImageBitmap(article.getImage().getBitmapImage());
            } catch (ServerCommunicationError e) {
                throw new RuntimeException(e);
            }
        }

        return view;
    }
}
