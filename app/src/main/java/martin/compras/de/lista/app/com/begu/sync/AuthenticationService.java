package martin.compras.de.lista.app.com.begu.sync;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Tinch on 5/3/2017.
 */

public class AuthenticationService extends Service {

    private UsuariosAuthenticator autenticador;

    @Override
    public void onCreate() {
        autenticador = new UsuariosAuthenticator(this);
    }

    public IBinder onBind(Intent intent){
        return autenticador.getIBinder();
    }
}
