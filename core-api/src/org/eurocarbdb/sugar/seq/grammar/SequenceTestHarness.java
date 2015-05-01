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
*   Last commit: $Rev: 1231 $ by $Author: glycoslave $ on $Date:: 2009-06-19 #$  
*/

package org.eurocarbdb.sugar.seq.grammar;

import java.io.*;
import java.util.*;

import antlr.collections.AST;
import antlr.CommonAST;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.TokenStreamRecognitionException;

import org.eurocarbdb.sugar.Sugar;
import org.eurocarbdb.sugar.SequenceFormatException;

import org.eurocarbdb.sugar.seq.grammar.GlycoctLexer;
import org.eurocarbdb.sugar.seq.grammar.GlycoctParser;

//import antlr.debug.DebuggingParser;

//import antlr.debug.misc.ASTFrame;

/**
<pre>  
Usage:

to test:
java -cp lib org.eurocarbdb.seq.grammar.GlycoctTest

for an interactive shell: 
java -cp 'lib:lib/antlr-2.7.5.jar' org.eurocarbdb.sugar.seq.grammar.GlycoctTest\$Shell

</pre>
*/
public abstract class SequenceTestHarness
{
    
    /** For interactive use. */
    public static class Shell
    {
        public static void main( String[] args ) throws Exception
        {
            org.apache.log4j.ConsoleAppender c 
                = new org.apache.log4j.ConsoleAppender( 
                    new org.apache.log4j.PatternLayout("%20C{1} : %m%n") );
            
            c.setImmediateFlush( true );
            org.apache.log4j.BasicConfigurator.configure( c );
        
            String format = args[0];
            SequenceTestHarness test 
                = (SequenceTestHarness) Class.forName( 
                    SequenceTestHarness.class.getPackage().getName()
                    +   format 
                    +   "Test"
                    ).newInstance(); 
        
            for ( 
                    System.out.print("enter a sugar sequence > " );; 
                    System.out.print("enter another sugar sequence > " ) 
            )
            {
                String seq = new BufferedReader( 
                                new InputStreamReader( 
                                    System.in )).readLine();
                
                try
                {
                    Sugar s = test.getSugar( seq );
                    System.err.println( "sequence is correct" );
                    
                    System.out.println( s );
                }
                catch ( SequenceFormatException e )
                {
                    System.err.println( "Syntax error: " + e.getMessage() );
                    e.printStackTrace();
                }
                catch ( Exception e )
                {
                    System.err.println( "quitting" );
                    System.exit( 1 );
                }

            }         
        }
    }


    public abstract ParserAdaptor getParserFor( String seq )
    ;

    public Sugar getSugar( String seq ) throws SequenceFormatException
    {
        System.out.println("parsing seq '" + seq + "'" );
        /*
        GlycoctLexer   lexer = new GlycoctLexer( new StringReader( seq ) );
        GlycoctParser parser = new GlycoctParser( lexer );
        parser.setSequence( seq );
        */

        ParserAdaptor parser = getParserFor( seq );
      
        long start = System.currentTimeMillis();
        try 
        { 
            parser.sugar(); 
        }  
        catch ( RecognitionException e )
        {
            throw new SequenceFormatException(  
                    seq, 
                    e.column - 1, 
                    e.getMessage() 
                    );
        }
        catch ( TokenStreamRecognitionException e )
        {
            throw new SequenceFormatException(  
                    seq, 
                    e.recog.column - 1, 
                    e.getMessage()  
                    );
        }
        catch ( TokenStreamException e )
        {
            // we don't really care about this i don't think...
            e.printStackTrace();
        }

        System.out.println();
        System.out.println( "parsed sugar AST:" );
        System.out.println( parser.graph );
        
        /* TODO: translation needs more work, esp. unknown/missing linkages.
        System.out.println( "translating AST -> sugar:" );
        
        Sugar s = parser.getSugar();
        
        System.out.println();
        System.out.println( "parsed sugar:" );
        System.out.println( s.toString() );
        */
        
        System.out.println("seq was " + seq );
        
        return null;
    }
    
    
    
