package edu.temple.bookshelf;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class BookDetailsFragment extends Fragment {

    private static final String BOOK_KEY = "bookKey";
    private Book book;

    TextView titleTextView, authorTextView;
    ImageView bookCoverImageView;
    BookPlayInterface parentActivity;
    boolean playing;

    public BookDetailsFragment() {}

    public static BookDetailsFragment newInstance(Book book) {
        BookDetailsFragment fragment = new BookDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(BOOK_KEY, book);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playing = false;
        if (getArguments() != null) {
            book = getArguments().getParcelable(BOOK_KEY);
            parentActivity.bookPlayStop();
            parentActivity.setSeekBarRange(book.getDuration());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookPlayInterface)
            parentActivity = (BookPlayInterface) context;
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
        View v = inflater.inflate(R.layout.fragment_book_details, container, false);
        titleTextView = v.findViewById(R.id.titleTextView);
        authorTextView = v.findViewById(R.id.authorTextView);
        bookCoverImageView = v.findViewById(R.id.coverImageView);

        v.findViewById(R.id.playButton).setOnClickListener(view -> {
            if (book == null)
                return;
            playing = true;
            titleTextView.setText("Now Playing: " + book.getTitle());
            parentActivity.bookPlay(book.getId());
            parentActivity.setCurrentDetailFrag(this);
        });
        v.findViewById(R.id.playButton).setOnLongClickListener(view-> {
            Toast.makeText(v.getContext(), "Play", Toast.LENGTH_SHORT).show();
            return true;
        });

        if (book != null) {
            changeBook(book);
        }
        return v;
    }

    public int getBookId(){
        if(book == null)
            return 0;
        return book.getId();
    }

    public boolean UIReady(){
        return (bookCoverImageView != null);
    }

    public void changeBook(Book book) {
        this.book = book;

        titleTextView.setText(book.getTitle());
        authorTextView.setText(book.getAuthor());
        Picasso.get().load(book.getCoverUrl()).into(bookCoverImageView);

    }

    public void bookStopPlay(){
        if(book == null)
            return;
        playing = false;
        titleTextView.setText(book.getTitle());
    }

    public void bookStartPlay(){
        if(book == null)
            return;
        playing = true;
        titleTextView.setText("Now Playing: " + book.getTitle());
    }

    interface BookPlayInterface {
        void bookPlay(int id);
        void bookPlayStop();
        void setSeekBarRange(int n);
        void setCurrentDetailFrag(BookDetailsFragment bdf);
    }
}
