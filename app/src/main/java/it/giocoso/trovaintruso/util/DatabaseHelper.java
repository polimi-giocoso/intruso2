package it.giocoso.trovaintruso.util;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "trovaintrusomail.db";
    private static final int DATABASE_VERSION = 1;

    // Lo statement SQL di creazione del database
    public static final String DATABASE_CREATE_MAIL = "create table mail (_id integer primary key, message text, emaildest text);";

    // Costruttore
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Questo metodo viene chiamato durante la creazione del database
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_MAIL);
    }

    // Questo metodo viene chiamato durante l'upgrade del database, ad esempio
    // quando viene incrementato il numero di versione
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {

        database.execSQL("DROP TABLE IF EXISTS mail");

        onCreate(database);

    }

}