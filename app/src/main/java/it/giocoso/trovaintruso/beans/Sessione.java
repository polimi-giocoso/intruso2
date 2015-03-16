package it.giocoso.trovaintruso.beans;

import android.support.annotation.IntegerRes;

import java.util.ArrayList;

/**
 * Created by chicco on 11/02/15.
 */
public class Sessione {

    private ArrayList<Schermata> schermate;
    private int criterio;
    private int punteggio, numOggettiTotale, numIntrusi, tempoMassimo, speed, numRighe, numColonne, attesa;

    //costruttore per modalità di gioco 1
    public Sessione(int criterio, int numSchermate, int numOggettiTotale, int numIntrusi, int tempoMassimo) {
        this.criterio = criterio;
        this.punteggio = 0;
        this.numOggettiTotale = numOggettiTotale;
        this.numIntrusi = numIntrusi;
        this.tempoMassimo = tempoMassimo;

        this.attesa = 0;
        this.speed = 0;

        this.schermate = new ArrayList<Schermata>();
        addSchermate(numSchermate);
    }

    //costruttore per modalità di gioco 2
    public Sessione(int criterio, int numSchermate, int numRighe, int numColonne, int numIntrusi, int tempoMassimo, int speed, int attesa) {
        this.criterio = criterio;
        this.punteggio = 0;
        this.numRighe = numRighe;
        this.numColonne = numColonne;
        this.numIntrusi = numIntrusi;
        this.tempoMassimo = tempoMassimo;
        this.speed = speed;
        this.attesa = attesa;
        this.numOggettiTotale = numColonne*numRighe;

        this.schermate = new ArrayList<Schermata>();
        addSchermate(numSchermate);
    }

    public ArrayList<Schermata> getSchermate() {
        return schermate;
    }

    public void setSchermate(ArrayList<Schermata> schermate) {
        this.schermate = schermate;
    }

    public void addSchermate(int numSchermate){

        for(int i = 0; i < numSchermate; i++){
            Schermata s = new Schermata("tema");
            this.schermate.add(s);
        }

    }

    public Schermata getSchermata(int index){
        return this.schermate.get(index);
    }

    public int getCriterio() {
        return criterio;
    }

    public void setCriterio(int criterio) {
        this.criterio = criterio;
    }

    public int getPunteggio() {
        return punteggio;
    }

    public void setPunteggio(int punteggio) {
        this.punteggio = punteggio;
    }

    public int getNumOggettiTotale() {
            return numOggettiTotale;
    }

    public void setNumOggettiTotale(int numOggettiTotale) {
        this.numOggettiTotale = numOggettiTotale;
    }

    public int getNumRighe() {
        return numRighe;
    }

    public void setNumRighe(int numRighe) {
        this.numRighe = numRighe;
    }

    public int getNumColonne() {
        return numColonne;
    }

    public void setNumColonne(int numColonne) {
        this.numColonne = numColonne;
    }

    public int getNumIntrusi() {
        return numIntrusi;
    }

    public void setNumIntrusi(int numIntrusi) {
        this.numIntrusi = numIntrusi;
    }

    public int getTempoMassimo() {
        return tempoMassimo;
    }

    public void setTempoMassimo(int tempoMassimo) {
        this.tempoMassimo = tempoMassimo;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getAttesa() {
        return attesa;
    }

    public void setAttesa(int attesa) {
        this.attesa = attesa;
    }
}
