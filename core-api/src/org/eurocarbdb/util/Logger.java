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

package org.eurocarbdb.util;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;

/*  class Logger  *//************************************************
*
*   Wrapper class for logging functions.
*
*   java.util.logging.Logger *does not work as advertised*, and log4j
*   requires an extra library jarfile just for logging. Therefore this
*   class was created to fulfill the requirements of basic logging without
*   using an external library, and to provide an abstraction over what
*   logging system we are currently using. 
*
*   The API used here is a subset of the java.util.logging.Logger
*   API (which is a copy of the log4j API). This is to make it easy to
*   delgate logging to log4j or java logging as/when required.
*
*   Final note: unlike java.util.logging.Logger, this class actually does work.
*
*   Typical usage:
*
<pre>
<code>
    import org.eurocarbdb.util.Logger;

    class MyClass 
    {
        private static final Logger log 
            = Logger.getLogger( MyClass.class.getName() );
    
        public static void logging_demo() 
        {
            //  determines minimum log level for which log messages will be shown.
            //  defaults to Logger.Level.Info.
            log.setLevel( Logger.Level.Debug2 );
            
            //  determines minimum log level that will show a small stack trace.
            //  defaults to Logger.Level.Warning.
            log.setTraceLevel( Logger.Level.Warning );
            
            //  determines minimum log level that will show a timestamp.
            //  defaults to Logger.Level.Warning.
            log.setTimestampLevel( Logger.Level.Warning );
            
            log.debug3("a debug level 3 message");
            log.debug2("a debug level 2 message");
            log.debug("a debug message");
            log.info("some info");
            log.notice("a notice");
            log.warning("a warning");
            log.severe("a severe warning");
            log.critical("a critical warning");
        }
    }
</code>
</pre>
*
*   @author mjh
*   @see java.util.logging.Logger
*/
public final class Logger
{
    public enum Level 
    {
        //~~~ ENUM VALUES ~~~//
        /*
        *   Structure is:
        *
        *   Name( 
        *       <arbitrary severity integer>, 
        *       <default string to put before log message>, 
        *       <default string to put after log message>
        *   )
        */
        
        /*  Log level that always logs (duh). */
        LogEverything( 0, "", ""    ),
        
        /*  debug levels  */
        Debug4(   5,  "            (", ")"          ),
        Debug3(  10,  "        (", ")"          ),
        Debug2(  15,  "    (", ")"          ),
        Debug(   20,  "(", ")"          ),
        
        /*  general informational messages. default level is Info.  */
        Info(    25,  "INFO: ", ""      ),
        Notice(  30,  "NOTICE: ", ""    ),
        
        /*  unexpected/problem messages.  */
        Warning( 50, "WARNING: ", ""    ),
        Severe( 100, "SEVERE: ", ""     ),
        Critical( 500, "CRITICAL: ", "" ),
        
        /*  Log level that never logs  */
        LogNothing( 999, "", ""     ),
        
        /*  internal use for bypassing logging test. */
        Unconditional( 1000, "", "")
        ;
    
    
        //~~~ OBJECT DATA ~~~//
    
        /** Arbitrary severity integer. */
        public final int   level;
        
        /** String prepended to log message. */
        final String      before; 
        
        /** String appended to log message. */
        final String       after; 
  
  
        /*  Constructor  *//*****************************************
        *
        */
        Level( int level, String before, String after )
        {   
            this.level  = level; 
            this.before = before; 
            this.after  = after;
        }
                
    } // end enum


    //~~~ STATIC DATA ~~~//
    
    /** The default lowest logging level for which logging messages will be shown. */
    public static Level DefaultLogLevel = Level.Info;
    
    /** The default lowest logging level for which stack traces for log messages
    *   will be shown.  */
    public static Level DefaultTraceLevel = Level.Warning;
    
    /**The default lowest logging level for which timestamps for log messages
    *   will be shown.  */
    public static Level DefaultTimestampLevel = Level.Warning;

    /** Private static hash of logger-id to Logger instance. */
    private static final Map<String,Logger> LogLevel = new HashMap<String,Logger>();


    //~~~ OBJECT DATA ~~~//

    /** Unique logging identifier string. Can be any arbitrary string, but
    *   would usually be the name of the Logger client class or package. */
    public final String id;

    /** The logging level of this Logger instance. Log messages with Levels
    *   lower than this will be silently discarded. Defaults to DefaultLogLevel. */
    public Level logLevel = DefaultLogLevel;
    
    /** The lowest logging level for which stack traces will be shown 
    *   for logging messages. */
    public Level traceLevel = DefaultTraceLevel;
    
