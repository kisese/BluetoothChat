package kisese.com.bluetoothchat;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Brayo on 5/13/2016.
 */
public class BTName {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;
    int PRIVATE_MODE = 0;

    private static final String SHARED_PREFER_FILE_NAME = "bluetooth_name";

    public BTName(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(SHARED_PREFER_FILE_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setName(String bt_name) {
        editor.putString("bt_name", bt_name);
        editor.commit();
    }



    public String getName(){
        return pref.getString("bt_name", null);
    }
}
