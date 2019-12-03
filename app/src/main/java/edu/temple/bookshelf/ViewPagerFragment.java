package edu.temple.bookshelf;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ViewPagerFragment extends Fragment implements Displayable {
    private static final String BOOKLIST_KEY = "booklist";

    ViewPager viewPager;
    private Library bookList;
    //BookListFragment.BookSelectedInterface parentActivity;


    public ViewPagerFragment() {}

    public static ViewPagerFragment newInstance(Library bookList) {
        ViewPagerFragment fragment = new ViewPagerFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_view_pager, container, false);

        viewPager = v.findViewById(R.id.viewPager);

        viewPager.setAdapter(new BookFragmentAdapter(getChildFragmentManager(), bookList));

        return v;
    }
    public ViewPager getViewPager(){
        return viewPager;
    }

    @Override
    public Library getBooks() {
        return bookList;
    }

    @Override
    public void setBooks(Library books) {
        bookList = books;
        viewPager.getAdapter().notifyDataSetChanged();
    }

    class BookFragmentAdapter extends FragmentStatePagerAdapter {

        Library bookList;

        public BookFragmentAdapter(FragmentManager fm, Library bookList) {
            super(fm);
            this.bookList = bookList;
        }

        @Override
        public Fragment getItem(int i) {
            return BookDetailsFragment.newInstance(bookList.getBookAt(i));
        }

        @Override
        public int getCount() {
            return bookList.size();
        }

        @Override
        public int getItemPosition(@NonNull Object object) {return PagerAdapter.POSITION_NONE;}
    }

}
