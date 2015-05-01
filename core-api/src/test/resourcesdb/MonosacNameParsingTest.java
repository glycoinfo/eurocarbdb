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
*   Last commit: $Rev: 1932 $ by $Author: glycoslave $ on $Date:: 2010-08-05 #$  
*/

package test.resourcesdb;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.*;

import org.eurocarbdb.resourcesdb.monosaccharide.Monosaccharide;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.GlycanNamescheme;

import static org.eurocarbdb.resourcesdb.GlycanNamescheme.IUPAC;
import static org.eurocarbdb.resourcesdb.GlycanNamescheme.MONOSACCHARIDEDB;


@Test( groups="resourcesdb.parsing", sequential=true )
public class MonosacNameParsingTest 
{
    
    /** Iupac names of some common monosaccharides */
    public static final List<String> simpleMonosacs 
        = Arrays.asList( 
              "a-D-Manp"
            , "b-D-Manp"
            , "b-D-Manf"
            , "b-d-Manp"
            , "B-d-Manp"
            , "a-Man"
            , "Manp"
            , "Man"
            , "b-D-Galp"
            , "Galf"
            , "Gal"
            , "b-D-Xylf"
            , "b-D-Araf"
            , "b-D-Ribf"
            , "b-D-Eryf"
        );                                      

    /** Iupac names of some common open-chain monosaccharides */        
    public static final List<String> openchainMonosacs
        = Arrays.asList(
            "aldehydo-l-gal"
            , "l-gal-aric"
            , "keto-d-fru"
            , "keto-d-xylhex2ulo"
            , "aldehydo-d-xylhex1,2diulo"
        );
        
    /** Iupac names of some common sialic acids */
    public static final List<String> sialicMonosacs
        = Arrays.asList(                   
            "a-D-neup"
            , "a-D-neup5ac"
            , "a-D-neup5nac"
            , "a-D-neup5gc"
            , "a-D-neup5ngc"
            , "a-D-neup5ac8ac"
            , "a-d-NeupAc"
            , "d-gro-a-d-3-deoxy-galnon2ulop5NAc-onic"
        );          

    /** Iupac names of some monosaccharides that have had a loss of 
    *   one or more stereochemical centres. */                
    public static final List<String> stereolossMonosacs
        = Arrays.asList(                   
            "a-d-4-deoxy-Glcp3en3OMe"
            , "a-d-glc2ulop"
            , "b-D-Glcp2NH"     
            , "b-D-4-deoxy-Glcp"
            , "b-D-2-deoxy-ManpA"
        );           
                   
    /** Iupac names of some indefinite (partially unknown) monosaccharides */
    public static final List<String> indefiniteMonosacs
        = Arrays.asList(                   
            "a-?-Fucp"
            , "D-Glc"
            , "?-D-Glc"
            , "?-L-Fuc"
        );           

    /** Iupac names of some monosaccharides with deliberate syntactic or semantic errors.
    *   All of these are expected to throw exceptions */
    public static final List<String> brokenMonosacs
        = Arrays.asList(                   
            "a-?-Fluc"              
            , "6-deoxy-Xylp"
            , "a-d-7-deoxy-manHepp"
        );           

    /** Iupac names of some large monosaccharides */
    public static final List<String> supersizeMonosacs
        = Arrays.asList(                   
            "a-D-Hepp"
            , "D-gro-a-D-manHepp"
            , "D-gro-a-D-Hepp"
            , "a-d-6-deoxy-manHepp"
            , "d-gro-a-d-7-deoxy-manHepp"
        );           
                    
        
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ TESTS ~~~~~~~~~~~~~~~~~~~~~~~~~~
        
    @Test
    public void resourcesdbParseSimple()
    {
        System.out.println("parsing simple monosacs...");
        parseList( simpleMonosacs );
    }
    

    @Test
    public void resourcesdbParseOpenchain()
    {
        System.out.println("parsing openchain monosacs...");
        parseList( openchainMonosacs );
    }
    

    @Test
    public void resourcesdbParseSialic()
    {
        System.out.println("parsing sialic monosacs...");
        parseList( sialicMonosacs );
    }
    

    @Test
    public void resourcesdbParseStereoloss()
    {
        System.out.println("parsing stereoloss monosacs...");
        parseList( stereolossMonosacs );
    }
    

