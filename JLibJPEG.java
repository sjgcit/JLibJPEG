
/*
 * JLibJPEG.java
 *
 * $Id: JLibJPEG.java,v 1.17 2013/11/18 14:06:04 sjg Exp $
 *
 * (c) Stephen Geary, Sep 2013
 *
 * Java wrapper for JLibJPEG JNI interface
 */

import java.lang.* ;
import java.awt.* ;
import java.awt.color.* ;
import java.awt.image.* ;

import java.io.* ;

public class JLibJPEG
{
    public static native int getComponentCount( String fname ) ;
    
    public static native int getWidth( String fname ) ;

    public static native int getHeight( String fname ) ;
    
    public static native byte[] loadImageToByteArray( String fname, int[] sizes ) ;
    
    private static void _DEBUG( String str )
    {
        System.err.println( "JLibJPEG :: " + str ) ;
    }
    
    static
    {
        try
        {
            System.loadLibrary( "JLibJPEG" ) ;
        }
        catch( java.lang.UnsatisfiedLinkError usle )
        {
            _DEBUG( "Failed to load JLibJPEG native library" ) ;
            
            _DEBUG( usle.toString() ) ;
        }
    }
    
    public static BufferedImage loadImage( String fname )
    {
        BufferedImage bi = null ;
        
        int n = 0 ;
        int w = 0 ;
        int h = 0 ;
        
        int[] sizes = new int[3] ;
        
        byte ba[] = null ;
        
        ba = JLibJPEG.loadImageToByteArray( fname, sizes ) ;
        
        if( ba == null )
        {
            _DEBUG( "Did not get byte array from native code." ) ;
            
            return null ;
        }
        
        n = sizes[0] ;
        w = sizes[1] ;
        h = sizes[2] ;
        
        DataBufferByte dbb = null ;
        
        try
        {
            dbb = new DataBufferByte( ba, n*w*h ) ;
        }
        catch( ArrayStoreException ase )
        {
            ase.printStackTrace() ;
            
            return null ;
        }
        
        WritableRaster r = null ;
        
        ColorModel cm = null ;
        
        ColorSpace cs = null ;
        
        int[] nBits = null ;
        int[] bOffs = null ;
        
        switch( n )
        {
            case 1 : // TYPE_BYTE_GRAY
                cs = ColorSpace.getInstance( ColorSpace.CS_GRAY ) ;
                
                nBits = new int[] { 8 } ;
                
                bOffs = new int[] { 0 } ;
                
                break ;
            
            case 3 : // TYPE_3BYTE_BGR
                cs = ColorSpace.getInstance( ColorSpace.CS_sRGB ) ;
                
                nBits = new int[] { 8, 8, 8 } ;
                
                bOffs = new int[] { 2, 1, 0 } ;
                
                break ;
            
            default :
                /* An unsupported format
                 */
                break ;
        }
        
        if( cs == null )
        {
            return null ;
        }
        
        cm = new ComponentColorModel( cs, nBits, false, false,
                                              Transparency.OPAQUE,
                                              DataBuffer.TYPE_BYTE ) ;
        
        if( cm == null )
        {
            return null ;
        }
        
        r = Raster.createInterleavedRaster( dbb,
                                            w, h,
                                            n*w, n,
                                            bOffs, null) ;
        
        if( r == null )
        {
            return null ;
        }
        
        bi = new BufferedImage( cm, r, false, null ) ;
        
        // _DEBUG( "Success" ) ;
        
        return bi ;
    }
}

