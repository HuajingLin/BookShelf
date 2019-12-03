package edu.temple.bookshelf;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import edu.temple.audiobookplayer.AudiobookService;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity implements BookListFragment.BookSelectedInterface,BookDetailsFragment.BookPlayInterface {

    FragmentManager fm;
    BookDetailsFragment bookDetailsFragment;
    boolean onePane;
    Library library;
    Fragment current1, current2;
    AudiobookService.MediaControlBinder binderService;
    boolean connected;
    boolean isTurning;
    int curBookId;
    SeekBar seekBar = null;

    Handler seekbarHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg == null)
                return;
            if(msg.obj == null) {
                bookStopPlay();
                return;
            }
            int pos = ((AudiobookService.BookProgress)msg.obj).getProgress();
            if(seekBar != null)
                seekBar.setProgress(pos);
            bookStartlay();
        }
    };

    ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            connected = true;
            binderService = (AudiobookService.MediaControlBinder) service;
            binderService.setProgressHandler(seekbarHandler);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connected = false;
            binderService = null;
        }
    };

    private final String SEARCH_URL = "https://kamorris.com/lab/audlib/booksearch.php?search=";

    Handler bookHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            try {
                library.clear();
                JSONArray booksArray = new JSONArray((String) message.obj);
                for (int i = 0; i < booksArray.length(); i++) {
                    library.addBook(new Book(booksArray.getJSONObject(i)));
                }

                if(fm.findFragmentById(R.id.container_1) == null)
                    setUpDisplay();
                else
                    updateBooks();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isTurning = false;
        Intent serviceIntent = new Intent(this, AudiobookService.class);
        bindService(serviceIntent, myConnection, Context.BIND_AUTO_CREATE);

        curBookId = 0;
        fm = getSupportFragmentManager();
        library = new Library();

        // Check for fragments in both containers
        current1 = fm.findFragmentById(R.id.container_1);
        current2 = fm.findFragmentById(R.id.container_2);

        onePane = findViewById(R.id.container_2) == null;

        if (current1 == null) {
            fetchBooks(null);
        } else {
            updateDisplay();
        }

        setEventOfControls();
    }

    private void setEventOfControls(){
        findViewById(R.id.searchButton).setOnClickListener(v -> fetchBooks(((EditText) findViewById(R.id.searchBox)).getText().toString()));

        findViewById(R.id.pauseButton).setOnClickListener(v -> {
            if(connected)
                binderService.pause();
        });

        findViewById(R.id.stopButton).setOnClickListener(v -> {
            if(connected)
                binderService.stop();
            bookStopPlay();
            seekBar.setProgress(0);
        });
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar
                .OnSeekBarChangeListener() {
            int pval = 0;
            // When the progress value has changed
            @Override
            public void onProgressChanged(
                    SeekBar seekBar,
                    int progress,
                    boolean fromUser)
            {
                pval = progress;
                //System.out.printf("=======position: %d\n",progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                binderService.seekTo(pval);
                int duration = seekBar.getMax();
                String tips = String.format("Progress: %.2f%%",((float) pval / (float)duration) * 100);
                Toast.makeText(getApplicationContext(), tips,Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.pauseButton).setOnLongClickListener(v -> {
            Toast.makeText(v.getContext(), "Pause", Toast.LENGTH_SHORT).show();
            return true;
        });
        findViewById(R.id.stopButton).setOnLongClickListener(v -> {
            Toast.makeText(v.getContext(), "Stop", Toast.LENGTH_SHORT).show();
            return true;

        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        System.out.printf("===== activity onStart\n");
    }
    @Override
    protected void onResume() {
        super.onResume();
        System.out.printf("===== activity onResume\n");
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        System.out.printf("===== activity onSaveInstanceState\n");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        isTurning = true;
        Fragment tmpFragment = current1;
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            setContentView(R.layout.activity_main);
            onePane = true;
            if (current1 instanceof BookListFragment) {
                current1 = ViewPagerFragment.newInstance(library);

                fm.beginTransaction()
                        .remove(tmpFragment)
                        .add(R.id.container_1, current1)
                        .commit();
                new Thread(){
                    @Override
                    public void run() {

                        ViewPager viewPager = ((ViewPagerFragment)current1).getViewPager();
                        //System.out.printf("====== turning: %d\n",1);
                        while (viewPager == null) {
                            try {
                                sleep(100);
                                viewPager = ((ViewPagerFragment)current1).getViewPager();

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }//System.out.printf("====== turning bid: %d\n",curBookId);

                        setViewPagerEvent(viewPager);

                        viewPager.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ViewPager viewPager = ((ViewPagerFragment)current1).getViewPager();
                                viewPager.setCurrentItem(curBookId-1);
                            }
                        }, 100);
                    }
                }.start();
                setEventOfControls();
                if(connected){
                    if(binderService.isPlaying())
                        bookStartlay();
                }
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.two_pane_base);
            onePane = false;

            if (current1 instanceof ViewPagerFragment) {
                ViewPager viewPager = ((ViewPagerFragment)current1).getViewPager();
                viewPager.removeAllViews();
                fm.beginTransaction().remove(tmpFragment).commit();

                current1 = BookListFragment.newInstance(library);
                System.out.printf("====== turned: %d\n",curBookId);
                Book book1 = library.getBookAt(curBookId-1);
                bookDetailsFragment = BookDetailsFragment.newInstance(book1);
                fm.beginTransaction()
                        .remove(tmpFragment)
                        .add(R.id.container_1, current1)
                        .add(R.id.container_2, bookDetailsFragment)
                        .commit();

                setEventOfControls();

                if(connected){
                    if(binderService.isPlaying())
                        bookStartlay();
                }
            }

        }
    }

    private void setViewPagerEvent(ViewPager viewPager){
        //ViewPager viewPager = ((ViewPagerFragment)current1).getViewPager();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                //System.out.printf("====== onPageScrolled: %d\n",i);
                                /*
                                if(bookDetailsFragment != null)
                                    System.out.printf("====== onPageScrolled: %d, book id:%d\n",i, bookDetailsFragment.getBookId());
                                else
                                    System.out.printf("====== onPageScrolled: get null\n");*/
            }

            @Override
            public void onPageSelected(int position) {

                if(!isTurning) {
                    if(bookDetailsFragment != null)
                        bookDetailsFragment.bookStopPlay();
                    bookPlayStop();
                }
                curBookId = position + 1;

                ViewPager viewPager = ((ViewPagerFragment)current1).getViewPager();
                View view = viewPager.getChildAt(position);
                bookDetailsFragment = (BookDetailsFragment) viewPager.getAdapter().instantiateItem(viewPager, position);

                System.out.printf("====== onPageSelected: %d\n",curBookId);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                if(i == 0)
                    isTurning = false;
                //System.out.printf("====== onPageScrollStateChanged: %d\n",i);
            }
        });
    }

    private void setUpDisplay() {
        // If there are no fragments at all (first time starting activity)

        if (onePane) {
            curBookId = 1;
            current1 = ViewPagerFragment.newInstance(library);
            fm.beginTransaction()
                    .add(R.id.container_1, current1)
                    .commit();

            new Thread(){
                @Override
                public void run() {

                    ViewPager viewPager = ((ViewPagerFragment) current1).getViewPager();
                    //System.out.printf("====== turning: %d\n",1);
                    while (viewPager == null) {
                        try {
                            sleep(100);
                            viewPager = ((ViewPagerFragment) current1).getViewPager();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }//System.out.printf("====== turning bid: %d\n",curBookId);

                    setViewPagerEvent(viewPager);
                }
            }.start();
        } else {
            current1 = BookListFragment.newInstance(library);
            bookDetailsFragment = new BookDetailsFragment();
            fm.beginTransaction()
                    .add(R.id.container_1, current1)
                    .add(R.id.container_2, bookDetailsFragment)
                    .commit();
        }

    }

    private void updateDisplay () {
        Fragment tmpFragment = current1;;
        library = ((Displayable) current1).getBooks();
        if (onePane) {

            if (current1 instanceof BookListFragment) {
                current1 = ViewPagerFragment.newInstance(library);
                // If we have the wrong fragment for this configuration, remove it and add the correct one
                fm.beginTransaction()
                        .remove(tmpFragment)
                        .add(R.id.container_1, current1)
                        .commit();
            }
        } else {
            if (current1 instanceof ViewPagerFragment) {
                current1 = BookListFragment.newInstance(library);
                fm.beginTransaction()
                        .remove(tmpFragment)
                        .add(R.id.container_1, current1)
                        .commit();
            }
            if (current2 instanceof BookDetailsFragment)
                bookDetailsFragment = (BookDetailsFragment) current2;
            else {
                bookDetailsFragment = new BookDetailsFragment();
                fm
                        .beginTransaction()
                        .add(R.id.container_2, bookDetailsFragment)
                        .commit();
            }
        }

        bookDetailsFragment = (BookDetailsFragment) current2;
    }

    private void updateBooks() {
        ((Displayable) current1).setBooks(library);
    }

    //=====================interface about book==============================
    @Override
    public void bookSelected(Book book) {
        System.out.printf("======= bookSelected1 %d\n",book.getId());
        if (bookDetailsFragment == null)
            return;
        if(connected)
            binderService.stop();//System.out.printf("======= bookSelected3 %d\n",book.getId());
        curBookId = book.getId();
        bookDetailsFragment.changeBook(book);
        setSeekBarRange(book.getDuration());
        //System.out.printf("=======getDuration %d\n",book.getDuration());
        //binderService.play(book.getId());
    }

    @Override
    public void bookPlay(int id) {

        if (connected) {
            binderService.play(id);
        }
    }

    @Override
    public void bookPlayStop(){
        System.out.printf("=======bookPlayStop1 %b\n",isTurning);
        if(isTurning)
            return;
        System.out.printf("=======bookPlayStop2 %b\n",isTurning);
        if (connected) {
            binderService.stop();
        }
        if (bookDetailsFragment != null) {
            bookDetailsFragment.bookStopPlay();
        }
    }

    @Override
    public void setSeekBarRange(int n)
    {
        seekBar.setMax(n);
        seekBar.setProgress(0);
    }

    @Override
    public void setCurrentDetailFrag(BookDetailsFragment bdf)
    {
        bookDetailsFragment = bdf;
    }

    private void bookStartlay(){
        if (bookDetailsFragment != null) {
            bookDetailsFragment.bookStartPlay();
        }
    }
    private void bookStopPlay()
    {
        if (bookDetailsFragment != null) {
            bookDetailsFragment.bookStopPlay();
        }
    }
    //===================================================

    private boolean isNetworkActive() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void fetchBooks(final String searchString) {
        new Thread() {
            @Override
            public void run() {
                if (isNetworkActive()) {

                    URL url;

                    try {
                        url = new URL(SEARCH_URL + (searchString != null ? searchString : ""));
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(
                                        url.openStream()));

                        StringBuilder response = new StringBuilder();
                        String tmpResponse;

                        while ((tmpResponse = reader.readLine()) != null) {
                            response.append(tmpResponse);
                        }

                        Message msg = Message.obtain();

                        msg.obj = response.toString();

                        Log.d("Books RECEIVED", response.toString());

                        bookHandler.sendMessage(msg);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.e("Network Error", "Cannot download books");
                }
            }
        }.start();
    }

}
