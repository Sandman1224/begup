package martin.compras.de.lista.app.com.begu.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import martin.compras.de.lista.app.com.begu.Clases.SessionManager;
import martin.compras.de.lista.app.com.begu.Helpers.DataBaseHelper;
import martin.compras.de.lista.app.com.begu.R;
import martin.compras.de.lista.app.com.begu.utils.Constantes;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario;
    private EditText etPassword;
    private CheckBox chkSesion;
    private Button btnLogin;
    private SessionManager sessionManager;

    DataBaseHelper myDbHelper;
    Cursor myCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsuario = (EditText) findViewById(R.id.etUsuario);
        etPassword = (EditText) findViewById(R.id.etPassword);
        chkSesion = (CheckBox) findViewById(R.id.chkSesion);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        //Código para crear la BD
        myDbHelper = new DataBaseHelper(this);
        try{
            myDbHelper.createDatabase();
        }catch (IOException e){
            throw new Error("No se puede crear la BD");
        }

        sessionManager = new SessionManager(this);
        if(sessionManager.isLoggedIn()){
            Intent intent = new Intent(this, PrincipalActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //Codigo que maneja el click del boton login
    public void loginClick(View view){
        String usuario = etUsuario.getText().toString();
        String password = etPassword.getText().toString();
        Boolean login = false;

        if (usuario.length() > 0 || password.length() > 0){
            try{
                myDbHelper.openDataBase();
                myCursor = myDbHelper.fetchLogin(usuario, password);

                if (myCursor.moveToFirst()){
                    if ((usuario.equals(myCursor.getString(1))) && (password.equals(myCursor.getString(2)))) {
                        //Toast.makeText(getApplicationContext(), "Login Correcto " + myCursor.getString(1), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, PrincipalActivity.class);
                        startActivity(intent);
                        finish();
                        login = true;

                        SharedPreferences preferences =
                                getSharedPreferences(Constantes.PREFERENCIAS, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("USUARIO", myCursor.getString(1));
                        editor.putString("ID_EMPRESA", myCursor.getString(5));
                        editor.commit();

                        if (chkSesion.isChecked())
                            sessionManager.setLogin(true);
                    }
                }

                myCursor.close();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }

        if (login == false){
            Toast.makeText(getApplicationContext(), "Login Incorrecto", Toast.LENGTH_SHORT).show();
        }
    }
}
