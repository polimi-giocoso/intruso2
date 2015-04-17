package it.giocoso.trovaintruso.util;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;

public class DbAdapter {

    @SuppressWarnings("unused")
    private static final String LOG_TAG = DbAdapter.class.getSimpleName();

    private Context context;
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    // nomi tabelle database

    public static final String DATABASE_MAIL = "mail";

    // nomi campi tabelle

    public static final String KEY_ID = "_id";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EMAIL = "emaildest";

    public DbAdapter(Context context) {
        this.context = context;
    }

    //metodo per aprire il db

    public DbAdapter open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    //metodo per chiudere il db

    public void close() {
        dbHelper.close();
    }

    //metodo che crea un oggetto ContentValues con i dati della mail

    private ContentValues createContentMail(String message, String emaildest) {
        ContentValues values = new ContentValues();
        values.put(KEY_MESSAGE, message);
        values.put(KEY_EMAIL, emaildest);
        return values;
    }

    //metodo che aggiunge una mail al db

    public long createMail(String message, String emaildest) {
        ContentValues initialValues = createContentMail(message, emaildest);
        return database.insertOrThrow(DATABASE_MAIL, null, initialValues);
    }

    //metodo che cancella una mail dal db

    public boolean deleteMail(String id) {
        return database.delete(DATABASE_MAIL, KEY_ID + "="
                + id, null) > 0;
    }

    //metodo che recupera tutte le mail da inviare

    public Cursor fetchAllEmails() {
        return database.query(DATABASE_MAIL, new String[] { KEY_ID, KEY_MESSAGE, KEY_EMAIL}, null, null,
                null, null, null);
    }

}
