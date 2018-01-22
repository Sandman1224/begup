package martin.compras.de.lista.app.com.begu.web;

/**
 * Created by Tinch on 16/3/2017.
 */

public class Alumno {
    public String DNI;
    public String NOMBRE;
    public String APELLIDO;
    public String GENERO;

    public Alumno(String dni, String nombre, String apellido, String genero){
        this.DNI = dni;
        this.NOMBRE = nombre;
        this.APELLIDO = apellido;
        this.GENERO = genero;
    }
}
