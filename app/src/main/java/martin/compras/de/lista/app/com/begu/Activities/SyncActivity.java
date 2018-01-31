package martin.compras.de.lista.app.com.begu.Activities;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SyncInfo;
import android.content.SyncStatusObserver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import martin.compras.de.lista.app.com.begu.R;
import martin.compras.de.lista.app.com.begu.providers.ContratoDatos;
import martin.compras.de.lista.app.com.begu.sync.SyncAdapter;
import martin.compras.de.lista.app.com.begu.utils.AdaptadorSincronizacion;
import martin.compras.de.lista.app.com.begu.utils.AdaptadorUsuarios;
import martin.compras.de.lista.app.com.begu.utils.Constantes;
import martin.compras.de.lista.app.com.begu.utils.MyObserver;

public class SyncActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private AdaptadorUsuarios adapter;
    private TextView tvFechaactualizacion;

    private ProgressDialog pDialog;
    public ArrayList<ListViewItem> datos;

    ContentResolver resolver;
    private static final String[] mProjectionConsumos = new String[]{
            ContratoDatos.Consumos.ID_CONSUMO,
            ContratoDatos.Consumos.NUM_TARJETA,
            ContratoDatos.Consumos.FECHA,
            ContratoDatos.Consumos.GPS_LATITUD,
            ContratoDatos.Consumos.GPS_LONGITUD,
            ContratoDatos.Consumos.ID_EMPRESA,
            ContratoDatos.Consumos.SYNC
    };

    public static final String ACTION_SYNC_ALUMNOSSINCRONIZADOS = "ALUMNOS/sincronizados";
    public static final String ACTION_SYNC_TARJETASSINCRONIZADAS = "TARJETAS/sincronizados";
    public static final String ACTION_SYNC_FOTOSSINCRONIZADAS = "FOTOS/sincronizados";
    public static final String ACTION_SYNC_CONSUMOSSINCRONIZADAS = "CONSUMOS/sincronizados";
    public static final String ACTION_SYNC_ALUMNOSERROR = "ALUMNOS/error";
    public static final String ACTION_SYNC_TARJETASERROR = "TARJETAS/error";
    public static final String ACTION_SYNC_FOTOSERROR = "FOTOS/error";
    public static final String ACTION_SYNC_CONSUMOSERROR = "CONSUMOS/error";

    public final static String SYNC_ACTION = "SYNC_ACTION";

    public static final String ACTION_SYNC_ERROR = "SYNC/error";
    public static final String ACTION_SYNC_SUCCESS = "SYNC/success";

    public static final String ACTION_SYNC = "martin.compras.de.lista.app.com.begu.Activities.SyncActivity.ACTION_SYNC";
    public static final String ACTION_SYNC_START = "martin.compras.de.lista.app.com.begu.Activities.SyncActivity.ACTION_SYNC_START";
    public static final String ACTION_SYNC_FINISH = "martin.compras.de.lista.app.com.begu.Activities.SyncActivity.ACTION_SYNC_FINISH";

    public static final String ACTION_SYNC_TARJETAS = "martin.compras.de.lista.app.com.begu.Activities.SyncActivity.ACTION_SYNC_TARJETAS";
    public static final String ACTION_SYNC_FOTOS = "martin.compras.de.lista.app.com.begu.Activities.SyncActivity.ACTION_SYNC_FOTOS";
    public static final String ACTION_SYNC_REMOTA = "martin.compras.de.lista.app.com.begu.Activities.SyncActivity.ACTION_SYNC_REMOTA";

    private MyReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        recyclerView = (RecyclerView) findViewById(R.id.reciclador);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //adapter = new AdaptadorUsuarios(this);
        //recyclerView.setAdapter(adapter);
        tvFechaactualizacion = (TextView)findViewById(R.id.tvFechaactualizacion);

        SyncAdapter.inicializarSyncAdapter(this);

        pDialog = new ProgressDialog(this);
        pDialog.setTitle("Estado de Sincronización");
        pDialog.setCancelable(false);

        datos = new ArrayList<ListViewItem>();

        if(savedInstanceState != null){
            datos.add(new ListViewItem("Alumnos", savedInstanceState.getString("Alumnos")));
            datos.add(new ListViewItem("Tarjetas", savedInstanceState.getString("Tarjetas")));
            datos.add(new ListViewItem("Fotos", savedInstanceState.getString("Fotos")));
            datos.add(new ListViewItem("Consumos", savedInstanceState.getString("Consumos")));
        }else {
            cargarPreferencias();
        }

        AdaptadorSincronizacion adaptador = new AdaptadorSincronizacion(this, datos);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adaptador);

        //UI para sincronización
        receiver = new MyReceiver(new Handler(), pDialog, adaptador, datos);
        registerReceiver(receiver, new IntentFilter(ACTION_SYNC));
        //sendBroadcast(new Intent(ACTION_SYNC));

        //Obtenemos de las preferencias la fecha de la ultima actualización realizada
        SharedPreferences prefs = getSharedPreferences(Constantes.PREFERENCIAS, Context.MODE_PRIVATE);
        tvFechaactualizacion.setText("Última Actualización: " + prefs.getString(Constantes.PREF_FECHAACTUALIZACION, "No Sincronizado"));

        inicioMes();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ListViewItem item;

        for (int i = 0; i < 4; i++){
            item = datos.get(i);

            switch (i){
                case 0:
                    outState.putString("Alumnos", item.estado);
                    break;
                case 1:
                    outState.putString("Tarjetas", item.estado);
                case 2:
                    outState.putString("Fotos", item.estado);
                    break;
                case 3:
                    outState.putString("Consumos", item.estado);
                    break;
                default:
                    outState.putString("No definido", "No definido");
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(ACTION_SYNC));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sync, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        resolver = this.getContentResolver();
        String[] mSelectionArgs = {""};
        String mSelection = ContratoDatos.Consumos.SYNC + " =? ";
        mSelectionArgs[0] = String.valueOf(Constantes.EN_SINCRONIZACION);
        Cursor c = resolver.query(ContratoDatos.Consumos.URI_CONTENIDO, mProjectionConsumos, mSelection, mSelectionArgs, null);

        Intent intent = new Intent(this.ACTION_SYNC);

        switch (item.getItemId()){
            case R.id.actionSync:
                intent.setAction(ACTION_SYNC_START);
                intent.putExtra(SyncAdapter.SYNC_MENSAJE, "Iniciando Sincronización");
                this.sendBroadcast(intent);
                sincronizarUI();

                if (c.getCount() > 0)
                    Toast.makeText(this, "Todavia no se sincronizaron todos los consumos exitosamente", Toast.LENGTH_LONG).show();
                else {
                    if(!SyncAdapter.estaSincronizando(this)) {
                        SyncAdapter.sincronizarAhora(getApplicationContext(), false);
                    }else{
                        Toast.makeText(this, "Hay una sincronización en curso", Toast.LENGTH_LONG).show();
                    }
                }

                return true;
            case R.id.actionSyncupload:
                if (c.getCount() > 0) {
                    SyncAdapter.sincronizarAhora(this, true);
                }else {
                    Toast.makeText(this, "No existen consumos de pasajes para sincronizar", Toast.LENGTH_LONG).show();
                    intent.putExtra(SYNC_ACTION, this.ACTION_SYNC_CONSUMOSSINCRONIZADAS);
                    this.sendBroadcast(intent);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void inicioMes(){
        Boolean salida = false;
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/y HH:mm:ss");
        SharedPreferences preferences = getSharedPreferences(Constantes.PREFERENCIAS, Context.MODE_PRIVATE);

        try {
            //Fecha de Ultima Sincronizacion
            Calendar ultimaSincronizacion = Calendar.getInstance();
            String valor = preferences.getString(Constantes.PREF_FECHAACTUALIZACION, null);
            if (valor != null)
                ultimaSincronizacion.setTime(sdf.parse(valor));
            else
                ultimaSincronizacion.setTime(sdf.parse("27/10/2017 11:35:54")); // VER FECHA Y HORA PUESTA AQUI

            if(Calendar.getInstance().get(Calendar.YEAR) == ultimaSincronizacion.get(Calendar.YEAR)){
                if(Calendar.getInstance().get(Calendar.MONTH) > ultimaSincronizacion.get(Calendar.MONTH))
                    salida = true;
            }else
                salida = true;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(salida){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("SyncTarjetas", true);
            editor.commit();
        }
    }

    private void sincronizarUI(){
        adapter = new AdaptadorUsuarios(this);
        datos = new ArrayList<ListViewItem>();

        datos.add(new ListViewItem("Alumnos", "Sincronizando"));
        datos.add(new ListViewItem("Tarjetas", "Sincronizando"));
        datos.add(new ListViewItem("Fotos", "Sincronizando"));
        datos.add(new ListViewItem("Consumos", "Sincronizando"));

        AdaptadorSincronizacion adaptador = new AdaptadorSincronizacion(this, datos);

        adaptador.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        guardarPreferencias();
        super.onBackPressed();
    }

    private void guardarPreferencias(){
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("alumnosSync", datos.get(0).estado);
        editor.putString("tarjetasSync", datos.get(1).estado);
        editor.putString("fotosSync", datos.get(2).estado);
        editor.putString("consumosSync", datos.get(3).estado);
        editor.commit();
    }

    private void cargarPreferencias(){
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        datos = new ArrayList<ListViewItem>();
        datos.add(new ListViewItem("Alumnos", sharedPreferences.getString("alumnosSync", "Sincronizando")));
        datos.add(new ListViewItem("Tarjetas", sharedPreferences.getString("tarjetasSync", "Sincronizando")));
        datos.add(new ListViewItem("Fotos", sharedPreferences.getString("fotosSync", "Sincronizando")));
        datos.add(new ListViewItem("Consumos", sharedPreferences.getString("consumosSync", "Sincronizando")));
    }

    public void updateTv(final String Mensaje, final int operacion){
        SyncActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvFechaactualizacion = (TextView)findViewById(R.id.tvFechaactualizacion);
                tvFechaactualizacion.setText(Mensaje);

                switch (operacion){
                    case 1:
                        tvFechaactualizacion.setTextColor(Color.parseColor("#1E8449"));
                        break;
                    case 2:
                        tvFechaactualizacion.setTextColor(Color.parseColor("#CC6699"));
                        break;
                    default:
                        tvFechaactualizacion.setTextColor(Color.parseColor("#F1C40F"));
                        break;
                }
            }
        });
    }

    public class ListViewItem{
        private String titulo;
        private String estado;

        public ListViewItem(String titulo, String estado){
            this.titulo = titulo;
            this.estado = estado;
        }

        public String getTitulo() {
            return titulo;
        }

        public void setTitulo(String text) {
            this.titulo = text;
        }

        public String getEstado(){
            return estado;
        }

        public void setEstado(String estado){
            this.estado = estado;
        }
    }

    public static class MyReceiver extends BroadcastReceiver{
        private final Handler handler;
        private ProgressDialog pDialog;
        private AdaptadorSincronizacion adaptador;
        private ArrayList<ListViewItem> items;

        public MyReceiver(Handler handler, ProgressDialog pDialog, AdaptadorSincronizacion adaptador, ArrayList<ListViewItem> items){
            this.handler = handler;
            this.pDialog = pDialog;
            this.adaptador = adaptador;
            this.items = items;
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String accion = intent.getStringExtra(SyncAdapter.SYNC_ACTION);
                    String mensaje = intent.getStringExtra(SyncAdapter.SYNC_MENSAJE);
                    ListViewItem item;

                    switch (accion){
                        case ACTION_SYNC_START:
                            Toast.makeText(context, "Iniciando sincronización", Toast.LENGTH_SHORT).show();
                            showDialog();
                            break;
                        case ACTION_SYNC_FINISH:
                            Toast.makeText(context, "Sincronización finalizada", Toast.LENGTH_SHORT).show();
                            hideDialog();
                            break;
                        case ACTION_SYNC_ERROR:
                            ((SyncActivity)context).updateTv("Error de Sincronización", 2);
                            hideDialog();
                            break;
                        case ACTION_SYNC_SUCCESS:
                            SharedPreferences prefs = context.getSharedPreferences(Constantes.PREFERENCIAS, Context.MODE_PRIVATE);
                            ((SyncActivity)context).updateTv("Última Actualización: " + prefs.getString(Constantes.PREF_FECHAACTUALIZACION, "No Sincronizado"), 1);

                            break;
                        case ACTION_SYNC_ALUMNOSSINCRONIZADOS:
                            item = items.get(0);
                            item.setTitulo("Alumnos");
                            item.setEstado("Sincronizado");
                            items.set(0, item);
                            adaptador.swap(items, 0);
                            break;
                        case ACTION_SYNC_TARJETASSINCRONIZADAS:
                            item = items.get(1);
                            item.setTitulo("Tarjetas");
                            item.setEstado("Sincronizado");
                            items.set(1, item);
                            adaptador.swap(items, 1);
                            break;
                        case ACTION_SYNC_FOTOSSINCRONIZADAS:
                            item = items.get(2);
                            item.setTitulo("Fotos");
                            item.setEstado("Sincronizado");
                            items.set(2, item);
                            adaptador.swap(items, 2);
                            break;
                        case ACTION_SYNC_CONSUMOSSINCRONIZADAS:
                            item = items.get(3);
                            item.setTitulo("Consumos");
                            item.setEstado("Sincronizado");
                            items.set(3, item);
                            adaptador.swap(items, 3);
                            break;
                        case ACTION_SYNC_ALUMNOSERROR:
                            item = items.get(0);
                            item.setTitulo("Alumnos");
                            item.setEstado("error");
                            items.set(0, item);
                            adaptador.swap(items, 0);
                            break;
                        case ACTION_SYNC_TARJETASERROR:
                            item = items.get(1);
                            item.setTitulo("Tarjetas");
                            item.setEstado("error");
                            items.set(1, item);
                            adaptador.swap(items, 1);
                            break;
                        case ACTION_SYNC_FOTOSERROR:
                            item = items.get(2);
                            item.setTitulo("Fotos");
                            item.setEstado("error");
                            items.set(2, item);
                            adaptador.swap(items, 2);
                            break;
                        case ACTION_SYNC_CONSUMOSERROR:
                            item = items.get(3);
                            item.setTitulo("Consumos");
                            item.setEstado("error");
                            items.set(3, item);
                            adaptador.swap(items, 3);
                            break;
                        case ACTION_SYNC_TARJETAS:
                            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
                            break;
                        case ACTION_SYNC_FOTOS:
                            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
                            break;
                        case ACTION_SYNC_REMOTA:
                            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        }

        private void showDialog(){
            if (!pDialog.isShowing())
                pDialog.show();
        }

        private void hideDialog(){
            if (pDialog.isShowing())
                pDialog.dismiss();
        }
    }
}
