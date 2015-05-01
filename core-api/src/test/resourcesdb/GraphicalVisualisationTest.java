/*
*   EuroCarbDB, a framework for carbohydrate bioinformatics
*
*   Copyright (c) 2006-2009, Eurocarb project, or third-party contributors as
*   indicated by the @author tags or express copyright attribution
*   statements applied by the authors.  
*
*   This copyrighted material is made available to anyone wishing to use, modify,
*   copy, or redistribute it subject to the terms and conditions of the GNU
*   Lesser General Public License, as published by the Free Software Foundation.
*   A copy of this license accompanies this distribution in the file LICENSE.txt.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
*   or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
*   for more details.
*
*   Last commit: $Rev: 1147 $ by $Author: glycoslave $ on $Date:: 2009-06-04 #$  
*/

package test.resourcesdb;

import java.io.Writer;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.*;

import org.eurocarbdb.resourcesdb.monosaccharide.Monosaccharide;
import org.eurocarbdb.resourcesdb.representation.Haworth;
import org.eurocarbdb.resourcesdb.representation.Fischer;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.GlycanNamescheme;

import static org.eurocarbdb.resourcesdb.GlycanNamescheme.IUPAC;
import static test.FileReporterListener.getTestOutputDirectory;


public class GraphicalVisualisationTest
{
    
    /** Iupac names of some common monosaccharides */
    public static final List<String> commonMonosacs 
        = Arrays.asList( 
            "a-D-Manp"
            , "b-D-Manp"
            , "b-D-Manf"
            , "b-D-Galp"
            , "b-D-Xylf"
            , "b-D-Araf"
            , "b-D-Ribf"
            , "b-D-Eryf"
        );
    
    
    @Test( groups="resourcesdb.visualisation" )
    /** Draws a bunch of haworth projections of the common monosaccharides 
    *   listed in {@link #monosacs}. 
    */
    public void resourcesdbDrawHaworth()
    {
        for ( String name : commonMonosacs )
            drawHaworth( IUPAC, name );
    }

    
    
    
    
    //~~~~ utility methods ~~~~

    /** 
    *   Draws a haworth projection of monosaccharide with given name 
    *   to <code>{@link FileReporterListener.getTestOutputDirectory()} 
    *   + '/' + getClass().getName() + '.' + name + ".haworth.svg"</code>. 
    *
    *   @see FileReporterListener.getTestOutputDirectory()
    *   @see Haworth
    */
    void drawHaworth( GlycanNamescheme format, String name )
    {
        System.out.println();
        System.out.println("trying to draw monsac '" + name + "':");
        long start = now();
        try 
        {
            Haworth pic = new Haworth();
            System.out.println("init took " + (now() - start) + "msec");
            
            System.out.println("parsing...");
            
            start = now();
            Monosaccharide man = new Monosaccharide( format, name );
            System.out.println("parse took " + (now() - start) + "msec");
            
            System.out.println("drawing...");
            
            start = now();
            pic.drawMonosaccharide( man );
            System.out.println("draw took " + (now() - start) + "msec");
            
            String file_name = getTestOutputDirectory()
                                + '/'
                                + getClass().getName() 
                                + '.'  
                                + name  
                                + ".haworth"
                                + ".svg"; 
            
            System.out.println("writing to file '" + file_name + "'...");
            Writer out = new BufferedWriter( new FileWriter( file_name ));
            
            start = now();
            pic.stream( out );    
            System.out.println("save to file " + (now() - start) + "msec");
        }
        catch ( Exception ex )
        {
            throw new RuntimeException( ex );   
        }
    }
    
    
    /*#*** fischer projections are not yet implemented in resourcesdb ***
    
    @Test( groups="resourcesdb" )
    public void resourcesdbDrawFischer()
    {
        for ( String name : monosacs )
            drawFischer( name );
    }


    void drawFischer( String name )
    {
        System.out.println();
        System.out.println("trying to draw monsac '" + name + "':");
        long start = now();
        try 
        {
            Fischer pic = new Fischer();
            System.out.println("init took " + (now() - start) + "msec");
            
            System.out.println("parsing...");
            
            start = now();
            Monosaccharide man = new Monosaccharide( IUPAC, name );
            System.out.println("parse took " + (now() - start) + "msec");
            
            System.out.println("drawing...");
            
            start = now();
            pic.drawMonosaccharide( man );
            System.out.println("draw took " + (now() - start) + "msec");
            
            String file_name = getTestOutputDirectory()
                                + '/'
                                + getClass().getName() 
                                + '.'  
                                + name
                                + ".fischer"
                                + ".svg"; 
            
            System.out.println("writing to file '" + file_name + "'...");
            Writer out = new BufferedWriter( new FileWriter( file_name ));
            
            start = now();
            pic.stream( out );    
            System.out.println("save to file " + (now() - start) + "msec");
        }
        catch ( Exception ex )
        {
            throw new RuntimeException( ex );   
        }
    }
    */


    private static final long now()
    {
        return System.currentTimeMillis();   
    }
}



