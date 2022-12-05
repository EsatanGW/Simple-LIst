//
// Created by Administrator on 2021/2/25.
//
#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_cyc_shell_ShellApplication_getSecretKey(
        JNIEnv *env,
        jobject /* this */) {
    std::string secretkey = "abcdefghijklmnop";
    return env->NewStringUTF(secretkey.c_str());
}
