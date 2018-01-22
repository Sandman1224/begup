package martin.compras.de.lista.app.com.begu.providers;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

import martin.compras.de.lista.app.com.begu.Helpers.DataBaseHelper;
import martin.compras.de.lista.app.com.begu.utils.Constantes;

public class DatosProvider extends ContentProvider {
    public static final String TAG = "Provider";
    public static final String URI_NO_SOPORTADA = "Uri no soportada";

    private DataBaseHelper helper;
    private ContentResolver resolver;

    public DatosProvider() {
    }

    // [URI_MATCHER]
    public static final UriMatcher uriMatcher;

    //Casos
    public static final int ALUMNOS = 100;
    public static final int ALUMNOS_DNI = 101;
    public static final int ALUMNOS_TARJETA = 102;

    public static final int TARJETAS = 200;
    public static final int TARJETAS_NUMERO = 201;

    public static final int FOTOS = 300;
    public static final int FOTOS_DNI = 301;

    public static final int DESCARGA = 400;
    public static final int DESCARGA_DNI = 401;

    public static final int CONSUMOS = 500;
    public static final int CONSUMOS_EMPRESA = 501;
    public static final int CONSUMOS_SYNC = 502;

    public static final String AUTORIDAD = "martin.compras.de.lista.app.com.begu";

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(AUTORIDAD, "EDU_ALUMNOS", ALUMNOS);
        uriMatcher.addURI(AUTORIDAD, "EDU_ALUMNOS/*", ALUMNOS_DNI);
        uriMatcher.addURI(AUTORIDAD, "EDU_ALUMNOS/*/BEGU_TARJETAS", ALUMNOS_TARJETA);

        uriMatcher.addURI(AUTORIDAD, "BEGU_TARJETAS", TARJETAS);
        uriMatcher.addURI(AUTORIDAD, "BEGU_TARJETAS/*", TARJETAS_NUMERO);

        uriMatcher.addURI(AUTORIDAD, "EDU_FOTOS", FOTOS);
        uriMatcher.addURI(AUTORIDAD, "EDU_FOTOS/*", FOTOS_DNI);

        uriMatcher.addURI(AUTORIDAD, "DESCARGA_FOTOS", DESCARGA);
        uriMatcher.addURI(AUTORIDAD, "DESCARGA_FOTOS/*", DESCARGA_DNI);

