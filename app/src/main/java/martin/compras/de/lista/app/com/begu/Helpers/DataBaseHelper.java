package martin.compras.de.lista.app.com.begu.Helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Tinch on 8/2/2017.
 */

public class DataBaseHelper extends SQLiteOpenHelper {
    private static String DB_PATH = "/data/data/martin.compras.de.lista.app.com.begu/databases/";
    private static String DB_NAME = "begupDb.sqlite";

    private SQLiteDatabase myDatabase;
    private final Context myContext;

    public interface Tablas{
        String EDU_ALUMNOS = "EDU_ALUMNOS";
        String BEGU_TARJETAS = "BEGU_TARJETAS";
        String EDU_FOTOS = "EDU_FOTOS";
        String DESCARGA_FOTOS = "DESCARGA_FOTOS";
        String USUARIOS = "USUARIOS";
        String BEGU_CONSUMO = "BEGU_CONSUMO";
    }

    public DataBaseHelper(Context context){
        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    public void createDatabase() throws IOException{
        boolean dbExist = checkDatabase();
        SQLiteDatabase db_read = null;

        if (dbExist){

        }else {
            db_read = this.getReadableDatabase();
            db_read.close();

            try{
                copyDataBase();
            }catch (IOException e){
                throw new Error("Error copiando base de datos");
            }
        }
    }

    public boolean checkDatabase(){
        SQLiteDatabase checkDB = null;

        String myPath = DB_PATH + DB_NAME;

        try{
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        }catch (Exception e){
            File dbFile = new File(DB_PATH + DB_NAME);
            return dbFile.exists();
        }

        if (checkDB != null){
            checkDB.close();
        }

        return checkDB != null ? true : false;
    }

    public void copyDataBase()throws IOException{
        InputStream myInput = myContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int lenght;

        while ((lenght = myInput.read(buffer)) != -1){
            if (lenght > 0){
                myOutput.write(buffer, 0, lenght);
            }
        }

        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void openDataBase() throws SQLException{
        String myPath = DB_PATH + DB_NAME;
        myDatabase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    public synchronized void close(){
        if (myDatabase != null)
            myDatabase.close();

        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //CODIGO PARA CREAR LA BD
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        try{
            createDatabase();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public Cursor fetchLogin(String usuario, String password) throws SQLException{
        Cursor cursor = myDatabase.rawQuery("SELECT * FROM USUARIOS WHERE USUARIO = ? AND PASSWORD = ?", new String[]{usuario, password});

        if (cursor != null)
            cursor.moveToFirst();

        return cursor;
    }

    public Cursor fetchTarjeta(String numeroTarjeta) throws SQLException{
        Cursor cursor = myDatabase.rawQuery(
                "SELECT _id, NUM_TARJETAS, DNI, CREDITO_TOTAL, CREDITO_USADO, CREDITO_TEMPORAL " +
                "from BEGU_TARJETAS " +
                "where (NUM_TARJETAS = ?)", new String[]{numeroTarjeta});

        if (cursor != null)
            cursor.moveToFirst();

        return cursor;
    }

    public Cursor fetchAlumno(String dni) throws SQLException{
        Cursor cursor = myDatabase.rawQuery(
                "SELECT DNI, NOMBRE, APELLIDO, GENERO " +
                        "FROM EDU_ALUMNOS " +
                        "WHERE (DNI = ?)", new String[]{dni}
        );

        if (cursor != null)
            cursor.moveToFirst();

        return cursor;
    }

    public Cursor fetchFoto(String dni) throws SQLException{
        Cursor cursor = myDatabase.rawQuery(
                "SELECT DNI, FOTO " +
                        "FROM EDU_FOTOS " +
                        "WHERE (DNI = ?)", new String[]{dni}
        );

        if (cursor != null)
            cursor.moveToFirst();

        return cursor;
    }

    public Cursor fetchDescarga(String dni) throws SQLException{
        Cursor cursor = myDatabase.rawQuery(
                "SELECT DNI, FECHA_SOLIC, SYNC " +
                        "FROM DESCARGA_FOTOS " +
                        "WHERE (DNI = ?)", new String[]{dni}
        );

        if (cursor != null){
            cursor.moveToFirst();
        }

        return cursor;
    }
}
