package martin.compras.de.lista.app.com.begu.providers;

import android.net.Uri;
import android.provider.BaseColumns;

import java.security.PublicKey;

/**
 * Created by Tinch on 28/3/2017.
 */

public class ContratoDatos {

    public final static String AUTHORITY = "martin.compras.de.lista.app.com.begu";
    public final static Uri URI_BASE = Uri.parse("content://" + AUTHORITY);
    public final static String BASE_CONTENIDOS = "begu.";
    public final static String TIPO_CONTENIDO = "vnd.android.cursor.dir/vnd." + BASE_CONTENIDOS;
    public final static String TIPO_CONTENIDO_ITEM = "vnd.android.cursor.item/vnd." + BASE_CONTENIDOS;
    public final static String RUTA_ALUMNOS = "EDU_ALUMNOS";
    public final static String RUTA_TARJETAS = "BEGU_TARJETAS";
    public final static String RUTA_FOTOS = "EDU_FOTOS";
    public final static String RUTA_DESCARGAS = "DESCARGA_FOTOS";
    public final static String RUTA_CONSUMOS = "BEGU_CONSUMO";

    interface ColumnasAlumno{
        public final static String DNI = "DNI";
        public final static String NOMBRE = "NOMBRE";
        public final static String APELLIDO = "APELLIDO";
        public final static String GENERO = "GENERO";

        public final static String ID_REMOTA = "id_remota";
    }

    interface ColumnasTarjeta{
        public final static String NUM_TARJETAS = "NUM_TARJETAS";
        public final static String DNI = "DNI";
        public final static String FECHA = "FECHA";
        public final static String CREDITO_TOTAL = "CREDITO_TOTAL";
        public final static String CREDITO_USADO = "CREDITO_USADO";
        public final static String CREDITO_TEMPORAL = "CREDITO_TEMPORAL";
        public final static String BORRADO = "BORRADO";
    }

    interface ColumnasFoto{
        public final static String DNI = "DNI";
        public final static String FOTO = "FOTO";
    }

    interface ColumnasDescargas{
        public final static String DNI = "DNI";
        public final static String FECHA_SOLIC = "FECHA_SOLIC";
        public final static String SYNC = "SYNC";
    }

    interface ColumnasConsumos{
        public final static String ID_CONSUMO = "_id";
        public final static String NUM_TARJETA = "NUM_TARJETA";
        public final static String FECHA = "FECHA";
        public final static String GPS_LATITUD = "GPS_LATITUD";
        public final static String GPS_LONGITUD = "GPS_LONGITUD";
        public final static String ID_EMPRESA = "ID_EMPRESA";
        public final static String SYNC = "SYNC";
    }

    public static String generarMime(String id){
        if (id != null){
            return TIPO_CONTENIDO + id;
        }else {
            return null;
        }
    }

    public static String generarMimeItem(String id){
        if(id != null){
            return TIPO_CONTENIDO_ITEM + id;
        }else {
            return null;
        }
    }

    /*
    * (*) Las siguientes clases definen las Uris para obtener los datos
    * */

    public static class Alumnos implements ColumnasAlumno{

        public static final Uri URI_CONTENIDO =
                URI_BASE.buildUpon().appendPath(RUTA_ALUMNOS).build();

        public static final String PARAMETRO_FILTRO = "DNI";

        public final static String DNI = "DNI";
        public final static String NOMBRE = "NOMBRE";
        public final static String APELLIDO = "APELLIDO";
        public final static String GENERO = "GENERO";

        public final static String ID_REMOTA = "id_remota";

        public static String obtenerDniAlumno(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static Uri crearUriAlumno(String dni){
            return URI_CONTENIDO.buildUpon().appendPath(dni).build();
        }

        public static boolean tieneFiltro(Uri uri){
            return uri != null && uri.getQueryParameter(PARAMETRO_FILTRO) != null;
        }
    }

    public static class Tarjetas implements ColumnasTarjeta {
        public static final Uri URI_CONTENIDO =
                URI_BASE.buildUpon().appendPath(RUTA_TARJETAS).build();

        public static String obtenerNumeroTarjeta(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static Uri crearUriTarjeta(String numTarjeta){
            return URI_CONTENIDO.buildUpon().appendPath(numTarjeta).build();
        }
    }

    public static class Fotos implements ColumnasFoto {
        public static final Uri URI_CONTENIDO =
                URI_BASE.buildUpon().appendPath(RUTA_FOTOS).build();

        public static String obtenerDniFoto(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static Uri crearUriFoto(String dni){
            return URI_CONTENIDO.buildUpon().appendPath(dni).build();
        }
    }

    public static class DescargaFotos implements ColumnasDescargas{
        public static final Uri URI_CONTENIDO =
                URI_BASE.buildUpon().appendPath(RUTA_DESCARGAS).build();

        public static String obtenerDescargafoto(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static Uri crearUriDescargafoto(String dni){
            return URI_CONTENIDO.buildUpon().appendPath(dni).build();
        }
    }

    public static class Consumos implements ColumnasConsumos{
        public static final Uri URI_CONTENIDO =
                URI_BASE.buildUpon().appendPath(RUTA_CONSUMOS).build();

        public static String obtenerConsumos(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static String obtenerSincronizar(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static Uri crearUriConsumos(String id){
            return URI_CONTENIDO.buildUpon().appendPath(id).build();
        }

        public static Uri crearUriSincronizar(){
            return URI_CONTENIDO.buildUpon().appendPath("sincronizar").build();
        }
    }
}
