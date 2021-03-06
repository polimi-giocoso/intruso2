package it.giocoso.trovaintruso.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import it.giocoso.trovaintruso.R;
import it.giocoso.trovaintruso.beans.Oggetto;
import it.giocoso.trovaintruso.beans.Sessione;
import it.giocoso.trovaintruso.util.ImgUtils;
import it.giocoso.trovaintruso.util.JsonUtils;
import it.giocoso.trovaintruso.util.MailUtils;
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
    Button next;
    LinearLayout gameInfo;
    RelativeLayout logo;

    ArrayList<Drawable> elementiDr = new ArrayList<Drawable>();
    Drawable intrusoDr;

    JSONArray elementi;

    SharedPreferences settings;

    DisplayMetrics displaymetrics;

    int backCounter = 0;
    int soundIds[] = new int[2];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mode_one2);

        //imposto la font di default

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/FedraSansBold.otf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        //nascondo la action bar e la status bar

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        stage = (RelativeLayout) findViewById(R.id.stage);
        timerView = (TextView) findViewById(R.id.timer);
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

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                terminaSchermata();
            }
        });

        //avvio il Tween Engine per le animazioni

        setTweenEngine();

        //creo la prima schermata

        creaSchermata();

    }

    /**
     * Crea la schermata di gioco
     */

    public void creaSchermata() {

        System.gc();

        //inizializzo valori per questa schermata

        backCounter = 0;
        tempoInizio = 0;
        tempoGioco = 0;
        cIntrusiTrovati = 0;
        next.setVisibility(View.VISIBLE);

        //estraggo uno scenario in base al criterio della sessione

        try {
            JSONObject scenariCriterio = new JSONObject(JsonUtils.loadJSONFromAsset(s.getCriterio(), getApplicationContext()));
            JSONArray scenari = scenariCriterio.getJSONArray("scene");
            JSONObject scena = scenari.getJSONObject(cSfondi);

            if (cSfondi < scenari.length() - 1) {
                cSfondi++;
            } else if (cSfondi == scenari.length() - 1) {
                cSfondi = 0;
            }

            elementi = scena.getJSONArray("elementi");
            intruso = scena.getString("target");
            background = scena.getString("sfondo");

            //definisco le drawable per oggetto e intruso

            intrusoDr = new BitmapDrawable(ImgUtils.decodeSampledBitmapFromResource(
                    getResources(), getResources().getIdentifier(intruso, "drawable", getPackageName()), 320, 320
            ));

            for (int f = 0; f < elementi.length(); f++) {
                elementiDr.add(new BitmapDrawable(ImgUtils.decodeSampledBitmapFromResource(
                        getResources(), getResources().getIdentifier(elementi.getJSONObject(f).getString("nome"), "drawable", getPackageName()), 320, 320
                )));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //codice da eseguire solo quando il layout è stato correttamente definito sullo schermo

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

                heightObj = displaymetrics.heightPixels / 10;
                widthObj = heightObj;

                widthScreen = displaymetrics.widthPixels - widthObj;
                heightScreen = displaymetrics.heightPixels - gameInfo.getMeasuredHeight();

                RelativeLayout l_next = (RelativeLayout) findViewById(R.id.l_next);
                ViewGroup.LayoutParams nextParams = l_next.getLayoutParams();
                nextParams.width = widthObj;
                nextParams.height = heightObj;
                l_next.setLayoutParams(nextParams);

                // imposto lo sfondo

                ImageView stageBg = (ImageView) findViewById(R.id.stage_bg);

                stageBg.setImageBitmap(ImgUtils.decodeSampledBitmapFromResource(
                        getResources(), getResources().getIdentifier(background, "drawable", getPackageName()), 640, 480
                ));

                // dispongo gli oggetti sullo stage

                popolaStage();

            }
        });

    }

    /**
     * Aggiunge gli elementi allo stage
     */

    public void popolaStage() {

        //aggiungo il logo del gioco

        logo = null;
        logo = new RelativeLayout(getApplicationContext());

        stage.addView(logo);

        RelativeLayout.LayoutParams logoParams = (RelativeLayout.LayoutParams) logo.getLayoutParams();
        logoParams.width = widthObj;
        logoParams.height = heightObj;
        logo.setLayoutParams(logoParams);
        logo.setBackground(getResources().getDrawable(R.drawable.icona_intruso));
        logo.setX(widthScreen - 15);
        logo.setY(15);

        //creo gli oggetti normali

        for (int i = 0; i < (s.getNumOggettiTotale() - s.getNumIntrusi()); i++) {

            Oggetto obj = new Oggetto(i, null, false);
            oggetti.add(obj);
            creaOggetto(obj, idxOggetti);
            idxOggetti++;
        }

        //creo gli intrusi

        for (int i = 0; i < s.getNumIntrusi(); i++) {

            Oggetto obj = new Oggetto(i + (s.getNumOggettiTotale() - s.getNumIntrusi()), null, true);
            oggetti.add(obj);
            creaOggetto(obj, idxOggetti);
            idxOggetti++;
        }

        //creo il timer (è in millisecondi)

        String timer = String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(s.getTempoMassimo() * 1000),
                TimeUnit.MILLISECONDS.toSeconds(s.getTempoMassimo() * 1000) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(s.getTempoMassimo() * 1000))
        );

        timerView.setText(timer.toString());

        cdt = new CountDownTimer(s.getTempoMassimo() * 1000 + 1000, 1000) {

            @Override
            public void onFinish() {

                //Cosa fare quando finisce il tempo

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

    /**
     * Crea il singolo oggetto
     */

    public void creaOggetto(Oggetto obj, int i) {

        RelativeLayout rl = new RelativeLayout(this);

        rl.setId(obj.getId());

        bottoniOggetti.add(rl);

        stage.addView(bottoniOggetti.get(i));

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bottoniOggetti.get(i).getLayoutParams();
        params.width = widthObj;
        params.height = heightObj;
        bottoniOggetti.get(i).setLayoutParams(params);

        Random r = new Random();

        if (obj.isIntruso()) {
            bottoniOggetti.get(i).setBackgroundDrawable(intrusoDr);
        } else {
            bottoniOggetti.get(i).setBackgroundDrawable(elementiDr.get(r.nextInt(elementiDr.size())));
        }

        bottoniOggetti.get(i).setX(r.nextInt(10 + i) * widthScreen / (10 + i));
        bottoniOggetti.get(i).setY(r.nextInt(6) * heightScreen / 7 + heightObj / 2);


        bottoniOggetti.get(i).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d("dd", "CLICK - ID: " + v.getId());
                if (oggetti.get(v.getId()).isIntruso()) {

                    //disabilito il click in modo che venga registrato un solo tocco

                    v.setClickable(false);

                    Log.d("dd", "INTRUSO");
                    tempoGioco = System.currentTimeMillis() - tempoInizio;

                    Log.d("tempo!", Long.toString(tempoGioco));

                    //memorizzo il tempo

                    s.getSchermata(cSchermate).addTempoDiRisposta(tempoGioco);
                    s.getSchermata(cSchermate).setTempoDiCompletamento(tempoGioco);
                    cIntrusiTrovati++;

                    sp.play(soundIds[0], 1, 1, 1, 0, 1);
                    ViewContainer cont = new ViewContainer();
                    cont.view = bottoniOggetti.get(v.getId());
                    v.setAlpha(1f);
                    v.animate().alpha(0f).scaleX(2).scaleY(2).setDuration(300).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            v.setVisibility(View.GONE);

                            //controllo se ci sono altri intrusi da trovare

                            if (cIntrusiTrovati == s.getNumIntrusi()) {
                                terminaSchermata();
                            }
                        }
                    });

                } else {
                    sp.play(soundIds[1], 1, 1, 1, 0, 1);

                    //registro l'errore

                    s.getSchermata(cSchermate).addNewErrore();
                }
            }
        });

        bottoniOggetti.get(i).setClickable(false);
    }

    /**
     * Inizializza il Tween Engine
     */

    private void setTweenEngine() {

        tweenManager = new TweenManager();

        //start animation thread

        setAnimationThread();

        //**Register Accessor, this is very important to do!
        //You need register actually each Accessor, but right now we have global one, which actually suitable for everything.

        Tween.registerAccessor(ViewContainer.class, new ViewContainerAccessor());

    }

    /**
     * Avvia l'animazione
     */

    public void startAnimation(View v) {

        Log.d("dd", "start");

        // attivo i listener su tutti gli oggetti nello stage

        for (int y = 0; y < bottoniOggetti.size(); y++) {
            bottoniOggetti.get(y).setClickable(true);
        }

        // start timer

        cdt.start();
        tempoInizio = System.currentTimeMillis();

        ///start animations

        int i = idxOggetti - s.getNumOggettiTotale();
        for (; i < idxOggetti; i++) {
            ViewContainer cont = new ViewContainer();
            cont.view = bottoniOggetti.get(i);
            float x = bottoniOggetti.get(i).getX();
            float y = bottoniOggetti.get(i).getY();

            Random r = new Random();

            Timeline timeline = Timeline.createSequence();
//            timeline.push(Tween.to(cont, ViewContainerAccessor.POSITION_XY, 1 + r.nextInt(6)).target(r.nextInt((int) (stage.getWidth() - x - widthObj)), r.nextInt((int) (stage.getHeight() - y - heightObj))));
            for (int j = 0; j < 30; j++) {
                int rndX = (int) (r.nextInt(stage.getWidth() - widthObj) - x);
                int rndY = (int) (r.nextInt(stage.getHeight() - heightObj) - y);
                timeline.push(Tween.to(cont, ViewContainerAccessor.POSITION_XY, 2 + r.nextInt(5)).target(rndX, rndY));
                Log.d("ModeOneActivity", i + ": (" + rndX + "," + rndY + ")");
            }
            timeline.start(tweenManager);
        }
    }

    /**
     * Thread in esecuzione per aggiornare la UI tramite Tween engine
     */

    private void setAnimationThread() {

        new Thread(new Runnable() {
            private long lastMillis = -1;

            @Override
            public void run() {
                while (isAnimationRunning) {
                    if (lastMillis > 0) {
                        long currentMillis = System.currentTimeMillis();
                        final float delta = (currentMillis - lastMillis) / 1000f;

                        /**
                         * We run all animation in UI thread instead of using post for each elements.
                         */

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
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
     * Termina la schermata di gioco
     */

    public void terminaSchermata() {

        next.setVisibility(View.GONE);

        //fermo il tempo

        cdt.cancel();

        int i = idxOggetti - s.getNumOggettiTotale();
        for (; i < idxOggetti; i++) {
            final View v = bottoniOggetti.get(i);
            final int c = i;
            if (stage.findViewById(v.getId()) != null) {
                v.animate().setStartDelay(i * 30).alpha(0f).scaleX(2).scaleY(2).setDuration(300).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        v.setVisibility(View.GONE);
                        v.setBackground(null);

                        if (c == idxOggetti - 1) {
                            cSchermate++;

                            mostraPunteggioSchermata();
                        }
                    }
                });
            } else {
                if (c == idxOggetti - 1) {
                    cSchermate++;
                    mostraPunteggioSchermata();
                }
            }

        }
    }

    /**
     * Mostra il punteggio della schermata appena giocata
     */

    public void mostraPunteggioSchermata() {

        next.setVisibility(View.GONE);

        //svuoto un po' di memoria

        svuotaMemoria();

        String message = "";
        String titolo = "";

        if (s.getSchermata(cSchermate - 1).getTempiDiRisposta().size() < s.getNumIntrusi()) {
            if (s.getSchermata(cSchermate - 1).getTempiDiRisposta().size() == 0) {

                //ho perso

                titolo = getString(R.string.md_perso_titolo);
                message = getString(R.string.md_perso_message);
            } else {

                //quasi tutti gli intrusi

                titolo = getString(R.string.md_parziale_titolo);
                message = getResources().getQuantityString(R.plurals.md_parziale_message,
                        s.getSchermata(cSchermate - 1).getTempiDiRisposta().size(),
                        s.getSchermata(cSchermate - 1).getTempiDiRisposta().size(),
                        s.getNumIntrusi());
            }
        } else if (s.getSchermata(cSchermate - 1).getTempiDiRisposta().size() == s.getNumIntrusi()) {

            //ho trovato tutti gli intrusi

            titolo = getString(R.string.md_totale_titolo);
            message = getString(R.string.md_totale_message);
        }


        LayoutInflater inflater = this.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_finale, null);
        builder.setView(dialogLayout);
        TextView tvMessage = (TextView) dialogLayout.findViewById(R.id.message);
        TextView tvTitolo = (TextView) dialogLayout.findViewById(R.id.titolo);
        Button avanti = (Button) dialogLayout.findViewById(R.id.avanti);
        tvMessage.setText(message);
        tvTitolo.setText(titolo);

        //ridimensiono il pulsante

        RelativeLayout l_avanti = (RelativeLayout) dialogLayout.findViewById(R.id.l_avanti);
        ViewGroup.LayoutParams settingsParams = l_avanti.getLayoutParams();
        settingsParams.width = widthObj;
        settingsParams.height = heightObj;
        l_avanti.setLayoutParams(settingsParams);

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        avanti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                if (cSchermate == s.getSchermate().size()) {
                    try {
                        mostraPunteggioFinale();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("creaschermata", "aaa");
                    tweenManager.resume();
                    creaSchermata();
                }
            }
        });

        dialog.show();

    }

    /**
     * Mostra il punteggio della sessione di gioco
     */

    public void mostraPunteggioFinale() throws SQLException {

        Log.d("mostrapunteggio", "AAA");

        timerView.setVisibility(View.GONE);

        final MailUtils mu = new MailUtils();
        final String riepilogo = mu.getRiepilogo(s, getApplicationContext());
        final Context ctx = this;

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_totale, null);
        builder.setView(dialogLayout);
        TextView tvMessage = (TextView) dialogLayout.findViewById(R.id.message);
        Button avanti = (Button) dialogLayout.findViewById(R.id.avanti);
        tvMessage.setText(mu.getPunteggioTotale(s, getApplicationContext()));

        //ridimensiono il pulsante

        RelativeLayout l_avanti = (RelativeLayout) dialogLayout.findViewById(R.id.l_avanti);
        ViewGroup.LayoutParams settingsParams = l_avanti.getLayoutParams();
        settingsParams.width = widthObj;
        settingsParams.height = heightObj;
        l_avanti.setLayoutParams(settingsParams);

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        avanti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //chiudo la dialog

                dialog.cancel();
                svuotaMemoria();
                System.gc();

                //se richiesto provo a inviare la mail

                if (settings.getBoolean("sendMail", false) == true) {
                    try {
                        mu.sendMail(riepilogo, settings.getString("emailMitt", ""), settings.getString("pswMitt", ""), settings.getString("s1_email", ""), ctx);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    Intent intent = new Intent(ctx.getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    ctx.startActivity(intent);
                }
            }
        });

        dialog.show();


    }

    /**
     * Elimina gli oggetti non più necessari per liberare memoria
     */

    public void svuotaMemoria() {

        //fermo il tempo

        if (cdt != null) {
            cdt.cancel();
        }

        //svuoto un po' di memoria

        stage.setBackgroundResource(0);
        bottoniOggetti = null;
        bottoniOggetti = new ArrayList<RelativeLayout>();
        oggetti = null;
        oggetti = new ArrayList<Oggetto>();
        elementiDr = null;
        elementiDr = new ArrayList<Drawable>();
        intrusoDr = null;
        cdt = null;
        idxOggetti = 0;
    }

    /**
     * Metodo invocato alla pressione del tasto Back
     */

    @Override
    public void onBackPressed() {

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_esci, null);
        exitBuilder.setView(dialogLayout);

        exitBuilder.setCancelable(false)
                .setNegativeButton(getString(R.string.md_nonesco), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(getString(R.string.md_esco),
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {

                                dialog.cancel();
                                svuotaMemoria();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                finish();
                                startActivity(intent);
                                System.gc();

                            }
                        });

        exitBuilder.create().show();
    }

    /**
     * Metodo per la gestione delle font personalizzate
     */

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
