package com.facesdk;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class FaceSDKNative {
    static final String TAG = "FaceSDKNative";
    //SDK初始化
    public native boolean FaceDetectionModelInit(String faceDetectionModelPath);

    //SDK人脸检测接口
    public native int[] FaceDetect(byte[] imageDate, int imageWidth , int imageHeight, int imageChannel);

    //SDK销毁
    public native boolean FaceDetectionModelUnInit();

    //按照人脸框截取图片
    public Bitmap CropImage(Bitmap bmp, int rect_x, int rect_y, int rect_w, int rect_h) {

        Bitmap cropBmp = null;

        try {
            int img_w = bmp.getWidth();
            int img_h = bmp.getHeight();
            int rect_x_t = ( (rect_x-rect_w/4) > 0 ) ? (rect_x-rect_w/4) : rect_x;
            int rect_y_t = ( (rect_y-rect_h/4) > 0 ) ? (rect_y-rect_h/4) : rect_y;
            int rect_w_t = (rect_w+rect_w/2) < img_w ? (rect_w+rect_w/2) : rect_w;
            int rect_h_t = (rect_h+rect_h/2) < img_h ? (rect_h+rect_h/2) : rect_h;
            rect_w_t  = (rect_w_t+rect_w/4) < img_w ? rect_w_t : rect_w;
            rect_h_t  = (rect_h_t+rect_h/4) < img_h ? rect_h_t : rect_h;

            if ( rect_x_t != rect_x && rect_y_t != rect_y && rect_w_t != rect_w && rect_h_t != rect_h) {
                cropBmp = Bitmap.createBitmap(bmp, rect_x_t, rect_y_t, rect_w_t, rect_h_t);
            } else {
                cropBmp = Bitmap.createBitmap(bmp, rect_x, rect_y, rect_w, rect_h);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return cropBmp;
    }

    //Base64 编码
    public String EncodeBase64(Bitmap bmp) {
        ByteArrayOutputStream byteArrayOutputStream = null;
        String encoded = null;

        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return encoded;
    }

    //just for debug
    public void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/facesdk_saved_images");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static {
        System.loadLibrary("facedetect");
    }
}
