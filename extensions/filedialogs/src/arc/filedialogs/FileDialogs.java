package arc.filedialogs;

import arc.util.*;

public class FileDialogs{
    /*JNI
	#include <tinyfiledialogs.h>

	 */

    public static void loadNatives() throws UnsatisfiedLinkError{
        new SharedLibraryLoader().load("arc-filedialogs");
    }

    /*

    char const * tinyfd_saveFileDialog (
	char const * const aTitle , /* ""
    char const * const aDefaultPathAndFile , /* ""
    int const aNumOfFilterPatterns , /* 0
    char const * const * const aFilterPatterns , /* NULL | {"*.jpg","*.png"}
    char const * const aSingleFilterDescription ) ; /* NULL | "text files"
    /* returns NULL on cancel
     */

    public static native @Nullable String saveFileDialog(String title, String defaultPathAndFile, @Nullable String[] patterns, @Nullable String filterDescription); /*
        const char *param[20];
        jsize stringCount = 0;
        if(patterns != NULL){
            stringCount = (*env).GetArrayLength(patterns);

            for(int i = 0; i < stringCount; i++){
                param[i] = env->GetStringUTFChars((jstring)env->GetObjectArrayElement(patterns, i), NULL);
            }
        }

        const char* result = tinyfd_saveFileDialog(title, defaultPathAndFile, stringCount, param, filterDescription);

        if(patterns != NULL){
            for(int i = 0; i < stringCount; i++){
                env->ReleaseStringUTFChars((jstring)env->GetObjectArrayElement(patterns, i), param[i]);
            }
        }

        return env->NewStringUTF(result);
    */

    /*
    char const * tinyfd_openFileDialog (
	char const * const aTitle ,  ""
    char const * const aDefaultPathAndFile ,
    int const aNumOfFilterPatterns ,  0
    char const * const * const aFilterPatterns ,  NULL {"*.jpg","*.png"}
    char const * const aSingleFilterDescription ,  NULL | "image files"
    int const aAllowMultipleSelects )
    */

    public static native @Nullable String openFileDialog(String title, String defaultPathAndFile, @Nullable String[] patterns, @Nullable String filterDescription, boolean allowMultipleSelects); /*
        const char *param[20];
        jsize stringCount = 0;
        if(patterns != NULL){
            stringCount = (*env).GetArrayLength(patterns);

            for(int i = 0; i < stringCount; i++){
                param[i] = env->GetStringUTFChars((jstring)env->GetObjectArrayElement(patterns, i), NULL);
            }
        }

        const char* result = tinyfd_openFileDialog(title, defaultPathAndFile, stringCount, param, filterDescription, allowMultipleSelects);

        if(patterns != NULL){
            for(int i = 0; i < stringCount; i++){
                env->ReleaseStringUTFChars((jstring)env->GetObjectArrayElement(patterns, i), param[i]);
            }
        }

        return env->NewStringUTF(result);
    */
}
