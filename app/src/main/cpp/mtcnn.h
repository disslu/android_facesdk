//
// Created by PingfengLuo on 2018.01.24
//
#pragma once

#ifndef __MTCNN_NCNN_H__
#define __MTCNN_NCNN_H__
#include "mat.h"
#include "net.h"
#include <string>
#include <vector>
#include <time.h>
#include <algorithm>
#include <map>
#include <iostream>
#include <android/log.h>

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define TAG "FaceSDKNative"

using namespace std;

struct Bbox
{
	float score;
	int x1;
	int y1;
	int x2;
	int y2;
	float area;
	float ppoint[10];
	float regreCoord[4];
};


class MTCNN {

public:
	MTCNN(const string &model_path);

	MTCNN(const std::vector<std::string> param_files, const std::vector<std::string> bin_files);

	~MTCNN();

	void SetMinFace(int minFaceSize);

	void SetNumThreads(int numThreads);

	void detect(ncnn::Mat& img_, std::vector<Bbox>& finalBbox);

	//  void detection(const cv::Mat& img, std::vector<cv::Rect>& rectangles);
private:
	void generateBbox(ncnn::Mat score, ncnn::Mat location, vector<Bbox>& boundingBox_, float scale);

	void nms(vector<Bbox> &boundingBox_, const float overlap_threshold, string modelname="Union");

	void refine(vector<Bbox> &vecBbox, const int &height, const int &width, bool square);

	void PNet();

	void RNet();

	void ONet();

	ncnn::Net Pnet, Rnet, Onet;

	ncnn::Mat img;

	const float nms_threshold[3] = {0.5f, 0.7f, 0.7f};

	const float mean_vals[3] = {127.5, 127.5, 127.5};

	const float norm_vals[3] = {0.0078125, 0.0078125, 0.0078125};

	const int MIN_DET_SIZE = 12;

	std::vector<Bbox> firstBbox_, secondBbox_,thirdBbox_;

	int img_w, img_h;

	const float threshold[3] = { 0.8f, 0.8f, 0.8f };
	int minsize = 160;
	const float pre_facetor = 0.709f;
    int num_threads = 1;

};

#endif //__MTCNN_NCNN_H__
