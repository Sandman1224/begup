package martin.compras.de.lista.app.com.begu.web;

/**
 * Created by Tinch on 3/4/2017.
 */

public class Tarjeta {
    public String KEY;
    public String DNI;
    public String FECHA;
    public String CREDITO_TOTAL;
    public String CREDITO_USADO;
    public String CREDITO_TEMPORAL;
    public String BORRADO;

    public Tarjeta(String key, String dni, String fecha, String credito_total, String credito_usado, String credito_temporal, String borrado){
        KEY = key;
        DNI = dni;
        FECHA = fecha;
        CREDITO_TOTAL = credito_total;
        CREDITO_USADO = credito_usado;
        CREDITO_TEMPORAL = credito_temporal;
        BORRADO = borrado;
    }
}
