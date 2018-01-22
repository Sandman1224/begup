package martin.compras.de.lista.app.com.begu.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import martin.compras.de.lista.app.com.begu.Activities.SyncActivity;
import martin.compras.de.lista.app.com.begu.R;

/**
 * Created by Tinch on 2/5/2017.
 */

public class AdaptadorSincronizacion extends RecyclerView.Adapter<AdaptadorSincronizacion.SincronizarViewHolder> {
    private Context context;
    ArrayList<SyncActivity.ListViewItem> items;

    public AdaptadorSincronizacion(Context context, ArrayList<SyncActivity.ListViewItem> items){
        this.context = context;
        this.items = items;
    }

    @Override
    public SincronizarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sync, parent, false);

        return new SincronizarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SincronizarViewHolder holder, int position) {
        holder.tv1.setText(items.get(position).getTitulo());

        String estado = items.get(position).getEstado();
        switch (estado){
            case "Sincronizando":
                holder.iv1.setImageResource(R.drawable.ic_sincronizar);
                break;
            case "Sincronizado":
                holder.iv1.setImageResource(R.drawable.ic_sync_realizado);
                break;
            case "error":
                holder.iv1.setImageResource(R.drawable.ic_sync_error);
                break;
        }
    }

    @Override
    public void onBindViewHolder(SincronizarViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class SincronizarViewHolder extends RecyclerView.ViewHolder{
        public TextView tv1;
        public ImageView iv1;

        public SincronizarViewHolder(View view){
            super(view);

            tv1 = (TextView) view.findViewById(R.id.tvTituloSincronizacion);
            iv1 = (ImageView) view.findViewById(R.id.ivIconoSincronizacion);
        }
    }

    public void swap(ArrayList<SyncActivity.ListViewItem> itemss, int index){
        ArrayList<SyncActivity.ListViewItem> datos = itemss;
        //items.clear();
        //items.addAll(datos);
        notifyDataSetChanged();
    }
}
