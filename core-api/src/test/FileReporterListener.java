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
*   Last commit: $Rev: 1237 $ by $Author: glycoslave $ on $Date:: 2009-06-21 #$  
*/

package test;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;

import java.io.File;
import java.io.PrintStream;
import java.io.FileNotFoundException;

import org.testng.IClass;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PropertyConfigurator;

import static java.lang.System.currentTimeMillis;


/**
*   This is a lame attempt at a quick and dirty listener class
*   plugin for TestNG so that we can see the output of each 
*   unit test that is run. It is hardcoded to take the log4j
*   settings found at <tt>./conf/log4j.properties</tt>
*   relative to where the test is running. It works by creating a file 
*   named <tt>[unit-test-class-name].output</tt> in the TestNG output
*   directory. As implied above, it is somewhat reliant on there 
*   being useful logging statements in the test code and/or the
*   library code being tested. If you're not getting output, first
*   try checking that logging is enabled for your class(es) in the
*   log4j.properties file given above. As an extra hack, this class
*   also redirects stderr & stdout to the unit test's .output file.
*
*   @author mjh 
*/
public class FileReporterListener extends TestListenerAdapter
{
    static final Logger log 
        = Logger.getLogger( FileReporterListener.class );
        
    /** Saved STDERR filehandle */
    private static final PrintStream stderr = System.err;
    
    /** Saved STDOUT filehandle */
    private static final PrintStream stdout = System.out;
        
    /** The TestNG output directory for the currently executing test suite. */
    private static String outputDirectory;
    
    /** Map of test-class to test-class' output filehandle */
    private static Map<IClass,PrintStream> handles = new HashMap<IClass,PrintStream>();
    
    /** names of text output files created */
    private static Map<IClass,String> 
        testOutputFileMap = new HashMap<IClass,String>();

    /** test classes whose tests failed */
    private static Set<IClass> classesWithErrors = new HashSet<IClass>();     
        
    public static String log4j_conf = "./conf/log4j.properties";

    long started;
    
    
    /** Returns the TestNG output directory for the currently executing test suite. */    
    public static String getTestOutputDirectory()
    {
        return outputDirectory;    
    }
    
    
    /** Sets up log4j */
    static void setupLogging()
    {
        if ( ! new File( log4j_conf ).canRead() )
            throw new RuntimeException(
                "Cannot access file '" + log4j_conf + "'" );
        
        //  setup log4j to use current core-api log4j config 
        stderr.println( 
            FileReporterListener.class.getName() 
            + ": using log4j configuration in " 
            + log4j_conf 
        );
        PropertyConfigurator.configure( log4j_conf );
    }
    

    /** 
    *   Constructor; also redirects log4j output to System.out, 
    *   which is itself re-directed on a per-test-class basis as described 
    *   in class description.
    */
    public FileReporterListener()
    {       
        setupLogging();
        
        //  configure log4j to write to STDOUT, which we will re-direct on 
        //  a per-test basis
        //
        ConsoleAppender logOutput 
            = new ConsoleAppender( 
                new PatternLayout("%5p: %m (%l)%n"), 
                    "System.out" ); 
             
        logOutput.setImmediateFlush( true );
        logOutput.setFollow( true );
        logOutput.activateOptions();
        
        Logger root = Logger.getRootLogger();
        
        root.removeAllAppenders();
        
        root.addAppender( logOutput );        
    }
    
    
    public void onStart( ITestContext testContext )
    {
        super.onStart( testContext );
        outputDirectory = testContext.getOutputDirectory();
        stderr.println("output of unit tests can be found in " + outputDirectory );
    }


