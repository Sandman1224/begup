package martin.compras.de.lista.app.com.begu.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

/**
 * Created by Tinch on 1/2/2018.
 */

public class PermisosDialogFragment extends DialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState){
        String titulo = getArguments().getString("titulo");
        String mensaje = getArguments().getString("mensaje");
        final String pack = getArguments().getString("package");
        final int requestAppSetting = getArguments().getInt("requestAppSetting");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNeutralButton("Otorgar Permisos", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent appSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + pack));
                        appSettings.addCategory(Intent.CATEGORY_DEFAULT);
                        appSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivityForResult(appSettings, requestAppSetting);

                    }
                });

        return builder.create();
    }
}
