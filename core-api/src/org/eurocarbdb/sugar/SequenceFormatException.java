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

package org.eurocarbdb.sugar;

/** 
*   An exception class encapsulating errors in sequence syntax. The class
*   is also capable of producing formatted sequence error messages indicating
*   the position of the relevant error through the method {@link #getErrorContext}.
* 
*   @author mjh
*/
public class SequenceFormatException extends SugarException 
{
    //~~~~~~~~~~~~~~~~~~~~~  STATIC FIELDS  ~~~~~~~~~~~~~~~~~~~~~~~//

    /** This is the character used in error context messages to indicate the 
    *   position of the sequence syntax error; the default is '^'.  */
    public static char ERROR_MARKER_CHAR = '^';
    
    /** This is the character that will be used for left-padding context 
    *   error messages; the default is ' '.  */
    public static char ERROR_SPACING_CHAR = ' ';
    public static char LINE_SEPARATOR = '\n';
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** The sequence string that contains the error. */
    String sequence;
    
    /** The lowest position (index) in the string where the error occurs. */
    int index_left;
    
    /** The highest position (index) in the string where the error occurs. */
    int index_right;
    
    /** The error message. */
    String error;

    
    //~~~~~~~~~~~~~~~~~~~~~~  CONSTRUCTORS  ~~~~~~~~~~~~~~~~~~~~~~~//

    /*  Constructor  *//*********************************************
    *
    *   Constructs a context-specific syntax exception.
    *
    *   @param  seq     The sequence string that contains the error.
    *   @param  index   The position (index) in the string where the error occurs.
    *   @param  error   The error message.
    */
    public SequenceFormatException( String seq, int index, String error )
    {
        this( seq, index, index, error );
    }
    
    
    /*  Constructor  *//*********************************************
    *
    *   Constructs a context-specific syntax exception plus a freeform 
    *   exception message. Note that this method is tolerant of a start_pos
    *   that is greater than end_pos; it will swap them. 
    *
    *   @param  seq         The sequence string that contains the error.
    *   @param  start_pos   The leftmost position (index) in the string where 
    *                       the error occurs.
    *   @param  end_pos     The rightmost position (index) in the string where 
    *                       the error occurs.
    *   @param  error       The error message.
    */
    public SequenceFormatException( String seq, 
                                    int start_pos, 
                                    int end_pos, 
                                    String error )
    {
        this.sequence    = seq;
        this.index_left  = start_pos;
        this.index_right = end_pos; 
        this.error       = error;
        
        if ( index_left > index_right ) 
        { 
            int swap = index_left; 
            index_left = index_right; 
            index_right = swap; 
        }
    }
     

    /*  Constructor  *//*********************************************
    *
    *   Constructor for non-context-specific usage.
    *
    *   @param  error   The error message.
    */
    public SequenceFormatException( String error )
    {
        this( null, 0, 0, error );
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//        

    /*  getMessage  *///*********************************************
    @Override
    public String getMessage()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( error );
        sb.append( LINE_SEPARATOR ); 
        sb.append( this.getErrorContext() );
        
        return sb.toString();
    }

    
    /*  getErrorContext  *//*****************************************
    * 
    *   Produces a formatted 2-line string of the erroneous sequence 
    *   including a context marker that indicates the position of the 
    *   syntax error. For example:
    *   
    *   <code>
    *   Glc(a1-4)Glc(a1-6Glc(a1-4)
    *                   ^
    *   </code>
    *   
    *   The use of spaces for spacing and the caret for position of error may
    *   be changed by setting ERROR_SPACING_CHAR and ERROR_MARKER_CHAR 
    *   respectively.
    *   
    *   @see ERROR_SPACING_CHAR
    *   @see ERROR_MARKER_CHAR 
    */
    public String getErrorContext()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append( sequence );
        sb.append( LINE_SEPARATOR ); 
        
        for ( int i = 0; i < index_left; i++ )
            sb.append( ERROR_SPACING_CHAR );
        
        for ( int i = index_left; i <= index_right; i++ )
            sb.append( ERROR_MARKER_CHAR );

        return sb.toString();
    }
    
} // end class


