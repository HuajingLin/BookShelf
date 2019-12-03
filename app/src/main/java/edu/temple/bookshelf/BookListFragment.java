package edu.temple.bookshelf;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class BookListFragment extends Fragment implements Displayable {
    private static final String BOOKLIST_KEY = "booklist";

    ListView listView;
    private Library bookList;
    BookSelectedInterface parentActivity;

    public BookListFragment() {}

    public static BookListFragment newInstance(Library bookList) {
        BookListFragment fragment = new BookListFragment();
        Bundle args = new Bundle();
        args.putParcelable(BOOKLIST_KEY, bookList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookList = getArguments().getParcelable(BOOKLIST_KEY);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookSelectedInterface)
            parentActivity = (BookSelectedInterface) context;
        else
            throw new RuntimeException("Please implement BookSelectedInterface");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parentActivity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_book_list, container, false);
        listView = layout.findViewById(R.id.listView);
        listView.setAdapter(new BookListAdapter((Context) parentActivity, bookList));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                parentActivity.bookSelected(bookList.getBookAt(position));
            }
        });

        return layout;
    }

    @Override
    public Library getBooks() {
        return bookList;
    }

    @Override
    public void setBooks(Library books) {
        bookList = books;
        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
    }

    interface BookSelectedInterface {
        void bookSelected(Book book);
    }

}
