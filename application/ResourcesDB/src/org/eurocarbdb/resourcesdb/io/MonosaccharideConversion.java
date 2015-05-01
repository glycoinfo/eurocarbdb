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
package org.eurocarbdb.resourcesdb.io;

import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.ResourcesDbException;

public interface MonosaccharideConversion {

    /**
     * Convert a monosaccharide from one notation scheme to another one
     * This version of the conversion method uses only the monosaccharide name as input
     * @param msNamestr Name of the monosaccharide to be converted
     * @param sourceScheme Notation scheme in which the input name is encoded
     * @param targetScheme Notation scheme for output
     * @throws ResourcesDbException in case the monosaccharide cannot be translated (because it is not a valid one or it contains elements that are not available in the output scheme)
     * @return MonosaccharideExchangeObject containing the information of the monosaccharide in the selected output scheme
     */
    public MonosaccharideExchangeObject convertMonosaccharide(String msNamestr, GlycanNamescheme sourceScheme, GlycanNamescheme targetScheme) throws ResourcesDbException;
    
    /**
     * Convert a monosaccharide from one notation scheme to another one
     * This version of the conversion method uses a MonosaccharideExchangeObject as input
     * @param msObj Monosaccharide exchange object to be converted
     * @param sourceScheme Notation scheme in which the input residue is encoded
     * @param targetScheme Notation scheme for output
     * @throws ResourcesDbException in case the monosaccharide cannot be translated (because it is not a valid one or it contains elements that are not available in the output scheme)
     * @return MonosaccharideExchangeObject containing the information of the monosaccharide in the selected output scheme
     */
    public MonosaccharideExchangeObject convertMonosaccharide(MonosaccharideExchangeObject msObj, GlycanNamescheme sourceScheme, GlycanNamescheme targetScheme) throws ResourcesDbException;

    /**
     * Convert a substituent name from one notation scheme to another one
     * @param sourceSubstitutentName Substituent to be converted
     * @param sourceScheme Notation scheme in which the input substituent is encoded
     * @param targetScheme Notation scheme for output
     * @throws ResourcesDbException in case the substituent is not a valid one in the given notation scheme, or no equivalent for that substituent is available in the given output scheme
     * @return Substituent name in the selected notation scheme
     */
    public String convertSubstituent(String sourceSubstituentName, GlycanNamescheme sourceScheme, GlycanNamescheme targetScheme) throws ResourcesDbException;
    
    /**
     * Convert a substituent object from one notation scheme to another one
     * @param sourceSubst Substituent to be converted
     * @param sourceScheme Notation scheme in which the input substituent is encoded
     * @param targetScheme Notation scheme for output
     * @throws ResourcesDbException in case the substituent is not a valid one in the given notation scheme, or no equivalent for that substituent is available in the given output scheme
     * @return Substituent object in the selected notation scheme
     */
    public SubstituentExchangeObject convertSubstituent(SubstituentExchangeObject sourceSubst, GlycanNamescheme sourceScheme, GlycanNamescheme targetScheme) throws ResourcesDbException;
    
    /**
     * Convert an aglycon name from one notation scheme to another one
     * @param sourceSubstitutentName Aglycon to be converted
     * @param sourceScheme Notation scheme in which the input aglycon is encoded
     * @param targetScheme Notation scheme for output
     * @throws ResourcesDbException in case the aglycon is not a valid one in the given notation scheme, or no equivalent for that aglycon is available in the given output scheme
     * @return aglycon name in the selected notation scheme
     */
    public String convertAglycon(String sourceAglyconName, GlycanNamescheme sourceScheme, GlycanNamescheme targetScheme) throws ResourcesDbException;
    
    /**
     * Check, if a substiuent name is a valid one in a given notation scheme
     * This method can also be used to get the primary name of a substiuent in a notation scheme. A parser might need to accept multiple names for one substituent (e.g. "chloro" and "cl" as names of a chlorine substituent), while an encoder should always use the same name
     * @param substName Name of the substituent to be checked
     * @param sourceScheme Notation scheme in which the substituent is encoded
     * @throws ResourcesDbException in case the substituent is not a valid one
     * @return The unique primary name of the given substituent in the current notation scheme
     */
    public String checkSubstituent(String substName, GlycanNamescheme sourceScheme) throws ResourcesDbException;
    
    /**
     * Check, if an aglycon name is a valid one in a given notation scheme
     * This method can also be used to get the primary name of an algycon in a notation scheme. A parser might need to accept multiple names for one aglycon (e.g. "methyl" and "me" as names of a methyl group), while an encoder should always use the same name
     * @param aglyconName Name of the aglycon to be checked
     * @param sourceScheme Notation scheme in which the aglycon is encoded
     * @throws ResourcesDbException in case the aglycan name is not a valid one
     * @return The unique primary name of the given aglycon in the current notation scheme
     */
    public String checkAglycon(String aglyconName, GlycanNamescheme sourceScheme) throws ResourcesDbException;
    
    /**
     * Convert a residue from one notation scheme to another one
     * The residue might be a monosaccharide, a substituent or an aglycon. The detected type of the residue is stated in the returned exchangeObject
     * @param resNamestr
     * @param sourceScheme
     * @param targetScheme
     * @throws ResourcesDbException in case an error occurs in the conversion process - see the conversion methods for defined residue types (monosaccharide, substituent, aglycon) for details
     * @return MonosaccharideExchangeObject containing the information of the residue in the selected output scheme
     */
    public MonosaccharideExchangeObject convertResidue(String resNamestr, GlycanNamescheme sourceScheme, GlycanNamescheme targetScheme) throws ResourcesDbException;
    
    public MonosaccharideExchangeObject validateGlycoCT(MonosaccharideExchangeObject a_objExchange) throws ResourcesDbException;
}
