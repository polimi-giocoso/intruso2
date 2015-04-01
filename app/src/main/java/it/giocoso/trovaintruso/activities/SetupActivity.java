package it.giocoso.trovaintruso.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import it.giocoso.trovaintruso.R;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SetupActivity extends ActionBarActivity {

    EditText emailMitt, pswMitt;
    Switch sendMail;

    EditText s1_email, s1_numSchermate, s1_numOggetti, s1_numIntrusi, s1_tempoMax;
    Spinner s1_criterio;

    EditText s2_email, s2_numSchermate, s2_numRighe, s2_numColonne, s2_numIntrusi, s2_tempoMax, s2_attesa, s2_speed;
    Spinner s2_criterio;

    Button save;

    String errori;

    SharedPreferences.Editor editor;

    LinearLayout stage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_mode_one);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        //actionBar.setDisplayHomeAsUpEnabled(true);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        //recupero tutti i campi

        emailMitt = (EditText) findViewById(R.id.setup_email_mittente);
        pswMitt = (EditText) findViewById(R.id.setup_email_password);
        sendMail = (Switch) findViewById(R.id.setup_sendmail);

        s1_email = (EditText) findViewById(R.id.setup_1_email_destinatario);
        s1_numSchermate = (EditText) findViewById(R.id.setup_1_numschermate);
        s1_numOggetti = (EditText) findViewById(R.id.setup_1_numoggettitotale);
        s1_numIntrusi = (EditText) findViewById(R.id.setup_1_numintrusi);
        s1_tempoMax = (EditText) findViewById(R.id.setup_1_tempomassimo);
        s1_criterio = (Spinner) findViewById(R.id.setup_1_criteri);

        s2_email = (EditText) findViewById(R.id.setup_2_email_destinatario);
        s2_numSchermate = (EditText) findViewById(R.id.setup_2_numschermate);
        s2_numRighe = (EditText) findViewById(R.id.setup_2_numrighe);
        s2_numColonne = (EditText) findViewById(R.id.setup_2_numcolonne);
        s2_numIntrusi = (EditText) findViewById(R.id.setup_2_numintrusi);
        s2_tempoMax = (EditText) findViewById(R.id.setup_2_tempomassimo);
        s2_attesa = (EditText) findViewById(R.id.setup_2_attesa);
        s2_speed = (EditText) findViewById(R.id.setup_2_speed);
        s2_criterio = (Spinner) findViewById(R.id.setup_2_criteri);

        stage = (LinearLayout) findViewById(R.id.stagesettings);

        //recupero i valori salvati nell'app oppure metto quelli di default

        SharedPreferences settings = getSharedPreferences(
                "settings", 0);
        editor = settings.edit();

        emailMitt.setText(settings.getString("emailMitt", "mail@example.com"));
        pswMitt.setText(settings.getString("pswMitt", "password"));
        sendMail.setChecked(settings.getBoolean("sendMail", true));

        sendMail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {
                    editor.putBoolean("sendMail", true);
                    Log.d("switch", "true");
                } else {
                    editor.putBoolean("sendMail", false);
                    Log.d("switch", "false");
                }

            }
        });

        s1_email.setText(settings.getString("s1_email", "mail@example.com"));
        s1_numSchermate.setText(Integer.toString(settings.getInt("s1_numSchermate", 3)));
        s1_numOggetti.setText(Integer.toString(settings.getInt("s1_numOggetti", 50)));
        s1_numIntrusi.setText(Integer.toString(settings.getInt("s1_numIntrusi", 5)));
        s1_tempoMax.setText(Integer.toString(settings.getInt("s1_tempoMax", 180)));
        s1_criterio.setSelection(settings.getInt("s1_criterio", 0));

        s2_email.setText(settings.getString("s2_email", "mail@example.com"));
        s2_numSchermate.setText(Integer.toString(settings.getInt("s2_numSchermate", 3)));
        s2_numRighe.setText(Integer.toString(settings.getInt("s2_numRighe", 6)));
        s2_numColonne.setText(Integer.toString(settings.getInt("s2_numColonne", 6)));
        s2_numIntrusi.setText(Integer.toString(settings.getInt("s2_numIntrusi", 5)));
        s2_tempoMax.setText(Integer.toString(settings.getInt("s2_tempoMax", 180)));
        s2_attesa.setText(Integer.toString(settings.getInt("s2_attesa", 5)));
        s2_speed.setText(Integer.toString(settings.getInt("s2_speed", 3)));
        s2_criterio.setSelection(settings.getInt("s2_criterio", 0));

        save = (Button) findViewById(R.id.setup_salva);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

    }

    public void save(){

        if(isValid()){

            //salvo tutti i campi
            editor.putString("emailMitt", emailMitt.getText().toString());
            editor.putString("pswMitt", pswMitt.getText().toString());

            editor.putString("s1_email", s1_email.getText().toString());
            editor.putInt("s1_numSchermate", Integer.parseInt(s1_numSchermate.getText().toString()));
            editor.putInt("s1_numOggetti", Integer.parseInt(s1_numOggetti.getText().toString()));
            editor.putInt("s1_numIntrusi", Integer.parseInt(s1_numIntrusi.getText().toString()));
            editor.putInt("s1_tempoMax", Integer.parseInt(s1_tempoMax.getText().toString()));
            editor.putInt("s1_criterio", s1_criterio.getSelectedItemPosition());

            editor.putString("s2_email", s2_email.getText().toString());
            editor.putInt("s2_numSchermate", Integer.parseInt(s2_numSchermate.getText().toString()));
            editor.putInt("s2_numRighe", Integer.parseInt(s2_numRighe.getText().toString()));
            editor.putInt("s2_numColonne", Integer.parseInt(s2_numColonne.getText().toString()));
            editor.putInt("s2_numIntrusi", Integer.parseInt(s2_numIntrusi.getText().toString()));
            editor.putInt("s2_tempoMax", Integer.parseInt(s2_tempoMax.getText().toString()));
            editor.putInt("s2_attesa", Integer.parseInt(s2_attesa.getText().toString()));
            editor.putInt("s2_speed", Integer.parseInt(s2_speed.getText().toString()));
            editor.putInt("s2_criterio", s2_criterio.getSelectedItemPosition());

            editor.commit();


            Toast.makeText(getApplicationContext(), "Impostazioni salvate correttamente!", Toast.LENGTH_SHORT).show();

            finish();

        }else{
            Toast.makeText(getApplicationContext(), errori, Toast.LENGTH_LONG).show();
        }
    }

    public boolean isValid(){

        boolean esito = true;
        errori = "ERRORI RILEVATI:\n\n";

        //controlli generici email
        if(emailMitt.getText().toString().equals("")){
            errori += "La mail per inviare i dati è richiesta\n";
            esito = false;
        }

        if(pswMitt.getText().toString().equals("")){
            errori += "La password della mail è richiesta\n";
            esito = false;
        }

        /*if(!emailMitt.getText().toString().matches("^[a-z0-9](\\.?[a-z0-9]){5,}@g(oogle)?mail\\.com$")){
            errori += "La mail per inviare i dati non è valida (solo GMail)\n";
            esito = false;
        }*/

        errori += "\n";

        //controlli su modalità 1

        if(s1_email.getText().toString().equals("")){
            errori += "Modalità in movimento: la mail di invio dati è richiesta\n";
            esito = false;
        }

        if(Integer.parseInt(s1_numOggetti.getText().toString())>50){
            errori += "Modalità in movimento: troppi oggetti (max. 50)\n";
            esito = false;
        }

        if(Integer.parseInt(s1_numIntrusi.getText().toString())==Integer.parseInt(s1_numOggetti.getText().toString())){
            errori += "Modalità in movimento: il numero di intrusi è uguale al numero di oggetti\n";
            esito = false;
        }else if(Integer.parseInt(s1_numIntrusi.getText().toString())>Integer.parseInt(s1_numOggetti.getText().toString())){
            errori += "Modalità in movimento: ci sono più intrusi che oggetti\n";
            esito = false;
        }

        //controlli su modalità 2

        errori += "\n";

        if(s2_email.getText().toString().equals("")){
            errori += "Modalità fissa: la mail di invio dati è richiesta\n";
            esito = false;
        }

        if(Integer.parseInt(s2_numRighe.getText().toString())>6){
            errori += "Modalità fissa: troppe righe (max. 6)\n";
            esito = false;
        }

        if(Integer.parseInt(s2_numColonne.getText().toString())>10){
            errori += "Modalità fissa: troppe colonne (max. 10)\n";
            esito = false;
        }

        if(Integer.parseInt(s2_numIntrusi.getText().toString())>(Integer.parseInt(s2_numRighe.getText().toString())*Integer.parseInt(s2_numColonne.getText().toString()))){
            errori += "Modalità fissa: ci sono più intrusi che oggetti\n";
            esito = false;
        }


        return esito;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_set_mode_one, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_save:
                save();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed(){
        finish();
    }
}
