# Android FaceSDK example

* face detect with DeepLearning algorithm
* support multi faces detect

# How to use

* Do FaceDetect

```
int faceInfo[] =  faceSDKNative.FaceDetect(imageDate, width, height, 4);

```
* Get FaceDetect results

```
int faceNum = faceInfo[0];//get face number

for (int i=0; i<faceNum; i++) {
    int left, top, right, bottom;
    left = faceInfo[1+14*i];
    top = faceInfo[2+14*i];
    right = faceInfo[3+14*i];
    bottom = faceInfo[4+14*i];
    //Draw faceRect
    canvas.drawRect(left, top, right, bottom, paint);

    //Draw landmark
    for (int j=0; j<5; j++) {
        int pointX = faceInfo[5+j+14*i];
        int pointY = faceInfo[5+j+5+14*i];
        canvas.drawCircle(pointX, pointY, 2, paint);
    }

}
```

# Application examples

![](https://github.com/pingfengluo/android_facesdk/raw/master/test_data/tester1.png)

![](https://github.com/pingfengluo/android_facesdk/raw/master/test_data/tester2.png)
