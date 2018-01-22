package martin.compras.de.lista.app.com.begu.utils;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

/**
 * Created by Tinch on 27/4/2017.
 */

public class MyObserver extends ContentObserver {
    public MyObserver(Handler handler){
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
        this.onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
    }
}
