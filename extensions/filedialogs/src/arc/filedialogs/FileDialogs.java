package arc.filedialogs;

import arc.util.*;

public class FileDialogs{
    /*JNI

	#include <tinyfiledialogs.h>

	#ifdef __WIN32__
	#include <cwchar>
	#endif

	 */

    public static void loadNatives() throws UnsatisfiedLinkError{
        new SharedLibraryLoader().load("arc-filedialogs");
    }

    //note: windows versions have to manually use UTF-16 versions of string because windows sucks

    public static native @Nullable String saveFileDialog(String obj_title, String obj_defaultPathAndFile, String[] patterns, String obj_filterDescription); /*MANUAL
        #ifdef __WIN32__
            const jchar* title = env->GetStringChars(obj_title, 0);
	        const jchar* defaultPathAndFile = env->GetStringChars(obj_defaultPathAndFile, 0);
	        const jchar* filterDescription = env->GetStringChars(obj_filterDescription, 0);


            const jchar *param[20];
            jsize stringCount = 0;
            if(patterns != NULL){
                stringCount = (*env).GetArrayLength(patterns);

                for(int i = 0; i < stringCount; i++){
                    param[i] = env->GetStringChars((jstring)env->GetObjectArrayElement(patterns, i), NULL);
                }
            }

            const wchar_t* result = tinyfd_saveFileDialogW((wchar_t*)title, (wchar_t*)defaultPathAndFile, stringCount, (wchar_t**)param, (wchar_t*)filterDescription);

            if(patterns != NULL){
                for(int i = 0; i < stringCount; i++){
                    env->ReleaseStringChars((jstring)env->GetObjectArrayElement(patterns, i), param[i]);
                }
            }

            env->ReleaseStringChars(obj_title, title);
	        env->ReleaseStringChars(obj_defaultPathAndFile, defaultPathAndFile);
	        env->ReleaseStringChars(obj_filterDescription, filterDescription);

            return result == NULL ? NULL : env->NewString((jchar*)result, wcslen(result));
        #else
            char* title = (char*)env->GetStringUTFChars(obj_title, 0);
            char* defaultPathAndFile = (char*)env->GetStringUTFChars(obj_defaultPathAndFile, 0);
            char* filterDescription = (char*)env->GetStringUTFChars(obj_filterDescription, 0);


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

            env->ReleaseStringUTFChars(obj_title, title);
	        env->ReleaseStringUTFChars(obj_defaultPathAndFile, defaultPathAndFile);
	        env->ReleaseStringUTFChars(obj_filterDescription, filterDescription);

            return env->NewStringUTF(result);

        #endif
    */

    public static native @Nullable String openFileDialog(String obj_title, String obj_defaultPathAndFile, String[] patterns, String obj_filterDescription, boolean allowMultipleSelects); /*MANUAL
        #ifdef __WIN32__
            const jchar* title = env->GetStringChars(obj_title, 0);
	        const jchar* defaultPathAndFile = env->GetStringChars(obj_defaultPathAndFile, 0);
	        const jchar* filterDescription = env->GetStringChars(obj_filterDescription, 0);

            const jchar *param[20];
            jsize stringCount = 0;
            if(patterns != NULL){
                stringCount = (*env).GetArrayLength(patterns);

                for(int i = 0; i < stringCount; i++){
                    param[i] = env->GetStringChars((jstring)env->GetObjectArrayElement(patterns, i), NULL);
                }
            }

            const wchar_t* result = tinyfd_openFileDialogW((wchar_t*)title, (wchar_t*)defaultPathAndFile, stringCount, (wchar_t**)param, (wchar_t*)filterDescription, allowMultipleSelects);

            if(patterns != NULL){
                for(int i = 0; i < stringCount; i++){
                    env->ReleaseStringChars((jstring)env->GetObjectArrayElement(patterns, i), param[i]);
                }
            }

            env->ReleaseStringChars(obj_title, title);
	        env->ReleaseStringChars(obj_defaultPathAndFile, defaultPathAndFile);
	        env->ReleaseStringChars(obj_filterDescription, filterDescription);

            return result == NULL ? NULL : env->NewString((jchar*)result, wcslen(result));

        #else
        	char* title = (char*)env->GetStringUTFChars(obj_title, 0);
	        char* defaultPathAndFile = (char*)env->GetStringUTFChars(obj_defaultPathAndFile, 0);
	        char* filterDescription = (char*)env->GetStringUTFChars(obj_filterDescription, 0);

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

            env->ReleaseStringUTFChars(obj_title, title);
	        env->ReleaseStringUTFChars(obj_defaultPathAndFile, defaultPathAndFile);
	        env->ReleaseStringUTFChars(obj_filterDescription, filterDescription);

            return env->NewStringUTF(result);

        #endif
    */
}
