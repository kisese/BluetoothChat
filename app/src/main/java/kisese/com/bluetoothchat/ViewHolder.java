package kisese.com.bluetoothchat;

import android.widget.ImageView;

import com.github.library.bubbleview.BubbleTextVew;

/**
 * Created by Brayo on 5/11/2016.
 */
public class ViewHolder{
    BubbleTextVew textVew;
    ImageView imageView;

    public ViewHolder(BubbleTextVew textVew, ImageView imageView) {
        this.textVew = textVew;
        this.imageView = imageView;
    }

    public BubbleTextVew getTextVew() {
        return textVew;
    }

    public void setTextVew(BubbleTextVew textVew) {
        this.textVew = textVew;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }
}
