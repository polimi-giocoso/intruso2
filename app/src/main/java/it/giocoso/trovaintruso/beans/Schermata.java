package it.giocoso.trovaintruso.beans;

import java.util.ArrayList;

public class Schermata {

    private String tema;
    private ArrayList<Long> tempiDiRisposta; //in secondi
    private long tempoDiCompletamento;
    private long errori;

    public Schermata(String tema) {
        this.tema = tema;
        this.tempiDiRisposta = new ArrayList<Long>();
        this.tempoDiCompletamento = 0;
        this.errori = 0;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public ArrayList<Long> getTempiDiRisposta() {
        return tempiDiRisposta;
    }

    public void setTempiDiRisposta(ArrayList<Long> tempiDiRisposta) {
        this.tempiDiRisposta = tempiDiRisposta;
    }

    public void addTempoDiRisposta(long tempoDiRisposta) {
        this.tempiDiRisposta.add(tempoDiRisposta);
    }

    public long getTempoDiCompletamento() {
        return tempoDiCompletamento;
    }

    public void setTempoDiCompletamento(long tempoDiCompletamento) {
        this.tempoDiCompletamento = tempoDiCompletamento;
    }

    public long getErrori() {
        return errori;
    }

    public void setErrori(long errori) {
        this.errori = errori;
    }

    public void addNewErrore(){
        this.errori++;
    }
}
