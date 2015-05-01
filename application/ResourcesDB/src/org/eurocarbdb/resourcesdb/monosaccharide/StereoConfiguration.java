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
*   Last commit: $Rev: 1210 $ by $Author: glycoslave $ on $Date:: 2009-06-12 #$  
*/
package org.eurocarbdb.resourcesdb.monosaccharide;

import org.eurocarbdb.resourcesdb.ResourcesDbException;

public enum StereoConfiguration {
    Dexter("dexter", "D", 'D', Stereocode.StereoD),
    Laevus("laevus", "L", 'L', Stereocode.StereoL),
    Unknown("unknown", "X", '?', Stereocode.StereoX),
    Nonchiral("non-chiral", "N", 'X', Stereocode.StereoN),
    XDexter("relative_dexter", "XD", '?', Stereocode.StereoXD),
    XLaevus("relative_laevus", "XL", '?', Stereocode.StereoXL);

    /** Configuration verbose name */
    private String fullname;

    /** Configuration short name. */
    private String symbol;
    
    /** Configuration stereocode symbol. */
    private char stereoSymbol;
    
    /** Configuration symbol as used in bcsdb style names. */
    private char bcsdbSymbol;

    /** Private constructor, see the getConfigurationByName methods for external use. */
    private StereoConfiguration(String fullnameStr, String symbolStr, char bcsdbSymbolStr, char stereosymbolChar) {
        this.fullname = fullnameStr;
        this.symbol = symbolStr;
        this.stereoSymbol = stereosymbolChar;
        this.bcsdbSymbol = bcsdbSymbolStr;
    }

    /** Returns this configuration's full name  */
    public String getFullname() {  
        return this.fullname;  
    }

    /** Returns the abbreviated name (symbol) of this configuration.  */
    public String getSymbol() {  
        return this.symbol;  
    }
    
    /** 
     * Returns the stereocode symbol of this configuration */
    public char getStereosymbol() {
        return this.stereoSymbol;
    }

    /** 
     * Returns the BCSDB stereocode symbol of this configuration */
   public char getBcsdbSymbol() {
        return this.bcsdbSymbol;
    }

    /** 
     * Returns the appropriate Configuration instance for the given name/symbol (e.g "dexter" or "D").
     * @param config Name or Symbol of the configuration  
     * @throws MonosaccharideException in case the parameter config does not match any configuration name or symbol */
    public static StereoConfiguration forNameOrSymbol(String config) throws MonosaccharideException {
        for(StereoConfiguration c : StereoConfiguration.values()) {
            if(config.equalsIgnoreCase(c.symbol)) {
                return c;
            }
            if(config.equalsIgnoreCase(c.fullname)) {
                return c;
            }
        }
        throw new MonosaccharideException("Invalid value for absolute configuration: " + config);
    }
    
    /** 
     * Returns the appropriate Configuration instance for the given stereocode symbol (e.g "1" or "2" or "x").
     * @param stereosymbol the character used in a stereocode for the configuration  
     * @throws ResourcesDbException in case the parameter stereosymbol does not match any configuration stereoSymbol */
   public static StereoConfiguration forStereosymbol(char stereosymbol) throws ResourcesDbException {
        for(StereoConfiguration c : StereoConfiguration.values()) {
            if(stereosymbol == c.stereoSymbol) {
                return c;
            }
        }
        throw new ResourcesDbException("Invalid stereosymbol for configuration: " + stereosymbol);
    }
   
   public static StereoConfiguration forBcsdbSymbol(char bcsdbsym) {
       for(StereoConfiguration c : StereoConfiguration.values()) {
           if(c.bcsdbSymbol == bcsdbsym) {
               return c;
           }
       }
       return null;
   }
    
    public static char stereosymbolAbsoluteToRelative(char absoluteSymbol) throws ResourcesDbException {
        StereoConfiguration c = StereoConfiguration.forStereosymbol(absoluteSymbol);
        if(c.equals(StereoConfiguration.Dexter)) {
            return StereoConfiguration.XDexter.stereoSymbol;
        }
        if(c.equals(StereoConfiguration.Laevus)) {
            return StereoConfiguration.XLaevus.stereoSymbol;
        }
        if(c.equals(StereoConfiguration.Nonchiral) || c.equals(StereoConfiguration.Unknown)) {
            return c.stereoSymbol;
        }
        throw new MonosaccharideException("Invalid stereosymbol for absolute configuration: " + absoluteSymbol);
    }

    public static char stereosymbolRelativeToAbsolute(char relativeSymbol) throws ResourcesDbException {
        StereoConfiguration c = StereoConfiguration.forStereosymbol(relativeSymbol);
        if(c.equals(StereoConfiguration.XDexter)) {
            return StereoConfiguration.Dexter.stereoSymbol;
        }
        if(c.equals(StereoConfiguration.XLaevus)) {
            return StereoConfiguration.Laevus.stereoSymbol;
        }
           return c.stereoSymbol;
    }
    
    public static StereoConfiguration invert(StereoConfiguration sConf) {
        if(sConf.equals(StereoConfiguration.Dexter)) {
            return StereoConfiguration.Laevus;
        }
        if(sConf.equals(StereoConfiguration.Laevus)) {
            return StereoConfiguration.Dexter;
        }
        if(sConf.equals(StereoConfiguration.XDexter)) {
            return StereoConfiguration.XLaevus;
        }
        if(sConf.equals(StereoConfiguration.XLaevus)) {
            return StereoConfiguration.XDexter;
        }
        return sConf;
    }

}
