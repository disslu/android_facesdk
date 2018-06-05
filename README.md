# SDK summary

* multi face detect
* face image crop
* face image encode
* face recognize



# SDK API

* Do FaceDetect

```
int faceInfo[] =  faceSDKNative.FaceDetect(imageDate, width, height, 4);

```
* Get FaceDetect results

```
int faceNum = faceInfo[0];//get face number

for (int i=0; i<faceNum; i++) {
    int score;                   //face score
    int left, top, right, bottom;//face rect
    score = faceInfo[1+15*i];
    left = faceInfo[2+15*i];
    top = faceInfo[3+15*i];
    right = faceInfo[4+15*i];
    bottom = faceInfo[5+15*i];

    //Draw rect
    canvas.drawRect(left, top, right, bottom, paint);

    //Draw landmark
    for (int j=0; j<5; j++) {
        int pointX = faceInfo[6+j+15*i];
        int pointY = faceInfo[6+j+5+15*i];
        canvas.drawCircle(pointX, pointY, 2, paint);
    }

}

```
* Do FaceRecognize

```
    //config recognize server
    mFaceRecognizer.setUrl(mUrl);
    mFaceRecognizer.setToken(mToken);
    mFaceRecognizer.setGroup(mGroup);

```

```
    //crop image with face detect rect
    Bitmap cropBmp = faceSDKNative.CropImage(drawBitmap, left, top, right-left, bottom-top);

    //encode with base64
    String encodeImg = faceSDKNative.EncodeBase64(cropBmp);

    //do face recognize
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

```

# SDK benchmark

|test device              |image size   |  detect     |    crop      |    encode     |   recognize  |
|-------------------------|-------------|-------------|--------------|---------------|--------------|
|GalaxyS6 Quad A57@2.1GHZ |480P         |   18ms      |    2ms       |    16ms       |    400ms     |
|GalaxyS6 Quad A57@2.1GHZ |720P         |   50ms      |    2ms       |    30ms       |    600ms     |
|GalaxyS6 Quad A57@2.1GHZ |1080P        |  110ms      |    2ms       |    60ms       |    1100ms    |
|RK3399  Dual A72@2.0GHZ  |480P         |   40ms      |    2ms       |    18ms       |    450ms     |
|RK3399  Dual A72@2.0GHZ  |720P         |   100ms     |    2ms       |    35ms       |    700ms     |
|RK3399  Dual A72@2.0GHZ  |1080P        |  190ms      |    2ms       |    75ms       |    1300ms    |
|RK3399  Quad A53@1.5GHZ  |480P         |   60ms      |    2ms       |    18ms       |    450ms     |
|RK3399  Quad A53@1.5GHZ  |720P         |  160ms      |    2ms       |    35ms       |    700ms     |
|RK3399  Quad A53@1.5GHZ  |1080P        |  280ms      |    2ms       |    75ms       |    1300ms    |

# SDK Application example

<img src="https://github.com/pingfengluo/android_facesdk/raw/master/test_data/tester1.png" width="360" height="640">
