package martin.compras.de.lista.app.com.begu.utils;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import martin.compras.de.lista.app.com.begu.providers.ContratoDatos;
import martin.compras.de.lista.app.com.begu.sync.SyncAdapter;

/**
 * Created by Tinch on 21/4/2017.
 */

public class Utilidades {
    //Indices de las columnas de la tabla BEGU_CONSUMOS
    public final static int CONSUMOid = 0;
    public final static int CONSUMOnrotarjeta = 1;
    public final static int CONSUMOfecha = 2;
    public final static int CONSUMOgpslatitud = 3;
    public final static int CONSUMOgpslongitud = 4;
    public final static int CONSUMOidempresa = 5;

    public final static String ID_CONSUMO = "_id";
    public final static String NUM_TARJETA = "KEY";
    public final static String FECHA = "FECH";
    public final static String GPS_LATITUD = "GPS_LATITUD";
    public final static String GPS_LONGITUD = "GPS_LONGITUD";
    public final static String ID_EMPRESA = "ID_EMPRESA";

    public static JSONObject deCursorAJSONObject(Cursor c){
        JSONObject jsonObject = new JSONObject();
        String fechaConsumo, idEmpresa, numTarjeta;
        Double gpsLatitud, gpsLongitud;

        //numTarjeta = "103067";
        numTarjeta = c.getString(CONSUMOnrotarjeta);
        fechaConsumo = c.getString(CONSUMOfecha);
        gpsLatitud = c.getDouble(CONSUMOgpslatitud);
        gpsLongitud = c.getDouble(CONSUMOgpslongitud);
        //idEmpresa = c.getString(CONSUMOidempresa);
        idEmpresa = numTarjeta;

        try{
            jsonObject.put(NUM_TARJETA, numTarjeta);
            jsonObject.put(FECHA, fechaConsumo);
            jsonObject.put(GPS_LATITUD, gpsLatitud);
            jsonObject.put(GPS_LONGITUD, gpsLongitud);
            jsonObject.put(ID_EMPRESA, idEmpresa);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("Cursor a JSONObject", String.valueOf(jsonObject));

        return jsonObject;
    }
}
