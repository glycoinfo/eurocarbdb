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

package test.eurocarbdb.sugar;

import org.testng.annotations.*;

import org.eurocarbdb.sugar.Superclass;
import test.eurocarbdb.dataaccess.CoreApplicationTest;

import static org.eurocarbdb.sugar.Superclass.*;
import static java.lang.System.out;

// @Test( groups={"sugar.lib.superclass"} )
@Test( groups={"sugar.lib"} )
public class SuperclassTest extends CoreApplicationTest
{
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~ TESTS ~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    @Test
    public void superclassFromSize()
    {
        assert Superclass.forSize( 3 ) == Triose;        
        assert Superclass.forSize( 6 ) == Hexose;        
        assert Superclass.forSize( 9 ) == Nonose;
        
        assert Superclass.forName("4") == Tetrose;        
        assert Superclass.forName("5") == Pentose;        
        assert Superclass.forName("7") == Heptose;        

        assert Superclass.forSize( 0 ) == UnknownSuperclass;
    }

    @Test
    public void superclassFromName()
    {
        assert Superclass.forName("Hex") == Hexose;        
        assert Superclass.forName("Pent") == Pentose;        
        assert Superclass.forName("pen") == Pentose;
        
        assert Superclass.forName("octose") == Octose;        
        assert Superclass.forName("undecose") == Undecose;        
        assert Superclass.forName("tetradecose") == Tetradecose;
        
        assert Superclass.forName("s11") == Undecose;        
        assert Superclass.forName("s12") == Dodecose;        
        assert Superclass.forName("s13") == Tridecose;
        
        assert Superclass.forName("undec") == Undecose;        
        assert Superclass.forName("dodec") == Dodecose;        
        assert Superclass.forName("tetradec") == Tetradecose;        
        
        assert Superclass.forName("?") == UnknownSuperclass;        
        assert Superclass.forName("x") == UnknownSuperclass;        
        assert Superclass.forName( null ) == null;        
    }        
    
    
}

