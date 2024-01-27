#ifndef JTIK_DL_UTIL_H
#define JTIK_DL_UTIL_H

using __loader_dlopen_fn = void *(*)(const char *filename, int flag,
                                     const void *caller_addr);


class DlHandler {
public:
    DlHandler(void* handle): mHandle(handle) {}
    void * GetHandle() { return mHandle;}
    ~DlHandler(){
        if (!mHandle) {
            ::dlclose(mHandle);
        }
    }
private:
        void* mHandle;
};

// only support android 8+
class DlUtil {
public:
    static void *dlopen(const char *lib_name, int flags, bool loaded_only = false);
    static bool sameFile(const char *path1, const char *path2);
};


#endif //JTIK_DL_UTIL_H
