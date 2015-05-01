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

import java.util.Map;
import java.util.HashMap;
import java.util.EnumSet;

import static org.eurocarbdb.sugar.Superclass.*;
import static org.eurocarbdb.sugar.CommonSubstituent.*;
// import static org.eurocarbdb.util.StringUtils.join;

public enum Massclass //implements Molecule
{
    OtherMassclass( "Other", 0.0, 0.0, OtherSuperclass ),
    
    Hex( "Hex", 0.0, 0.0, Hexose, NH2 ),
    
    HexN( "Hexoseamine", 0.0, 0.0, Hexose, NH2 ),
    
    HexNAc( "N-acetylhexosamine", 0.0, 0.0, Hexose, NAc ),
    
    dHex( "deoxyhexose", 0.0, 0.0, Hexose, Deoxy ),
    
    ddHex( "dideoxyhexose", 0.0, 0.0, Hexose, Deoxy, Deoxy ),
    
    dHept( "deoxyheptose", 0.0, 0.0, Heptose, Deoxy ),
    
    HexU( "Hexuronic acid", 0.0, 0.0, Hexose, Uronic ),
    
    HexA( "Hexosealdaric acid", 0.0, 0.0, Hexose, Aldaric ),
    
    KDO( "3-Deoxy-D-manno-oct-2-ulosonic acid", 0.0, 0.0, Octose, Deoxy, Carbonyl, Ulosonic ),
    
    //  sialic acids
    
    KDN( "2-keto-3-deoxy-nonic acid", 0.0, 0.0, Nonose, Deoxy, Carbonyl, Ulosonic ),
    
    NeuAc( "Neuraminic acid", 0.0, 0.0, Nonose, Acetyl, Carboxyl ),
    
    NeuGc( "N-glycolyl-neuraminic acid", 0.0, 0.0, Nonose )
    ;
    
    Massclass( String s, double m1, double m2, Object... x )
    {
    }
    
}
