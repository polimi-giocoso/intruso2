package it.giocoso.trovaintruso.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import it.giocoso.trovaintruso.R;
import it.giocoso.trovaintruso.beans.Sessione;

public class MailUtils {

    public void sendMail(String riepilogo, String emailMitt, String pswMitt, String emailDest){

        new SendEmailAsyncTask().execute(riepilogo, emailMitt, pswMitt, emailDest);

    }

    class SendEmailAsyncTask extends AsyncTask<String, Void, Boolean> {



        @Override
        protected Boolean doInBackground(String... params) {

            String riepilogo = params[0];
            String emailMitt = params[1];
            String pswMitt = params[2];
            String emailDest = params[3];

            GMailSender sender = new GMailSender(emailMitt, pswMitt);


            try {
                sender.sendMail("Trova l'intruso 2 - Riepilogo sessione di gioco",
                        riepilogo,
                        emailMitt,
                        emailDest);
                Log.d("SendMail", "mail inviata");

                return true;
            } catch (AuthenticationFailedException e) {
                Log.e(SendEmailAsyncTask.class.getName(), "Bad account details");
                e.printStackTrace();
                return false;
            } catch (MessagingException e) {
                Log.e(SendEmailAsyncTask.class.getName(), "failed");
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public String getRiepilogo(Sessione s, Context ctx){
        String riepilogo = "";
        Long time;

        String[] criteri = ctx.getResources().getStringArray(R.array.criteri);

        riepilogo += "## TROVA L'INTRUSO 2 - DATI SESSIONE DI GIOCO ##\n";
        if(s.isDinamica()){
            riepilogo += "Modalità di gioco: dinamica (con oggetti in movimento)\n";
        }else{
            riepilogo += "Modalità di gioco: statica (con griglia di oggetti fissi)\n";
        }
        riepilogo += "Criterio di gioco: "+ criteri[s.getCriterio()] + "\n";
        riepilogo += "Tempo massimo per schermata: "+ s.getTempoMassimo() + " secondi\n";
        riepilogo += "Numero totale di oggetti per schermata: "+ s.getNumOggettiTotale() + "\n";
        riepilogo += "Numero di intrusi: "+ s.getNumIntrusi() + "\n";
        riepilogo += "Numero di schermate: "+ s.getSchermate().size() + "\n";
        if(s.getAttesa()!=0){
            riepilogo += "Tempo di attesa per comparsa oggetto: "+ s.getAttesa() + " secondi\n";
            riepilogo += "Tempo di permanenza oggetto sullo schermo: "+ s.getSpeed() + " secondi\n";
        }
        riepilogo += "\n";


        for(int i = 0; i<s.getSchermate().size(); i++){

            riepilogo += "## SCHERMATA "+Integer.toString(i + 1)+" ##\n";
            riepilogo += "Numero di oggetti trovati: "+ s.getSchermate().get(i).getTempiDiRisposta().size() + "\n";
            riepilogo += "Numero di errori commessi: " + s.getSchermate().get(i).getErrori() + "\n";

            for(int j = 0; j<s.getSchermate().get(i).getTempiDiRisposta().size(); j++){

                time = s.getSchermate().get(i).getTempiDiRisposta().get(j);
                riepilogo += "Tempo di risposta "+Integer.toString(j+1)+" - "+ TimeUtils.getTimeString(time) + "\n";

            }

            time = s.getSchermate().get(i).getTempoDiCompletamento();
            riepilogo += "Tempo di completamento schermata "+Integer.toString(i+1)+" - " + TimeUtils.getTimeString(time) + "\n";
            riepilogo += "\n";

        }

        return riepilogo;
    }


    public String getPunteggioTotale(Sessione s, Context ctx){
        String message = "";
        int oggettiTrovati = 0;
        int oggettiTotali = s.getSchermate().size()*s.getNumIntrusi();

        for(int i = 0; i<s.getSchermate().size(); i++){

            oggettiTrovati += s.getSchermate().get(i).getTempiDiRisposta().size();

        }

        if(oggettiTrovati < oggettiTotali){
            if(oggettiTrovati == 0){
                message = ctx.getString(R.string.mu_perso_message);
            }else{
                message = ctx.getResources()
                        .getQuantityString(R.plurals.mu_parziale_message,
                                oggettiTrovati, oggettiTrovati, oggettiTotali);
            }
        }else if(oggettiTrovati == oggettiTotali){
            message = ctx.getString(R.string.mu_totale_message);
        }

        return message;
    }
}
