package it.giocoso.trovaintruso.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import it.giocoso.trovaintruso.R;
import it.giocoso.trovaintruso.activities.MainActivity;
import it.giocoso.trovaintruso.activities.ModeOneActivity;
import it.giocoso.trovaintruso.beans.Sessione;

public class MailUtils {

    Context ctx;
    Activity mActivity;
    DbAdapter db;
    String email, psw;
    ArrayList<SendEmailAsyncTask> mailTasks = new ArrayList<SendEmailAsyncTask>();
    ProgressDialog dialog;

    //metodo che aggiunge la mail alla coda di invio e richiama l'elaborazione della coda

    public void sendMail(String riepilogo, String emailMitt, String pswMitt, String emailDest, Context context) throws SQLException {

        ctx = context;
        mActivity = (Activity) context;
        db = new DbAdapter(ctx);

        //aggiungo la mail alla coda

        db.open();
        db.createMail(riepilogo, emailDest);
        db.close();

        email = emailMitt;
        psw = pswMitt;

        //provo a elaborare la coda di email

        elaboraCoda();
    }

    //metodo che recupera le email da inviare e prova a inoltrarle

    public void elaboraCoda() throws SQLException {

        Cursor cMail;

        db.open();
        cMail = db.fetchAllEmails();

        //se ci sono mail da inviare

        if(cMail.getCount()>0){

            //mostro il messaggio di attesa

            dialog = new ProgressDialog(ctx);
            dialog.setMessage(ctx.getString(R.string.mu_attesa_message));
            dialog.show();

            while(cMail.moveToNext()){

                Log.d("mailUtils", "mail da inviare");

                //eseguo un task asincrono per l'invio di una mail

                SendEmailAsyncTask mailTask = new SendEmailAsyncTask();
                mailTask.execute(cMail.getString(cMail.getColumnIndex(DbAdapter.KEY_MESSAGE)),
                        email, psw, cMail.getString(cMail.getColumnIndex(DbAdapter.KEY_EMAIL)),
                        cMail.getString(cMail.getColumnIndex(DbAdapter.KEY_ID)));

                //e lo aggiungo a un arraylist per tenere traccia di quanti task sono in esecuzione

                mailTasks.add(mailTask);

            }

        }

    }

    //classe asincrona per l'invio delle mail

    class SendEmailAsyncTask extends AsyncTask<String, Void, Boolean> {

        AlertDialog.Builder alertDialog;
        String riepilogo, emailMitt, pswMitt, emailDest, errore, id;
        View dialogLayout;
        LayoutInflater inflater;
        boolean esito = true;


        protected void onPreExecute() {

            //preparo la dialog di errore

            super.onPreExecute();
            alertDialog = new AlertDialog.Builder(ctx);
            inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            dialogLayout = inflater.inflate(R.layout.dialog_errore, null);
            alertDialog.setView(dialogLayout);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            riepilogo = params[0];
            emailMitt = params[1];
            pswMitt = params[2];
            emailDest = params[3];
            id = params[4];

            GMailSender sender = new GMailSender(emailMitt, pswMitt);

            //provo a inviare la mail

            try {
                sender.sendMail(ctx.getString(R.string.mu_oggetto_mail),
                        riepilogo,
                        emailMitt,
                        emailDest);
                return true;
            } catch (AuthenticationFailedException e) {
                Log.e(SendEmailAsyncTask.class.getName(), "Bad account details");
                errore = ctx.getString(R.string.d_errore_message_1);
                e.printStackTrace();
                return false;
            } catch (MessagingException e) {
                Log.e(SendEmailAsyncTask.class.getName(), "failed");
                errore = ctx.getString(R.string.d_errore_message_2);
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                errore = ctx.getString(R.string.d_errore_message_3);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            //se la mail è stata inviata allora la cancello dalla coda

            if(aBoolean){
                db.deleteMail(id);
                Log.d("SendMail", "mail inviata");
            }

            //se la mail non è stata inviata e non ho ancora segnato che l'esito generale è negativo

            if(!aBoolean && esito) {
                //segno che c'è almeno un task fallito
                esito = false;
            }

            //controllo quanti sono i task finiti

            int unfinishedTasks = 0;
            for (SendEmailAsyncTask myMT : mailTasks){
                if(!(myMT.getStatus() == AsyncTask.Status.FINISHED)){
                    unfinishedTasks++;
                }
            }

            //se questo è l'ultimo task in esecuzione

            if(unfinishedTasks == 1){

                //se la dialog di attesa è visibile la chiudo

                if (dialog.isShowing()) {
                    dialog.dismiss();

                    //all'ultimo task chiudo il db

                    db.close();
                }
            }

            //se c'è almeno un task fallito
            //e questo è l'ultimo task in esecuzione
            //allora posso mostrare l'errore

            if((!esito)&&(unfinishedTasks==1)) {
                TextView errorMessage = (TextView) dialogLayout.findViewById(R.id.message);
                errorMessage.setText(errore);
                alertDialog.setPositiveButton(ctx.getString(R.string.mu_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(ctx.getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        //chiudo l'activity che ha aperto la dialog

                        mActivity.finish();
                        ctx.startActivity(intent);
                    }
                });
                alertDialog.show();
            }else if((esito)&&(unfinishedTasks==1)) {

                //se tutte le mail sono state inviate e questo è l'ultimo task
                //allora chiudo l'activity di gioco e torno alla schermata principale

                Intent intent = new Intent(ctx.getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                //chiudo l'activity che ha aperto la dialog

                mActivity.finish();
                ctx.startActivity(intent);
            }
        }
    }

    //metodo per la generazione del testo della mail

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

    //metodo che fornisce il testo della dialog a fine partita

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