    /** The lowest logging level for which timestamps will be shown 
    *   for logging messages. */
    public Level timestampLevel = DefaultTimestampLevel;


    //~~~ STATIC METHODS ~~~//


    /*  getLogger  *//***********************************************
    *
    *   Static factory constructor for Logger instances. The given
    *   logger id may be any arbitrary string, but would usually be
    *   the name of the class or package in which this Logger will
    *   be used.
    */
    public static final Logger getLogger( String logger_id )
    {
        Logger logger = LogLevel.get( logger_id );
        if ( logger == null )
        {
            logger = new Logger( logger_id );
            LogLevel.put( logger_id, logger );
        }
         
        return logger;
    }


    /*  log  *//*****************************************************
    *
    *   Convenience method for trivial logging usage.
    */
    public static final void log( String logger_id, Level severity, Object... messages )
    {
        Logger log = getLogger( logger_id );
           
        //  show message?
        if ( log.logLevel.level > severity.level ) 
            return;
        
        //  show message(s)
        for ( Object m : messages )
            if ( m instanceof Throwable )
                ((Throwable) m).printStackTrace();
            else
                System.err.println( severity.before + m.toString() + severity.after );


        //  show timestamp?
        if ( log.timestampLevel.level <= severity.level )
        {
            System.err.println( severity.before 
                                + "[timestamp] " 
                                + new Date().toString() 
                                + severity.after
                                );
        }
        
        //  show mini stack trace?
        if ( log.traceLevel.level <= severity.level )
        {
            StackTraceElement[] ste = Thread.currentThread().getStackTrace();
            for ( int i = 4; i < 8 && i < ste.length; i++ )
            {
                System.err.println( severity.before 
                                    + "[traceback] method "
                                    + (i - 3)
                                    + ": "
                                    + ste[i].getMethodName()
                                    + " ("
                                    + ste[i].getFileName()
                                    + " line "
                                    + ste[i].getLineNumber()
                                    + ")"
                                    + severity.after
                );
            }
        }
        
        return;
    }
    
    
    //~~~ CONSTRUCTORS ~~~//
    
    Logger( String id )
    {
        this.id = id;
    }
    
    
    //~~~ OBJECT METHODS ~~~//
    
    /** Logs a debug level message. */
    public void debug( Object... messages ) {  log( id, Level.Debug, messages );  }    

    /** Logs a debug2 level message. */
    public void debug2( Object... messages ) {  log( id, Level.Debug2, messages );  }    

    /** Logs a debug3 level message. */
    public void debug3( Object... messages ) {  log( id, Level.Debug3, messages );  }    

    /** Logs a debug4 level message. */
    public void debug4( Object... messages ) {  log( id, Level.Debug4, messages );  }    

    /** Logs an info level message. */
    public void info( Object... messages ) {  log( id, Level.Info,  messages );  }    

    /** Logs a notice level message. */
    public void notice(  Object... messages ) {  log( id, Level.Notice, messages );  }

    /** Logs a warning message. */
    public void warning( Object... messages ) {  log( id, Level.Warning, messages );  }

    /** Logs a severe warning message. */
    public void severe( Object... messages ) {  log( id, Level.Severe, messages );  }

    /** Logs a critical warning message. */
    public void critical( Object... messages ) {  log( id, Level.Critical, messages );  }

    /** Logs an error message. */
    public void error( Object... messages ) {  log( id, Level.Critical, messages );  }

    public void unconditionally( Object... messages ) {  log( id, Level.Unconditional, messages );  }
    
    
    /*  isLoggable  *//**********************************************
    *
    *   Returns true if a message of the given level would be logged.
    */
    public final boolean isLoggable( Level severity )
    {
        assert severity != null;
        return ( this.logLevel.level <= severity.level );
    }
    
    public final boolean isDebugEnabled() {  return this.logLevel.level <= Level.Debug.level;  }
    
    
    /*  setLevel  *//************************************************
    *
    *   Sets logging level for this Logger instance; log messages with
    *   levels below the given Level will be silently discarded.
    */
    public final void setLevel( Level level )
    {
        assert( level != null );
        logLevel = level;
    }
    
    /*  setTraceLevel  *//*******************************************
    *
    *   Sets the minimum logging level for which stack traces will be
    *   printed for log messages.
    */
    public final void setTraceLevel( Level level )
    {
        assert( level != null );
        traceLevel = level;
    }

    /*  setTimestampLevel  *//***************************************
    *
    *   Sets the minimum logging level for which stack traces will be
    *   printed for timestamp messages.
    */
    public final void setTimestampLevel( Level level )
    {
        assert( level != null );
        timestampLevel = level;
    }


}
