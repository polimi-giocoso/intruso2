package it.giocoso.trovaintruso.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import it.giocoso.trovaintruso.R;
import it.giocoso.trovaintruso.beans.Sessione;

/**
 * Created by chicco on 26/02/15.
 */
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
                sender.sendMail("Riepilogo sessione di gioco",
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

        riepilogo += "## DATI SESSIONE DI GIOCO ##\n";
        riepilogo += "Criterio di gioco: "+ criteri[s.getCriterio()] + "\n";
        riepilogo += "Tempo massimo per schermata: "+ s.getTempoMassimo() + "\n";
        riepilogo += "Numero totale di oggetti per schermata: "+ s.getNumOggettiTotale() + "\n";
        riepilogo += "Numero di intrusi: "+ s.getNumIntrusi() + "\n";
        riepilogo += "Numero di schermate: "+ s.getSchermate().size() + "\n";
        riepilogo += "Velocità: "+ s.getSpeed() + "\n";
        riepilogo += "\n";


        for(int i = 0; i<s.getSchermate().size(); i++){

            riepilogo += "## SCHERMATA "+Integer.toString(i+1)+" ##\n";
            riepilogo += "Numero di oggetti trovati: "+ s.getSchermate().get(i).getTempiDiRisposta().size() + "\n";

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
}
