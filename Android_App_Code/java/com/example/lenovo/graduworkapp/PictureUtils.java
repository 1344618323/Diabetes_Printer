package com.example.lenovo.graduworkapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

/**
 * Created by Lenovo on 2018/3/11.
 */

public class PictureUtils {
    public static Bitmap getScaledBitmap(Activity activity, int id, int destWidth, int destHeight) {
        if (destHeight == 0 || destWidth == 0) {
            Point size = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(size);
            return getScaledBitmap(activity, id, size.x, size.y);
        } else {
            //Read int the dimensions of the image on desk
            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inJustDecodeBounds = true;
            /**If set to true, the decoder will return null (no bitmap), but the out... fields will still be set,
             *allowing the caller to query the bitmap without having to allocate the memory for its pixels.
             */

            BitmapFactory.decodeResource(activity.getResources(), id, options);
            float srcWidth = options.outWidth;
            float srcHeight = options.outHeight;

            //figure out how much to scale down by
            int inSampleSize = 1;
            if (srcHeight > destHeight || srcWidth > destWidth) {
                float heightScale = srcHeight / destHeight;
                float widthScale = srcWidth / destWidth;
                inSampleSize = Math.round(heightScale > widthScale ? heightScale : widthScale);
            }
            options = new BitmapFactory.Options();
            options.inSampleSize = inSampleSize;
            //Read in and create final bitmap
            return BitmapFactory.decodeResource(activity.getResources(), id, options);
        }
    }

    public static Bitmap getScaledBitmap(Activity activity, int id) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return getScaledBitmap(activity, id, size.x, size.y);
    }

    public static int dp2px(Context context, float dpValue){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale+0.5f);
    }
}