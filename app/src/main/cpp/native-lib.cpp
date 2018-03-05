//#include <jni.h>
//#include <string>
//#include "opencv2/imgcodecs.hpp"
//#include "opencv2/highgui.hpp"
//#include "opencv2/imgproc.hpp"
//
//
//#include <iostream>
//
//using namespace std;
//using namespace cv;
//
//const Scalar RED = Scalar(0, 0, 255);
//const Scalar PINK = Scalar(230, 130, 255);
//const Scalar BLUE = Scalar(255, 0, 0);
//const Scalar LIGHTBLUE = Scalar(255, 255, 160);
//const Scalar GREEN = Scalar(0, 255, 0);
//
//
//static void getBinMask(const Mat &comMask, Mat &binMask) {
//    if (comMask.empty() || comMask.type() != CV_8UC1)
//        CV_Error(Error::StsBadArg, "comMask is empty or has incorrect type (not CV_8UC1)");
//    if (binMask.empty() || binMask.rows != comMask.rows || binMask.cols != comMask.cols)
//        binMask.create(comMask.size(), CV_8UC1);
//    binMask = comMask & 1;
//}
//
//class GCApplication {
//public:
//    enum {
//        NOT_SET = 0, IN_PROCESS = 1, SET = 2
//    };
//    static const int radius = 20;
//    static const int thickness = -1;
//
//    GCApplication();
//
//    ~GCApplication();
//
//    void reset();
//
//    void setImageAndShowId(Mat *_image, jmethodID _showId);
//
//    void showImage(JNIEnv *env, jobject instance) const;
//
//    void mouseClick(int event, int x, int y, int flags, JNIEnv *env, jobject instance);
//
//    int nextIter();
//
//    int getIterCount() const { return iterCount; }
//
//private:
//    void setRectInMask();
//
//    void setLblsInMask(int flags, Point p, bool isPr);
//
//    const Mat *image;
//    jmethodID showId;
//
//
//    Mat mask;
//    Mat bgdModel, fgdModel;
//
//    uchar rectState, lblsState, prLblsState;
//    bool isInitialized;
//
//    Rect rect;
//    vector<Point> fgdPxls, bgdPxls, prFgdPxls, prBgdPxls;
//    int iterCount;
//};
//
//GCApplication::GCApplication() {
//
//}
//
//GCApplication::~GCApplication() {
//
//}
//
//void GCApplication::reset() {
//    if (!mask.empty())
//        mask.setTo(Scalar::all(GC_BGD));
//    bgdPxls.clear();
//    fgdPxls.clear();
//    prBgdPxls.clear();
//    prFgdPxls.clear();
//
//    isInitialized = false;
//    rectState = NOT_SET;
//    lblsState = NOT_SET;
//    prLblsState = NOT_SET;
//    iterCount = 0;
//}
//
//void GCApplication::setImageAndShowId(Mat *_image, jmethodID _showId) {
//    if (_image->empty())
//        return;
//    image = _image;
//    showId = _showId;
//    mask.create(image->size(), CV_8UC1);
//    reset();
//}
//
//void GCApplication::showImage(JNIEnv *env, jobject instance) const {
//    if (image->empty())
//        return;
//
//    Mat res;
//    Mat binMask;
//    if (!isInitialized)
//        image->copyTo(res);
//    else {
//        getBinMask(mask, binMask);
//        image->copyTo(res, binMask);
//    }
//
//    vector<Point>::const_iterator it;
//    for (it = bgdPxls.begin(); it != bgdPxls.end(); ++it)
//        circle(res, *it, radius, BLUE, thickness);
//    for (it = fgdPxls.begin(); it != fgdPxls.end(); ++it)
//        circle(res, *it, radius, RED, thickness);
//    for (it = prBgdPxls.begin(); it != prBgdPxls.end(); ++it)
//        circle(res, *it, radius, LIGHTBLUE, thickness);
//    for (it = prFgdPxls.begin(); it != prFgdPxls.end(); ++it)
//        circle(res, *it, radius, PINK, thickness);
//
////    if (rectState == IN_PROCESS || rectState == SET)
////        rectangle(res, Point(rect.x, rect.y), Point(rect.x + rect.width, rect.y + rect.height),
////                  GREEN, 2);
//    long img = (long) &res;
//    env->CallVoidMethod(instance, showId, img);
//}
//
////设置矩形范围大小
//void GCApplication::setRectInMask() {
//    CV_Assert(!mask.empty());
//    mask.setTo(GC_BGD);
//    rect.x = max(0, rect.x);
//    rect.y = max(0, rect.y);
//    rect.width = min(rect.width, image->cols - rect.x);
//    rect.height = min(rect.height, image->rows - rect.y);
//    (mask(rect)).setTo(Scalar(GC_PR_FGD));
//}
//
////设置上小圆点
//void GCApplication::setLblsInMask(int flags, Point p, bool isPr) {
//    vector<Point> *bpxls, *fpxls;
//    uchar bvalue, fvalue;
//    if (!isPr) {
//        bpxls = &bgdPxls;
//        fpxls = &fgdPxls;
//        bvalue = GC_BGD;
//        fvalue = GC_FGD;
//    } else {
//        bpxls = &prBgdPxls;
//        fpxls = &prFgdPxls;
//        bvalue = GC_PR_BGD;
//        fvalue = GC_PR_FGD;
//    }
//    if (flags == 2) {
//        bpxls->push_back(p);
//        circle(mask, p, radius, bvalue, thickness);
//    }
//    if (flags == 1) {
//        fpxls->push_back(p);
//        circle(mask, p, radius, fvalue, thickness);
//    }
//}
//
//// event:DOWN = 0,UP = 1,MOVE = 2
//void GCApplication::mouseClick(int event, int x, int y, int flags, JNIEnv *env, jobject instance) {
//    // TODO add bad args check
//    switch (event) {
//        case 0: {
//            if (flags == 0 && rectState == NOT_SET) {
//                rectState = IN_PROCESS;
//                rect = Rect(x, y, 1, 1);
//            }
//            if (flags == 1 && rectState == SET)
//                lblsState = IN_PROCESS;
//            if (flags == 2 && rectState == SET)
//                prLblsState = IN_PROCESS;
//        }
//            break;
//        case 1: {
//            if (flags == 0 || flags == 1) {
//                if (rectState == IN_PROCESS) {
//                    rect = Rect(Point(rect.x, rect.y), Point(x, y));
//                    rectState = SET;
//                    setRectInMask();
//                    CV_Assert(bgdPxls.empty() && fgdPxls.empty() && prBgdPxls.empty() &&
//                              prFgdPxls.empty());
//                    showImage(env, instance);
//                }
//                if (lblsState == IN_PROCESS) {
//                    setLblsInMask(flags, Point(x, y), false);
//                    lblsState = SET;
//                    showImage(env, instance);
//                }
//            }
//            if (flags == 2 && prLblsState == IN_PROCESS) {
//                setLblsInMask(flags, Point(x, y), false);
//                prLblsState = SET;
//                showImage(env, instance);
//            }
//        }
//            break;
//        case 2: {
//            if (rectState == IN_PROCESS) {
//                rect = Rect(Point(rect.x, rect.y), Point(x, y));
//                CV_Assert(bgdPxls.empty() && fgdPxls.empty() && prBgdPxls.empty() &&
//                          prFgdPxls.empty());
//                showImage(env, instance);
//            } else if (lblsState == IN_PROCESS) {
//                setLblsInMask(flags, Point(x, y), false);
//                showImage(env, instance);
//            } else if (prLblsState == IN_PROCESS) {
//                setLblsInMask(flags, Point(x, y), false);
//                showImage(env, instance);
//            }
//        }
//            break;
//    }
//
//}
//
//int GCApplication::nextIter() {
//    if (isInitialized)
//        grabCut(*image, mask, rect, bgdModel, fgdModel, 1);
//    else {
//        if (rectState != SET)
//            return iterCount;
//
//        if (lblsState == SET || prLblsState == SET)
//            grabCut(*image, mask, rect, bgdModel, fgdModel, 1, GC_INIT_WITH_MASK);
//        else
//            grabCut(*image, mask, rect, bgdModel, fgdModel, 1, GC_INIT_WITH_RECT);
//
//        isInitialized = true;
//    }
//    iterCount++;
//
//    bgdPxls.clear();
//    fgdPxls.clear();
//    prBgdPxls.clear();
//    prFgdPxls.clear();
//    return iterCount;
//};
//
//static void
//on_mouse(GCApplication *gcapp, int event, int x, int y, int flags, JNIEnv *env, jobject instance) {
//    gcapp->mouseClick(event, x, y, flags, env, instance);
//}
//
//extern "C"
//JNIEXPORT GCApplication *JNICALL
//Java_com_martin_ads_testopencv_Main3Activity_initGrabCut(JNIEnv *env, jobject instance,
//                                                         jlong image) {
//
//    // TODO
//    Mat *img = (Mat *) image;//得到输入
//    GCApplication *gcapp = new GCApplication();
//
//    jclass jc = env->GetObjectClass(instance);
//    jmethodID showId = env->GetMethodID(jc, "showImage", "(J)V");
//
//    gcapp->setImageAndShowId(img, showId);
//    gcapp->showImage(env, instance);
//    return gcapp;
//}extern "C"
//JNIEXPORT void JNICALL
//Java_com_martin_ads_testopencv_Main3Activity_moveGrabCut(JNIEnv *env, jobject instance, jint event,
//                                                         jint x,
//                                                         jint y, jint flags, jlong gcapp) {
//
//    // TODO
//    GCApplication *g = (GCApplication *) gcapp;
//    on_mouse(g, event, x, y, flags, env, instance);
//}extern "C"
//JNIEXPORT void JNICALL
//Java_com_martin_ads_testopencv_Main3Activity_reset(JNIEnv *env, jobject instance, jlong gcapp) {
//
//    // TODO
//    GCApplication *g = (GCApplication *) gcapp;
//    g->reset();
//    g->showImage(env, instance);
//}extern "C"
//JNIEXPORT jboolean JNICALL
//Java_com_martin_ads_testopencv_Main3Activity_grabCut(JNIEnv *env, jobject instance, jlong gcapp) {
//
//    // TODO
//    GCApplication *g = (GCApplication *) gcapp;
//    int iterCount = g->getIterCount();
//    int newIterCount = g->nextIter();
//    return (jboolean) (newIterCount > iterCount);
//}extern "C"
//JNIEXPORT void JNICALL
//Java_com_martin_ads_testopencv_Main3Activity_grabCutOver(JNIEnv *env, jobject instance,
//                                                         jlong gcapp) {
//
//    // TODO
//    GCApplication *g = (GCApplication *) gcapp;
//    g->showImage(env, instance);
//}
//
////Mat Java_com_martin_ads_testopencv_Main3Activity_nativeGrabcut(JNIEnv *env, jobject instance,
////                                                               jlong img) {
////    Mat *image = (Mat *) img;
////    Mat result; // 分割结果 (4种可能取值)
////    Mat bgModel, fgModel; // 模型(内部使用)
////
////    // 设定矩形,该矩形的长宽分别-1
////    Rect rectangle(1, 1, image->cols - 1, image->rows - 1);
////    grabCut(*image, result, rectangle, bgModel, fgModel, 1, cv::GC_INIT_WITH_RECT);
////
////    // 得到可能为前景的像素
////    compare(result, cv::GC_PR_FGD, result, cv::CMP_EQ);
////    // 生成输出图像
////    Mat foreground(image->size(), CV_8UC3, cv::Scalar(255, 255, 255));
////    image->copyTo(foreground, result); // 不复制背景数据
////
////    return foreground;
////}
//
//
//
//
//extern "C"
//JNIEXPORT Mat JNICALL
//Java_com_martin_ads_testopencv_Main2Activity_nativeCVSmooth(JNIEnv *env, jobject instance,
//                                                            jlong img) {
//    Mat *image = (Mat *) img;
//    Mat *image2 = (Mat *) img;
//
//    cvSmooth(image, image2, CV_BLUR, 11, 11);
//
//    return *image2;
//}
//
//
//
//
