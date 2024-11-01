#include <jni.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <sys/mman.h>
#include <array>

#include "simpleproclock.h"


#define ASHMEM_NAME_LEN         256
#define __ASHMEMIOC             0x77
#define ASHMEM_SET_NAME         _IOW(__ASHMEMIOC, 1, char[ASHMEM_NAME_LEN])
#define ASHMEM_SET_SIZE         _IOW(__ASHMEMIOC, 3, size_t)


struct memArea {
    int *map;
    int fd;
    int size;
};

struct memArea maps[10];
int num = 0;

static int getShmemSize() {
    return 1024 * 4 * ::sysconf(_SC_PAGE_SIZE);
}


static jint getFD(JNIEnv *env, jclass cl, jstring memName) {
    const char *name = env->GetStringUTFChars(memName, NULL);

    jint fd = open("/dev/ashmem", O_RDWR);

    int size = getShmemSize();
    ioctl(fd, ASHMEM_SET_NAME, name);
    ioctl(fd, ASHMEM_SET_SIZE, size);

    maps[num].size = size;
    maps[num].fd = fd;
    maps[num++].map = (int *) mmap(0, size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);

    env->ReleaseStringUTFChars(memName, name);

    return fd;
}

static jint setNum(JNIEnv *env, jclass cl, jint fd, jint pos, jint num) {
    for (int i = 0; i <= pos; i++) {
        if (maps[i].fd == fd) {
            if (pos < (maps[i].size / sizeof(int))) {
                maps[i].map[pos] = num;
//                char* tmp  = new char[1024*1024*3];
//                memset(tmp, 2, 1024*1024*3);
//                memcpy(maps[i].map, tmp, 1024*1024*3);
                return 0;
            }
            return -1;
        }
    }
    return -1;
}

static jint getNum(JNIEnv *env, jclass cl, jint fd, jint pos) {
    for (int i = 0; i < num; i++) {
        if (maps[i].fd == fd) {
            if (pos < (maps[i].size / sizeof(int))) {
                return maps[i].map[pos];
            }
            return -1;
        }
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
        jclass clazz = env->FindClass("com/example/developer/testashmem/ShmLib");
        if (clazz) {
            std::array<JNINativeMethod, 6> methods{
                    JNINativeMethod{"getShmemSize", "()I", (void *) getShmemSize},
                    JNINativeMethod{"setVal", "(III)I", (void *) setNum},
                    JNINativeMethod{"getVal", "(II)I", (void *) getNum},
                    JNINativeMethod{"getFD", "(Ljava/lang/String;)I", (void *) getFD},
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