package kisese.com.bluetoothchat;

import android.graphics.Bitmap;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageBox {

    private String sender;
    private String message;
    private Bitmap image;
    private Date time;
    private byte[] video;

    private boolean self;
    private boolean isImage;
    private boolean isVideo;

    public MessageBox(String sender, String message, Date time, boolean self) {
        this.sender = sender;
        this.message = message;
        this.time = time;
        this.self = self;
        this.isImage = false;
        this.isVideo = false;
    }

    public MessageBox(String sender, Bitmap image, Date time, boolean self) {
        this(sender, "", time, self);
        this.image = image;
        this.isImage = true;
        this.isVideo = false;
    }

    public MessageBox(String sender, byte[] video, Date time, boolean self) {
        this(sender, "", time, self);
        this.video = video;
        this.isImage = false;
        this.isVideo = true;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getTime() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("hh:mm");
        return dateFormatter.format(time);
    }

    public boolean isSelf() {
        return self;
    }

    public boolean isImage() {
        return isImage;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public byte[] getVideo() {
        return video;
    }
}
