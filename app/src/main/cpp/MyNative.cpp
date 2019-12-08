#include "com_zju_openeye_MainActivity.h"
#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring

JNICALL
Java_com_zju_openeye_MainActivity_myNativeString(
        JNIEnv *env,
        jobject /* this */) {
    return env->NewStringUTF("myNativeMethod 执行了");
}