        uriMatcher.addURI(AUTORIDAD, "BEGU_CONSUMO", CONSUMOS);
        uriMatcher.addURI(AUTORIDAD, "BEGU_CONSUMO/*", CONSUMOS_EMPRESA);
        uriMatcher.addURI(AUTORIDAD, "BEGU_CONSUMO/sincronizar", CONSUMOS_SYNC);
    }
    // [/URI_MATCHER]

    //[CAMPOS_AUXILIARES]
    private static final String BEGUTARJETAS_JOIN_EDUFOTOS ="EDU_ALUMNOS " +
            "INNER JOIN BEGU_TARJETAS " +
            "ON EDU_ALUMNOS.DNI = BEGU_TARJETAS.DNI" +
            "INNER JOIN EDU_FOTOS " +
            "ON EDU_ALUMNOS.DNI = EDU_FOTOS.DNI";

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

    public static final String[] mProjectionConsumos = new String[]{
            ContratoDatos.Consumos.ID_CONSUMO,
            ContratoDatos.Consumos.NUM_TARJETA,
            ContratoDatos.Consumos.FECHA,
            ContratoDatos.Consumos.GPS_LATITUD,
            ContratoDatos.Consumos.GPS_LONGITUD,
            ContratoDatos.Consumos.ID_EMPRESA,
            ContratoDatos.Consumos.SYNC
    };
    //[/CAMPOS_AUXILIARES]

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        switch (uriMatcher.match(uri)){
            case ALUMNOS:
                return ContratoDatos.generarMime("EDU_ALUMNOS");
            case ALUMNOS_DNI:
                return ContratoDatos.generarMimeItem("EDU_ALUMNOS");
            default:
                throw new UnsupportedOperationException("Uri desconocida: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        Log.d(TAG, "Inserción en " + uri + "(" + values.toString());

        SQLiteDatabase db = helper.getWritableDatabase();
        String id = null;

        switch (uriMatcher.match(uri)){
            case ALUMNOS:
                db.insertOrThrow(DataBaseHelper.Tablas.EDU_ALUMNOS, null, values);
                notificarCambio(uri);

                return ContratoDatos.Alumnos.crearUriAlumno(id);
            case TARJETAS:
                db.insertOrThrow(DataBaseHelper.Tablas.BEGU_TARJETAS, null, values);
                notificarCambio(uri);

                return ContratoDatos.Tarjetas.crearUriTarjeta(id);
            case FOTOS:
                db.insertOrThrow(DataBaseHelper.Tablas.EDU_FOTOS, null, values);
                notificarCambio(uri);

                return ContratoDatos.Fotos.crearUriFoto(id);
            case DESCARGA:
                db.insertOrThrow(DataBaseHelper.Tablas.DESCARGA_FOTOS, null, values);
                notificarCambio(uri);

                return ContratoDatos.DescargaFotos.crearUriDescargafoto(id);
            case CONSUMOS:
                db.insertOrThrow(DataBaseHelper.Tablas.BEGU_CONSUMO, null, values);
                notificarCambio(uri);

                return ContratoDatos.Consumos.crearUriConsumos(id);
            default:
                throw new UnsupportedOperationException(URI_NO_SOPORTADA);
        }
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        helper = new DataBaseHelper(getContext());
        resolver = getContext().getContentResolver();

        return true;
    }

    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        final SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try{
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for(int i = 0; i < numOperations; i++){
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();

            return results;
        }finally {
            db.endTransaction();
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        SQLiteDatabase db = helper.getReadableDatabase();

        int match = uriMatcher.match(uri);

        String id;
        Cursor c;

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        switch (match){
            case ALUMNOS:
                //Devuelve todos los ALUMNOS
                //Obtener filtro
                String filtro = ContratoDatos.Alumnos.tieneFiltro(uri)
                        ? construirFiltro(uri.getQueryParameter("filtro")) : null;
                //Consultando todos los alumnos
                builder.setTables("EDU_ALUMNOS");
                c = builder.query(db, mProjectionAlumnos, null, null, null, null, filtro);
                break;
            case ALUMNOS_DNI:
                //Devuelve un ALUMNO en Particular
                id = ContratoDatos.Alumnos.obtenerDniAlumno(uri);
                builder.setTables("EDU_ALUMNOS");
                c = builder.query(db, mProjectionAlumnos,
                        ContratoDatos.Alumnos.DNI + "=" + "\'" + id + "\'"
                                + (!TextUtils.isEmpty(selection) ?
                                " AND (" + selection + ')' : ""),
                        selectionArgs, null, null, null);
                break;
            case TARJETAS:
                //Devuelve todas las TARJETAS
                builder.setTables("BEGU_TARJETAS");
                c = builder.query(db, mProjectionTarjetas, null, null, null, null, null);
                break;
            case TARJETAS_NUMERO:
                //Devuelve una TARJETA en particular
                id = ContratoDatos.Tarjetas.obtenerNumeroTarjeta(uri);
                builder.setTables("BEGU_TARJETAS");
                c = builder.query(db, mProjectionTarjetas,
                        ContratoDatos.Tarjetas.NUM_TARJETAS + "=" + "\'" + id + "\'"
                                + (!TextUtils.isEmpty(selection) ?
                                " AND (" + selection + ')' : ""),
                        selectionArgs, null, null, null);
                break;
            case FOTOS:
                //Devuelve todas las FOTOS de los alumnos
                builder.setTables("EDU_FOTOS");
                c = builder.query(db, mProjectionFotos, null, null, null, null, null);
                break;
            case DESCARGA:
                //Devuelve todas las solicitudes de descargas de fotos
                builder.setTables("DESCARGA_FOTOS");
                c = builder.query(db, mProjectionDescargas, ContratoDatos.DescargaFotos.SYNC + "=" + "\'" + Constantes.EN_SINCRONIZACION + "\'", null, null, null, null);
                break;
            case CONSUMOS:
                //Devuelve todos los consumos realizados
                builder.setTables(DataBaseHelper.Tablas.BEGU_CONSUMO);
                c = builder.query(db, mProjectionConsumos, selection, selectionArgs, null, null, null);
                break;
            case CONSUMOS_SYNC:
                //Devuelve los consumos pendientes de sincronización
                builder.setTables(DataBaseHelper.Tablas.BEGU_CONSUMO);
                c = builder.query(db, mProjectionConsumos, ContratoDatos.Consumos.SYNC + "=" + "\'" + Constantes.EN_SINCRONIZACION + "\'", null, null, null, null);
                break;
            case CONSUMOS_EMPRESA:
                //Devuelve todos los consumos de una empresa en particular
                id = ContratoDatos.Consumos.obtenerConsumos(uri);
                builder.setTables(DataBaseHelper.Tablas.BEGU_CONSUMO);
                c = builder.query(db, mProjectionConsumos,
                        ContratoDatos.Consumos.ID_EMPRESA + "=" + "\'" + id + "\'"
                        + (!TextUtils.isEmpty(selection) ?
                        " AND (" + selection + ')' : ""),
                        selectionArgs, null, null, null);
            default:
                throw new UnsupportedOperationException(URI_NO_SOPORTADA);
        }

        c.setNotificationUri(resolver, uri);

        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        SQLiteDatabase db = helper.getWritableDatabase();
        String id;
        int afectados;

        switch (uriMatcher.match(uri)){
            case ALUMNOS_DNI:
                id = ContratoDatos.Alumnos.obtenerDniAlumno(uri);
                afectados = db.update(
                        DataBaseHelper.Tablas.EDU_ALUMNOS,
                        values,
                        ContratoDatos.Alumnos.DNI + " = ?",
                        new String[]{id});
                notificarCambio(uri);
                break;
            case TARJETAS_NUMERO:
                id = ContratoDatos.Tarjetas.obtenerNumeroTarjeta(uri);
                afectados = db.update(
                        DataBaseHelper.Tablas.BEGU_TARJETAS,
                        values,
                        ContratoDatos.Tarjetas.NUM_TARJETAS + " = ?",
                        new String[]{id}
                );
                notificarCambio(uri);
                break;
            case FOTOS_DNI:
                id = ContratoDatos.Fotos.obtenerDniFoto(uri);
                afectados = db.update(
                        DataBaseHelper.Tablas.EDU_FOTOS,
                        values,
                        ContratoDatos.Fotos.DNI + " = ?",
                        new String[]{id}
                );
                notificarCambio(uri);
                break;
            case DESCARGA_DNI:
                id = ContratoDatos.DescargaFotos.obtenerDescargafoto(uri);
                afectados = db.update(
                        DataBaseHelper.Tablas.DESCARGA_FOTOS,
                        values,
                        ContratoDatos.DescargaFotos.DNI + " = ?",
                        new String[]{id}
                );
                notificarCambio(uri);
                break;
            case CONSUMOS_EMPRESA:
                id = ContratoDatos.Consumos.obtenerConsumos(uri);
                afectados = db.update(
                        DataBaseHelper.Tablas.BEGU_CONSUMO,
                        values,
                        ContratoDatos.Consumos.ID_CONSUMO + " = ?",
                        new String[]{id}
                );
                notificarCambio(uri);
                break;
            default:
                throw new UnsupportedOperationException(URI_NO_SOPORTADA);
        }

        return afectados;
    }

    private String construirFiltro(String filtro){
        String sentencia = null;

        switch (filtro) {
            case "FILTRODNI":
                sentencia = "EDU_ALUMNOS.DNI";
                break;
        }

        return sentencia;
    }

    private void notificarCambio(Uri uri){
        resolver.notifyChange(uri, null);
    }
}