    @Test
    public void resourcesdbParseIndefinite()
    {
        System.out.println("parsing indefinite monosacs...");
        parseList( indefiniteMonosacs );
    }
    
    
    @Test
    public void resourcesdbParseSupersize()
    {
        System.out.println("parsing supersized monosacs...");
        parseList( supersizeMonosacs );
    }
    
    
    @Test
    public void resourcesdbParseIncorrect()
    {
        System.out.println("parsing deliberately broken monosacs...");
        parseIncorrect( brokenMonosacs );
    }
    
    
    static final List<String> commonSubstituents = Arrays.asList(
        "n-acetyl"//,
        // "deoxy" - doesn't work
        //"nh2"    - doesn't work
    );
    
    
    /** Test not enabled -- Resourcesdb has bugs with name generation */
    @Test( enabled=false )
    public void resourcesdbAddSubstituents()
    throws ResourcesDbException
    {
        System.out.println("trying to add various substituents...");
        
        System.out.println();
        System.out.println("--- simple monosacs ---");
        generateNameFor( simpleMonosacs );
        
        System.out.println();
        System.out.println("--- indefinite monosacs ---");
        generateNameFor( indefiniteMonosacs );

        System.out.println();
        System.out.println("--- supersized monosacs ---");
        generateNameFor( supersizeMonosacs );

        System.out.println();
        System.out.println("--- open chain monosacs ---");
        generateNameFor( openchainMonosacs );
        
    }
    
    
    /** Test not enabled -- Resourcesdb mass calculation doesn't work */
    @Test( enabled=false )
    public void resourcesdbCalculateMass()
    throws ResourcesDbException
    {
        Monosaccharide m;
        for ( String name : simpleMonosacs )
        {
            m = new Monosaccharide( IUPAC, name );//parseIupac( name );
            double mass = m.getMonoMass();
            System.out.println( name + ": " + mass );
        }
    }
    

    // private methods ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    private final void generateNameFor( List<String> list )
    throws ResourcesDbException
    {
        long start;
        int errors = 0;
        
        for ( String monosac_name : list )
        {
            for ( String substit_name : commonSubstituents )
            {
                System.out.println( 
                    "trying "
                    + monosac_name 
                    + " + "
                    + substit_name
                    + ":"
                );
                
                Monosaccharide m = parseIupac( monosac_name );
                try 
                {
                    m.addSubstitution( substit_name, 2 );
                    System.out.println("addition of substituent ok");
                }
                catch ( ResourcesDbException ex )
                {
                    System.out.println("addition of substituent error: " + ex.getMessage() );
                    errors++;
                    continue;
                }
                
                start = now();
                String name = getMonosacNameFor( m ); 
                
                System.out.println( 
                    "generated name: "
                    + name 
                    + " ("
                    + (now() - start)
                    + "msec )"
                );
                
                //  check that MSDB can parse its own generated name
                try
                {
                    m = parseMsdb( name );
                    System.out.println("generated name parsed ok");
                    System.out.println();
                }
                catch ( ResourcesDbException ex )
                {
                    System.out.println(
                        "generated name error: MSDB failed to parse generated name: " 
                        + ex.getMessage() 
                    );
                    System.out.println();
                    errors++;
                    // throw ex;
                }
            }
        }
        
        if ( errors > 0 )
        {
            throw new RuntimeException(
                errors 
                + " sequences that were generated by MSDB failed to parse."
                + " check the test output for details."
            );
        }
    }
    
    
    private final String getMonosacNameFor( Monosaccharide m )
    throws ResourcesDbException
    {
        // try
        // {
            m.buildName();
        // }
        // catch ( ResourcesDbException ex )
        // {
        //     return "error: " + ex.toString() + "\n";       
        // }
        
        return m.getName();
    }
    
     
    private final void parseIncorrect( List<String> names )
    {
        int successful = 0, failed = 0;

        for ( String name : names )
        {
            try
            {
                parseIupac( name );
                System.out.println( name  + ": failed, expected an exception" );
                failed++;
            }
            catch ( Exception ex )
            {
                System.out.println( name + ": successful, exception was: " + ex );
                successful++;
            }
        }
        
        System.out.println();
        report( successful, failed );
    }
    
    
        
    private final void parseList( List<String> names )
    {
        int successful = 0, failed = 0;
        
        long loop_start = now();
        for ( String name : names )
        {
            long seq_start = now();
            try
            {
                parseIupac( name );   
                System.out.println( name + ": successful, " + (now() - seq_start) + "msec");
                successful++;
            }
            catch ( Exception ex )
            {
                System.out.println( name + ": failed, " + (now() - seq_start) + "msec");
                System.out.println( "-> " + ex.toString() );
                failed++;
            }
        }
        
        System.out.println();
        System.out.println(
            "average parse time: " 
            + ((now() - loop_start) / (successful + failed))
            + "msec/monosac"
        );

        report( successful, failed );
    }
  
    
    private final Monosaccharide parseIupac( String monosac_name )
    throws ResourcesDbException
    {
        return new Monosaccharide( IUPAC, monosac_name );
    }
    
    
    private final Monosaccharide parseMsdb( String monosac_name )
    throws ResourcesDbException
    {
        return new Monosaccharide( MONOSACCHARIDEDB, monosac_name );
    }
    
    
    private static final void report( int successful, int failed )
    {
        int total = successful + failed;
        
        System.out.println(
            ""
            + total    
            + " monosacs tested, "
            + successful 
            + " successful, "
            + failed 
            + " failed"
        );

        System.out.println();
        
        if ( failed > 0 )
        {
            throw new RuntimeException(
                "" 
                + failed 
                + " of " 
                + total 
                + " sequences failed"
            );
        }
    }
        
    private static final long now()
    {
        return System.currentTimeMillis();   
    }
}



