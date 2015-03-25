package it.giocoso.trovaintruso.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import it.giocoso.trovaintruso.R;
import it.giocoso.trovaintruso.beans.Oggetto;
import it.giocoso.trovaintruso.beans.Sessione;
import it.giocoso.trovaintruso.util.MailUtils;
import it.giocoso.trovaintruso.util.TimeUtils;
import it.giocoso.trovaintruso.util.JsonUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class ModeTwoActivity extends ActionBarActivity {


    private ArrayList<RelativeLayout> bottoniOggetti = new ArrayList<RelativeLayout>();
    private ArrayList<Oggetto> oggetti = new ArrayList<Oggetto>();
    private ArrayList<CountDownTimer> timerOggetti = new ArrayList<CountDownTimer>();
    private RelativeLayout stage;

    private Sessione s;

    private CountDownTimer cdt;

    TextView timerView;
    SoundPool sp;
    ArrayList<Integer> posizioni = new ArrayList<Integer>();
    int speed, attesa, c, cIntrusiTrovati, cSchermate, cSfondi;
    long tempoInizio, tempoGioco;
    String intruso, background;
    AlertDialog.Builder builder, exitBuilder;
    Button start, pause, next;
    LinearLayout gameInfo;
    RelativeLayout logo;

    JSONArray elementi;
    JSONObject elemento;

    int idxOggetti;

    int widthObj,heightObj, margin, widthScreen, heightScreen, startX, startY;

    SharedPreferences settings;

    DisplayMetrics displaymetrics;

    int backCounter = 0;
    int soundIds[] = new int[2];

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mode_one2);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        stage = (RelativeLayout) findViewById(R.id.stage);
        timerView = (TextView) findViewById(R.id.timer);
        start = (Button) findViewById(R.id.start);
        pause = (Button) findViewById(R.id.pause);
        next = (Button) findViewById(R.id.next);

        sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundIds[0] = sp.load(getApplicationContext(), R.raw.ok, 1);
        soundIds[1] = sp.load(getApplicationContext(), R.raw.error, 1);

        builder = new AlertDialog.Builder(this);
        exitBuilder = new AlertDialog.Builder(this);

        gameInfo = (LinearLayout) findViewById(R.id.gameInfo);

        settings = getSharedPreferences("settings", 0);

        s = new Sessione(settings.getInt("s2_criterio", 0),
                         settings.getInt("s2_numSchermate", 3),
                         settings.getInt("s2_numRighe", 6),
                         settings.getInt("s2_numColonne", 6),
                         settings.getInt("s2_numIntrusi", 4),
                         settings.getInt("s2_tempoMax", 180),
                         settings.getInt("s2_speed", 3),
                         settings.getInt("s2_attesa", 5));

        speed = s.getSpeed();
        attesa = s.getAttesa();
        cSchermate = 0;
        cSfondi = 0;
        idxOggetti = 0;

        creaSchermata();

        /*start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start.setVisibility(View.GONE);
                next.setVisibility(View.VISIBLE);
                startAnimation(v);
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start.setVisibility(View.VISIBLE);
                pause.setVisibility(View.GONE);
                pauseGame();
            }
        });*/

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                terminaSchermata();
            }
        });

    }

    public void startAnimation(View v) {

        // start game
        this.iniziaGioco();

    }

    public void creaSchermata(){

        //inizializzo valori per questa schermata

        backCounter = 0;
        tempoGioco = 0;
        tempoInizio = 0;
        cIntrusiTrovati = 0;
        idxOggetti = 0;
        posizioni.clear();
        stage.removeAllViews();
        timerOggetti.clear();
        timerView.setVisibility(View.VISIBLE);
        //start.setVisibility(View.VISIBLE);
        //pause.setVisibility(View.GONE);
        //next.setVisibility(View.GONE);
        next.setVisibility(View.VISIBLE);

        System.gc();

        //estraggo uno scenario in base al criterio della sessione

        try {
            JSONObject scenariCriterio = new JSONObject(JsonUtils.loadJSONFromAsset(s.getCriterio(), getApplicationContext()));

            JSONArray scenari = scenariCriterio.getJSONArray("scene");

            /*Random r = new Random();

            JSONObject scena = scenari.getJSONObject(r.nextInt(scenari.length()));*/

            JSONObject scena = scenari.getJSONObject(cSfondi);

            if(cSfondi<scenari.length()-1) {
                cSfondi++;
            }else if(cSfondi==scenari.length()-1){
                cSfondi = 0;
            }

            elementi = scena.getJSONArray("elementi");
            intruso = scena.getString("target");
            background = scena.getString("sfondo");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //imposto lo sfondo

        stage.setBackgroundResource(getResources().getIdentifier(background,
                "drawable", getPackageName()));


        gameInfo.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //Remove it here unless you want to get this callback for EVERY
                //layout pass, which can get you into infinite loops if you ever
                //modify the layout from within this method.
                gameInfo.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                //Now you can get the width and height from content
                displaymetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

                widthScreen = displaymetrics.widthPixels;
                heightScreen = displaymetrics.heightPixels - gameInfo.getMeasuredHeight();

                //determino quanto devono essere grandi gli oggetti in base alla risoluzione
                //considerando che sono immagini quadrate

                heightObj = displaymetrics.heightPixels/10;
                widthObj = heightObj;
                margin = heightObj/4;

                RelativeLayout l_next = (RelativeLayout) findViewById(R.id.l_next);
                ViewGroup.LayoutParams nextParams = l_next.getLayoutParams();
                nextParams.width = widthObj;
                nextParams.height = heightObj;
                l_next.setLayoutParams(nextParams);

                widthScreen = displaymetrics.widthPixels;
                heightScreen = displaymetrics.heightPixels - gameInfo.getMeasuredHeight();

                startX = (widthScreen - s.getNumColonne()*widthObj - margin*(s.getNumColonne()-1))/2;
                startY = (heightScreen - s.getNumRighe()*heightObj - margin*(s.getNumRighe()-1))/2;

                popolaStage();
            }
        });

    }

    public void popolaStage(){
        //creo gli oggetti normali
        for(int i = 0; i < s.getNumOggettiTotale(); i++){
            Oggetto obj = new Oggetto(i, null, false);
            oggetti.add(obj);
        }

        // creo la matrice sullo schermo

        for(int i = 0; i<s.getNumRighe(); i++){
            for(int j = 0; j<s.getNumColonne(); j++){
                creaOggetto(oggetti.get(idxOggetti), idxOggetti, i, j, startX, startY);
                idxOggetti++;
            }
        }

        //sorteggio le posizioni (gli id degli oggetti da sostituire) in cui compariranno gli intrusi

        ArrayList<Integer> list = new ArrayList<Integer>();

        for (int i = idxOggetti-s.getNumOggettiTotale(); i<s.getNumOggettiTotale(); i++) {
            list.add(new Integer(i));
        }
        Collections.shuffle(list);
        for (int i=0; i<s.getNumIntrusi(); i++) {
            posizioni.add(list.get(i));
        }


        //creo gli intrusi
        for(int i = 0; i<s.getNumIntrusi(); i++){

            Oggetto obj = new Oggetto(s.getNumOggettiTotale()+i, null, true);
            oggetti.add(obj);

            creaOggetto(obj, idxOggetti, i, 0, 0, 0);

            idxOggetti++;
        }


        //creo il timer (è in millisecondi)

        String timer = String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(s.getTempoMassimo()*1000),
                TimeUnit.MILLISECONDS.toSeconds(s.getTempoMassimo()*1000) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(s.getTempoMassimo()*1000))
        );

        timerView.setText(timer.toString());

        cdt = new CountDownTimer(s.getTempoMassimo()*1000+1000,1000){
            @Override
            public void onFinish() {
                //Cosa fare quando finisce
                timerView.setText("0:00");
                terminaSchermata();
                cdt.cancel();
            }

            @Override
            public void onTick(long millisUntilFinished) {
                //cosa fare ad ogni passaggio

                String timer = String.format("%d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
                );

                timerView.setText(timer.toString());

            }
        };

        //aggiungo il logo del gioco

        logo = null;
        logo = new RelativeLayout(getApplicationContext());

        stage.addView(logo);

        RelativeLayout.LayoutParams logoParams = (RelativeLayout.LayoutParams) logo.getLayoutParams();
        logoParams.width = widthObj;
        logoParams.height = heightObj;
        logo.setLayoutParams(logoParams);
        logo.setBackground(getResources().getDrawable(R.drawable.icona_intruso));
        logo.setX(widthScreen-widthObj-15);
        logo.setY(15);


        this.iniziaGioco();
    }

    public void creaOggetto(Oggetto oggetto, final int idxOggetti, final int i, int j, int startX, int startY) {

        RelativeLayout rl = new RelativeLayout(this);

        rl.setId(oggetto.getId());

        bottoniOggetti.add(rl);
        stage.addView(bottoniOggetti.get(idxOggetti));

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bottoniOggetti.get(idxOggetti).getLayoutParams();
        params.width = widthObj;
        params.height = heightObj;
        bottoniOggetti.get(idxOggetti).setLayoutParams(params);

        Random r = new Random();

        if(oggetto.isIntruso()) {
            bottoniOggetti.get(idxOggetti).setBackgroundResource(getResources().getIdentifier(intruso,
                    "drawable", getPackageName()));
            bottoniOggetti.get(idxOggetti).setX(bottoniOggetti.get(posizioni.get(i)).getX());
            bottoniOggetti.get(idxOggetti).setY(bottoniOggetti.get(posizioni.get(i)).getY());

            bottoniOggetti.get(idxOggetti).setAlpha(0f);
            bottoniOggetti.get(posizioni.get(i)).setAlpha(1f);

            c = i;
            //creo il timer per il periodo di accensione/spegnimento dell'intruso

            final CountDownTimer cdtSpento = new CountDownTimer(3600000,1000){

                int secondi;

                @Override
                public void onFinish() {
                    //Cosa fare quando finisce

                }

                @Override
                public void onTick(long millisUntilFinished) {
                    secondi++;

                    if(secondi==i*2+attesa){
                        final View appare = bottoniOggetti.get(idxOggetti);  //intruso
                        final View scompare = bottoniOggetti.get(posizioni.get(i));  //normale
                        appare.animate().alpha(1f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                appare.setAlpha(1f);
                                appare.setClickable(true);
                                scompare.setAlpha(0f);
                                scompare.setClickable(false);
                            }
                        });
                    }

                    if(secondi==i*2+attesa+speed){
                        final View scompare = bottoniOggetti.get(idxOggetti);  //intruso
                        final View appare = bottoniOggetti.get(posizioni.get(i));  //normale
                        appare.animate().alpha(1f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                appare.setAlpha(1f);
                                appare.setClickable(true);
                                scompare.setAlpha(0f);
                                scompare.setClickable(false);
                            }
                        });
                        secondi=i;
                    }
                }
            };

            timerOggetti.add(cdtSpento);


            bottoniOggetti.get(idxOggetti).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Log.d("dd", "CLICK - ID: "+v.getId());
                    Log.d("dd", "INTRUSO");
                    tempoGioco = System.currentTimeMillis() - tempoInizio;

                    Log.d("tempo!", Long.toString(tempoGioco));

                    //memorizzo il tempo
                    s.getSchermata(cSchermate).addTempoDiRisposta(tempoGioco);
                    s.getSchermata(cSchermate).setTempoDiCompletamento(tempoGioco);
                    cIntrusiTrovati++;

                    sp.play(soundIds[0], 1, 1, 1, 0, 1);
                    v.setAlpha(1f);
                    v.animate().alpha(0f).scaleX(2).scaleY(2).setDuration(300).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            //v.setVisibility(View.GONE);
                            stage.removeView(v);

                            cdtSpento.cancel();
                            bottoniOggetti.get(posizioni.get(i)).animate().alpha(1f).setDuration(300);

                            //controllo se ci sono altri intrusi da trovare
                            if(cIntrusiTrovati==s.getNumIntrusi()){
                                terminaSchermata();
                            }
                        }
                    });

                }
            });


        }else{
            try {
                elemento = elementi.getJSONObject(r.nextInt(elementi.length()));
                bottoniOggetti.get(idxOggetti).setBackgroundResource(getResources().getIdentifier(elemento.getString("nome"),
                        "drawable", getPackageName()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            bottoniOggetti.get(idxOggetti).setX(startX + j * (widthObj+margin));
            bottoniOggetti.get(idxOggetti).setY(startY + i * (heightObj+margin));

            bottoniOggetti.get(idxOggetti).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View v) {
                    Log.d("dd", "CLICK - ID: "+v.getId());
                    sp.play(soundIds[1], 1, 1, 1, 0, 1);
                }
            });
        }

        bottoniOggetti.get(idxOggetti).setClickable(false);


    }

    public void iniziaGioco() {

        // attivo i listener su tutti gli oggetti nello stage

        for(int y = 0; y<bottoniOggetti.size(); y++){
            bottoniOggetti.get(y).setClickable(true);
        }

        //Avvio il tempo
        for(int j=0; j<s.getNumIntrusi(); j++){
            try{
                timerOggetti.get(j).start();
            }catch(Exception e){
                Log.d("start", "exc"+Integer.toString(c));
            }
        }

        cdt.start();
        tempoInizio = System.currentTimeMillis();

    }

    public void terminaSchermata(){

        next.setVisibility(View.GONE);

        //fermo il tempo
        cdt.cancel();

        //ferma i timer degli intrusi e li rimuove dallo stage
        for(int j=0; j<s.getNumIntrusi(); j++){
            try{
                timerOggetti.get(j).cancel();
                stage.removeView(bottoniOggetti.get(j+idxOggetti-s.getNumIntrusi()));
                stage.removeView(bottoniOggetti.get(posizioni.get(j)));
            }catch(Exception e){
                Log.d("termina", "exc"+Integer.toString(c));
            }
        }

        //elimino tutti gli altri oggetti
        int i = idxOggetti-s.getNumOggettiTotale()-s.getNumIntrusi();

        for (; i < idxOggetti-s.getNumIntrusi(); i++) {
            final View v = bottoniOggetti.get(i);
            final int c = i;
            Log.d("for", Integer.toString(i));

            if(stage.findViewById(v.getId())!=null){
                v.animate().setStartDelay(i*60).alpha(0f).scaleX(2).scaleY(2).setDuration(300).setListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        try {
                            stage.removeView(v);
                        }catch (Exception e){
                            Log.d("removeView", "not found");
                        }

                        if(c==idxOggetti-s.getNumIntrusi()-1){
                            cSchermate++;

                            mostraPunteggioSchermata();
                        }
                    }

                });
            }else{
                if(c==idxOggetti-s.getNumIntrusi()-1){
                    cSchermate++;

                    mostraPunteggioSchermata();
                }
            }


        }
    }

    public void mostraPunteggioSchermata(){

        start.setVisibility(View.GONE);
        next.setVisibility(View.GONE);
        timerView.setVisibility(View.GONE);

        //svuoto un po' di memoria
        stage.removeAllViews();
        stage.setBackgroundResource(0);
        timerOggetti = null;
        timerOggetti = new ArrayList<CountDownTimer>();
        bottoniOggetti = null;
        bottoniOggetti = new ArrayList<RelativeLayout>();
        oggetti = null;
        oggetti = new ArrayList<Oggetto>();
        idxOggetti = 0;
        System.gc();

        String message = "";

        if(s.getSchermata(cSchermate-1).getTempiDiRisposta().size() < s.getNumIntrusi()){
            if(s.getSchermata(cSchermate-1).getTempiDiRisposta().size() == 0){
                message = "Uffa! Non hai trovato nessun oggetto!";
            }else if(s.getSchermata(cSchermate-1).getTempiDiRisposta().size() == 1){
                message = "Hai trovato 1 oggetto su "+s.getNumIntrusi()+"!";
            }else{
                message = "Hai trovato "+s.getSchermata(cSchermate-1).getTempiDiRisposta().size()+" oggetti su "+s.getNumIntrusi()+"!";
            }
        }else if(s.getSchermata(cSchermate-1).getTempiDiRisposta().size() == s.getNumIntrusi()){
            message = "Hai trovato tutti gli oggetti!";
        }

        builder.setMessage(
                message)
                .setCancelable(false)
                .setPositiveButton("Vai avanti",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {

                                dialog.cancel();

                                if(cSchermate == s.getSchermate().size()){
                                    mostraPunteggioFinale();
                                }else{
                                    Log.d("creaschermata", "aaa");
                                    creaSchermata();
                                }

                            }
                        });

        builder.create().show();

    }

    public void mostraPunteggioFinale(){

        start.setVisibility(View.GONE);
        pause.setVisibility(View.GONE);
        timerView.setVisibility(View.GONE);

        Log.d("mostrapunteggio", "AAA");

        MailUtils mu = new MailUtils();
        String riepilogo = mu.getRiepilogo(s, getApplicationContext());

        if(settings.getBoolean("sendMail", false) == true) {
            mu.sendMail(riepilogo, settings.getString("emailMitt", ""), settings.getString("pswMitt", ""), settings.getString("s2_email", ""));
        }

        LinearLayout risultati = new LinearLayout(getApplicationContext());
        risultati.setPadding(40, 40, 40, 40);
        risultati.setBackgroundColor(Color.argb(128, 0, 0, 0));

        stage.addView(risultati);

        risultati.setOrientation(LinearLayout.VERTICAL);
        stage.setGravity(Gravity.CENTER);
        ViewGroup.LayoutParams params = risultati.getLayoutParams();
        params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        risultati.setLayoutParams(params);

        TextView title = new TextView(getApplicationContext());
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP,24);
        title.setText(mu.getPunteggioTotale(s, getApplicationContext()));
        risultati.addView(title);

    }

    public void pauseGame(){

    }

    public void svuotaMemoria(){

        //fermo il tempo
        cdt.cancel();

        //ferma i timer degli intrusi e li rimuove dallo stage
        for(int j=0; j<s.getNumIntrusi(); j++){
            try{
                timerOggetti.get(j).cancel();
                stage.removeView(bottoniOggetti.get(j+idxOggetti-s.getNumIntrusi()));
                stage.removeView(bottoniOggetti.get(posizioni.get(j)));
            }catch(Exception e){
                Log.d("termina", "exc"+Integer.toString(c));
            }
        }

        stage.removeAllViews();
        stage.setBackgroundResource(0);
        timerOggetti = null;
        timerOggetti = new ArrayList<CountDownTimer>();
        bottoniOggetti = null;
        bottoniOggetti = new ArrayList<RelativeLayout>();
        oggetti = null;
        oggetti = new ArrayList<Oggetto>();
        idxOggetti = 0;
    }

    @Override
    public void onBackPressed() {

        exitBuilder.setMessage(
                "Vuoi davvero uscire dal gioco?")
                .setCancelable(false)
                .setNegativeButton("No, continuo a giocare!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Si, esci dal gioco!",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {

                                dialog.cancel();
                                svuotaMemoria();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                System.gc();

                            }
                        });

        exitBuilder.create().show();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
