package martin.compras.de.lista.app.com.begu.web;

/**
 * Created by Tinch on 5/3/2017.
 */

public class Usuario {
    public String ID;
    public String USUARIO;
    public String PASSWORD;
    public String ROL;
    public String CUIT_EMPRESA;
    public String UNIDAD_TRANSPORTE;


    public Usuario(String id, String usuario, String password, String cuit, String empresa, String rol){
        this.ID = id;
        this.USUARIO = usuario;
        this.PASSWORD = password;
        this.ROL = rol;
        this.CUIT_EMPRESA = cuit;
        this.UNIDAD_TRANSPORTE = empresa;
    }
}
