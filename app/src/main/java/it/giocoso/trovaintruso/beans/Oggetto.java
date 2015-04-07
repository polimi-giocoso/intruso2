package it.giocoso.trovaintruso.beans;

import android.graphics.Bitmap;

public class Oggetto {

    private int id;
    private Bitmap img;
    private boolean intruso;


    public Oggetto(int id, Bitmap img, boolean intruso) {
        this.id = id;
        this.img = img;
        this.intruso = intruso;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Bitmap getImg() {
        return img;
    }

    public void setImg(Bitmap img) {
        this.img = img;
    }

    public boolean isIntruso() {
        return intruso;
    }

    public void setIntruso(boolean intruso) {
        this.intruso = intruso;
    }
}