    public void testParsing( String[] correct_sequences, 
                             String[] incorrect_sequences )
    {
        //  setup logging handler (ughhh)        
        org.apache.log4j.ConsoleAppender c 
            = new org.apache.log4j.ConsoleAppender( 
                new org.apache.log4j.PatternLayout("%20C{1} : %m%n") );
        
        c.setImmediateFlush( true );
        org.apache.log4j.BasicConfigurator.configure( c );

        List<Exception> failed = new ArrayList<Exception>();

        
        //  CORRECT SEQUENCES
        //
        //  iterate through the collection of syntactically-correct 
        //  sequences - none of these should throw format exceptions.
        //
        System.err.println("=== correct sequences ===");
        
        int count_correct = 0;
        int count_failed  = 0;

        long parse_time_msec = 0;
        long cumulative_time_msec = 0;
        int  count_total_chars_parsed = 0;
        
        for ( String seq : correct_sequences ) 
        {
            try 
            { 
                System.err.println();
                System.err.println( "--- parsing correct sequence " 
                                  + ++count_correct 
                                  + " ---");
                System.err.println( seq );
                
                long start_time = System.currentTimeMillis();

                getSugar( seq );        

                parse_time_msec = System.currentTimeMillis() - start_time;
                cumulative_time_msec += parse_time_msec;
                count_total_chars_parsed += seq.length();
                
                System.err.println("TEST PASSED: sequence appears correct");                
                System.err.println("parse took " 
                                  + parse_time_msec 
                                  + " msec"
                                  );
            }
            catch ( SequenceFormatException e )
            {
                // this means a correct sequence is actually wrong, or
                //  there is an error in the parser.
                System.err.println();
                System.err.println( "*** TEST FAILED ***" );
                System.err.println( "this sequence should have parsed "
                                  + "correctly, but threw a parse exception "
                                  + "-- check it!!!"
                                  );                

                e.printStackTrace();
                
                failed.add( e );
                count_failed++;
            }
                        
        }
        
    
        //  INCORRECT SEQUENCES
        //    
        //  iterate through the collection of sequences that have deliberate 
        //  syntax errors - all of these *should* throw SequenceFormatExceptions. 
        //
        System.err.println();
        System.err.println("=== syntactically incorrect sequences ===");
        
        int count_incorrect = 0;
        for ( String seq : incorrect_sequences ) 
        {
            try 
            { 
                System.err.println();
                System.err.println( "--- parsing incorrect sequence " 
                                  + ++count_incorrect
                                  + " ---");
                System.err.println( seq );
                
                getSugar( seq ); 
                
                //  this test fails if it gets to this point since we were
                //  expectingly sequence format exceptions to have been thrown.
                count_failed++; 
                throw new RuntimeException(
                        "*** TEST FAILED ***\n"
                        + seq
                        + "\n"
                        + "the sequence above was determined to be correct, "
                        + "when it should have thrown a sequence format error "
                        + "-- check it!" );
            }
            catch ( SequenceFormatException e )
            {
                System.err.println(); 
                System.err.println( "TEST PASSED: Sequence correctly judged as "
                                  + "incorrect, exception was:" );
                e.printStackTrace();
            }
            catch ( RuntimeException e )
            {
                System.err.println( 
                    "ERROR!!! Expected a SequenceFormatException, but got a " 
                    + e.getClass().getName() 
                );
                System.err.println( e.toString() );
                
                failed.add( e );
            }

        }
        
        
        //  reporting...  
        System.err.println();
        System.err.println("=== SUMMARY ===");
        System.err.println( count_failed == 0 
                            ? "All tests successful"
                            : count_failed + " test(s) failed"
                            );        
        
        //  some performance metrics...
        System.err.println( String.format( 
                            "total parse time (for the %d correct sequences): %d msec",
                            count_correct, cumulative_time_msec  ) );
        
        System.err.println( String.format( 
                            "avg parse time / sequence: %.1f msec", 
                            (double) cumulative_time_msec / count_correct  ) );
        
        System.err.println( String.format( 
                            "avg parse time / sequence char: %.3f msec", 
                            (double) cumulative_time_msec / count_total_chars_parsed  ) );


        if ( failed.size() > 0 )
        {
            System.err.println(); 
            System.err.println( "The tests that failed were:");
            
            for ( Exception e : failed ) 
            {
                System.err.println(); 
                e.printStackTrace();
            }
        }

        return;
    }
    
    
 
}
