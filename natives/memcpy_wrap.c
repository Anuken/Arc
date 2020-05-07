#ifndef __ANDROID__
#ifdef __linux__
#ifdef __x86_64__

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

__asm__(".symver memcpy,memcpy@GLIBC_2.2.5");
__asm__(".symver powf,powf@GLIBC_2.2.5");
__asm__(".symver logf,logf@GLIBC_2.2.5");
__asm__(".symver expf,expf@GLIBC_2.2.5");
__asm__(".symver pow,pow@GLIBC_2.2.5");

void *__wrap_memcpy(void * destination, const void * source, size_t num){
    return memcpy(destination, source, num);
}

float __wrap_expf(float x){
	return expf(x);
}

double __wrap_pow(double x, double y){
	return pow(x, y);
}

#endif
#endif
#endif
