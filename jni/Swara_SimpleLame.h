/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class Swara_SimpleLame */

#ifndef _Included_Swara_SimpleLame
#define _Included_Swara_SimpleLame
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     Swara_SimpleLame
 * Method:    init
 * Signature: (IIIII)V
 */
JNIEXPORT void JNICALL Java_Swara_SimpleLame_init
  (JNIEnv *, jclass, jint, jint, jint, jint, jint);

/*
 * Class:     Swara_SimpleLame
 * Method:    encode
 * Signature: ([S[SI[B)I
 */
JNIEXPORT jint JNICALL Java_Swara_SimpleLame_encode
  (JNIEnv *, jclass, jshortArray, jshortArray, jint, jbyteArray);

/*
 * Class:     Swara_SimpleLame
 * Method:    flush
 * Signature: ([B)I
 */
JNIEXPORT jint JNICALL Java_Swara_SimpleLame_flush
  (JNIEnv *, jclass, jbyteArray);

/*
 * Class:     Swara_SimpleLame
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_Swara_SimpleLame_close
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
