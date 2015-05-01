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
*   Last commit: $Rev: 1259 $ by $Author: glycoslave $ on $Date:: 2009-06-26 #$  
*/

package test.eurocarbdb.sugar;

import java.util.List;
import java.util.ArrayList;

import org.testng.annotations.*;
import org.eurocarbdb.sugar.Anomer;
import test.eurocarbdb.dataaccess.CoreApplicationTest;

import static java.lang.System.out;
import static org.eurocarbdb.sugar.Anomer.*;

// @Test( groups={"sugar.lib.anomer"}, dependsOnGroups={"util.bitset"} )
@Test( groups={"sugar.lib"} )
public class AnomerTest extends CoreApplicationTest
{
    
    static final String[] anomers = new String[] {
        "a",
        "A",
        "b",
        "B",
        "?",
        "u",
        "o"
    };
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~ TESTS ~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    @Test
    public void anomerBasic()
    {
        out.println("sanity checking all common basetypes");
        
        for ( Anomer a : Anomer.values() )   
            describe( a );
    }
    
    
    @Test
    public void anomerParse()
    {
        for ( String text : anomers )
        {
            out.println();
            out.println("attempting to parse '" + text + "':" );
            Anomer a = Anomer.forName( text );
            describe( a );
            assert a != null : "received null for text '" + text + "'";
        }
    }
    
    
    final void describe( Anomer a )
    {
        out.println("anomer: " + a + ", " + a.getFullname() );
    }
    
}

