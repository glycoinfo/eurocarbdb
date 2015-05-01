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

package test.eurocarbdb;

import java.util.List;
import java.util.ArrayList;
import org.testng.*;
import org.apache.log4j.Logger;
import test.FileReporterListener;

/**
*   This is a convenience base class for other unit tests to inherit
*   from so they can be easily run from the command-line using TestNG
*   as well as the more normal test suite run by Ant. The main thing it
*   does is to initialise Log4J (because it's too retarded to have a sensible
*   default configuration), locate & load the appropriate test classes based
*   on what's given on the command-line, and to then fire up TestNG to run 
*   the tests.
*
*   Any test subclass can be run from the CLI with the following command:
*<pre>
*       java -cp [path to class files] CommandLineTest [test class]
*</pre>
*   where <tt>test class</tt> can be given with the 'test.eurocarbdb.' prefix 
*   and 'Test' suffix dropped for convenience. Eg: to run 
*   <tt>test.eurocarbdb.util.graph.GraphTest</tt> directly from the CLI, you
*   can use any of the following:
*<pre>
*       java -cp [path to class files] CommandLineTest util.graph.Graph
*       java -cp [path to class files] CommandLineTest util.graph.GraphTest
*       java -cp [path to class files] CommandLineTest test.eurocarbdb.util.graph.Graph
*       java -cp [path to class files] CommandLineTest test.eurocarbdb.util.graph.GraphTest
*</pre>
*
*   @author mjh [glycoslave@gmail.com]
*/
public abstract class CommandLineTest
{
    /** Inheritable logging handle. */
    protected static final Logger log = Logger.getLogger( CommandLineTest.class );
    
        
    
    /*  init fucking log4j  */
    private static final void initLogging()
    {
        System.out.println("Initialising logging subsystem");
        
        //org.apache.log4j.BasicConfigurator.resetConfiguration(); 
        //org.apache.log4j.PropertyConfigurator
        
        //  setup logging handler (ughhh) because log4j is retarded        
        org.apache.log4j.ConsoleAppender c 
            = new org.apache.log4j.ConsoleAppender( 
                new org.apache.log4j.PatternLayout("%20C{1} : %m%n") );
        
        c.setImmediateFlush( true );
        org.apache.log4j.BasicConfigurator.configure( c );    
    }
    
        
    public static void main( String[] args ) throws Exception
    {
        initLogging();
        
        List<Class> classes = new ArrayList<Class>();
        
        for ( String test_name : args )
        {
            Class c = findTestClassForName( test_name );  
            if ( c == null )
            {
                System.out.println( "Couldn't find or load a test class "
                                  + "corresponding to name '" 
                                  + test_name 
                                  + "'"
                                  );
                continue;
            }
            
            //runTest( c );
            classes.add( c );
        }
        
        //TestListenerAdapter tla = new FileReporterListener();
        TestNG testng = new TestNG();
        
        testng.setTestClasses( classes.toArray( new Class[classes.size()] ) );
        //testng.addListener( tla );
        
        testng.run(); 
    }
    
    
    public static Class findTestClassForName( String class_name )
    {
        Class c = loadClass( class_name );
        
        if ( c == null )
        {
            if ( ! class_name.startsWith("test.eurocarbdb") )
                class_name = "test.eurocarbdb." + class_name;
            
            c = loadClass( class_name );
        }

        if ( c == null )
        {
            if ( ! class_name.endsWith("Test") )
                class_name = class_name + "Test";

            c = loadClass( class_name );             
        }
        
        return c;
    }
    
    
    private static Class loadClass( String class_name )
    {
        Class c = null;
        log.debug("trying to load a class with name '" + class_name + "'");
        try {  c = Class.forName( class_name );  }
        catch ( Exception ignored ) {}
        return c;
    }
    

    /*
    public static void runTest( Class c ) throws Exception
    {
        Object o = c.newInstance();
        if ( ! (o instanceof CommandLineTest) )
        {
            System.out.println( "Class " 
                              + c.getName() 
                              + " does not support CLI testing, skipping...");
            return;
        }
        
        CommandLineTest test = (CommandLineTest) o;
        
        System.out.println();
        System.out.println("Running CLI test for class " + c.getName() );
        
        test.commandLineTest();        
    }
    */
}
