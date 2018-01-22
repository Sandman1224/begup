package martin.compras.de.lista.app.com.begu.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SyncInfo;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.journeyapps.barcodescanner.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import martin.compras.de.lista.app.com.begu.Activities.SyncActivity;
import martin.compras.de.lista.app.com.begu.Helpers.DataBaseHelper;
import martin.compras.de.lista.app.com.begu.R;
import martin.compras.de.lista.app.com.begu.providers.ContratoDatos;
import martin.compras.de.lista.app.com.begu.utils.Constantes;
import martin.compras.de.lista.app.com.begu.utils.Utilidades;
import martin.compras.de.lista.app.com.begu.web.Alumno;
import martin.compras.de.lista.app.com.begu.web.Foto;
import martin.compras.de.lista.app.com.begu.web.Tarjeta;
import martin.compras.de.lista.app.com.begu.web.VolleySingleton;

/**
 * Created by Tinch on 28/3/2017.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = SyncAdapter.class.getSimpleName();
    ContentResolver resolver;
    private Gson gson = new Gson();
    private boolean banSyncRemota;

    public interface banSyncRemotaListener{
        public void onBanSyncRemotaChange(boolean ban);
    }


    //[CAMPOS_AUXILIARES]
    private static final String[] mProjectionAlumnos = new String[]{
            ContratoDatos.Alumnos.DNI,
            ContratoDatos.Alumnos.NOMBRE,
            ContratoDatos.Alumnos.APELLIDO,
            ContratoDatos.Alumnos.GENERO,
            ContratoDatos.Alumnos.ID_REMOTA,
    };

    private static final String[] mProjectionTarjetas = new String[]{
            ContratoDatos.Tarjetas.NUM_TARJETAS,
            ContratoDatos.Tarjetas.DNI,
            ContratoDatos.Tarjetas.FECHA,
            ContratoDatos.Tarjetas.CREDITO_TOTAL,
            ContratoDatos.Tarjetas.CREDITO_USADO,
            ContratoDatos.Tarjetas.CREDITO_TEMPORAL,
            ContratoDatos.Tarjetas.BORRADO
    };

    private static final String[] mProjectionFotos = new String[]{
            ContratoDatos.Fotos.DNI,
            ContratoDatos.Fotos.FOTO
    };

    private static final String[] mProjectionDescargas = new String[]{
            ContratoDatos.DescargaFotos.DNI,
            ContratoDatos.DescargaFotos.FECHA_SOLIC,
            ContratoDatos.DescargaFotos.SYNC
    };

    private static final String[] mProjectionConsumos = new String[]{
            ContratoDatos.Consumos.ID_CONSUMO,
            ContratoDatos.Consumos.NUM_TARJETA,
            ContratoDatos.Consumos.FECHA,
            ContratoDatos.Consumos.GPS_LATITUD,
            ContratoDatos.Consumos.GPS_LONGITUD,
            ContratoDatos.Consumos.ID_EMPRESA,
            ContratoDatos.Consumos.SYNC
    };
    //[/CAMPOS_AUXILIARES]

    //[ID_COLUMNAS_SQL]
    //Tabla Alumnos
    public static final int DNIalumno = 0;
    public static final int NOMBREalumno = 1;
    public static final int APELLIDOalumno = 2;
    public static final int GENEROalumno = 3;

    //Tabla Tarjetas
    public static final int NUMEROtarjeta = 0;
    public static final int DNItarjeta = 1;
    public static final int FECHAtarjeta = 2;
    public static final int CREDITOTOTALtarjeta = 3;
    public static final int CREDITOUSADOtarjeta = 4;
    public static final int CREDITOTEMPORALtarjeta = 5;
    public static final int BORRADOtarjeta = 6;

    //Tabla Fotos
    public static final int DNIfoto = 0;
    public static final int FOTOfoto = 1;

    //Tabla DescargaFotos
    public final static String DESCARGAdni = "0";
    public final static String DESCARGAfecha_solic = "1";
    public final static String DESCARGAsync = "2";

    //Tabla Consumos
    public final static String CONSUMOid = "0";
    public final static String CONSUMOnrotarjeta = "1";
    public final static String CONSUMOfecha = "2";
    public final static String CONSUMOgpslatitud = "3";
    public final static String CONSUMOgpslongitud = "4";
    public final static String CONSUMOidempresa = "5";
    //[/ID_COLUMNAS_SQL]

    //[CONSTANTES UTILES]
    public final static String SYNCRECEIVER = "SYNCRECEIVER";
    public final static String SYNC_STATUS = "SYNC_STATUS";
    public final static String SYNC_START = "SYNC_START";
    public final static String SYNC_PROGRESS = "SYNC_PROGRESS";
    public final static String SYNC_FINISH = "SYNC_FINISH";
    public final static String SYNC_ERROR = "SYNC_ERROR";
    public final static String SYNC_MENSAJE = "SYNC_MENSAJE";
    public final static String SYNC_ALUMNOS = "SYNC_ALUMNOS";
    public final static String SYNC_TARJETAS = "SYNC_TARJETAS";

    public final static String SYNC_ACTION = "SYNC_ACTION";
    //[/CONSTANTES UTILES]


    public SyncAdapter(Context context, boolean autoInitialize){
        super(context, autoInitialize);
        resolver = context.getContentResolver();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs){
        super(context, autoInitialize, allowParallelSyncs);
        resolver = context.getContentResolver();
    }

    public static void inicializarSyncAdapter(Context context){
        obtenerCuentaASincronizar(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Log.i(TAG, "onPerformSync()...");

        boolean subida = bundle.getBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, false);

        Intent intent = new Intent(SyncActivity.ACTION_SYNC);
        intent.putExtra(SYNC_ACTION, SyncActivity.ACTION_SYNC_START);
        getContext().sendBroadcast(intent);


        if(subida) {
            //Hacia el Servidor
            datosPasajes();
        }else{
            //Desde el Servidor
            datosAlumnos(syncResult);
            datosTarjetas(syncResult);
            datosFotos(syncResult);
        }

        intent.putExtra(SYNC_ACTION, SyncActivity.ACTION_SYNC_FINISH);
        getContext().sendBroadcast(intent);

        /*
        (*) Los datos para realizar las peticiones al servidor se van encolando, por lo que es bueno mandar primero los datos de PASAJES para que
        el servidor actualice sus datos de consumos y envie datos actualizados cuando se hagan las consultas de datos de TARJETAS
         */
    }

    private String obtenerDireccionws(String url){
        //Devuelve la dirección del servicio web para realizar la llamada incluyendo el id de sincronización
        SharedPreferences preferences = getContext().getSharedPreferences(Constantes.PREFERENCIAS, Context.MODE_PRIVATE);
        String DIRECCION_WS = "";

        switch (url){
            case "Alumnos":
                String syncId = preferences.getString(Constantes.PREF_SYNCALUMNOS, "");
                if (syncId == "")
                    DIRECCION_WS = Constantes.GET_URL_ALUMNOS_CATCH;
                else
                    DIRECCION_WS = Constantes.GET_URL_ALUMNOS_SYNCID  + "/" + preferences.getString(Constantes.PREF_SYNCALUMNOS, "");
                break;
            case "Tarjetas":
                Boolean tarjSync = preferences.getBoolean("SyncTarjetas", false);
                String SyncId = preferences.getString(Constantes.PREF_SYNCTARJETAS, "");
                if(tarjSync || SyncId == "")
                    DIRECCION_WS = Constantes.GET_URL_TARJETAS_CATCH;
                else
                    DIRECCION_WS = Constantes.GET_URL_TARJETAS_SYNCID + "/" + preferences.getString(Constantes.PREF_SYNCTARJETAS , "");
                break;
            default:
                Log.i(TAG, "Error");
        }

        return DIRECCION_WS;
    }

    public void datosAlumnos(final SyncResult syncResult){
        //Sincronización de los Datos de la tabla ALUMNOS
        Log.i(TAG, "Obteniendo datos de alumnos desde el servidor");

        Intent intent = new Intent(SyncActivity.ACTION_SYNC);
        boolean bFin = false;
        String DIRECCION_WS = obtenerDireccionws("Alumnos");
        String syncId = null;
        int cantidadPaginas = 0;

        while (!bFin) {
            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, DIRECCION_WS, new JSONObject(), future, future);
            request.setRetryPolicy(new DefaultRetryPolicy(60000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(getContext()).addToRequestQueue(request);

            try{
                JSONObject response = future.get(60, TimeUnit.SECONDS);
                int cantidadRegistros = response.getInt("TotalCount");
                int pagecountServer = response.getInt("TotalPageCount");
                syncId = response.getString("Sync");

                if(cantidadRegistros > 0){
                    JSONArray alumnos = response.getJSONArray("Alumnos");

                    int respuesta = alumnos.length();
                    int regProcesados = respuestaAlumno(syncResult, alumnos);

                    Log.i("Paginacion", DIRECCION_WS);

                    if(verificarSuma(respuesta, regProcesados)) {
                        DIRECCION_WS = response.getString("NextPageUrl");
                        cantidadPaginas++;

                        if ((DIRECCION_WS == "null") && (pagecountServer == cantidadPaginas))
                            bFin = true;
                    }else {
                        syncResult.stats.numParseExceptions++;
                        bFin = true;
                    }
                }else {
                    bFin = true;
                }
            }catch (InterruptedException | ExecutionException | TimeoutException e){
                e.printStackTrace();
                bFin = true;
                syncResult.stats.numIoExceptions++;
            }catch (JSONException e){
                e.printStackTrace();
                bFin = true;
                syncResult.stats.numParseExceptions++;
            }
        }

        if (syncResult.stats.numParseExceptions > 0 || syncResult.stats.numIoExceptions > 0){
            syncResult.delayUntil = 30;

            //Enviar una señal informando el error
            intent.putExtra(SYNC_ACTION, SyncActivity.ACTION_SYNC_ALUMNOSERROR);
        }else{
            SharedPreferences preferences = getContext().getSharedPreferences(Constantes.PREFERENCIAS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constantes.PREF_SYNCALUMNOS, syncId);
            editor.commit();

            //Enviar una señal informando la sincronización correcta
            intent.putExtra(SYNC_ACTION, SyncActivity.ACTION_SYNC_ALUMNOSSINCRONIZADOS);
        }

        Log.i("SyncResultAlumnos", "Inserciones: " + syncResult.stats.numInserts + " Entradas: " + syncResult.stats.numEntries + " Actualizaciones: " + syncResult.stats.numUpdates + " Eliminaciones: " + syncResult.stats.numDeletes);

        getContext().sendBroadcast(intent);
    }

    public void datosTarjetas(final SyncResult syncResult){
        Log.i(TAG, "Obteniendo datos de tarjetas desde el servidor");

        Intent intent = new Intent(SyncActivity.ACTION_SYNC);
        boolean bFin = false;
        String DIRECCION_WS = obtenerDireccionws("Tarjetas");
        String syncId = null;
        int cantidadPaginas = 0;

        //Sincronización de las tarjetas por cambios de datos
        while (!bFin){
            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, DIRECCION_WS, new JSONObject(), future, future);
            request.setRetryPolicy(new DefaultRetryPolicy(60000, 0 , DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(getContext()).addToRequestQueue(request);

            try{
                JSONObject response = future.get(60, TimeUnit.SECONDS);
                int cantidadRegistros = response.getInt("TotalCount");
                int pagecountServer = response.getInt("TotalPageCount");
                syncId = response.getString("Sync");

                if(cantidadRegistros > 0){
                    JSONArray tarjetas = response.getJSONArray("Tarjetas");

                    int respuesta = tarjetas.length();
                    int regProcesados = respuestaTarjetas(syncResult, tarjetas);

                    Log.i("Paginacion Tarjetas", DIRECCION_WS);

                    if(verificarSuma(respuesta, regProcesados)){
                        DIRECCION_WS = response.getString("NextPageUrl");
                        cantidadPaginas++;

                        if((DIRECCION_WS == "null") && (pagecountServer == cantidadPaginas))
                            bFin = true;
                    }else {
                        syncResult.stats.numParseExceptions++;
                        bFin = true;
                    }
                }else {
                    bFin = true;
                }
            }catch (InterruptedException | ExecutionException | TimeoutException e){
                e.printStackTrace();
                bFin = true;
                syncResult.stats.numIoExceptions++;
            }catch (JSONException e){
                e.printStackTrace();
                bFin = true;
                syncResult.stats.numParseExceptions++;
            }
        }

        //Sincronizacion de las tarjetas por pasajes consumidos
        datosPasajesconsumidos(syncResult);

        //Resultado de la sincronización
        if(syncResult.stats.numParseExceptions > 0 || syncResult.stats.numIoExceptions > 0){
            syncResult.delayUntil = 30;

            //Enviar una señal informando el error
            intent.putExtra(SYNC_ACTION, SyncActivity.ACTION_SYNC_TARJETASERROR);
        }else{
            SharedPreferences preferences = getContext().getSharedPreferences(Constantes.PREFERENCIAS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constantes.PREF_SYNCTARJETAS, syncId);
            editor.putBoolean("SyncTarjetas", false);
            editor.commit();

            //Enviar una señal informando la sincronización correcta
            intent.putExtra(SYNC_ACTION, SyncActivity.ACTION_SYNC_TARJETASSINCRONIZADAS);
        }

        Log.i("SyncResultTarjetas", "Inserciones: " + syncResult.stats.numInserts + " Entradas: " + syncResult.stats.numEntries + " Actualizaciones: " + syncResult.stats.numUpdates + " Eliminaciones: " + syncResult.stats.numDeletes);

        getContext().sendBroadcast(intent);
    }

    private void datosPasajesconsumidos(final SyncResult syncResult){
        //En construcción
        boolean bFin = false;
        String DIRECCION_WS = "http://begup2.jujuy.gob.ar:81/ws/tarjetaupdate";
        int cantidadPaginas = 0;

        long entradas = syncResult.stats.numEntries;

        while(!bFin){
            try{
                //Obtener la ultima fecha de sincronización
                SharedPreferences preferences = getContext().getSharedPreferences(Constantes.PREFERENCIAS, Context.MODE_PRIVATE);
                SimpleDateFormat userFormat = new SimpleDateFormat("d/M/y HH:mm:ss");
                SimpleDateFormat serverFormat = new SimpleDateFormat("y-M-d HH:mm:ss");

                String fechaActualizacion = serverFormat.format(userFormat.parse(preferences.getString(Constantes.PREF_FECHAACTUALIZACION, null)));
                JSONObject parametroFecha = new JSONObject();
                parametroFecha.put("Date", fechaActualizacion);

                RequestFuture<JSONObject> future = RequestFuture.newFuture();
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, DIRECCION_WS, parametroFecha, future, future);
                request.setRetryPolicy(new DefaultRetryPolicy(60000, 0 , DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                VolleySingleton.getInstance(getContext()).addToRequestQueue(request);

                JSONObject response = future.get(60,TimeUnit.SECONDS);
                int cantidadRegistros = response.getInt("TotalCount");
                int pagecountServer = response.getInt("TotalPageCount");

                if(cantidadRegistros > 0){
                    JSONArray tarjetas = response.getJSONArray("Tarjetas");

                    int respuesta = tarjetas.length();
                    int regProcesados = respuestaTarjetas(syncResult, tarjetas);//Revisar esta línea

                    Log.i("Paginación Consumos", DIRECCION_WS);

                    if(verificarSuma(respuesta, regProcesados)){
                        DIRECCION_WS = response.getString("NextPageUrl");
                        cantidadPaginas++;

                        if((DIRECCION_WS == "null") && (pagecountServer == cantidadPaginas))
                            bFin = true;
                    }else {
                        syncResult.stats.numParseExceptions++;
                    }

                }else{
                    bFin = true;
                }
            }catch (InterruptedException | ExecutionException | TimeoutException e){
                e.printStackTrace();
                bFin = true;
                syncResult.stats.numIoExceptions++;
            }catch (JSONException | ParseException e){
                e.printStackTrace();
                bFin = true;
                syncResult.stats.numParseExceptions++;
            }
        }
    }

    private void datosFotos(final SyncResult syncResult){
        Uri uri = ContratoDatos.DescargaFotos.URI_CONTENIDO;
        Cursor c = resolver.query(uri, mProjectionDescargas, null, null, null);
        boolean bFin = false;

        if(c.getCount() > 0) {
            while (c.moveToNext()) {
                final String param = c.getString(0);
                String url = Constantes.GET_URL_FOTOS + "/" + param;

                RequestFuture<JSONObject> future = RequestFuture.newFuture();
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(), future, future);
                request.setRetryPolicy(new DefaultRetryPolicy(60000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                VolleySingleton.getInstance(getContext()).addToRequestQueue(request);

                try{
                    JSONObject response = future.get(60, TimeUnit.SECONDS);
                    Log.i("Datos Servidor", response.toString());

                    respuestaFotos(syncResult, response);
                }catch (InterruptedException e){
                    e.printStackTrace();
                    bFin = true;
                }catch (ExecutionException e){
                    e.printStackTrace();
                    bFin = true;
                }catch (TimeoutException e){
                    e.printStackTrace();
                    bFin = true;
                }

                if(bFin){
                    //Enviamos mensaje de error a la interfaz
                    Intent intent = new Intent(SyncActivity.ACTION_SYNC);
                    intent.putExtra(SYNC_ACTION, SyncActivity.ACTION_SYNC_FOTOSERROR);
                    getContext().sendBroadcast(intent);
                }
            }
        }else {
            Intent intent = new Intent(SyncActivity.ACTION_SYNC);
            intent.putExtra(SYNC_ACTION, SyncActivity.ACTION_SYNC_FOTOSSINCRONIZADAS);
            getContext().sendBroadcast(intent);
        }
    }

    private void datosPasajes(){
        Log.i(TAG, "Sincronizando la base de datos remota");
        Intent intent = new Intent(SyncActivity.ACTION_SYNC);

        SharedPreferences preferences = getContext().getSharedPreferences(Constantes.PREFERENCIAS, Context.MODE_PRIVATE);
        String idEmpresa = preferences.getString("ID_EMPRESA", "NO DEFINIDO");
        String[] mSelectionArgs = {""};

        //Consulta de datos
        String mSelection = ContratoDatos.Consumos.SYNC + " =? ";
        mSelectionArgs[0] = String.valueOf(Constantes.EN_SINCRONIZACION);
        final Cursor c = resolver.query(ContratoDatos.Consumos.URI_CONTENIDO, mProjectionConsumos, mSelection, mSelectionArgs, null);

        Log.i(TAG, "Se encontraron " + c.getCount() + " registros para sincronizar");

        if(c.getCount() > 0){
            banSyncRemota = false;

            while (c.moveToNext()){
                final String idConsumo = c.getString(c.getColumnIndex(ContratoDatos.Consumos.ID_CONSUMO));
                Log.i(TAG, "LATITUD: " + c.getString(c.getColumnIndex(ContratoDatos.Consumos.GPS_LATITUD)) +
                        "; LONGITUD: " + c.getString(c.getColumnIndex(ContratoDatos.Consumos.GPS_LONGITUD)) +
                        "; CAMPO: " + c.getString(c.getColumnIndex(ContratoDatos.Consumos.SYNC)));

                RequestFuture<JSONObject> future = RequestFuture.newFuture();
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, Constantes.INSERT_CONSUMOS, Utilidades.deCursorAJSONObject(c), future, future);
                request.setRetryPolicy(new DefaultRetryPolicy(60000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                VolleySingleton.getInstance(getContext()).addToRequestQueue(request);

                try{
                    JSONObject response = future.get(60, TimeUnit.SECONDS);
                    Log.i("Datos Servidor", response.toString());

                    //CreditoUsado < Credito Temporal
                    Log.i("Envio de Datos", "Todo ok");
                    ContentValues contentUpdate = new ContentValues();
                    contentUpdate.put("SYNC", Constantes.SINCRONIZADO);

                    String mSelectionClause = ContratoDatos.Consumos.ID_CONSUMO + " =? ";
                    mSelectionArgs[0] = idConsumo;
                    int mRowsUpdated = 0;

                    //* Ver de ubicar el registro mediante el ID. Estudiar el método network response
                    mRowsUpdated = getContext().getContentResolver().update(
                            ContratoDatos.Consumos.crearUriConsumos(idConsumo),
                            contentUpdate,
                            null,
                            null
                    );

                    if(mRowsUpdated > 0)
                        Log.i(TAG, "Registro actualizado");
                    else {
                        Log.i(TAG, "Registro no actualizado");
                        banSyncRemota = true;
                    }

                }catch (InterruptedException e){
                    e.printStackTrace();
                    banSyncRemota = true;
                }catch (ExecutionException e){
                    e.printStackTrace();
                    banSyncRemota = true;
                }catch (TimeoutException e){
                    e.printStackTrace();
                    banSyncRemota = true;
                }
            }
        }else {
            Log.i(TAG, "No se requiere sincronización");
        }

        //Si hubo un error en el envio de consumos
        if(banSyncRemota)
            intent.putExtra(SYNC_ACTION, SyncActivity.ACTION_SYNC_CONSUMOSERROR);
        else
            intent.putExtra(SYNC_ACTION, SyncActivity.ACTION_SYNC_CONSUMOSSINCRONIZADAS);

        getContext().sendBroadcast(intent);
    }

    private int respuestaAlumno(SyncResult syncResult, JSONArray alumnos){
        int regProcesados = 0;

        if(alumnos != null) {
            //Parsear con Gson
            Alumno[] res = gson.fromJson(alumnos != null ? alumnos.toString() : null, Alumno[].class);
            List<Alumno> data = Arrays.asList(res);

            //Lista para recolección de operaciones pendientes
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

            //Tabla hash para recibir las entradas del servidor
            HashMap<String, Alumno> alumnoHashMap = new HashMap<String, Alumno>();
            for (Alumno alumno : data) {
                alumnoHashMap.put(alumno.DNI, alumno);
                syncResult.stats.numEntries++;
                regProcesados++;
            }

            //regProcesados = alumnoHashMap.size();

            //Consultar en la BD local los registros remotos recibidos
            Uri uri = ContratoDatos.Alumnos.URI_CONTENIDO;
            Cursor c = resolver.query(uri, mProjectionAlumnos, null, null, null);
            assert c != null;

            Log.i(TAG, "Se encontraron " + c.getCount() + " registros locales");

            //Encontrar datos obsoletos
            String dni, nombre, apellido, genero;
            while (c.moveToNext()) {
                dni = c.getString(DNIalumno);
                nombre = c.getString(NOMBREalumno);
                apellido = c.getString(APELLIDOalumno);
                genero = c.getString(GENEROalumno);

                Alumno match = alumnoHashMap.get(dni);

                if (match != null) {
                    //Esta entrada existe, por lo que se remueve del mapeo
                    alumnoHashMap.remove(dni);

                    Uri existingUri = ContratoDatos.Alumnos.crearUriAlumno(dni);

                    //Comprobar si el ALUMNO necesita ser actualizado
                    boolean b1 = match.NOMBRE != null && !match.NOMBRE.equals(nombre);
                    boolean b2 = match.APELLIDO != null && !match.APELLIDO.equals(apellido);
                    boolean b3 = match.GENERO != null && !match.GENERO.equals(genero);

                    if (b1 || b2 || b3) {
                        Log.i(TAG, "Programando actualización de: " + existingUri);

                        ops.add(ContentProviderOperation.newUpdate(existingUri)
                                .withValue(ContratoDatos.Alumnos.NOMBRE, match.NOMBRE)
                                .withValue(ContratoDatos.Alumnos.APELLIDO, match.APELLIDO)
                                .withValue(ContratoDatos.Alumnos.GENERO, match.GENERO)
                                .build()
                        );
                        syncResult.stats.numUpdates++;
                    } else {
                        Log.i(TAG, "No hay acciones para este registro: " + existingUri);
                    }
                } else {
                    Log.i(TAG, "Ver si se elimina");
                    // * Ver caso de eliminación
                    //syncResult.stats.numDeletes++;
                }
            }
            c.close();

            //Insertar items resultantes
            for (Alumno a : alumnoHashMap.values()) {
                Log.i(TAG, "Programando inserción de: " + a.DNI);
                ops.add(ContentProviderOperation.newInsert(ContratoDatos.Alumnos.URI_CONTENIDO)
                        .withValue(ContratoDatos.Alumnos.DNI, a.DNI)
                        .withValue(ContratoDatos.Alumnos.NOMBRE, a.NOMBRE)
                        .withValue(ContratoDatos.Alumnos.APELLIDO, a.APELLIDO)
                        .withValue(ContratoDatos.Alumnos.GENERO, a.GENERO)
                        .build()
                );
                syncResult.stats.numInserts++;
            }

            if (syncResult.stats.numInserts > 0 || syncResult.stats.numUpdates > 0 || syncResult.stats.numDeletes > 0) {
                Log.i(TAG, "Aplicando operaciones");

                try {
                    resolver.applyBatch(ContratoDatos.AUTHORITY, ops);
                } catch (RemoteException | OperationApplicationException e) {
                    e.printStackTrace();

                    //Al haber un error se resta el número de entradas para que no coincida la suma de verificacion
                    //regProcesados = -1;
                    syncResult.stats.numIoExceptions++;
                }

                resolver.notifyChange(ContratoDatos.Alumnos.URI_CONTENIDO, null);
                Log.i(TAG, "Sincronización finalizada");

            } else {
                Log.i(TAG, "No se requiere sincronización");
            }

        }

        return regProcesados;
    }

    private int respuestaTarjetas(SyncResult syncResult, JSONArray tarjetas){
        int regProcesados = 0;

        if(tarjetas != null) {
            //Parsear con Gson
            Tarjeta[] res = gson.fromJson(tarjetas != null ? tarjetas.toString() : null, Tarjeta[].class);
            List<Tarjeta> data = Arrays.asList(res);

            //Lista para recolección de operaciones pendientes
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

            //Tabla hash para recibir las entradas del servidor
            HashMap<String, Tarjeta> tarjetaHashMap = new HashMap<String, Tarjeta>();
            for (Tarjeta t : data) {
                tarjetaHashMap.put(t.KEY, t);
                syncResult.stats.numEntries++;
                regProcesados++;
            }

            //Consultar registros remotos actuales
            Uri uri = ContratoDatos.Tarjetas.URI_CONTENIDO;
            Cursor c = resolver.query(uri, mProjectionTarjetas, null, null, null);
            assert c != null;

            Log.i(TAG, "Se encontraron " + c.getCount() + "tarjetas locales");

            //Encontrar datos obsoletos
            String num_tarjeta, dni, fecha, credito_total, credito_usado, credito_temporal;
            while (c.moveToNext()) {
                num_tarjeta = c.getString(NUMEROtarjeta);
                dni = c.getString(DNItarjeta);
                fecha = c.getString(FECHAtarjeta);
                credito_total = c.getString(CREDITOTOTALtarjeta);
                credito_usado = c.getString(CREDITOUSADOtarjeta);
                credito_temporal = c.getString(CREDITOTEMPORALtarjeta);

                Tarjeta match = tarjetaHashMap.get(num_tarjeta);

                if (match != null) {
                    //Esta entrada existe, por lo que se remueve del mapeo
                    tarjetaHashMap.remove(num_tarjeta);

                    Uri existingUri = ContratoDatos.Tarjetas.crearUriTarjeta(num_tarjeta);

                    //Comprobar si la tarjeta necesita ser actualizada
                    boolean b1 = match.KEY != null && !match.KEY.equals(num_tarjeta);
                    boolean b2 = match.DNI != null && !match.DNI.equals(dni);
                    boolean b3 = match.FECHA != null && !match.FECHA.equals(fecha);
                    boolean b4 = match.CREDITO_TOTAL != null && !match.CREDITO_TOTAL.equals(credito_total);
                    boolean b5 = match.CREDITO_USADO != null && !match.CREDITO_USADO.equals(credito_usado);
                    boolean b6 = match.CREDITO_TEMPORAL != null && !match.CREDITO_TEMPORAL.equals(credito_temporal);

                    if (b1 || b2 || b3 || b4 || b5 || b6) {
                        Log.i(TAG, "Programando actualización de: " + existingUri);

                        ops.add(ContentProviderOperation.newUpdate(existingUri)
                                .withValue(ContratoDatos.Tarjetas.NUM_TARJETAS, match.KEY)
                                .withValue(ContratoDatos.Tarjetas.FECHA, match.FECHA)
                                .withValue(ContratoDatos.Tarjetas.DNI, match.DNI)
                                .withValue(ContratoDatos.Tarjetas.CREDITO_TOTAL, match.CREDITO_TOTAL)
                                .withValue(ContratoDatos.Tarjetas.CREDITO_USADO, match.CREDITO_USADO)
                                .withValue(ContratoDatos.Tarjetas.CREDITO_TEMPORAL, match.CREDITO_TEMPORAL)
                                .build()
                        );
                        syncResult.stats.numUpdates++;
                    } else {
                        Log.i(TAG, "No hay acciones para este registro: " + existingUri);
                    }
                } else {
                    Log.i(TAG, "Ver si se elimina");
                    // * Ver caso de eliminación
                }
            }
            c.close();

            //Insertar items resultantes
            for (Tarjeta t : tarjetaHashMap.values()) {
                Log.i(TAG, "Programando inserción de: " + t.KEY);
                ops.add(ContentProviderOperation.newInsert(ContratoDatos.Tarjetas.URI_CONTENIDO)
                        .withValue(ContratoDatos.Tarjetas.NUM_TARJETAS, t.KEY)
                        .withValue(ContratoDatos.Tarjetas.FECHA, t.FECHA)
                        .withValue(ContratoDatos.Tarjetas.DNI, t.DNI)
                        .withValue(ContratoDatos.Tarjetas.CREDITO_TOTAL, t.CREDITO_TOTAL)
                        .withValue(ContratoDatos.Tarjetas.CREDITO_USADO, t.CREDITO_USADO)
                        .withValue(ContratoDatos.Tarjetas.CREDITO_TEMPORAL, t.CREDITO_TEMPORAL)
                        .build()
                );
                syncResult.stats.numInserts++;
            }

            if (syncResult.stats.numInserts > 0 || syncResult.stats.numUpdates > 0 || syncResult.stats.numDeletes > 0) {
                Log.i(TAG, "Aplicando operaciones");

                try {
                    resolver.applyBatch(ContratoDatos.AUTHORITY, ops);
                } catch (RemoteException | OperationApplicationException e) {
                    e.printStackTrace();

                    syncResult.stats.numIoExceptions++;
                }

                resolver.notifyChange(ContratoDatos.Tarjetas.URI_CONTENIDO, null);
                Log.i(TAG, "Sincronización finalizada");
            } else {
                Log.i(TAG, "No se requiere sincronización");
            }
        }

        return regProcesados;
    }

    private int respuestaConsumos(SyncResult syncResult, JSONArray consumos){
        int regProcesados = 0;

        if(consumos != null) {
            //Parsear con Gson
            Tarjeta[] res = gson.fromJson(consumos != null ? consumos.toString() : null, Tarjeta[].class);
            List<Tarjeta> data = Arrays.asList(res);

            //Lista para recolección de operaciones pendientes
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

            //Tabla hash para recibir las entradas del servidor
            HashMap<String, Tarjeta> tarjetaHashMap = new HashMap<String, Tarjeta>();
            for (Tarjeta t : data) {
                tarjetaHashMap.put(t.KEY, t);
                syncResult.stats.numEntries++;
                regProcesados++;
            }

            //Consultar registros remotos actuales
            Uri uri = ContratoDatos.Tarjetas.URI_CONTENIDO;
            Cursor c = resolver.query(uri, mProjectionTarjetas, null, null, null);
            assert c != null;

            Log.i(TAG, "Se encontraron " + c.getCount() + "tarjetas locales");

            //Encontrar datos obsoletos
            String num_tarjeta, dni, fecha, credito_total, credito_usado, credito_temporal;
            while (c.moveToNext()) {
                num_tarjeta = c.getString(NUMEROtarjeta);
                dni = c.getString(DNItarjeta);
                fecha = c.getString(FECHAtarjeta);
                credito_total = c.getString(CREDITOTOTALtarjeta);
                credito_usado = c.getString(CREDITOUSADOtarjeta);
                credito_temporal = c.getString(CREDITOTEMPORALtarjeta);

                Tarjeta match = tarjetaHashMap.get(num_tarjeta);

                if (match != null) {
                    //Esta entrada existe, por lo que se remueve del mapeo
                    tarjetaHashMap.remove(num_tarjeta);

                    Uri existingUri = ContratoDatos.Tarjetas.crearUriTarjeta(num_tarjeta);

                    //Comprobar si la tarjeta necesita ser actualizada
                    boolean b1 = match.KEY != null && !match.KEY.equals(num_tarjeta);
                    boolean b2 = match.DNI != null && !match.DNI.equals(dni);
                    boolean b3 = match.FECHA != null && !match.FECHA.equals(fecha);
                    boolean b4 = match.CREDITO_TOTAL != null && !match.CREDITO_TOTAL.equals(credito_total);
                    boolean b5 = match.CREDITO_USADO != null && !match.CREDITO_USADO.equals(credito_usado);
                    boolean b6 = match.CREDITO_TEMPORAL != null && !match.CREDITO_TEMPORAL.equals(credito_temporal);

                    if (b1 || b2 || b3 || b4 || b5 || b6) {
                        Log.i(TAG, "Programando actualización de: " + existingUri);

                        ops.add(ContentProviderOperation.newUpdate(existingUri)
                                .withValue(ContratoDatos.Tarjetas.NUM_TARJETAS, match.KEY)
                                .withValue(ContratoDatos.Tarjetas.FECHA, match.FECHA)
                                .withValue(ContratoDatos.Tarjetas.DNI, match.DNI)
                                .withValue(ContratoDatos.Tarjetas.CREDITO_TOTAL, match.CREDITO_TOTAL)
                                .withValue(ContratoDatos.Tarjetas.CREDITO_USADO, match.CREDITO_USADO)
                                .withValue(ContratoDatos.Tarjetas.CREDITO_TEMPORAL, match.CREDITO_TEMPORAL)
                                .build()
                        );
                        syncResult.stats.numUpdates++;
                    } else {
                        Log.i(TAG, "No hay acciones para este registro: " + existingUri);
                    }
                } else {
                    Log.i(TAG, "Ver si se elimina");
                    // * Ver caso de eliminación
                }
            }
            c.close();

            //Insertar items resultantes
            for (Tarjeta t : tarjetaHashMap.values()) {
                Log.i(TAG, "Programando inserción de: " + t.KEY);
                ops.add(ContentProviderOperation.newInsert(ContratoDatos.Tarjetas.URI_CONTENIDO)
                        .withValue(ContratoDatos.Tarjetas.NUM_TARJETAS, t.KEY)
                        .withValue(ContratoDatos.Tarjetas.FECHA, t.FECHA)
                        .withValue(ContratoDatos.Tarjetas.DNI, t.DNI)
                        .withValue(ContratoDatos.Tarjetas.CREDITO_TOTAL, t.CREDITO_TOTAL)
                        .withValue(ContratoDatos.Tarjetas.CREDITO_USADO, t.CREDITO_USADO)
                        .withValue(ContratoDatos.Tarjetas.CREDITO_TEMPORAL, t.CREDITO_TEMPORAL)
                        .build()
                );
                syncResult.stats.numInserts++;
            }

            if (syncResult.stats.numInserts > 0 || syncResult.stats.numUpdates > 0 || syncResult.stats.numDeletes > 0) {
                Log.i(TAG, "Aplicando operaciones");

                try {
                    resolver.applyBatch(ContratoDatos.AUTHORITY, ops);
                } catch (RemoteException | OperationApplicationException e) {
                    e.printStackTrace();

                    syncResult.stats.numIoExceptions++;
                }

                resolver.notifyChange(ContratoDatos.Tarjetas.URI_CONTENIDO, null);
                Log.i(TAG, "Sincronización finalizada");
            } else {
                Log.i(TAG, "No se requiere sincronización");
            }
        }

        return regProcesados;
    }

    private void respuestaFotos(SyncResult syncResult, JSONObject response){
        //(*) Solo falta cambiar el valor del campo SYNC de la tabla DESCARGA una ves que se dieron de alta
        Intent intent = new Intent(SyncActivity.ACTION_SYNC);

        //Parsear con Gson
        Foto res = gson.fromJson(response != null ? response.toString() : null, Foto.class);
        List<Foto> data = Arrays.asList(res);

        //Lista para recolección de operaciones pendientes
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        //Tabla hash para recbir las entradas del servidor
        HashMap<String, Foto> fotoHashMap = new HashMap<String, Foto>();
        for (Foto foto : data){
            fotoHashMap.put(foto.DNI, foto);
        }

        //Consultar en la BD local los registros remotos recibidos
        Uri uri = ContratoDatos.Fotos.URI_CONTENIDO;
        Cursor c = resolver.query(uri, mProjectionFotos, null, null,null);
        assert c != null;

        Log.i(TAG, "Se encontraron " + c.getCount() + " registros locales");

        //Encontrar datos obsoletos
        String dni, foto;
        while (c.moveToNext()){
            syncResult.stats.numEntries++;

            dni = c.getString(DNIfoto);
            foto = c.getString(FOTOfoto);

            Foto match = fotoHashMap.get(dni);

            if(match != null){
                //Esta entrada existe, por lo tanto se remueve del mapeo
                fotoHashMap.remove(dni);

                //Uri para actualizar la FOTO si hubiera un cambio
                Uri existingUri = ContratoDatos.Fotos.crearUriFoto(dni);

                //Comprobar si la FOTO necesita ser actualizada
                boolean b1 = match.DNI != null && !match.DNI.equals(dni);
                boolean b2 = match.FOTO != null && !match.FOTO.equals(foto);

                if (b1 || b2){
                    Log.i(TAG, "Programando actualización de: " + existingUri);

                    ops.add(ContentProviderOperation.newUpdate(existingUri)
                            .withValue(ContratoDatos.Fotos.DNI, match.DNI)
                            .withValue(ContratoDatos.Fotos.FOTO, match.FOTO)
                            .build()
                    );

                    syncResult.stats.numUpdates++;
                }else {
                    Log.i(TAG, "No hay acciones para este registro: " + existingUri);
                }
            }else {
                Log.i(TAG, "Ver si se elimina");
                // * Ver caso de eliminación
            }
        }
        c.close();

        for(Foto f : fotoHashMap.values()){
            Log.i(TAG, "Programando inserción de: " + f.DNI);
            ops.add(ContentProviderOperation.newInsert(ContratoDatos.Fotos.URI_CONTENIDO)
                    .withValue(ContratoDatos.Fotos.DNI, f.DNI)
                    .withValue(ContratoDatos.Fotos.FOTO, f.FOTO)
                    .build()
            );

            Uri syncUri = ContratoDatos.DescargaFotos.crearUriDescargafoto(f.DNI);
            ops.add(ContentProviderOperation.newUpdate(syncUri)
                    .withValue(ContratoDatos.DescargaFotos.DNI, f.DNI)
                    .withValue(ContratoDatos.DescargaFotos.SYNC, Constantes.SINCRONIZADO)
                    .build()
            );
            syncResult.stats.numInserts++;
        }

        if(syncResult.stats.numInserts > 0 || syncResult.stats.numUpdates > 0 || syncResult.stats.numDeletes > 0){
            Log.i(TAG, "Aplicando operaciones");

            try{
                resolver.applyBatch(ContratoDatos.AUTHORITY, ops);

                intent.putExtra(SYNC_ACTION, SyncActivity.ACTION_SYNC_FOTOSSINCRONIZADAS);
            }catch (RemoteException | OperationApplicationException e){
                e.printStackTrace();

                intent.putExtra(SYNC_ACTION, SyncActivity.ACTION_SYNC_FOTOSERROR);
            }
            resolver.notifyChange(ContratoDatos.Fotos.URI_CONTENIDO, null, false);

            Log.i(TAG, "Sincronización finalizada");
        }else {
            Log.i(TAG, "No se requiere sincronización de FOTOS");
            intent.putExtra(SYNC_ACTION, SyncActivity.ACTION_SYNC_FOTOSSINCRONIZADAS);
        }

        resolver.notifyChange(ContratoDatos.DescargaFotos.URI_CONTENIDO, null, false);

        getContext().sendBroadcast(intent);
    }

    private boolean verificarSuma(int respuesta, int procesados){
        //Devuelve verdadero si la suma de verificacion es correcta
        if (respuesta == procesados)
            return true;
        else
            return false;
    }

    public static Account obtenerCuentaASincronizar(Context context){
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(context.getString(R.string.app_name), Constantes.ACCOUNT_TYPE);

        if(null == accountManager.getPassword(newAccount)){
            if(!accountManager.addAccountExplicitly(newAccount, "", null))
                return null;
        }

        Log.i(TAG, "Cuenta de usuario obtenida");
        return newAccount;
    }

    public static void sincronizarAhora(Context context, boolean onlyUpload){
        Log.i(TAG, "Realizando petición de sincronización manual");

        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        if(onlyUpload)
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, true);

        ContentResolver.requestSync(obtenerCuentaASincronizar(context), context.getString(R.string.provider_authority), bundle);
    }

    public static boolean estaSincronizando(Context context){
        boolean pendiente = ContentResolver.isSyncActive(obtenerCuentaASincronizar(context), context.getString(R.string.provider_authority));
        if (pendiente)
            return true;
        else
            return false;
    }


}
