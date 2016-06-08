package kisese.com.bluetoothchat;

import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.library.bubbleview.BubbleTextVew;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class MessageFeedAdapter extends ArrayAdapter<MessageBox> {

    private final String fullPath;
    Context mContext;
    ClipboardManager clipboard;
    ArrayList<MessageBox> messages;
    int TYPE_SELF = 1;
    int TYPE_NOT_SELF = 2;
    int TYPE_IMAGE = 3;
    int TYPE_VIDEO = 4;
    private MessageBox message;
    private byte[] video;
    private final static String PATH_SD_CARD = "/BluetoothChat/";
    private final static String THUMBNAIL_PATH_SD_CARD = "video";

    public MessageFeedAdapter(Context context, ArrayList<MessageBox> messages) {
        super(context, R.layout.message_left, messages);
        this.messages = messages;
        mContext = context;
        fullPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + PATH_SD_CARD + THUMBNAIL_PATH_SD_CARD;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        message = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.message_list, parent, false);
        }

        BubbleTextVew textVew = (BubbleTextVew) convertView.findViewById(R.id.message_text);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.image_ms);

        if ((message.isSelf()) && (!message.isImage()) && (!message.isVideo())) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 1.0f;
            params.gravity = Gravity.RIGHT;
            imageView.setVisibility(View.GONE);
            textVew.setLayoutParams(params);
            textVew.setVisibility(View.VISIBLE);
        } else if ((!message.isSelf()) && (!message.isImage()) && (!message.isVideo())) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 1.0f;
            params.gravity = Gravity.LEFT;
            imageView.setVisibility(View.GONE);
            textVew.setVisibility(View.VISIBLE);
            textVew.setLayoutParams(params);
        } else if (message.isImage()) {
            textVew.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            Log.e("Dunno", "Dunno");
            imageView.setImageBitmap(message.getImage());
        } else if (message.isVideo()) {
            textVew.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
        }

        if (!message.isImage() && (!message.isVideo())) {
            textVew.setText(Html.fromHtml(message.getMessage()));
            imageView.setImageDrawable(null);
        } else {
            textVew.setText("");
            Log.e("Dunno", "Dunno");
            if (message.isImage()) {
                Log.e("Image", "Image");
                imageView.setImageBitmap(message.getImage());
            } else {
                Log.e("Video", "Video");
                //
                imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_video));

                saveFile(message.getVideo());
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mContext, VideoPlayerActivity.class);
                        i.putExtra("video", fullPath + "/Video.mp4");
                        mContext.startActivity(i);
                    }
                });
            }
        }

        if(message.isImage())
            Toast.makeText(mContext, "Video sent from, may take a few seconds " + message.getSender(), Toast.LENGTH_LONG).show();

        return convertView;
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID},
                MediaStore.Video.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Video.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    public void saveFile(byte[] mp3SoundByteArray) {
        try {
            Log.e("Video Status", "Saving Video");


            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File tempMp3 = new File(fullPath, "Video.mp4");
            tempMp3.createNewFile();
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
