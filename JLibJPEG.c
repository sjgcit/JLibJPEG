
/*
 * JLibJPEG.c
 *
 * $Id: JLibJPEG.c,v 1.14 2013/11/18 14:06:04 sjg Exp $
 *
 * (c) Stephen Geary, Sep 2013
 *
 * Native code interface
 */

#include <jni.h>

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include <jpeglib.h>

#include "JLibJPEG.h"


#define getString(_js)          (*env)->GetStringUTFChars( env, _js, NULL)

#define releaseString(_js,_cp)  (*env)->ReleaseStringUTFChars( env, _js, _cp )


#define JLibJPEG_FETCH_HEADER \
    struct jpeg_decompress_struct cinfo ; \
    struct jpeg_error_mgr jerr ; \
    \
    jboolean fileopened = JNI_FALSE ; \
    \
    FILE *infile = NULL ; \
    \
    const char *fn = NULL ; \
    \
    fn = getString( fname ) ; \
    \
    if( fn == NULL ) \
    { \
        goto ret_error ; \
    } \
    \
    infile = fopen( fn, "r" ) ; \
    \
    if( infile == NULL ) \
    { \
        goto ret_error ; \
    } \
    \
    fileopened = JNI_TRUE ; \
    \
    cinfo.err = jpeg_std_error( &jerr ) ; \
    \
	jpeg_create_decompress( &cinfo ) ; \
    \
    jpeg_stdio_src( &cinfo, infile ) ; \
    \
    jpeg_read_header( &cinfo, TRUE ) ;


#define JLibJPEG_Tidyup \
    if( fileopened ) \
    { \
    	jpeg_destroy_decompress( &cinfo ) ; \
	} \
    \
    if( infile != NULL ) \
    { \
        fclose( infile ) ; \
    } \
    \
    if( fn != NULL ) \
    { \
        releaseString( fname, fn ) ; \
    }


#define FUNCTION_get(_name,_field) \
    JNIEXPORT jint JNICALL Java_JLibJPEG_get##_name( JNIEnv *env, jclass cls, jstring fname ) \
    { \
        int retval = 0 ; \
        \
        JLibJPEG_FETCH_HEADER \
        \
        retval = _field ; \
        \
        JLibJPEG_Tidyup \
        \
        return (jint)retval ; \
        \
    ret_error: \
        \
        JLibJPEG_Tidyup ; \
        \
        return -1 ; \
    }


FUNCTION_get( ComponentCount, cinfo.num_components )

FUNCTION_get( Width,          cinfo.image_width )

FUNCTION_get( Height,         cinfo.image_height )




JNIEXPORT jbyteArray JNICALL Java_JLibJPEG_loadImageToByteArray(
                            JNIEnv *env, jclass cls,
                            jstring fname,
                            jintArray sizearray )
{
    jbyteArray jba = NULL ;
    
    JLibJPEG_FETCH_HEADER
    
    switch( cinfo.num_components )
    {
        case 1 :
            cinfo.out_color_space = JCS_GRAYSCALE ;
            break ;
            
        case 3 :
            cinfo.out_color_space = JCS_EXT_BGR ;
            break ;
        
        default :
            goto ret_error ;
            break ;
    }
    
    /* return size info
     */
     
    jint *sizes = NULL ;
    
    sizes = (jint *)( (*env)->GetPrimitiveArrayCritical( env, sizearray, NULL ) ) ;
    
    if( sizes != NULL )
    {
        sizes[0] = (jint)( cinfo.num_components ) ;
        sizes[1] = (jint)( cinfo.image_width ) ;
        sizes[2] = (jint)( cinfo.image_height ) ;
    }
    
    (*env)->ReleasePrimitiveArrayCritical( env, sizearray, (jint *)sizes, 0 ) ;
    
    if( sizes == NULL )
    {
        printf( "%s :: % 4d :: Could not get new sizes[] array\n", __FILE__, __LINE__ ) ;
    
        goto ret_error ;
    }
    
    /* create the byte array for the data
     */
    
    jba = (*env)->NewByteArray( env, cinfo.num_components * cinfo.image_width * cinfo.image_height ) ;
    
    if( jba == NULL )
    {
        printf( "%s :: % 4d :: Could not get new jba[] array\n", __FILE__, __LINE__ ) ;
    
        goto ret_error ;
    }

    /* decompress the image into the array
     */
    
    jpeg_start_decompress( &cinfo ) ;
    
    unsigned char *raw_image = NULL ;
    
    jboolean iscopy = JNI_FALSE ;
    
    raw_image = (char *)( (*env)->GetPrimitiveArrayCritical( env, jba, &iscopy  ) ) ;
    
    if( iscopy )
    {
        printf( "%s :: % 4d :: Recieved COPY of byte array.\n", __FILE__, __LINE__ ) ;
    }
    
    if( raw_image != NULL )
    {
	    /* now actually read the jpeg into the raw buffer */
	    
	    int scanlen = cinfo.output_width * cinfo.num_components ;
	
	        /* read one scan line at a time */
	
            unsigned long location = 0;
            
            char *p = NULL ;

	        while( cinfo.output_scanline < cinfo.image_height )
	        {
                p = &( raw_image[location] ) ;
                
		        jpeg_read_scanlines( &cinfo, (JSAMPARRAY)( &p ) , 1 ) ;
		        
		        location += scanlen ;
            } ;
            
            (*env)->ReleasePrimitiveArrayCritical( env, jba, (jbyte *)raw_image, 0 ) ;
    }
    
    
    jpeg_finish_decompress( &cinfo ) ;

    JLibJPEG_Tidyup
    
    return jba ;

ret_error:

    JLibJPEG_Tidyup ;

    return NULL ;
}

