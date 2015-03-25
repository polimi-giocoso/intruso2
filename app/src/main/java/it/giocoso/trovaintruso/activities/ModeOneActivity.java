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
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Bounce;
import it.giocoso.trovaintruso.R;
import it.giocoso.trovaintruso.beans.Oggetto;
import it.giocoso.trovaintruso.beans.Schermata;
import it.giocoso.trovaintruso.beans.Sessione;
import it.giocoso.trovaintruso.util.JsonUtils;
import it.giocoso.trovaintruso.util.MailUtils;
import it.giocoso.trovaintruso.util.TimeUtils;
import it.giocoso.trovaintruso.util.ViewContainer;
import it.giocoso.trovaintruso.util.ViewContainerAccessor;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ModeOneActivity extends ActionBarActivity {

    private TweenManager tweenManager;
    private boolean isAnimationRunning = true;

    private ArrayList<RelativeLayout> bottoniOggetti = new ArrayList<RelativeLayout>();
    private ArrayList<Oggetto> oggetti = new ArrayList<Oggetto>();
    private RelativeLayout stage;

    private Sessione s;

    private CountDownTimer cdt;

    TextView timerView;
    SoundPool sp;
    int cIntrusiTrovati, idxOggetti, cSchermate, cSfondi;
    int widthObj, heightObj, widthScreen, heightScreen;
    long tempoInizio, tempoGioco;
    String intruso, background;
    AlertDialog.Builder builder, exitBuilder;
    Button start, pause, next;
    LinearLayout gameInfo;
    RelativeLayout logo;

    JSONArray elementi;
    JSONObject elemento;

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

        settings = getSharedPreferences("settings", 0);

        gameInfo = (LinearLayout) findViewById(R.id.gameInfo);

        idxOggetti = 0;
        cSchermate = 0;
        cSfondi = 0;

        SharedPreferences settings = getSharedPreferences(
                "settings", 0);

        s = new Sessione(settings.getInt("s1_criterio", 0),
                settings.getInt("s1_numSchermate", 3),
                settings.getInt("s1_numOggetti", 30),
                settings.getInt("s1_numIntrusi", 6),
                settings.getInt("s1_tempoMax", 180));

        setTweenEngine();

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

    public void creaSchermata(){

        //inizializzo valori per questa schermata
        System.gc();

        backCounter = 0;
        tempoInizio = 0;
        tempoGioco = 0;
        cIntrusiTrovati = 0;
        //start.setVisibility(View.VISIBLE);
        //pause.setVisibility(View.GONE);
        //next.setVisibility(View.GONE);
        next.setVisibility(View.VISIBLE);

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

        stage.setBackgroundResource(getResources().getIdentifier(background, "drawable", getPackageName()));

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

                //determino quanto devono essere grandi gli oggetti in base alla risoluzione
                //considerando che sono immagini quadrate

                heightObj = displaymetrics.heightPixels/10;
                widthObj = heightObj;
                //widthScreen = displaymetrics.widthPixels - displaymetrics.widthPixels/6;
                //heightScreen = displaymetrics.heightPixels - gameInfo.getMeasuredHeight() - heightObj;

                widthScreen = displaymetrics.widthPixels - widthObj;
                heightScreen = displaymetrics.heightPixels - gameInfo.getMeasuredHeight();

                RelativeLayout l_next = (RelativeLayout) findViewById(R.id.l_next);
                ViewGroup.LayoutParams nextParams = l_next.getLayoutParams();
                nextParams.width = widthObj;
                nextParams.height = heightObj;
                l_next.setLayoutParams(nextParams);

                popolaStage();
            }
        });

    }

    public void popolaStage(){

        //aggiungo il logo del gioco

        logo = null;
        logo = new RelativeLayout(getApplicationContext());

        stage.addView(logo);

        RelativeLayout.LayoutParams logoParams = (RelativeLayout.LayoutParams) logo.getLayoutParams();
        logoParams.width = widthObj;
        logoParams.height = heightObj;
        logo.setLayoutParams(logoParams);
        logo.setBackground(getResources().getDrawable(R.drawable.icona_intruso));
        logo.setX(widthScreen-15);
        logo.setY(15);


        //creo gli oggetti normali
        for(int i = 0; i < (s.getNumOggettiTotale() - s.getNumIntrusi()); i++){

            Oggetto obj = new Oggetto(i, null, false);
            oggetti.add(obj);
            creaOggetto(obj, idxOggetti);
            idxOggetti++;
        }

        //creo gli intrusi
        for(int i = 0; i<s.getNumIntrusi(); i++){

            Oggetto obj = new Oggetto(i + (s.getNumOggettiTotale() - s.getNumIntrusi()), null, true);
            oggetti.add(obj);
            creaOggetto(obj, idxOggetti);
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
                tweenManager.pause();
                terminaSchermata();
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

        startAnimation(next);
    }


    public void creaOggetto(Oggetto obj, int i){
        RelativeLayout rl = new RelativeLayout(this);

        rl.setId(obj.getId());

        bottoniOggetti.add(rl);

        stage.addView(bottoniOggetti.get(i));

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bottoniOggetti.get(i).getLayoutParams();
        params.width = widthObj;
        params.height = heightObj;
        bottoniOggetti.get(i).setLayoutParams(params);

        Random r = new Random();

        if(obj.isIntruso()) {
            bottoniOggetti.get(i).setBackgroundResource(getResources().getIdentifier(intruso,
                    "drawable", getPackageName()));
        }else{
            try {
                elemento = elementi.getJSONObject(r.nextInt(elementi.length()));
                bottoniOggetti.get(i).setBackgroundResource(getResources().getIdentifier(elemento.getString("nome"),
                        "drawable", getPackageName()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }



        bottoniOggetti.get(i).setX(r.nextInt(10+i) * widthScreen/(10+i));
        bottoniOggetti.get(i).setY(r.nextInt(6) * heightScreen / 6 + heightObj/2);



        bottoniOggetti.get(i).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d("dd", "CLICK - ID: "+v.getId());
                if(oggetti.get(v.getId()).isIntruso()){
                    Log.d("dd", "INTRUSO");
                    tempoGioco = System.currentTimeMillis() - tempoInizio;

                    Log.d("tempo!", Long.toString(tempoGioco));

                    //memorizzo il tempo
                    s.getSchermata(cSchermate).addTempoDiRisposta(tempoGioco);
                    s.getSchermata(cSchermate).setTempoDiCompletamento(tempoGioco);
                    cIntrusiTrovati++;

                    //mpOK.start();
                    sp.play(soundIds[0], 1, 1, 1, 0, 1);
                    ViewContainer cont = new ViewContainer();
                    cont.view = bottoniOggetti.get(v.getId());
                    v.setAlpha(1f);
                    v.animate().alpha(0f).scaleX(2).scaleY(2).setDuration(300).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            v.setVisibility(View.GONE);

                            //controllo se ci sono altri intrusi da trovare
                            if(cIntrusiTrovati==s.getNumIntrusi()){
                                terminaSchermata();
                            }
                        }
                    });

                }else{
                    sp.play(soundIds[1], 1, 1, 1, 0, 1);
                }
            }
        });

        bottoniOggetti.get(i).setClickable(false);
    }

    /**
     * Initiate the Tween Engine
     */
    private void setTweenEngine() {

        tweenManager = new TweenManager();
        //start animation theread
        setAnimationThread();

        //**Register Accessor, this is very important to do!
        //You need register actually each Accessor, but right now we have global one, which actually suitable for everything.
        Tween.registerAccessor(ViewContainer.class, new ViewContainerAccessor());

        //this.startAnimation();
    }


    public void startAnimation(View v) {

        Log.d("dd", "start");

        // attivo i listener su tutti gli oggetti nello stage

        for(int y = 0; y<bottoniOggetti.size(); y++){
            bottoniOggetti.get(y).setClickable(true);
        }

        // start timer

        cdt.start();
        tempoInizio = System.currentTimeMillis();

        ///start animations
        int i = idxOggetti-s.getNumOggettiTotale();
        int c = 0;
        for(; i<idxOggetti; i++) {
            ViewContainer cont = new ViewContainer();
            cont.view = bottoniOggetti.get(i);
            float x = bottoniOggetti.get(i).getX();
            float y = bottoniOggetti.get(i).getY();

            Random r = new Random();

            if(c == 0) {
                Timeline.createSequence()
                        .push(Tween.to(cont, ViewContainerAccessor.POSITION_XY, 6 + r.nextInt(12)).target(stage.getWidth() - x - widthObj, 0))
                        .push(Tween.to(cont, ViewContainerAccessor.POSITION_XY, 6+r.nextInt(12)).target(-x, 0))
                        .push(Tween.to(cont, ViewContainerAccessor.POSITION_XY, 6+r.nextInt(12)).target(0, 0))
                        .repeat(30, 0)
                        .start(tweenManager);
                c++;
            }else if(c == 1){
                Timeline.createSequence()
                        .push(Tween.to(cont, ViewContainerAccessor.POSITION_XY, 6+r.nextInt(12)).target(-x, 0))
                        .push(Tween.to(cont, ViewContainerAccessor.POSITION_XY, 6+r.nextInt(12)).target(stage.getWidth()-x-widthObj, 0))
                        .push(Tween.to(cont, ViewContainerAccessor.POSITION_XY, 6+r.nextInt(12)).target(0, 0))
                        .repeat(30,0)
                        .start(tweenManager);
                c++;
            }else if(c == 2){
                Timeline.createSequence()
                        .push(Tween.to(cont, ViewContainerAccessor.POSITION_XY, 6 + r.nextInt(12)).target(0, stage.getHeight() - y - heightObj))
                        .push(Tween.to(cont, ViewContainerAccessor.POSITION_XY, 6+r.nextInt(12)).target(0, -y))
                        .push(Tween.to(cont, ViewContainerAccessor.POSITION_XY, 6+r.nextInt(12)).target(0, 0))
                        .repeat(30, 0)
                        .start(tweenManager);
                c++;

            }else if(c == 3){
                Timeline.createSequence()
                        .push(Tween.to(cont, ViewContainerAccessor.POSITION_XY, 6+r.nextInt(12)).target(0, -y))
                        .push(Tween.to(cont, ViewContainerAccessor.POSITION_XY, 6+r.nextInt(12)).target(0, stage.getHeight()-y-heightObj))
                        .push(Tween.to(cont, ViewContainerAccessor.POSITION_XY, 6+r.nextInt(12)).target(0, 0))
                        .repeat(30,0)
                        .start(tweenManager);

                c = 0;
            }
        }

    }

    /***
     * Thread that should run for update UI via Tween engine
     */
    private void setAnimationThread() {

        new Thread(new Runnable() {
            private long lastMillis = -1;

            @Override public void run() {
                while (isAnimationRunning) {
                    if (lastMillis > 0) {
                        long currentMillis = System.currentTimeMillis();
                        final float delta = (currentMillis - lastMillis) / 1000f;

            /*
            view.post(new Runnable(){
              @Override public void run() {

              }
            });
            */
                        /**
                         * We run all animation in UI thread instead of using post for each elements.
                         */
                        runOnUiThread(new Runnable() {

                            @Override public void run() {
                                tweenManager.update(delta);
                            }
                        });

                        lastMillis = currentMillis;
                    } else {
                        lastMillis = System.currentTimeMillis();
                    }

                    try {
                        Thread.sleep(1000 / 60);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }).start();

    }

    /**
     * Stop animation thread
     */
    public void setAnimationFalse(View v) {
        tweenManager.pause();
    }

    public void resumeAnimation(View v) {
        tweenManager.resume();
    }

    /**
     * Make animation thread alive
     */
    private void setAnimationTrue() {
        isAnimationRunning = true;
    }

    public void terminaSchermata(){

        next.setVisibility(View.GONE);

        //fermo il tempo
        cdt.cancel();

        int i = idxOggetti-s.getNumOggettiTotale();
        for (; i < idxOggetti; i++) {
            final View v = bottoniOggetti.get(i);
            final int c = i;
            if(stage.findViewById(v.getId())!=null){
                v.animate().setStartDelay(i*30).alpha(0f).scaleX(2).scaleY(2).setDuration(300).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        v.setVisibility(View.GONE);
                        v.setBackground(null);
                        //System.gc();
                        if(c==idxOggetti-1){
                            cSchermate++;

                            mostraPunteggioSchermata();
                        }
                    }
                });
            }else{
                if(c==idxOggetti-1){
                    cSchermate++;
                    mostraPunteggioSchermata();
                }
            }

        }
    }

    public void mostraPunteggioSchermata(){

        start.setVisibility(View.GONE);
        pause.setVisibility(View.GONE);
        next.setVisibility(View.GONE);

        //svuoto un po' di memoria
        stage.removeAllViews();
        stage.setBackgroundResource(0);
        bottoniOggetti = null;
        bottoniOggetti = new ArrayList<RelativeLayout>();
        oggetti = null;
        oggetti = new ArrayList<Oggetto>();
        cdt = null;
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
                                    tweenManager.resume();
                                    creaSchermata();
                                }

                            }
                        });

        builder.create().show();

    }

    public void mostraPunteggioFinale(){

        Log.d("mostrapunteggio","AAA");

        start.setVisibility(View.GONE);
        pause.setVisibility(View.GONE);
        timerView.setVisibility(View.GONE);

        MailUtils mu = new MailUtils();
        String riepilogo = mu.getRiepilogo(s, getApplicationContext());

        if(settings.getBoolean("sendMail", false) == true) {
            mu.sendMail(riepilogo, settings.getString("emailMitt", ""), settings.getString("pswMitt", ""), settings.getString("s2_email", ""));
        }

        LinearLayout risultati = new LinearLayout(getApplicationContext());
        risultati.setPadding(18, 18, 18, 18);
        risultati.setPadding(40, 40, 40, 40);
        risultati.setBackgroundColor(Color.argb(128, 0, 0, 0));
        stage.addView(risultati);
        stage.setGravity(Gravity.CENTER);

        risultati.setOrientation(LinearLayout.VERTICAL);
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

    /*@Override
    public void onBackPressed() {
        backCounter++;
        if(backCounter == 7){
            finish();
        }
    }*/

    public void svuotaMemoria(){

        //fermo il tempo
        if(cdt!=null) {
            cdt.cancel();
        }

        //svuoto un po' di memoria
        stage.removeAllViews();
        stage.setBackgroundResource(0);
        bottoniOggetti = null;
        bottoniOggetti = new ArrayList<RelativeLayout>();
        oggetti = null;
        oggetti = new ArrayList<Oggetto>();
        cdt = null;
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
