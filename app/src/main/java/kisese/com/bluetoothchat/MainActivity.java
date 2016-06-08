package kisese.com.bluetoothchat;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;

public class MainActivity extends AppCompatActivity {

    BootstrapButton host, join, chat;
    public static final String UUID = "28286a80-137b-11e4-bbe8-0002a5d5c51b";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        join = (BootstrapButton)findViewById(R.id.join_button);
        host = (BootstrapButton)findViewById(R.id.host_button);

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ClientActivity.class);
                startActivity(i);
            }
        });


        host.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, HostActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String username = sharedPref.getString("username", bluetoothAdapter.getName());
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        //save Name
        BTName btName = new BTName(MainActivity.this);
        btName.setName(username);

        Log.e("BTName", username);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit_name) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String username = sharedPref.getString("username", bluetoothAdapter.getName());
            final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            //save Name
            BTName btName = new BTName(MainActivity.this);
            btName.setName(username);

            Log.e("BTName", username);

            final EditText nameInput = new EditText(this);
            nameInput.setSingleLine();
            nameInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
            nameInput.setText(username);
            nameInput.setSelectAllOnFocus(true);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Enter your username");
            builder.setView(nameInput);
            builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    imm.hideSoftInputFromWindow(nameInput.getWindowToken(), 0);
                    sharedPref.edit().putString("username", nameInput.getText().toString()).apply();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    imm.hideSoftInputFromWindow(nameInput.getWindowToken(), 0);
                }
            });

            final AlertDialog dialog = builder.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

            nameInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    if (charSequence.length() > 0 && charSequence.length() <= 22) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    } else {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {}
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device, now exiting.", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
