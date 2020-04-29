#ifndef __PIX__
#define __PIX__

#include <stdint.h>

#ifndef NOJNI
#include <jni.h>
#else
#define JNIEXPORT
#endif

#ifdef __cplusplus
extern "C" {
#endif

/**
 * pixmap formats, components are laid out in memory
 * in the order they appear in the constant name. E.g.
 * PIX_FORMAT_RGB => pixmap[0] = r, pixmap[1] = g, pixmap[2] = b.
 * Components are 8-bit each except for RGB565 and RGBA4444 which
 * take up two bytes each. The order of bytes is machine dependent
 * within a short the high order byte holds r and the first half of g
 * the low order byte holds the lower half of g and b as well as a
 * if the format is RGBA4444
 */
#define PIX_FORMAT_ALPHA 				1
#define PIX_FORMAT_LUMINANCE_ALPHA 	2
#define PIX_FORMAT_RGB888 			3
#define PIX_FORMAT_RGBA8888			4
#define PIX_FORMAT_RGB565				5
#define PIX_FORMAT_RGBA4444			6

/**
 * blending modes, to be extended
 */
#define PIX_BLEND_NONE 		0
#define PIX_BLEND_SRC_OVER 	1

/**
 * scaling modes, to be extended
 */
#define PIX_SCALE_NEAREST		0
#define PIX_SCALE_BILINEAR	1

/**
 * simple pixmap struct holding the pixel data,
 * the dimensions and the format of the pixmap.
 * the format is one of the pix_FORMAT_XXX constants.
 */
typedef struct {
	uint32_t width;
	uint32_t height;
	uint32_t format;
	uint32_t blend;
	uint32_t scale;
	const unsigned char* pixels;
} pix_handle;

JNIEXPORT pix_handle* pix_load (const unsigned char *buffer, uint32_t len);
JNIEXPORT pix_handle* pix_new  (uint32_t width, uint32_t height, uint32_t format);
JNIEXPORT void 		 pix_free (const pix_handle* pixmap);

JNIEXPORT void pix_set_blend	  (pix_handle* pixmap, uint32_t blend);
JNIEXPORT void pix_set_scale	  (pix_handle* pixmap, uint32_t scale);

JNIEXPORT const char*   pix_get_failure_reason(void);
JNIEXPORT void		pix_clear	   	  (const pix_handle* pixmap, uint32_t col);
JNIEXPORT void		pix_set_pixel   (const pix_handle* pixmap, int32_t x, int32_t y, uint32_t col);
JNIEXPORT uint32_t pix_get_pixel	  (const pix_handle* pixmap, int32_t x, int32_t y);
JNIEXPORT void		pix_draw_line   (const pix_handle* pixmap, int32_t x, int32_t y, int32_t x2, int32_t y2, uint32_t col);
JNIEXPORT void		pix_draw_rect   (const pix_handle* pixmap, int32_t x, int32_t y, uint32_t width, uint32_t height, uint32_t col);
JNIEXPORT void		pix_draw_circle (const pix_handle* pixmap, int32_t x, int32_t y, uint32_t radius, uint32_t col);
JNIEXPORT void		pix_fill_rect   (const pix_handle* pixmap, int32_t x, int32_t y, uint32_t width, uint32_t height, uint32_t col);
JNIEXPORT void		pix_fill_circle (const pix_handle* pixmap, int32_t x, int32_t y, uint32_t radius, uint32_t col);
JNIEXPORT void		pix_fill_triangle(const pix_handle* pixmap,int32_t x1, int32_t y1, int32_t x2, int32_t y2, int32_t x3, int32_t y3, uint32_t col);
JNIEXPORT void		pix_draw_pixmap (const pix_handle* src_pixmap,
								   const pix_handle* dst_pixmap,
								   int32_t src_x, int32_t src_y, uint32_t src_width, uint32_t src_height,
								   int32_t dst_x, int32_t dst_y, uint32_t dst_width, uint32_t dst_height);

JNIEXPORT uint32_t pix_bytes_per_pixel(uint32_t format);

#ifdef __cplusplus
}
#endif

#endif
