#include <jni.h>
#include <stdio.h>
#include <string.h>
#include "wayland-client-protocol.h"
#include "appmenu.h"

struct WLFrame {
    jobject pad1;
    struct wl_surface *wl_surface;
    // more stuff follows, but we don't care about it
};

struct org_kde_kwin_appmenu_manager *org_kde_kwin_appmenu_manager = NULL;

static void registry_global(void *data, struct wl_registry *registry, uint32_t name, const char *interface, uint32_t version) {
    if (strcmp(interface, "org_kde_kwin_appmenu_manager") == 0) {
        org_kde_kwin_appmenu_manager = wl_registry_bind(registry, name, &org_kde_kwin_appmenu_manager_interface, 1);
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

JNIEXPORT jlong JNICALL Java_io_gitlab_jfronny_globalmenu_Native_create(JNIEnv *env, jobject obj, jlong ptr) {
    if (org_kde_kwin_appmenu_manager == NULL) {
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), "Appmenu manager not initialized");
        return 0;
    }
    struct WLFrame *frame = (struct WLFrame *) ptr;
    return (jlong) (intptr_t) org_kde_kwin_appmenu_manager_create(org_kde_kwin_appmenu_manager, frame->wl_surface);
}

JNIEXPORT void JNICALL Java_io_gitlab_jfronny_globalmenu_Native_destroy(JNIEnv *env, jobject obj, jlong ptr) {
    struct org_kde_kwin_appmenu *frame = (struct org_kde_kwin_appmenu *) ptr;
    org_kde_kwin_appmenu_release(frame);
    org_kde_kwin_appmenu_destroy(frame);
}

JNIEXPORT void JNICALL Java_io_gitlab_jfronny_globalmenu_Native_setAddress(JNIEnv *env, jobject obj, jlong ptr, jstring serviceName, jstring objectPath) {
    struct org_kde_kwin_appmenu *frame = (struct org_kde_kwin_appmenu *) ptr;
    char *service_name = (*env)->GetStringUTFChars(env, serviceName, NULL);
    char *object_path = (*env)->GetStringUTFChars(env, objectPath, NULL);
    org_kde_kwin_appmenu_set_address(frame, service_name, object_path);
    (*env)->ReleaseStringUTFChars(env, serviceName, service_name);
    (*env)->ReleaseStringUTFChars(env, objectPath, object_path);
}