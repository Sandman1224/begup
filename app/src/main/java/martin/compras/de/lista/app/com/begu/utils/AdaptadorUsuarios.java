package martin.compras.de.lista.app.com.begu.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.Blob;

import martin.compras.de.lista.app.com.begu.R;

/**
 * Created by Tinch on 6/3/2017.
 */

public class AdaptadorUsuarios extends RecyclerView.Adapter<AdaptadorUsuarios.UsuarioViewHolder> {
    private Cursor cursor;
    private Context context;

    public static class UsuarioViewHolder extends RecyclerView.ViewHolder{
        public TextView tv1;
        public TextView tv2;
        public TextView tv3;
        //public ImageView ivFoto;

        public UsuarioViewHolder(View view){
            super(view);

            tv1 = (TextView) view.findViewById(R.id.iv1);
            tv2 = (TextView) view.findViewById(R.id.iv2);
            tv3 = (TextView) view.findViewById(R.id.iv3);
            //ivFoto = (ImageView) view.findViewById(R.id.ivFoto);

        }
    }

    public AdaptadorUsuarios(Context context){
        this.context = context;
    }

    @Override
    public int getItemCount() {
        if (cursor != null)
            return cursor.getCount();

        return 0;
    }

    @Override
    public UsuarioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false);

        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UsuarioViewHolder holder, int position) {
        cursor.moveToPosition(position);

        String leyenda1;
        String leyenda2;
        String leyenda3;
        //Bitmap foto;

        leyenda1 = cursor.getString(0);
        leyenda2 = cursor.getString(1);
        leyenda3 = cursor.getString(2);
        //cuit = cursor.getString(3);

        /*byte[] imgByte = cursor.getString(1);

        byte[] imgByte = Base64.decode(cursor.getString(1), Base64.DEFAULT);
        if (imgByte != null) {
            foto = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
            holder.ivFoto.setImageBitmap(foto);
        }
        */

        holder.tv1.setText(leyenda1);
        holder.tv2.setText(leyenda2);
        holder.tv3.setText(leyenda3);
        //holder.tvCuit.setText(cuit);
    }

    public void swapCursor(Cursor newCursor){
        cursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor(){
        return cursor;
    }
}
