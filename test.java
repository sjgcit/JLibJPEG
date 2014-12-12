
/*
 * $Id: test.java,v 1.7 2013/11/18 14:06:04 sjg Exp $
 */

import java.lang.* ;
import java.io.* ;
import java.awt.image.* ;
import javax.imageio.* ;

public class test
{
    public static void debug( Object obj )
    {
        System.out.println( obj.toString() ) ;
    }

    public static void main( String[] args )
    {
        int n = -1 ;
        int w = -1 ;
        int h = -1 ;
        
        n = JLibJPEG.getComponentCount( args[0] ) ;
        
        if( n == -1 )
        {
            return ;
        }
        
        w = JLibJPEG.getWidth( args[0] ) ;
        
        if( w == -1 )
        {
            return ;
        }
        
        h = JLibJPEG.getHeight( args[0] ) ;
        
        debug( "Image is " + w + " by " + h + " with " + n + " components" ) ;
        
        long t1, t2 ;
        
        BufferedImage bi = null ;
        
        t1 = System.nanoTime() ;
        
        bi = JLibJPEG.loadImage( args[0] ) ;
        
        t2 = System.nanoTime() ;
        
        debug( "JLibJPEG.loadImage() : Time taken : " + ( t2-t1 ) ) ;
        
        t1 = System.nanoTime() ;
        
        try
        {
            bi = ImageIO.read( new File( args[0] ) ) ;
        }
        catch( IOException ioe )
        {
            debug( ioe ) ;
        }
        
        t2 = System.nanoTime() ;
        
        debug( "ImageIO.read()      : Time taken : " + ( t2-t1 ) ) ;
        
        debug( bi ) ;
    }
}
