#include <dlfcn.h>
#include <link.h>
#include "dl_util.h"
#include "log.h"
#include "jni_util.h"
#include <sys/stat.h>

void *DlUtil::dlopen(const char *lib_name, int flags, bool loaded_only) {
    if (lib_name == nullptr && strlen(lib_name) <=0) {
        return nullptr;
    }

    void *handle = ::dlopen("libdl.so", RTLD_NOW);
    CHECK_NULL_RETURN_NULL(handle, "open libdl fail");
    auto __loader_dlopen = reinterpret_cast<__loader_dlopen_fn>(
            ::dlsym(handle, "__loader_dlopen"));
    CHECK_NULL_RETURN_NULL(__loader_dlopen, "__loader_dlopen get fail");
    if (!loaded_only) {
        handle = __loader_dlopen(lib_name, flags, (void *)__loader_dlopen);
    }  else {
        handle = nullptr;
    }
    if (handle == nullptr) {
        ALOGI("open use dl_iterate_phdr for %s, load only %d", lib_name, loaded_only);
        dl_phdr_info targetData;
        targetData.dlpi_name = lib_name;
        targetData.dlpi_addr = 0;
        auto iteratePhdrCb = [](struct dl_phdr_info *phdr_info, size_t size,
                                        void *data) -> int {
            dl_phdr_info *targetInfo = reinterpret_cast<dl_phdr_info *>(data);
            if (!phdr_info->dlpi_name) {
                return 0;
            }
            const char *sub_str = strstr(phdr_info->dlpi_name, targetInfo->dlpi_name);
            if (sub_str && strlen(sub_str) == strlen(targetInfo->dlpi_name)) {
                targetInfo->dlpi_addr = phdr_info->dlpi_addr;
                return 1;
            }
            if (targetInfo->dlpi_name != nullptr && targetInfo->dlpi_name[0] == '/'
                && sameFile(targetInfo->dlpi_name, phdr_info->dlpi_name)) {
                ALOGE("dl_iterate_phdr find same file for %s, %s", targetInfo->dlpi_name, phdr_info->dlpi_name);
                targetInfo->dlpi_addr = phdr_info->dlpi_addr;
                return 1;
            }

            return 0;
        };
        dl_iterate_phdr(iteratePhdrCb,
                        reinterpret_cast<void *>(&targetData));
        if (targetData.dlpi_addr == 0) {
            ALOGE("dl_iterate_phdr fail for %s", lib_name);
            return nullptr;
        }
        handle = __loader_dlopen(lib_name, flags, (void *)targetData.dlpi_addr);
    }
    return handle;
}

bool DlUtil::sameFile(const char *path1, const char *path2) {
    struct stat stat1, stat2;

    if (stat(path1, &stat1) < 0) {
        return false;
    }

    if (stat(path2, &stat2) < 0) {
        return false;
    }
    return (stat1.st_ino == stat2.st_ino) && (stat1.st_dev == stat2.st_dev);
}