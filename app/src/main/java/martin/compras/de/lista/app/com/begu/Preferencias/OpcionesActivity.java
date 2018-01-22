package martin.compras.de.lista.app.com.begu.Preferencias;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import martin.compras.de.lista.app.com.begu.R;

/**
 * Created by Tinch on 14/2/2017.
 */

public class OpcionesActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.configuracion);
    }
}
