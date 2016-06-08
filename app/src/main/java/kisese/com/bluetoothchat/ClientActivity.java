package kisese.com.bluetoothchat;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ClientActivity extends AppCompatActivity {


    public static final int PICK_IMAGE = 1;
    public static final int PICK_VIDEO = 2;

    public static ArrayList<Integer> acceptableDevices = new ArrayList<Integer>();
    private EditText mMessage;
    private ProgressDialog mProgressDialog;
    private String mUsername;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mSocket;
    private ChatManager mChatManager;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int deviceClass = device.getBluetoothClass().getDeviceClass();

                if (acceptableDevices.contains(deviceClass)) {
                    new ConnectThread(device).start();
                }
            }
        }
    };
    private String selectedVideoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//        mUsername = sharedPref.getString("username", mBluetoothAdapter.getName());
        BTName btName = new BTName(this);
        mUsername = btName.getName();

        final ActionBar actionBar = ((ClientActivity)this).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mUsername);
        }

        acceptableDevices.add(BluetoothClass.Device.COMPUTER_HANDHELD_PC_PDA);
        acceptableDevices.add(BluetoothClass.Device.COMPUTER_PALM_SIZE_PC_PDA);
        acceptableDevices.add(BluetoothClass.Device.PHONE_SMART);
        getWindow().setFormat(PixelFormat.UNKNOWN);

        Button mAttachButton = (Button) findViewById(R.id.attach);
        Button mSendButton = (Button) findViewById(R.id.send);
        mMessage = (EditText) findViewById(R.id.message);
        mChatManager = new ChatManager(this, false);
        mProgressDialog = new ProgressDialog(this);

        mAttachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadAttachment();
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        mProgressDialog.setMessage("Looking for ChatRoom...");
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
            }
        });
        mProgressDialog.show();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        startDeviceSearch();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        } else if (id == R.id.action_video) {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, PICK_VIDEO);
        }
        return super.onOptionsItemSelected(item);
    }

    private void startDeviceSearch() {
        mBluetoothAdapter.enable();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        new WaitForBluetoothThread().start();
    }

    private void sendMessage() {

        if (mMessage.getText().toString().length() == 0) {
            return;
        }

        new Thread(new Runnable() {
            public void run() {
                try {
                    byte[] byteArray;
                    String message = mMessage.getText().toString();
                    message = message + " - <small>" + mUsername + "</small>";
                    byte[] messageBytes = message.getBytes();
                    Log.e("Message ", mMessage.getText().toString());
                    byteArray = mChatManager.buildPacket(
                            ChatManager.MESSAGE_SEND,
                            mUsername,
                            messageBytes
                    );
                    mChatManager.writeMessage(byteArray);
                } catch (Exception e) {
                    return;
                }
            }
        }).start();

        mMessage.setText("");
    }

    private void uploadAttachment() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("Video Path", requestCode + " ");
        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                Uri image = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(image, filePathColumn, null, null, null);

                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);

                new SendImageThread(picturePath).start();
                cursor.close();
            }
        } else if (requestCode == PICK_VIDEO) {
            if (resultCode == RESULT_OK) {
                selectedVideoPath = getPath(data.getData());
                try {
                    if (selectedVideoPath == null) {
                        finish();
                    } else {

                        Log.e("Video Path", selectedVideoPath);
                        /**
                         * try to do something there
                         * selectedVideoPath is path to the selected video
                         */
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    String strFile = null;
                                    File file = new File(selectedVideoPath);
                                    byte[] data_ = FileUtils.readFileToByteArray(file);//Convert any file, image or video into byte array

                                    strFile = Base64.encodeToString(data_, Base64.NO_WRAP);
                                    try {
                                        System.err.println("Sending video");
                                        byte[] packet = mChatManager.buildPacket(
                                                ChatManager.MESSAGE_SEND_VIDEO,
                                                mUsername,
                                                data_
                                        );
                                        mChatManager.writeMessage(packet);
                                    } catch (Exception e) {
                                        System.err.println("Failed to send video");
                                        System.err.println(e.toString());
                                    }
                                } catch (IOException e) {
                                    //#debug
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                    }
                } catch (Exception e) {
                    //#debug
                    e.printStackTrace();
                }
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);

        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                System.err.println("Failed to close socket");
                System.err.println(e.toString());
            }
        }

        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }

        mBluetoothAdapter.cancelDiscovery();
    }

    private void manageSocket(BluetoothSocket socket) {
        mSocket = socket;
        mChatManager.startConnection(socket, mProgressDialog);
    }

    private class WaitForBluetoothThread extends Thread {

        public void run() {
            while (true) {
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.startDiscovery();
                    break;
                }
            }
        }

    }

    private class SendImageThread extends Thread {

        private Bitmap bitmap;

        public SendImageThread(String picturePath) {
            this.bitmap = BitmapFactory.decodeFile(picturePath);
        }

        public void run() {
            if (bitmap == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), "Image is incompatible or not locally stored", Toast.LENGTH_SHORT).show();
                    }
                });

                return;
            }

            if (bitmap.getWidth() > 1024 || bitmap.getHeight() > 1024) {
                float scalingFactor;

                if (bitmap.getWidth() >= bitmap.getHeight()) {
                    scalingFactor = 1024f / bitmap.getWidth();
                } else {
                    Matrix fixRotation = new Matrix();
                    fixRotation.postRotate(90);
                    scalingFactor = 1024f / bitmap.getHeight();
                }

                bitmap = Bitmap.createScaledBitmap(
                        bitmap,
                        (int) (bitmap.getWidth() * scalingFactor),
                        (int) (bitmap.getHeight() * scalingFactor),
                        false
                );
            }

            try {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 15, output);
                byte[] imageBytes = output.toByteArray();
                byte[] packet = mChatManager.buildPacket(
                        ChatManager.MESSAGE_SEND_IMAGE,
                        mUsername,
                        imageBytes
                );
                mChatManager.writeMessage(packet);
            } catch (Exception e) {
                System.err.println("Failed to send image");
                System.err.println(e.toString());
            }
        }

    }

    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(
                        java.util.UUID.fromString(MainActivity.UUID));
            } catch (Exception e) {
                System.err.println("Failed to connect");
                System.err.println(e.toString());
            }

            mmSocket = tmp;
        }

        public void run() {
            try {
                mmSocket.connect();
            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    return;
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    manageSocket(mmSocket);
                }
            });
        }

    }

}
