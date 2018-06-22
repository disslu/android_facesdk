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

# Speed

|device              |image size   |  detect     |    crop      |    encode     |   recognize  |
|-------------------------|-------------|-------------|--------------|---------------|--------------|
|GalaxyS6 Quad A57@2.1GHZ |480P         |   18ms      |    2ms       |    16ms       |    400ms     |
|GalaxyS6 Quad A57@2.1GHZ |720P         |   50ms      |    2ms       |    30ms       |    600ms     |
|GalaxyS6 Quad A57@2.1GHZ |1080P        |  110ms      |    2ms       |    60ms       |    1100ms    |

