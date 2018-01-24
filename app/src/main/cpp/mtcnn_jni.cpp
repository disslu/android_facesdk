#include <android/bitmap.h>
#include <android/log.h>
#include <jni.h>
#include <string>
#include <vector>
#include "net.h"
#include "mtcnn.h"

#define TAG "FaceSDKNative"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

using namespace std;

static MTCNN *mtcnn;
bool detection_sdk_init_ok = false;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_facesdk_FaceSDKNative_FaceDetectionModelInit(JNIEnv *env, jobject instance,
                                                      jstring faceDetectionModelPath_) {
    LOGD("JNI开始人脸检测SDK初始化");
    if (detection_sdk_init_ok) {
        LOGD("人脸检测SDK已经初始化");
        return true;
    }
    jboolean tRet = false;
    if (NULL == faceDetectionModelPath_) {
        LOGD("导入的人脸检测的目录为空");
        return tRet;
    }

    //获取模型的绝对路径的目录（不是/aaa/bbb.bin这样的路径，是/aaa/)
    const char *faceDetectionModelPath = env->GetStringUTFChars(faceDetectionModelPath_, 0);
    if (NULL == faceDetectionModelPath) {
        LOGD("导入的人脸检测的目录为空");
        return tRet;
    }

    string tFaceModelDir = faceDetectionModelPath;
    string tLastChar = tFaceModelDir.substr(tFaceModelDir.length()-1, 1);
    //目录补齐/
    if ("\\" == tLastChar) {
        tFaceModelDir = tFaceModelDir.substr(0, tFaceModelDir.length() - 1) + "/";
    } else if (tLastChar != "/") {
        tFaceModelDir += "/";
    }
    LOGD("init, tFaceModelDir=%s", tFaceModelDir.c_str());

    mtcnn = new MTCNN(tFaceModelDir);

    env->ReleaseStringUTFChars(faceDetectionModelPath_, faceDetectionModelPath);
    detection_sdk_init_ok = true;
    tRet = true;

    return tRet;
}

JNIEXPORT jintArray JNICALL
Java_com_facesdk_FaceSDKNative_FaceDetect(JNIEnv *env, jobject instance, jbyteArray imageDate_,
                                          jint imageWidth, jint imageHeight, jint imageChannel) {
    if(!detection_sdk_init_ok){
        LOGD("人脸检测SDK未初始化，直接返回空");
        return NULL;
    }

    int tImageDateLen = env->GetArrayLength(imageDate_);
    if(imageChannel == tImageDateLen / imageWidth / imageHeight){
        LOGD("数据宽=%d,高=%d,通道=%d",imageWidth,imageHeight,imageChannel);
    }
    else{
        LOGD("数据长宽高通道不匹配，直接返回空");
        return NULL;
    }

    jbyte *imageDate = env->GetByteArrayElements(imageDate_, NULL);
    if (NULL == imageDate){
        LOGD("导入数据为空，直接返回空");
        return NULL;
    }

    if(imageWidth<80||imageHeight<80){
        LOGD("导入数据的宽和高小于80，直接返回空");
        return NULL;
    }

    //TODO 通道需测试
    if(3 == imageChannel || 4 == imageChannel){
        //图像通道数只能是3或4；
    }else{
        LOGD("图像通道数只能是3或4，直接返回空");
        return NULL;
    }

    mtcnn->SetMinFace(40);

    unsigned char *faceImageCharDate = (unsigned char*)imageDate;
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

    int out_size = 1+num_face*14;
    int *faceInfo = new int[out_size];
    faceInfo[0] = num_face;
    for (int i=0; i<num_face; i++) {
        faceInfo[14*i+1] = finalBbox[i].x1;//left
        faceInfo[14*i+2] = finalBbox[i].y1;//top
        faceInfo[14*i+3] = finalBbox[i].x2;//right
        faceInfo[14*i+4] = finalBbox[i].y2;//bottom
        for (int j =0; j<10; j++) {
            faceInfo[14*i+5] = static_cast<int>(finalBbox[i].ppoint[j]);
        }
    }

    jintArray tFaceInfo = env->NewIntArray(out_size);
    env->SetIntArrayRegion(tFaceInfo,0,out_size,faceInfo);
    env->ReleaseByteArrayElements(imageDate_, imageDate, 0);

    return tFaceInfo;
}

JNIEXPORT jboolean JNICALL
Java_com_facesdk_FaceSDKNative_FaceDetectionModelUnInit(JNIEnv *env, jobject instance) {

    jboolean tDetectionUnInit = false;

    if (!detection_sdk_init_ok) {
        LOGD("人脸检测SDK已经释放或者未初始化");
        return true;
    }

    delete mtcnn;

    detection_sdk_init_ok = false;

    tDetectionUnInit = true;

    LOGD("人脸检测SDK释放成功");

    return tDetectionUnInit;
}

}