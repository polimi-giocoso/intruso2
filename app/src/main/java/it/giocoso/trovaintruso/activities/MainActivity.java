package it.giocoso.trovaintruso.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import it.giocoso.trovaintruso.R;
import it.giocoso.trovaintruso.util.ImgUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MainActivity extends ActionBarActivity {


    RelativeLayout gameInfo;
    int widthObj, heightObj, widthScreen, heightScreen;
    ImageView stageBg;
    AlertDialog.Builder welcomeBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //imposto la font di default

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/FedraSansBold.otf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        // nascondo action bar e status bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        gameInfo = (RelativeLayout) findViewById(R.id.stage);
        stageBg = (ImageView) findViewById(R.id.stage_bg);
        welcomeBuilder = new AlertDialog.Builder(this);

        final Button mode1 = (Button) findViewById(R.id.button);
        final Button mode2 = (Button) findViewById(R.id.button2);
        final Button settings = (Button) findViewById(R.id.button3);
        settings.setAlpha(0);

        mode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gameInfo.animate().alpha(0).setDuration(300).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        gameInfo.setBackgroundResource(0);
                        gameInfo = null;
                        stageBg.setImageResource(0);
                        stageBg = null;
                        Intent intent = new Intent(getApplicationContext(), ModeOneActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        System.gc();
                    }
                });

            }
        });

        mode2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gameInfo.animate().alpha(0).setDuration(300).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        gameInfo.setBackgroundResource(0);
                        gameInfo = null;
                        stageBg.setImageResource(0);
                        stageBg = null;
                        Intent intent = new Intent(getApplicationContext(), ModeTwoActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        System.gc();
                    }
                });

            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
                startActivity(intent);
            }
        });

        //codice da eseguire solo quando il layout è stato correttamente definito sullo schermo

        gameInfo.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                //Remove it here unless you want to get this callback for EVERY
                //layout pass, which can get you into infinite loops if you ever
                //modify the layout from within this method.

                gameInfo.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                //Now you can get the width and height from content

                DisplayMetrics displaymetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

                widthScreen = displaymetrics.widthPixels;
                heightScreen = displaymetrics.heightPixels;
                heightObj = displaymetrics.heightPixels / 10;
                widthObj = heightObj;

                // imposto lo sfondo

                stageBg.setImageBitmap(ImgUtils.decodeSampledBitmapFromResource(
                        getResources(), R.drawable.sfondo_arancio, 640, 480
                ));

                // dispongo il pulsante delle impostazioni

                RelativeLayout l_settings = (RelativeLayout) findViewById(R.id.l_settings);
                RelativeLayout.LayoutParams settingsParams = (RelativeLayout.LayoutParams) l_settings.getLayoutParams();
                settingsParams.width = widthObj;
                settingsParams.height = heightObj;
                l_settings.setLayoutParams(settingsParams);
                l_settings.setX(30);
                l_settings.setY(30);

                // dispongo il titolo

                RelativeLayout l_titolo = (RelativeLayout) findViewById(R.id.l_titolo);
                RelativeLayout.LayoutParams titoloParams = (RelativeLayout.LayoutParams) l_titolo.getLayoutParams();
                titoloParams.width = widthObj * 6;
                l_titolo.setLayoutParams(titoloParams);
                l_titolo.setX((gameInfo.getMeasuredWidth() - widthObj * 6) / 2);
                l_titolo.setY((float) ((gameInfo.getMeasuredHeight() - heightObj * 3 - (widthObj * 6) * 0.625) / 2));

                ImageView titolo = (ImageView) findViewById(R.id.titolo);
                Picasso.with(getApplicationContext()).load(R.drawable.titolo)
                        .resize(l_titolo.getMeasuredWidth(), (int) ((widthObj * 6) * 0.625)).centerInside().into(titolo, new Callback() {
                    @Override
                    public void onSuccess() {

                        LinearLayout buttons = (LinearLayout) findViewById(R.id.buttons);
                        buttons.animate().alpha(1).setDuration(300).start();
                        settings.animate().alpha(1).setDuration(300).start();

                    }

                    @Override
                    public void onError() {

                    }
                });

                //dispongo il pulsante della versione dinamica

                RelativeLayout l_dinamico = (RelativeLayout) findViewById(R.id.l_dinamico);
                ViewGroup.LayoutParams dinamicoParams = l_dinamico.getLayoutParams();
                dinamicoParams.width = widthObj * 3;
                dinamicoParams.height = heightObj * 3;
                l_dinamico.setLayoutParams(dinamicoParams);


                //dispongo il pulsante della versione statica

                RelativeLayout l_statico = (RelativeLayout) findViewById(R.id.l_statico);
                ViewGroup.LayoutParams staticoParams = l_statico.getLayoutParams();
                staticoParams.width = widthObj * 3;
                staticoParams.height = heightObj * 3;
                l_statico.setLayoutParams(staticoParams);

            }
        });

    }

    /**
     * Mostra il messaggio di benvenuto nel caso in cui il setup non sia stato ancora effettuato
     */

    public void getWelcomeScreen(){

        SharedPreferences appSettings = getSharedPreferences(
                "settings", 0);

        if(appSettings.getBoolean("no_setup",true)){
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogLayout = inflater.inflate(R.layout.dialog_welcome, null);
            welcomeBuilder.setView(dialogLayout);

            welcomeBuilder.setCancelable(false)
                    .setPositiveButton(getString(R.string.d_welcome_button),
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
                                    startActivity(intent);
                                }
                            });

            welcomeBuilder.create().show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        System.gc();
        getWelcomeScreen();
    }

    /**
     * Metodo per la gestione delle font personalizzate
     */

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
