#ifndef __ANDROID__
#ifdef __linux__
#ifdef __x86_64__

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <time.h>

__asm__(".symver memcpy,memcpy@GLIBC_2.2.5");
__asm__(".symver powf,powf@GLIBC_2.2.5");
__asm__(".symver logf,logf@GLIBC_2.2.5");
__asm__(".symver log,log@GLIBC_2.2.5");
__asm__(".symver expf,expf@GLIBC_2.2.5");
__asm__(".symver exp,exp@GLIBC_2.2.5");
__asm__(".symver pow,pow@GLIBC_2.2.5");
__asm__(".symver clock_gettime,clock_gettime@GLIBC_2.2.5");

void *__wrap_memcpy(void * destination, const void * source, size_t num){
    return memcpy(destination, source, num);
}

int __wrap_clock_gettime(clockid_t clockid, struct timespec *tp){
	return clock_gettime(clockid, tp);
}

float __wrap_expf(float x){
	return expf(x);
}

double __wrap_exp(float x){
	return exp(x);
}

double __wrap_log(double x){
        return log(x);
}

double __wrap_pow(double x, double y){
	return pow(x, y);
}

double __wrap_powf(float x, float y){
	return powf(x, y);
}

#endif
#endif
#endif
