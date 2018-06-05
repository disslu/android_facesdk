#include <android/bitmap.h>
#include <jni.h>
#include <string>
#include <vector>
#include "net.h"
#include "mtcnn.h"


using namespace std;

static MTCNN *mtcnn = nullptr;
static bool detection_sdk_init_ok = false;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_facesdk_FaceSDKNative_FaceDetectionModelInit(JNIEnv *env, jobject instance,
                                                      jstring faceDetectionModelPath_) {
    jboolean tRet = false;
    LOGD("JNI init native sdk");
    if (detection_sdk_init_ok) {
        LOGD("sdk already init");
        tRet = true;
        return tRet;
    }
    if (NULL == faceDetectionModelPath_) {
        LOGD("model dir is empty");
        return tRet;
    }

    //get abs path（should not be /aaa/bbb.bin，but /aaa/)
    const char *faceDetectionModelPath = env->GetStringUTFChars(faceDetectionModelPath_, 0);
    if (NULL == faceDetectionModelPath) {
        LOGD("model dir is empty");
        return tRet;
    }

    string tFaceModelDir = faceDetectionModelPath;
    string tLastChar = tFaceModelDir.substr(tFaceModelDir.length()-1, 1);
    //adjust dir
    if ("\\" == tLastChar) {
        tFaceModelDir = tFaceModelDir.substr(0, tFaceModelDir.length()-1) + "/";
    } else if (tLastChar != "/") {
        tFaceModelDir += "/";
    }
    LOGD("init, tFaceModelDir=%s", tFaceModelDir.c_str());

    mtcnn = new MTCNN(tFaceModelDir);


    env->ReleaseStringUTFChars(faceDetectionModelPath_, faceDetectionModelPath);
    detection_sdk_init_ok = true;

    if (mtcnn != nullptr) {
        tRet = true;
        detection_sdk_init_ok = true;
    }

    return tRet;
}

JNIEXPORT jintArray JNICALL
Java_com_facesdk_FaceSDKNative_FaceDetect(JNIEnv *env, jobject instance, jbyteArray imageDate_,
                                          jint imageWidth, jint imageHeight, jint imageChannel) {
    if(!detection_sdk_init_ok){
        LOGD("sdk not init");
        return NULL;
    }

    int tImageDateLen = env->GetArrayLength(imageDate_);
    if(imageChannel == tImageDateLen / imageWidth / imageHeight){
        LOGD("imgW=%d, imgH=%d,imgC=%d",imageWidth, imageHeight, imageChannel);
    }
    else{
        LOGD("img data format error");
        return NULL;
    }

    jbyte *imageDate = env->GetByteArrayElements(imageDate_, NULL);

    if (NULL == imageDate){
        LOGD("img data is null");
        return NULL;
    }

    if(imageWidth<200||imageHeight<200){
        LOGD("img is too small");
        return NULL;
    }

    //TODO channel valid
    if(3 == imageChannel || 4 == imageChannel){
    }
    else{
        LOGD("img data format error, channel just support 3 or 4");
        return NULL;
    }

    unsigned char *faceImageCharDate = (unsigned char*) imageDate;

    ncnn::Mat ncnn_img;
    if (imageChannel==3) {
        ncnn_img = ncnn::Mat::from_pixels(faceImageCharDate,
                                          ncnn::Mat::PIXEL_BGR2RGB,
                                          imageWidth,
                                          imageHeight);
    } else {
        ncnn_img = ncnn::Mat::from_pixels(faceImageCharDate,
                                          ncnn::Mat::PIXEL_RGBA2RGB,
                                          imageWidth,
                                          imageHeight);
    }

    std::vector<Bbox> finalBbox;
    mtcnn->detect(ncnn_img, finalBbox);

    int32_t num_face = static_cast<int32_t>(finalBbox.size());

    LOGD("native detected face number: %d", num_face);

    int out_size = 1 + num_face*15;
    int *faceInfo = new int[out_size];
    faceInfo[0] = num_face;
    for (int i=0; i<num_face; i++) {
        faceInfo[15*i+1] = finalBbox[i].score*10000;//score
        faceInfo[15*i+2] = finalBbox[i].x1;//left
        faceInfo[15*i+3] = finalBbox[i].y1;//top
        faceInfo[15*i+4] = finalBbox[i].x2;//right
        faceInfo[15*i+5] = finalBbox[i].y2;//bottom
        //store 5 keypoints [x0,x1,x2,x3,x4,y0,y1,y2,y3,y4]
        for (int j =0; j<10; j++) {
            faceInfo[15*i+6+j] = static_cast<int>(finalBbox[i].ppoint[j]);
        }
    }

    jintArray tFaceInfo = env->NewIntArray(out_size);
    env->SetIntArrayRegion(tFaceInfo, 0, out_size, faceInfo);
    env->ReleaseByteArrayElements(imageDate_, imageDate, 0);

    delete [] faceInfo;

    return tFaceInfo;
}

JNIEXPORT jboolean JNICALL Java_com_facesdk_FaceSDKNative_SetMinFaceSize(JNIEnv *env, jobject instance, jint minFaceSize) {
    if(!detection_sdk_init_ok){
        LOGD("sdk not inited, do nothing");
        return false;
    }

    if(minFaceSize <= 100){
        minFaceSize = 100;
    }

    mtcnn->SetMinFace(minFaceSize);

    return true;
}

JNIEXPORT jboolean JNICALL Java_com_facesdk_FaceSDKNative_SetThreadsNumber(JNIEnv *env, jobject instance, jint threadsNumber) {

    if(!detection_sdk_init_ok){
        LOGD("sdk not inited, do nothing");
        return false;
    }

    mtcnn->SetNumThreads(threadsNumber);

    return  true;
}

JNIEXPORT jboolean JNICALL Java_com_facesdk_FaceSDKNative_FaceDetectionModelUnInit(JNIEnv *env, jobject instance) {

    jboolean tDetectionUnInit = false;

    if (!detection_sdk_init_ok) {
        LOGD("sdk not inited, do nothing");
        return true;
    }

    delete mtcnn;

    detection_sdk_init_ok = false;

    tDetectionUnInit = true;

    LOGD("sdk release ok");

    return tDetectionUnInit;
}

}