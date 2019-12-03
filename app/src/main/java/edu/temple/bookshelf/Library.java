package edu.temple.bookshelf;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Library implements Parcelable {
    private ArrayList<Book> books;

    public Library () {
        books = new ArrayList<>();
    }

    protected Library(Parcel in) {
        books = in.createTypedArrayList(Book.CREATOR);
    }

    public static final Creator<Library> CREATOR = new Creator<Library>() {
        @Override
        public Library createFromParcel(Parcel in) {
            return new Library(in);
        }

        @Override
        public Library[] newArray(int size) {
            return new Library[size];
        }
    };

    public Book getBookAt(int position){
        return books.get(position);
    }

    public void addBook(Book book) {
        books.add(book);
    }

    public int size () {
        return books.size();
    }

    public void clear() {
        books.clear();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(books);
    }
}
