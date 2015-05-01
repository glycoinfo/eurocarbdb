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
*   Last commit: $Rev: 1561 $ by $Author: glycoslave $ on $Date:: 2009-07-21 #$  
*/

package org.eurocarbdb.sugar;

/*  enum Anomer  *//*************************************************
*<p>  
*   An object wrapper for the anomeric configurations of 
*   monosaccharides.
*</p>
*<p>
*    Created 30-Mar-2006.
*</p>  
*   @author mjh
*
*/
public enum Anomer implements PotentiallyIndefinite
{
    /** Indicates the alpha anomeric configuration */
    Alpha("alpha", 'a', LinkageType.Glycosidic_Alpha ),
    
    /** Indicates the beta anomeric configuration */
    Beta("beta", 'b', LinkageType.Glycosidic_Beta ),
    
    /** Indicates that there is no anomeric configuration in effect, because the
    *   relevant monosaccharide is in open-chain form. */
    OpenChain("open-chain", 'o', LinkageType.None ),
    
    /** Indicates the anomeric configuration is not known. */
    UnknownAnomer("unknown", '?', LinkageType.Unknown ),
    
    /** Indicates that an anomeric configuration is not applicable. */
    None("(no anomer)", ' ', LinkageType.None )
    ;
    
    /** The default anomer; currently "unknown". */
    public static Anomer DefaultAnomer = None;
    
    /** Anomer verbose name (duh!) */
    private String fullname;
    
    /** Anomer short name. */
    private char symbol;
    
    
    private LinkageType type;
    
    
    /** Private constructor, see the forName methods for external use. */
    private Anomer( String fullname, char symbol, LinkageType type ) 
    { 
        this.fullname = fullname; 
        this.symbol = symbol; 
        this.type = type;
    }
    
    
    /** Returns the appropriate Anomer instance for the given String.  */
    public static Anomer forName( String anomer )
    {  
        return forName( anomer.charAt(0) );  
    }
    
    
    /** Returns the appropriate Anomer instance for the given character/symbol.  */
    public static Anomer forName( char anomer ) 
    { 
        switch ( anomer )
        {
            case 'a':
            case 'A': 
                return Alpha;
            
            case 'b': 
            case 'B': 
                return Beta;
            
            case '?': 
            case 'u': 
                return UnknownAnomer;
            
            case 'o': 
            case 'O': 
                return OpenChain;
            
            default: 
                return None;
        }
    }
    
    
    /** Returns this anomer's full name - "alpha", "beta", etc  */
    public String getFullname() {  return fullname;  }
    
    
    /** Returns the abbreviated name (symbol) of this anomer - "a", "b".  */
    public String getName() {  return ( this.equals(None) ) ? "" : "" + getSymbol();  }

    
    /** Returns the abbreviated name (symbol) of this anomer as a char - 'a', 'b'.  */
    public String getSymbol() {  return (this.equals(None)) ? "" : "" + symbol;  }
    
    
    public LinkageType getType() {  return type;  }
    
    
    public boolean isDefinite() {  return ! ( this == UnknownAnomer || this == None );  }
    
    
    /** Returns the abbreviated name (symbol) of this anomer, same as {@link getSymbol()}.  */
    public char toChar() {  return symbol;  }
    
    
    /** Returns the short name (symbol) representing this anomer.  */
    public String toString() {  return getSymbol();  }
    
} // end enum Anomer


