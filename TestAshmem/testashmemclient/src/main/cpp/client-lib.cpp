#include <jni.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <sys/mman.h>

#include "simpleproclock.h"



int *map ;
int size;

static void setmap(JNIEnv *env, jclass cl, jint fd, jint sz)
{
    size = sz;
    map = (int *)mmap(0,size,PROT_READ|PROT_WRITE,MAP_SHARED,fd,0);
}

static jint setNum(JNIEnv *env, jclass cl, jint pos,jint num)
{
            if(pos < (size/ sizeof(int)))
            {
                map[pos] = num;
                return 0;
            }
            return -1;
}
static jint getNum(JNIEnv *env, jclass cl, jint pos)
{
            if(pos < (size/ sizeof(int)))
            {
                return map[pos];
            }
            return -1;
}

SimpleInterProcLock* g_lock = nullptr;
static bool requireInterProcLock(JNIEnv *env, jclass cl, jstring filepath)
{
    const char *name = env->GetStringUTFChars(filepath, nullptr);
    if (!g_lock)
    {
        g_lock = new SimpleInterProcLock(name);
    }

    return g_lock->try_lock();
}

static void releaseInterProcLock(JNIEnv *env, jclass cl)
{
    if (g_lock)
    {
        g_lock->release_lock();
        delete g_lock;
        g_lock = nullptr;
    }
}



static JNINativeMethod method_table[] = {
        { "setVal", "(II)I", (void *) setNum },
        { "getVal", "(I)I", (void *) getNum },
        { "setMap", "(II)V", (void *)setmap },
        { "requireProcLock", "(Ljava/lang/String;)Z", (void *)requireInterProcLock },
        { "releaseProcLock", "()V", (void *)releaseInterProcLock }

};


extern "C" jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    } else {
        jclass clazz = env->FindClass("com/example/testashmemclient/ShmClientLib");
        if (clazz) {
            jint ret = env->RegisterNatives(clazz, method_table, sizeof(method_table) / sizeof(method_table[0]));
            env->DeleteLocalRef(clazz);
            return ret == 0 ? JNI_VERSION_1_6 : JNI_ERR;
        } else {
            return JNI_ERR;
        }
    }
}