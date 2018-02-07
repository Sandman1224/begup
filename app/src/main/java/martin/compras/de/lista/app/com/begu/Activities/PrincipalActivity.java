package martin.compras.de.lista.app.com.begu.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.location.Location;
import android.net.Uri;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.camera.CameraSettings;

import java.io.ByteArrayOutputStream;
import java.sql.Blob;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import martin.compras.de.lista.app.com.begu.Clases.SessionManager;
import martin.compras.de.lista.app.com.begu.Fragments.PermisosDialogFragment;
import martin.compras.de.lista.app.com.begu.Helpers.DataBaseHelper;
import martin.compras.de.lista.app.com.begu.Preferencias.OpcionesActivity;
import martin.compras.de.lista.app.com.begu.R;
import martin.compras.de.lista.app.com.begu.providers.ContratoDatos;
import martin.compras.de.lista.app.com.begu.utils.Constantes;

public class PrincipalActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener{

    private static final String TAG = PrincipalActivity.class.getSimpleName();
    private static final String LOGGPS = "localizacion";
    private static final String LOGCAMERA = "camara";
    private TextView tvNombre;
    private TextView tvGenero;
    private TextView tvSaldo;
    private ImageView ivAlumno;
    private ImageView ivEstado;
    private DecoratedBarcodeView barcodeView;
    private RelativeLayout loDatos;
    private String lastText;
    private DataBaseHelper myDBHelper;
    private ContentResolver resolver;
    private Cursor cursorTarjetas, cursorAlumno, cursorFoto, cursorDescarga;
    private SessionManager sessionManager;
    private BeepManager beepManager;
    private GoogleApiClient apiClient;
    private LocationRequest locRequest;
    private Location location;
    private static final int PETICION_CONFIG_UBICACION = 201;
    private static final int PETICION_PERMISO_LOCALIZACION = 101;
    private static final int PETICION_PERMISO_CAMARA = 102;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() == null || result.getText().equals(lastText)){
                //Prevenir escaneos duplicados
                if ((result.getText() != null) && (result.getText().equals(lastText))) {
                    Log.i("ESCANEO", "El código ya fue escaneado");
                }
                return;
            }

            lastText = result.getText();
            beepManager.playBeepSoundAndVibrate();

            consultarTarjeta(lastText);
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
            //Resultados posibles
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        barcodeView = (DecoratedBarcodeView) findViewById(R.id.barcode_scanner);
        barcodeView.decodeContinuous(callback);
        beepManager = new BeepManager(this);

        resolver = this.getContentResolver();

        //GPS
        apiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        //Inicializamos la obtención de localizaciones
        enableLocationUpdates();
    }

    private void enableLocationUpdates(){
        locRequest = new LocationRequest();
        locRequest.setInterval(2000);
        locRequest.setFastestInterval(1000);
        locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest locSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locRequest)
                .build();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(apiClient, locSettingsRequest);

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()){
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(LOGGPS, "Configuración correcta");
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try{
                            Log.i(LOGGPS, "Se requiere actuación del usuario para modificar los permisos necesarios");
                            status.startResolutionForResult(PrincipalActivity.this, PETICION_CONFIG_UBICACION);
                        }catch (IntentSender.SendIntentException e){
                            Log.i(LOGGPS, "Error al intentar solucionar la configuración de ubicación");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(LOGGPS, "No se puede cumplir la configuración de ubicación necesaria");
                        //Desactualizar ubicación
                        break;
                }
            }
        });
    }

    private void disableLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this);
    }

    private void startLocationUpdates(){
        if(ContextCompat.checkSelfPermission(PrincipalActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Log.i(LOGGPS, "Inicio de recepción de ubicaciones");

            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locRequest, PrincipalActivity.this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activityprincipal, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()){
            case R.id.actionConfigurar:
                startActivity(new Intent(PrincipalActivity.this, OpcionesActivity.class));
                return true;
            case R.id.actionSincronizar:
                startActivity(new Intent(PrincipalActivity.this, SyncActivity.class));
                return true;
            case R.id.actionSalir:
                sessionManager = new SessionManager(this);
                sessionManager.setLogin(false);
                intent = new Intent(PrincipalActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        enableLocationUpdates();
    }

    @Override
    protected void onPause(){
        super.onPause();

        barcodeView.pause();
    }

    public void pause(View view){
        barcodeView.pause();

        loDatos = (RelativeLayout) findViewById(R.id.loDatos);
        loDatos.setVisibility(View.GONE);
        lastText = null;

        ivAlumno = (ImageView) findViewById(R.id.ivAlumno);
        ivAlumno.setImageResource(R.drawable.ic_alumno);
    }

    public void resume(View view){
        if(ContextCompat.checkSelfPermission(PrincipalActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(PrincipalActivity.this, Manifest.permission.CAMERA)){
                // (*) Mostrar una explicación de lo que sucede
                Log.i("Permisos", "La camara no puede funcionar si no tiene permisos asignados");

                //Confeccionamos el cuadro de dialogo para explicar porque no se puede utilizar la camara
                Bundle args = new Bundle();
                args.putString("titulo", "Permisos de Camara Denegado");
                args.putString("mensaje", "El usuario de la app no otorgo permisos para usar la camara");
                args.putString("package", getApplicationContext().getPackageName());
                args.putInt("requestAppSetting", PETICION_PERMISO_CAMARA);

                PermisosDialogFragment dialog = new PermisosDialogFragment();
                dialog.setArguments(args);
                dialog.show(getFragmentManager(), "camara");

            }else {
                ActivityCompat.requestPermissions(PrincipalActivity.this, new String[]{Manifest.permission.CAMERA}, PETICION_PERMISO_CAMARA);
            }
        }else {
            configurarScanner();
            barcodeView.resume();
        }
    }

    public void triggerScan(View view){
        barcodeView.decodeSingle(callback);
    }

    public void configurarScanner(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(PrincipalActivity.this);
        int camara = Integer.parseInt(preferences.getString("opcion1", "0"));

        if (barcodeView.getBarcodeView().isPreviewActive())
            barcodeView.pause();

        CameraSettings settings = barcodeView.getBarcodeView().getCameraSettings();

        switch (camara){
            case 0:
                settings.setRequestedCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
                break;
            case 1:
                settings.setRequestedCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
                break;
        }

        barcodeView.getBarcodeView().setCameraSettings(settings);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    public void consultarTarjeta(final String numeroTarjeta){
        loDatos = (RelativeLayout) findViewById(R.id.loDatos);

        myDBHelper = new DataBaseHelper(this);
        myDBHelper.openDataBase();
        cursorTarjetas = myDBHelper.fetchTarjeta(numeroTarjeta);
        if(cursorTarjetas.moveToFirst() && datosAlumno(cursorTarjetas.getString(2), myDBHelper)){
            //Hago visible el layout que contiene la información
            loDatos.setVisibility(View.VISIBLE);
            tvSaldo = (TextView) findViewById(R.id.tvSaldo);
            ivEstado = (ImageView) findViewById(R.id.ivEstado);

            int credito = cursorTarjetas.getInt(Constantes.TARJETAScreditotemporal) - cursorTarjetas.getInt(Constantes.TARJETAScreditousado);

            Log.i("Saldos", "TEMPORAL: " + cursorTarjetas.getInt(Constantes.TARJETAScreditotemporal) + " ;USADO: " + cursorTarjetas.getInt(Constantes.TARJETAScreditousado));

            if(credito > 0 && cursorTarjetas.getInt(Constantes.TARJETASborrado) == 0) {
                ivEstado.setImageResource(R.drawable.aprobado);
                descontarPasaje(cursorTarjetas, location);

                int saldo = credito - 1;
                tvSaldo.setText(String.valueOf(saldo));
            }
            else {
                ivEstado.setImageResource(R.drawable.desaprobado);
                tvSaldo.setText("Tarjeta sin saldo o bloqueada");
            }

            datosFoto(cursorTarjetas.getString(Constantes.TARJETASdni), myDBHelper);
        }else{
            loDatos.setVisibility(View.GONE);
            Toast.makeText(this, "No se encuentra el pasajero", Toast.LENGTH_LONG).show();
        }

        cursorTarjetas.close();
    }

    public void descontarPasaje(Cursor cursorTarjetas, Location location){
        int pasajes = cursorTarjetas.getInt(Constantes.TARJETAScreditousado) + 1;
        String numTarjeta = cursorTarjetas.getString(Constantes.TARJETASnum_tarjetas);
        java.text.DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss");
        String fechaActual = dateFormat.format(Calendar.getInstance().getTime());
        SharedPreferences preferences = getSharedPreferences(Constantes.PREFERENCIAS, Context.MODE_PRIVATE);
        String cuitEmpresa = preferences.getString("ID_EMPRESA", "");

        try{
            //Actualizamos el credito en la tabla BEGU_TARJETAS
            ContentValues contentUpdate = new ContentValues();
            contentUpdate.put("CREDITO_USADO", pasajes);
            resolver.update(ContratoDatos.Tarjetas.crearUriTarjeta(numTarjeta), contentUpdate, null, null);

            //Registramos el pasaje en la tabla BEGU_CONSUMOS
            ContentValues contentInsert = new ContentValues();
            contentInsert.put("NUM_TARJETA", numTarjeta);
            contentInsert.put("FECHA", fechaActual);
            if (location != null) {
                Toast.makeText(this, "LATITUD: " + location.getLatitude() + "; LONGITUD: " + location.getLongitude()+ "; PRECISION: " + location.getAccuracy(), Toast.LENGTH_LONG).show();
                Log.i(LOGGPS, "LATITUD: " + location.getLatitude() + "; LONGITUD: " + location.getLongitude()+ "; PRECISION: " + location.getAccuracy());
                contentInsert.put("GPS_LATITUD", location.getLatitude());
                contentInsert.put("GPS_LONGITUD", location.getLongitude());
            }
            contentInsert.put("ID_EMPRESA", cuitEmpresa);
            contentInsert.put("SYNC", Constantes.EN_SINCRONIZACION);
            resolver.insert(ContratoDatos.Consumos.URI_CONTENIDO, contentInsert);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean datosAlumno(String dni, DataBaseHelper dbHelper){
        boolean resultado = false;
        cursorAlumno = dbHelper.fetchAlumno(dni);

        if(cursorAlumno.moveToFirst()){
            tvNombre = (TextView) findViewById(R.id.tvNombre);
            tvGenero = (TextView) findViewById(R.id.tvGenero);

            tvNombre.setText(cursorAlumno.getString(1) + ", " + cursorAlumno.getString(2));
            switch (cursorAlumno.getString(3)){
                case "1":
                    tvGenero.setText("Mujer");
                    break;
                case "2":
                    tvGenero.setText("Hombre");
                    break;
                default:
                    tvGenero.setText("Sin definir");
                    break;
            }
            resultado = true;
        }
        cursorAlumno.close();

        return resultado;
    }

    public boolean datosFoto(String dni, DataBaseHelper dbHelper){
        boolean resultado = false;
        cursorFoto = dbHelper.fetchFoto(dni);
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        ivAlumno = (ImageView) findViewById(R.id.ivAlumno);

        if(cursorFoto.moveToFirst()){
            Bitmap foto;

            try {
                byte[] imgByte = Base64.decode(cursorFoto.getString(1), Base64.DEFAULT);
                if (imgByte != null) {
                    foto = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
                    ivAlumno.setImageBitmap(foto);
                } else{
                    Toast.makeText(this, "No se encontró la foto del pasajero", Toast.LENGTH_LONG).show();
                }
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(this, "No se puede cargar la foto", Toast.LENGTH_LONG).show();
            }

            resultado = true;
        }else {
            //Como no se encuentra cargada la foto, añadimos la sincronización en la tabla DESCARGA_FOTOS
            ivAlumno.setImageResource(R.drawable.ic_alumno);

            //Obtener Fecha Actual
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String fechaActual = dateFormat.format(calendar.getTime());

            ContentValues contentValues = new ContentValues();
            contentValues.put(ContratoDatos.DescargaFotos.DNI, dni);
            contentValues.put(ContratoDatos.DescargaFotos.FECHA_SOLIC, fechaActual);
            contentValues.put(ContratoDatos.DescargaFotos.SYNC, Constantes.EN_SINCRONIZACION);

            if(!existeSolicitud(dni)) {
                //La solicitud no existe
                db.insertOrThrow(DataBaseHelper.Tablas.DESCARGA_FOTOS, null, contentValues);
            }else{
                //La solicitud ya existe
                db.update(DataBaseHelper.Tablas.DESCARGA_FOTOS, contentValues, "DNI = " + dni, null);
            }
        }

        cursorFoto.close();

        return resultado;
    }

    private boolean existeSolicitud(String dni){
        //Busca si un pedido de sincronización ya se encuentra registrado y si esta en estado de sincronización
        boolean resultado = false;
        cursorDescarga = myDBHelper.fetchDescarga(dni);

        if (cursorDescarga.moveToFirst()){
            resultado = true;
        }

        return resultado;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Conectado correctamente a Google Play Services
        Log.i(LOGGPS, "conectado");

        if(ContextCompat.checkSelfPermission(PrincipalActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(PrincipalActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PETICION_PERMISO_LOCALIZACION);
        }else {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient);

            //* Obtener ultima localización
            location = lastLocation;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(LOGGPS, "Se ha interrumpido la conexión con Google Play Services");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Se ha producido un error que no se puede resolver automáticamente
        //y la conexión con los Google Play Services no se ha establecido.
        Log.e(LOGGPS, "Error grave al conectar con Google Play Services");
    }

    @Override
    public void onLocationChanged(Location loc) {
        Log.i(LOGGPS, "Nueva ubicación recibida");

        //Mandamos la nueva ubicación
        location = loc;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case PETICION_CONFIG_UBICACION:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(LOGGPS, "El usuario no ha realizado los cambios de configuración necesarios");
                        disableLocationUpdates();
                        break;
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PETICION_PERMISO_LOCALIZACION:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Permiso concedido
                    @SuppressWarnings("MissingPermission")
                    Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient);

                    //Actualizar posición
                    location = lastLocation;
                }else{
                    //Permiso denegado
                    Log.e(LOGGPS, "Permiso denegado");
                }
                return;
            }
            case PETICION_PERMISO_CAMARA:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Permiso concedido
                    Log.i(LOGCAMERA, "Permiso de camara concedido");
                    configurarScanner();
                    barcodeView.resume();
                }else {
                    //Permiso denegado
                    Log.e(LOGCAMERA, "Permiso de camara denegado");
                }
                return;
            }
        }
    }
}
