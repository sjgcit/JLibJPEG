
/*
 * debug.java
 *
 * $Id: debug.java,v 1.5 2013/08/27 12:52:46 sjg Exp $
 *
 * (c) Stephen Geary, Sept 2012
 *
 * Debugging aids.
 */

import java.lang.* ;

public class debug
{
    public static long id = 0 ;
    
    public static void header( int linen )
    {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[3] ;

        String linenum    = null ;
        
        if( linen == -1 )
        {
            linenum = String.format( "% 6d", ste.getLineNumber() ) ;
        }
        else
        {
            linenum = String.format( "% 6d", linen ) ;
        }
        
        String classname  = ste.getClassName() ;
        String methodname = ste.getMethodName() ;
        
        String idstr = String.format( "% 6d", id ) ;
    
        debug.id++ ;
    
        System.out.print( idstr + " :: " + classname + " :: " + linenum + " :: " + methodname + " :: " ) ;
    }
    
    public static synchronized void debug( Object... objs )
    {
        debug.header( -1 ) ;
        
        debug.debugfn( 0, objs ) ;
    }
    
    public static synchronized void debugline( int linenum, Object... objs )
    {
        debug.header( linenum ) ;
        
        debug.debugfn( 0, objs ) ;
    }
    
    
    private static void debugfn( int offset, Object[] objs )
    {
        Object s = null ;
        
        int i = 0 ;
    
        for( i = offset ; i < objs.length ; i++ )
        {
            s = objs[i] ;
        
            if( s instanceof Object[] )
            {
                Object[] a = (Object[])s ;
    
                for( Object ae : a )
                {
                    System.out.print( ae + " " ) ;
                }
            }
            else
            {
                System.out.print( s + " " ) ;
            }
        }
        
        System.out.println( "" ) ;
        
        System.out.flush() ;
    }
    
    public static synchronized void debugArray( Object objs )
    {
        debug.header( -1 ) ;
    
        Object[] a = (Object[])objs ;
    
        for( Object s : a )
        {
            System.out.print( s + " " ) ;
        }
        
        System.out.println( "" ) ;
        
        System.out.flush() ;
    }
}