    public void onFinish( ITestContext testContext )
    {
        /*  restore saved stdout/stderr  */
        System.setOut( stdout );
        System.setErr( stderr );
        
        //assert ( testOutputFileMap.size() > 0 );

        /*  close open filehandles  */
        for ( PrintStream fh : handles.values() )
        {
            //  fh.flush();
            fh.close();
        }
        
        /*  report test output files created  */
        int offset = outputDirectory.length() + 1;
        
        List<String> uniqueFilenames 
            = new ArrayList<String>( 
                new HashSet<String>( testOutputFileMap.values() ) );
        Collections.sort( uniqueFilenames );

        //  derive set of files whose test classes had errors
        Set<String> filesWithErrors = new HashSet<String>(); 
        for ( IClass c : classesWithErrors )        
            filesWithErrors.add( testOutputFileMap.get( c ) );

        stderr.println();
        stderr.println(
            "wrote " 
            + uniqueFilenames.size() 
            + " test output files to directory "
            + outputDirectory
            + ":"
        );
        
        for ( String filename : uniqueFilenames )
        {
            boolean containsErrors = filesWithErrors.contains( filename ); 
            stderr.println( 
                (containsErrors ? "!!! " : "    ")
                + filename.substring( offset ) 
                + (containsErrors ? " (contains errors)" : "")
            );
        }   
        /*
        if ( classesWithErrors.size() > 0 )
        {
            Set<String> filesWithErrors = new HashSet<String>(); 
            for ( IClass c : classesWithErrors )        
                filesWithErrors.add( testOutputFileMap.get( c ) );
                
            List<String> sortedFilesWithErrors = new ArrayList<String>( filesWithErrors );
            Collections.sort( sortedFilesWithErrors );
    
            stderr.println();
            stderr.println("test output files whose test classes had errors:");
            for ( String filename : sortedFilesWithErrors )
                stderr.println("    " + filename );
        }
        */
            
        super.onFinish( testContext );
    }

    
    public void onTestStart( ITestResult context )
    {
        PrintStream out = getPrintStreamForClass( context.getTestClass() );

        out.println();
        out.println( "================== starting test: " 
                   + context.getMethod().getMethodName() 
                   + " =================="
                   );
        
        started = currentTimeMillis();
        
        super.onTestStart( context );
    }
    
    
    public void onTestSuccess( ITestResult context )
    {
        PrintStream out = getPrintStreamForClass( context.getTestClass() );
        
        out.println();
        out.println( "================== test " 
                   + context.getMethod().getMethodName() 
                   + " successful =================="
                   );
        out.println( "(test took " + (currentTimeMillis() - started) + " msec)");
        out.println();
        
        // stderr.println( "... test passed : " + context.getMethod() );
        stderr.println( 
            "... test passed : " 
            + context.getMethod().getRealClass().getSimpleName() 
            + "."
            + context.getMethod().getMethodName()
        );
        
        super.onTestSuccess( context );
    }
    

    public void onTestFailure( ITestResult context )
    {
        IClass c = context.getTestClass();
        
        PrintStream out = getPrintStreamForClass( c );
        classesWithErrors.add( c );
        
        Throwable t = context.getThrowable();

        out.println();
        t.printStackTrace( out );
        
        out.println( 
            "^^^^^^^^^^ test " 
           + context.getMethod().getMethodName() 
           + " failed ^^^^^^^^^^"
        );
        
        stderr.println( 
            "XXX test failed : " 
            + context.getMethod().getRealClass().getSimpleName() 
            + "."
            + context.getMethod().getMethodName()
            + " ("
            + t.getClass().getSimpleName()
            + ")"
        );
        
        super.onTestFailure( context );
    }
    

    public void onTestSkipped( ITestResult context )
    {
        // stderr.println( "--- test skipped: " + context.getMethod() );
        stderr.println( 
            "--- test skipped: " 
            + context.getMethod().getRealClass().getSimpleName() 
            + "."
            + context.getMethod().getMethodName()
        );
        
        super.onTestSkipped( context );
    }


    public void onTestFailedButWithinSuccessPercentage( ITestResult context )
    {
        PrintStream out = getPrintStreamForClass( context.getTestClass() );
        
        out.println();
        out.println( "^^^^^^^^^^ test " 
                   + context.getMethod().getMethodName() 
                   + " within given success percentage ^^^^^^^^^^"
                   );
        
        super.onTestFailedButWithinSuccessPercentage( context );
    }

    
    /** 
    *   Creates a new output file for the passed test class name 
    *   if not already created, returning a {@link PrintStream} to this file, 
    *   and re-directing {@link System.out} and {@link System.err} to this stream.
    *   Name of file created is: <tt>${outputDirectory}/${currentClassName}.output</tt>.
    *   File handles are held open until all tests have been run, see {@link #onFinish}.
    */
    protected PrintStream getPrintStreamForClass( org.testng.IClass test_class )
    {
        Class<?> c = test_class.getRealClass();
        
        String filename = outputDirectory
                        + "/"
                        + c.getName()
                        + ".output"
                        ;
         
        //  open file for current test class
        // stderr.println("getting filehandle for class=" + c );
        PrintStream out = null;
        try 
        {
            synchronized ( handles )
            {
                out = handles.get( test_class );
                
                if ( out == null  )
                {
                    //stderr.println("> creating new file '" + filename + "' for " + c);
                    out = new PrintStream( filename );
                    testOutputFileMap.put( test_class, filename );
                    handles.put( test_class, out );

                    out.println("**************************************************");
                    out.println("*   test output from " + c );
                    out.println("*   test started at " + new Date() );
                    out.println("**************************************************");
                    
                    out.println();
                }
                else
                {
                    //stderr.println("> using cached handle for " + c );   
                }            
            } // end synchronized  
        }
        catch ( Exception e ) 
        {
            stderr.println( "Problem encountered while creating test output file: " + e );
            e.printStackTrace();  
        }

        //  (temporarily) redirect stdout/stderr to file
        try
        {
            System.out.flush();
            System.err.flush();
            System.setOut( out );
            System.setErr( out );
        }
        catch ( Exception e ) 
        {
            stderr.println( "Problem encountered while re-directing stderr/stdout to file: " + e );
            e.printStackTrace();  
        }
        
        assert out != null;
        
        return out;
    }
    
} // end class
