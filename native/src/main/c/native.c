#include <jni.h>
#include <stdio.h>
#include <string.h>
#include "wayland-client-protocol.h"
#include "appmenu.h"
#include "xdg-decoration-unstable-v1.h"

// see WLComponentPeer.c in JDK
struct WLFrame {
    jobject pad1;
    struct wl_surface *wl_surface;
    void *pad2;
    void *pad3;
    void *pad4;
    void *pad5;
    void *pad6;
    void *pad7;
    void *pad8;
    jboolean toplevel;
    union {
        struct xdg_toplevel *xdg_toplevel;
        struct xdg_popup *xdg_popup;
    };
    // more stuff follows, but we don't care about it
};

struct org_kde_kwin_appmenu_manager *org_kde_kwin_appmenu_manager = NULL;
struct zxdg_decoration_manager_v1 *zxdg_decoration_manager_v1 = NULL;

static void registry_global(void *data, struct wl_registry *registry, uint32_t name, const char *interface, uint32_t version) {
    if (strcmp(interface, org_kde_kwin_appmenu_manager_interface.name) == 0) {
        org_kde_kwin_appmenu_manager = wl_registry_bind(registry, name, &org_kde_kwin_appmenu_manager_interface, 1);
    } else if (strcmp(interface, zxdg_decoration_manager_v1_interface.name) == 0) {
        zxdg_decoration_manager_v1 = wl_registry_bind(registry, name, &zxdg_decoration_manager_v1_interface, 1);
    }
}

static void registry_global_remove(void *data, struct wl_registry *registry, uint32_t name) {
    // Do nothing
}

static const struct wl_registry_listener wl_registry_listener = {
        .global = registry_global,
        .global_remove = registry_global_remove,
};

JNIEXPORT void JNICALL Java_io_gitlab_jfronny_globalmenu_Native_init(JNIEnv *env, jobject obj, jlong ptr) {
    struct wl_display *wl_display = (struct wl_display *) ptr;
    struct wl_registry *wl_registry = wl_display_get_registry(wl_display);
    if (wl_registry == NULL) {
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), "Failed to get registry");
        return;
    }

    wl_registry_add_listener(wl_registry, &wl_registry_listener, NULL);
    if (wl_display_roundtrip(wl_display) < 0) {
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), "Failed to roundtrip");
        return;
    }
}

JNIEXPORT jlong JNICALL Java_io_gitlab_jfronny_globalmenu_Native_createMenu(JNIEnv *env, jobject obj, jlong ptr) {
    if (org_kde_kwin_appmenu_manager == NULL) {
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), "Appmenu manager not initialized");
        return 0;
    }
    struct WLFrame *frame = (struct WLFrame *) ptr;
    return (jlong) (intptr_t) org_kde_kwin_appmenu_manager_create(org_kde_kwin_appmenu_manager, frame->wl_surface);
}

JNIEXPORT void JNICALL Java_io_gitlab_jfronny_globalmenu_Native_destroyMenu(JNIEnv *env, jobject obj, jlong ptr) {
    struct org_kde_kwin_appmenu *frame = (struct org_kde_kwin_appmenu *) ptr;
    org_kde_kwin_appmenu_release(frame);
    org_kde_kwin_appmenu_destroy(frame);
}

JNIEXPORT void JNICALL Java_io_gitlab_jfronny_globalmenu_Native_setMenuAddress(JNIEnv *env, jobject obj, jlong ptr, jstring serviceName, jstring objectPath) {
    struct org_kde_kwin_appmenu *frame = (struct org_kde_kwin_appmenu *) ptr;
    char *service_name = (*env)->GetStringUTFChars(env, serviceName, NULL);
    char *object_path = (*env)->GetStringUTFChars(env, objectPath, NULL);
    org_kde_kwin_appmenu_set_address(frame, service_name, object_path);
    (*env)->ReleaseStringUTFChars(env, serviceName, service_name);
    (*env)->ReleaseStringUTFChars(env, objectPath, object_path);
}

JNIEXPORT jlong JNICALL Java_io_gitlab_jfronny_globalmenu_Native_createDecoration(JNIEnv *env, jobject obj, jlong ptr) {
    if (zxdg_decoration_manager_v1 == NULL) {
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), "Decoration manager not initialized");
        return 0;
    }
    struct WLFrame *frame = (struct WLFrame *) ptr;
    if (!frame->toplevel) {
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), "Not a toplevel");
        return 0;
    }
    return (jlong) (intptr_t) zxdg_decoration_manager_v1_get_toplevel_decoration(zxdg_decoration_manager_v1, frame->xdg_toplevel);
}

JNIEXPORT void JNICALL Java_io_gitlab_jfronny_globalmenu_Native_destroyDecoration(JNIEnv *env, jobject obj, jlong ptr) {
    struct zxdg_toplevel_decoration_v1 *frame = (struct zxdg_toplevel_decoration_v1 *) ptr;
    zxdg_toplevel_decoration_v1_unset_mode(frame);
    zxdg_toplevel_decoration_v1_destroy(frame);
}

JNIEXPORT void JNICALL Java_io_gitlab_jfronny_globalmenu_Native_setDecoration(JNIEnv *env, jobject obj, jlong ptr, jint mode) {
    struct zxdg_toplevel_decoration_v1 *frame = (struct zxdg_toplevel_decoration_v1 *) ptr;
    if (mode) {
        zxdg_toplevel_decoration_v1_set_mode(frame, mode);
    } else {
        zxdg_toplevel_decoration_v1_unset_mode(frame);
    }
}