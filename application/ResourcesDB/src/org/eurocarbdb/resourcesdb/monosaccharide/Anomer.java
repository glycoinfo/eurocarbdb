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

public enum Anomer
{
    ALPHA("alpha", "a", "a", 'a', StereoConfiguration.Dexter.getStereosymbol()),
    BETA("beta", "b", "b", 'b', StereoConfiguration.Laevus.getStereosymbol()),
    OPEN_CHAIN("open-chain", "o", "", 'x', StereoConfiguration.Nonchiral.getStereosymbol()),
    UNKNOWN("unknown", "x", "?", '?', StereoConfiguration.Unknown.getStereosymbol()),
    NONE("none", "n", "", 'x', StereoConfiguration.Nonchiral.getStereosymbol());

    /** 
     * Anomer verbose name 
     */
    private String fullname;

    /** 
     * Anomer short name. 
     */
    private String symbol;
    
    private String carbbankSymbol;
    private char bcsdbSymbol;
    
    /** 
     * Anomer stereosymbol (for Monosaccharide in D configuration) 
     */
    private String stereosymbolD; //TODO: replace String with char

    /** 
     * Private constructor, see the getAnomerByName method for external use.
     */
    private Anomer(String fullname, String symbol, String carbbankSym, char bcsdbSym, char stereosymbolD) {
        this.fullname = fullname;
        this.symbol = symbol;
        this.carbbankSymbol = carbbankSym;
        this.bcsdbSymbol = bcsdbSym;
        this.stereosymbolD = "" + stereosymbolD;
    }

    /**
     * Returns this anomer's full name  
     */
    public String getFullname() {  
        return this.fullname;  
    }

    /** 
     * Returns the abbreviated name (symbol) of this anomer. 
     */
    public String getSymbol() {  
        return this.symbol;  
    }
    
    public String getCarbbankSymbol() {
        return this.carbbankSymbol;
    }
    
    /**
     * Format the carbbank symbol of this anomer for direct use in a carbbank style name,
     * i.e. the carbbank symbol followed by a dash unless the symbol is an empty String.
     * @return the formatted carbbank symbol
     */
    public String formatCarbbankSymbol() {
        if(this.carbbankSymbol.length() > 0) {
            return this.carbbankSymbol + "-";
        } else {
            return "";
        }
    }
    
    public char getBcsdbSymbol() {
        return this.bcsdbSymbol;
    }

    /** 
     * Return the stereosymbol for this anomer (for a monosaccharide with anomeric reference carbon in D conformation)
     */
    public String getStereosymbolD() {
        return stereosymbolD;
    }

    /** 
     * Returns the appropriate Anomer instance for the given name or symbol.  
     * @throws MonosaccharideException
     */
    public static Anomer forNameOrSymbol(String anomerStr) throws MonosaccharideException {
        for(Anomer anom : Anomer.values()) {
            if(anomerStr.equalsIgnoreCase(anom.symbol)) {
                return anom;
            }
            if(anomerStr.equalsIgnoreCase(anom.fullname)) {
                return anom;
            }
        }
        throw new MonosaccharideException("Invalid value for anomer: " + anomerStr);
    }
    
    public static Anomer forBcsdbSymbol(char sym) {
        for(Anomer anom : Anomer.values()) {
            if(anom.bcsdbSymbol == sym) {
                return anom;
            }
        }
        return null;
    }
}
