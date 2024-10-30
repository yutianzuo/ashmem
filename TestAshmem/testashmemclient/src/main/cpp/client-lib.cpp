#include <jni.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <sys/mman.h>
#include <array>
#include "simpleproclock.h"


int *map;
int size;

static void setmap(JNIEnv *env, jclass cl, jint fd) {
    size = 1024 * 4 * ::sysconf(_SC_PAGE_SIZE);
    map = (int *) mmap(0, size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
}

static jint setNum(JNIEnv *env, jclass cl, jint pos, jint num) {
    if (pos < (size / sizeof(int))) {
        map[pos] = num;
        return 0;
    }
    return -1;
}

static jint getNum(JNIEnv *env, jclass cl, jint pos) {
    if (pos < (size / sizeof(int))) {
        return map[pos];
    }
    return -1;
}

SimpleInterProcLock *g_lock = nullptr;

static jboolean requireInterProcLock(JNIEnv *env, jclass cl, jstring filepath) {
    const char *name = env->GetStringUTFChars(filepath, nullptr);
    if (!g_lock) {
        g_lock = new SimpleInterProcLock(name);
    }

    return g_lock->try_lock();
}

static void releaseInterProcLock(JNIEnv *env, jclass cl) {
    if (g_lock) {
        g_lock->release_lock();
        delete g_lock;
        g_lock = nullptr;
    }
}

extern "C" jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    } else {
        jclass clazz = env->FindClass("com/example/testashmemclient/ShmClientLib");
        if (clazz) {
            std::array<JNINativeMethod, 5> methods{
                    JNINativeMethod{"setVal", "(II)I", (void *) setNum},
                    JNINativeMethod{"getVal", "(I)I", (void *) getNum},
                    JNINativeMethod{"setMap", "(I)V", (void *) setmap},
                    JNINativeMethod{"requireProcLock", "(Ljava/lang/String;)Z",
                                    (void *) requireInterProcLock},
                    JNINativeMethod{"releaseProcLock", "()V", (void *) releaseInterProcLock}
            };
            jint ret = env->RegisterNatives(clazz, methods.data(),
                                            methods.size());
            env->DeleteLocalRef(clazz);
            return ret == 0 ? JNI_VERSION_1_6 : JNI_ERR;
        } else {
            return JNI_ERR;
        }
    }
}