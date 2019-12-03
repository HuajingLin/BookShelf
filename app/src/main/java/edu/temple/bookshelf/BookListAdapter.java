package edu.temple.bookshelf;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BookListAdapter extends BaseAdapter {

    Context c;
    Library l;

    public BookListAdapter (Context c, Library l) {
        this.c = c;
        this.l = l;
    }

    @Override
    public int getCount() {
        return l.size();
    }

    @Override
    public Object getItem(int i) {
        return l.getBookAt(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        TextView textView = view == null ? new TextView(c): (TextView) view;

        textView.setText(l.getBookAt(i).getTitle());
        textView.setTextSize(25);
        textView.setPadding(4,4,4,4);

        return textView;
    }
}
