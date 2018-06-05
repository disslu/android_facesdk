package com.facesdk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.json.JSONException;
import org.json.JSONObject;

//import com.google.android.gms.appindexing.AppIndex;
//import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends AppCompatActivity {
    private static final int SELECT_IMAGE = 1;
    static final String TAG = "MainActivity";

    private TextView infoResult;
    private ImageView imageView;
    private Bitmap yourSelectedImage = null;
    private static boolean sdk_init_ok = false;

    private FaceSDKNative faceSDKNative = new FaceSDKNative();
    private FaceRecognizer mFaceRecognizer = new FaceRecognizer();
    private static String mUrl = "http://172.0.0.1:8031/identify";
    private static String mToken = "xxxxxx";
    private static String mGroup=  "test001";

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    //private GoogleApiClient client;

    //Check Permissions
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
    };


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        infoResult = (TextView) findViewById(R.id.infoResult);
        imageView = (ImageView) findViewById(R.id.imageView);

        Button buttonImage = (Button) findViewById(R.id.buttonImage);
        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, SELECT_IMAGE);
            }
        });

        //config recognize server
        mFaceRecognizer.setUrl(mUrl);
        mFaceRecognizer.setToken(mToken);
        mFaceRecognizer.setGroup(mGroup);


        Button buttonDetect = (Button) findViewById(R.id.buttonDetect);
        buttonDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (yourSelectedImage == null) {
                    infoResult.setText("no image found");
                    return;
                }
                int width = yourSelectedImage.getWidth();
                int height = yourSelectedImage.getHeight();
                byte[] imageDate = getPixelsRGBA(yourSelectedImage);

                long timeDetectFace = System.currentTimeMillis();
                //do FaceDetect
                int faceInfo[] =  faceSDKNative.FaceDetect(imageDate, width, height,4);
                timeDetectFace = System.currentTimeMillis() - timeDetectFace;

                //Get Results
               if (faceInfo!=null && faceInfo.length>1) {
                   int faceNum = faceInfo[0];
                   String show_text = "detect time："+timeDetectFace+"ms,   face number：" + faceNum;
                   Log.i(TAG, "detect take time："+timeDetectFace);
                   Log.i(TAG, "face num：" + faceNum );

                   Bitmap drawBitmap = yourSelectedImage.copy(Bitmap.Config.ARGB_8888, true);
                   for (int i=0; i<faceNum; i++) {
                       int mDetectScore; //face detect score
                       int left, top, right, bottom;//face rect
                       Canvas canvas = new Canvas(drawBitmap);
                       Paint paint = new Paint();
                       mDetectScore = faceInfo[1+15*i]/10000;
                       left = faceInfo[2+15*i];
                       top = faceInfo[3+15*i];
                       right = faceInfo[4+15*i];
                       bottom = faceInfo[5+15*i];
                       paint.setColor(Color.RED);
                       paint.setStyle(Paint.Style.STROKE);
                       paint.setStrokeWidth(5);
                       paint.setTextSize(40);
                       //crop image
                       long timeDetectFace_c_s = System.currentTimeMillis();
                       Bitmap cropBmp = faceSDKNative.CropImage(drawBitmap, left, top, right-left, bottom-top);
                       long timeDetectFace_c_e = System.currentTimeMillis();
                       Log.i(TAG, "crop take time："+ (timeDetectFace_c_e - timeDetectFace_c_s));

                       //just for debug
                       //faceSDKNative.SaveImage(cropBmp);

                       //encode img to base64
                       long timeDetectFace_e_s = System.currentTimeMillis();
                       String encodeImg = faceSDKNative.EncodeBase64(cropBmp);
                       long timeDetectFace_e_e = System.currentTimeMillis();
                       Log.i(TAG, " encode take time："+ (timeDetectFace_e_e - timeDetectFace_e_s));

                       long timeDetectFace_r_s = System.currentTimeMillis();
                       mFaceRecognizer.recognize(
                               encodeImg,
                              new FaceRecognizer.RecognizerCallback() {
                                   @Override
                                   public void onRespond(String response) {
                                       long timeDetectFace_r_e = System.currentTimeMillis();
                                       Log.i(TAG, " recognize take time："+ (timeDetectFace_r_e - timeDetectFace_r_s));
                                       Log.e(TAG, "onRespond: " + response);
                                       try {
                                           JSONObject jsonObject = new JSONObject(response);
                                           String mRecName = jsonObject.getString("user_name"); //recognize name
                                           double mRecScore = jsonObject.getDouble("score");    //recognize score
                                           Log.e(TAG, "onRespond: recognize Name :" + mRecName + " recognize Score: " + mRecScore);
                                       } catch (JSONException e) {
                                           Log.e(TAG, "onRespond Json parse: ", e);
                                       }
                                   }

                                   @Override
                                   public void onNetworkFail() {
                                       Log.e(TAG, "onNetworkFail: ");
                                   }
                               });

                       //Draw rect
                       canvas.drawRect(left, top, right, bottom, paint);
                       //canvas.drawText(mDetectScore, left, top, paint);

                       //Draw landmark
                       for (int j=0; j<5; j++) {
                           int pointX = faceInfo[6+j+15*i];
                           int pointY = faceInfo[6+j+5+15*i];
                           canvas.drawCircle(pointX, pointY, 2, paint);
                       }

                   }
                   infoResult.setText(show_text);
                   imageView.setImageBitmap(drawBitmap);
                }else{
                   infoResult.setText("no face found");
               }

            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Fail to request permission", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
            onPermissionGranted();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void onPermissionGranted() {
        copyModels();

        File sdDir = Environment.getExternalStorageDirectory();
        String sdPath = sdDir.toString() + "/facesdk/";
        sdk_init_ok = faceSDKNative.FaceDetectionModelInit(sdPath);
        faceSDKNative.SetThreadsNumber(1); //set with your CPU kernel number, range [1-8]
        faceSDKNative.SetMinFaceSize(160); //adjust with your Input Image Resolution Size, range [80-200]
        Log.d(TAG, "sdk init : "+ sdk_init_ok);
        if (!sdk_init_ok) {
            Toast.makeText(this, "SDK Native init failed, as cant't read model", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void copyModels() {
        try {
            copyBigDataToSD("det1.bin");
            copyBigDataToSD("det2.bin");
            copyBigDataToSD("det3.bin");
            copyBigDataToSD("det1.param");
            copyBigDataToSD("det2.param");
            copyBigDataToSD("det3.param");
        } catch (IOException e) {
            Log.e(TAG, "copyModels: ", e);
            Toast.makeText(this, "Copy model failed", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();

            try {
                if (requestCode == SELECT_IMAGE) {
                    Bitmap bitmap = decodeUri(selectedImage);

                    Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                    yourSelectedImage = rgba;

                    imageView.setImageBitmap(yourSelectedImage);
                }
            } catch (FileNotFoundException e) {
                Log.e("MainActivity", "FileNotFoundException");
                return;
            }
        }
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        //o.inSampleSize = 1;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to

        //// Find the correct scale value. It should be the power of 2.
        int scale = o.outHeight/1080;

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
    }

    private byte[] getPixelsRGBA(Bitmap image) {
        // calculate how many bytes our image consists of
        int bytes = image.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create a new buffer
        image.copyPixelsToBuffer(buffer); // Move the byte data to the buffer
        byte[] temp = buffer.array(); // Get the underlying array containing the

        return temp;
    }

    private void copyBigDataToSD(String strOutFileName) throws IOException {
        Log.i(TAG, "start copy file " + strOutFileName);
        File sdDir = Environment.getExternalStorageDirectory();//get root dir
        File file = new File(sdDir.toString()+"/facesdk/");
        if (!file.exists()) {
            file.mkdir();
        }

        String tmpFile = sdDir.toString()+"/facesdk/" + strOutFileName;
        File f = new File(tmpFile);
        if (f.exists()) {
            Log.i(TAG, "file exists " + strOutFileName);
            return;
        }
        InputStream myInput;
        java.io.OutputStream myOutput = new FileOutputStream(sdDir.toString()+"/facesdk/"+ strOutFileName);
        myInput = this.getAssets().open(strOutFileName);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
        Log.i(TAG, "end copy file " + strOutFileName);

    }

    private void checkPermissions() {
        boolean allPermissionGranted = true;
        for (String permission : PERMISSIONS) {
            int result = ActivityCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                allPermissionGranted = false;
                break;
            }
        }

        if (allPermissionGranted) {
            onPermissionGranted();
            return;
        }

        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

}
