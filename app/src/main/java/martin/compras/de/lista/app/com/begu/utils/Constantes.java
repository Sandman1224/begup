package martin.compras.de.lista.app.com.begu.utils;

/**
 * Created by Tinch on 6/3/2017.
 */

public class Constantes {
    /**
     * Puerto que utilizas para la conexión.
     * Dejalo en blanco si no has configurado esta característica.
     */
    private static final String PUERTO_HOST_IIS = ":81";

    /**
     * Dirección IP de genymotion o AVD
     */
    private static final String IP = "http://192.168.1.106";
    private static final String DIRECCION_WEB = "http://begup.jujuy.gob.ar";

    /**
     * URLs del Web Service
     */
    public static final String GET_URL_ALUMNOS_CATCH = DIRECCION_WEB + PUERTO_HOST_IIS + "/ws/inialumno";
    public static final String GET_URL_ALUMNOS_SYNCID = DIRECCION_WEB + PUERTO_HOST_IIS + "/ws/alumno";

    public static final String GET_URL_TARJETAS_CATCH = DIRECCION_WEB + PUERTO_HOST_IIS + "/ws/initarjeta";
    public static final String TARJETAS_UPDATE = DIRECCION_WEB + PUERTO_HOST_IIS + "/ws/tarjetaupdate";
    public static final String TARJETAS_BAJA = DIRECCION_WEB + PUERTO_HOST_IIS + "/ws/tarjetabaja";

    public static final String GET_URL_FOTOS = DIRECCION_WEB + PUERTO_HOST_IIS + "/ws/fotosbegup";
    public static final String INSERT_CONSUMOS = DIRECCION_WEB + PUERTO_HOST_IIS + "/ws/consumobegup/";

    /**
     * Tipo de cuenta para la sincronización
     */
    public static final String ACCOUNT_TYPE = "martin.compras.de.lista.app.com.begu.account";

    /**
     * Constantes para SINCRONIZACIÓN de datos
     */
    public static final int EN_SINCRONIZACION = 0;
    public static final int SINCRONIZADO = 1;

    public static final String PREFERENCIAS = "preferencias";
    public static final String PREF_SYNCALUMNOS = "prefSyncalumnos";
    public static final String PREF_SYNCTARJETAS = "prefSyncalumnos";
    public static final String PREF_FECHAACTUALIZACION = "prefFechaactualizacion";

    /**
     * Parámetros de consultas al servidor
     */
    public static final int MY_SOCKET_TIMEOUT_MS = 60;          //5 Minutos - Segundos de espera de la solicitud
    public static final int NUM_MAX_RETRIES = 5;

    /**
     * Tablas de la BD
     */
    //TARJETAS
    public static final int TARJETASid = 0;
    public static final int TARJETASnum_tarjetas = 1;
    public static final int TARJETASdni = 2;
    public static final int TARJETASfecha = 3;
    public static final int TARJETAScreditototal = 4;
    public static final int TARJETAScreditousado = 5;
    public static final int TARJETAScreditotemporal = 6;
    public static final int TARJETASborrado = 7;
}
