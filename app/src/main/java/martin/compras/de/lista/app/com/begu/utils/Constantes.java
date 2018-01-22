package martin.compras.de.lista.app.com.begu.utils;

/**
 * Created by Tinch on 6/3/2017.
 */

public class Constantes {
    /**
     * Puerto que utilizas para la conexión.
     * Dejalo en blanco si no has configurado esta característica.
     */
    private static final String PUERTO_HOST = ":8080";
    private static final String PUERTO_HOST_IIS = ":81";

    /**
     * Dirección IP de genymotion o AVD
     */
    private static final String IP = "http://192.168.1.106";
    private static final String DIRECCION_WEB = "http://begup2.jujuy.gob.ar";

    /**
     * URLs del Web Service
     */
    public static final String GET_URL_ALUMNOS_CATCH = DIRECCION_WEB + PUERTO_HOST_IIS + "/ws/inialumno";
    public static final String GET_URL_ALUMNOS_SYNCID = DIRECCION_WEB + PUERTO_HOST_IIS + "/ws/alumno";

    public static final String GET_URL_TARJETAS_CATCH = DIRECCION_WEB + PUERTO_HOST_IIS + "/ws/initarjeta";
    public static final String GET_URL_TARJETAS_SYNCID = DIRECCION_WEB + PUERTO_HOST_IIS + "/ws/tarjeta";

    public static final String GET_URL_FOTOS = DIRECCION_WEB + PUERTO_HOST_IIS + "/ws/fotosbegup";
    public static final String INSERT_CONSUMOS = DIRECCION_WEB + PUERTO_HOST_IIS + "/ws/consumobegup/";

    /**
     * Campos de las respuestas Json
     */
    public static final String ID_USUARIO = "idGasto";
    public static final String ESTADO = "estado";
    public static final String USUARIOS = "datos";
    public static final String DATOS = "datos";
    public static final String MENSAJE = "mensaje";

    /**
     * Códigos del campo {@link ESTADO}
     */
    public static final String SUCCESS = "1";
    public static final String FAILED = "2";

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
    public static final int MY_SOCKET_TIMEOUT_MS = 300000;          //5 Minutos - Segundos de espera de la solicitud
}
