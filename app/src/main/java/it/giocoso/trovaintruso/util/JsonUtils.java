package it.giocoso.trovaintruso.util;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import it.giocoso.trovaintruso.R;


public class JsonUtils {

    public static String loadJSONFromAsset(int criterio, Context ctx) {

        String[] criteri = ctx.getResources().getStringArray(R.array.criteri);

        String json = null;
        try {
            InputStream is = ctx.getAssets().open(criteri[criterio]+".json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }

    public static int getResId(String variableName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(variableName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
