package com.upm.androidnewsletter.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.upm.androidnewsletter.model.Article;
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
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(resourceLayout, null);
        }

        Article article = getItem(position);

        if (article != null) {
            TextView titleTextView = view.findViewById(R.id.textViewTitle);
            TextView abstractTextView = view.findViewById(R.id.textViewAbstract);

            titleTextView.setText(article.getTitleText());
            abstractTextView.setText(article.getAbstractText().replaceAll("<[^>]*>", ""));
        }

        return view;
    }
}
