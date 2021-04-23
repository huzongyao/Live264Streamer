//
// Created by huzongyao on 2019/4/20.
//

#include <include/GenericMediaServer.hh>
#include <include/RTSPServer.hh>
#include <include/StreamReplicator.hh>
#include <BasicUsageEnvironment.hh>
#include <GroupsockHelper.hh>
#include "TopStreamServer.h"
#include "../live555api.h"
#include "subsession/UnicastServerMediaSubsession.h"

TopStreamServer *
TopStreamServer::createNew() {
    return new TopStreamServer();
}

void TopStreamServer::addUserRecord(char const *userName, char const *password) {
    if (mAuthDB == nullptr) {
        mAuthDB = new UserAuthenticationDatabase;
    }
    mAuthDB->addUserRecord(userName, password);
}

void TopStreamServer::doEventLoop() {
    LOGI("TopStreamServer::doEventLoop");
    mWatchVariable = 0;
    mEnv->taskScheduler().doEventLoop(&mWatchVariable);
    LOGI("TopStreamServer::Loop End!");
    if (mStreamUrl) {
        delete[] mStreamUrl;
        mStreamUrl = nullptr;
    }
    Medium::close(mH264FramedSource);
    mMediaSession->deleteAllSubsessions();
    Medium::close(mMediaSession);
    Medium::close(mRTSPServer);
    mEnv->reclaim();
}

void TopStreamServer::stopServer() {
    LOGI("TopStreamServer::stopServer");
    mWatchVariable = 1;
}

TopStreamServer::TopStreamServer() {
}

TopStreamServer::~TopStreamServer() {
    LOGI("TopStreamServer::~TopStreamServer");
}

void TopStreamServer::feedH264Data(char *data, unsigned int dataSize) {
    mH264FramedSource->pushRawData(data, dataSize);
}

char *TopStreamServer::getStreamUrl() {
    return mStreamUrl;
}

int TopStreamServer::startServer(int port, const char *streamName) {
    mScheduler = BasicTaskScheduler::createNew();
    mEnv = BasicUsageEnvironment::createNew(*mScheduler);
    mRTSPServer = RTSPServer::createNew(*mEnv, (u_int16_t) port, mAuthDB);
    if (mRTSPServer == nullptr) {
        LOGE("Failed to create RTSP server: %s", mEnv->getResultMsg());
        return -1;
    }
    mH264FramedSource = H264ArrayFramedSource::createNew(*mEnv, 32, true);
    if (mH264FramedSource == nullptr) {
        LOGE("Failed to create H264ArrayFramedSource!");
        return -1;
    }
    OutPacketBuffer::maxSize = 800000;
    // Create media session
    {
        mMediaSession = ServerMediaSession::createNew(*mEnv, streamName);
        StreamReplicator *replicator =
                StreamReplicator::createNew(*mEnv, mH264FramedSource, false);

        UnicastServerMediaSubsession *subSession =
                UnicastServerMediaSubsession::createNew(*mEnv, replicator);
        mMediaSession->addSubsession(subSession);
        mRTSPServer->addServerMediaSession(mMediaSession);
        mStreamUrl = mRTSPServer->rtspURL(mMediaSession);
        LOGE("Play Stream on URL : [%s]", mStreamUrl);
    }
    return 0;
}


