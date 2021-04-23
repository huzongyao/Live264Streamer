//
// Created by huzongyao on 2018/11/20.
//
#include <jni.h>
#include <include/liveMedia_version.hh>
#include <include/RTSPServer.hh>
#include "live555api.h"
#include "server/TopStreamServer.h"

JNIEXPORT jstring JNICALL
JAVA_FUNC(getVersionInfo)(JNIEnv *env, jclass type) {
    return env->NewStringUTF(LIVEMEDIA_LIBRARY_VERSION_STRING);
}

JNIEXPORT jlong JNICALL
JAVA_FUNC(createNew)(JNIEnv *env, jclass type) {
    TopStreamServer *server = TopStreamServer::createNew();
    auto pServer = (jlong) server;
    LOGE("Server Created Instance[%ld]", pServer);
    return pServer;
}

JNIEXPORT jint JNICALL
JAVA_FUNC(addUserRecord)(JNIEnv *env, jclass type, jlong instanceId, jstring userName_,
                         jstring password_) {
    int ret = 0;
    auto *userName = env->GetStringUTFChars(userName_, nullptr);
    auto *password = env->GetStringUTFChars(password_, nullptr);
    if (instanceId > 0) {
        auto *server = (TopStreamServer *) instanceId;
        server->addUserRecord(userName, password);
    } else {
        LOGE("No Instance On[%ld]", instanceId);
        ret = -1;
    }
    env->ReleaseStringUTFChars(userName_, userName);
    env->ReleaseStringUTFChars(password_, password);
    return ret;
}

JNIEXPORT jint JNICALL
JAVA_FUNC(startServer)(JNIEnv *env, jclass type, jlong instanceId, jint port, jstring streamName_) {
    int ret;
    const char *streamName = env->GetStringUTFChars(streamName_, nullptr);
    if (instanceId > 0) {
        auto *server = (TopStreamServer *) instanceId;
        ret = server->startServer(port, streamName);
    } else {
        LOGE("No Instance On[%ld]", instanceId);
        ret = -1;
    }
    env->ReleaseStringUTFChars(streamName_, streamName);
    return ret;
}

JNIEXPORT jstring JNICALL
JAVA_FUNC(getStreamUrl)(JNIEnv *env, jclass type, jlong instanceId) {
    char *ret = nullptr;
    if (instanceId <= 0) {
        LOGE("No Instance On[%ld]", instanceId);
    } else {
        LOGE("getStreamUrl[%ld]", instanceId);
        auto *server = (TopStreamServer *) instanceId;
        ret = server->getStreamUrl();
    }
    if (!ret) {
        ret = (char *) "unknown";
    }
    return env->NewStringUTF(ret);
}

JNIEXPORT jint JNICALL
JAVA_FUNC(doEventLoop)(JNIEnv *env, jclass type, jlong instanceId) {
    if (instanceId <= 0) {
        LOGE("No Instance On[%ld]", instanceId);
        return -1;
    }
    LOGE("Start Loop Instance[%ld]", instanceId);
    auto *server = (TopStreamServer *) instanceId;
    server->doEventLoop();
    return 0;
}

JNIEXPORT jint JNICALL
JAVA_FUNC(feedH264Data)(JNIEnv *env, jclass type, jlong instanceId, jbyteArray data_) {
    int ret = 0;
    if (instanceId > 0) {
        auto *server = (TopStreamServer *) instanceId;
        int len = env->GetArrayLength(data_);
        char *buf = new char[len];
        env->GetByteArrayRegion(data_, 0, len, (jbyte *) buf);
        server->feedH264Data(buf, (unsigned int) len);
    } else {
        LOGE("No Instance On[%ld]", instanceId);
        ret = -1;
    }
    return ret;
}

JNIEXPORT jint JNICALL
JAVA_FUNC(stopServer)(JNIEnv *env, jclass type, jlong instanceId) {
    if (instanceId <= 0) {
        LOGE("No Instance On[%ld]", instanceId);
        return -1;
    }
    LOGE("Stop Server Instance[%ld]", instanceId);
    auto *server = (TopStreamServer *) instanceId;
    server->stopServer();
    return 0;
}