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

package test.eurocarbdb.util;

import java.util.List;
import java.io.Writer;
import java.io.StringWriter;

import org.testng.Assert;
import org.testng.annotations.*;

import test.eurocarbdb.dataaccess.CoreApplicationTest;

import org.eurocarbdb.util.Version;

import static java.lang.System.out;

public class VersionTest extends CoreApplicationTest
{
    static String[] version_strings = new String[] {
        "200809081531",
        "200809081530",
        "200809071531",
        "200808081531",
        "200709081531"
    };
    
    
    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~ TESTS ~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

    /** */
    @Test
    (   
        groups={"util.versioning"} 
        // dependsOnGroups={"ecdb.db.populated"}
    )
    public void versionSmokeTest()
    {
        Version v = new Version();
        out.println( 
            v 
            + " -> "
            + v.getDate() 
            + " (now)"
        );
        
        for ( String vs : version_strings )
        {
            v = new Version( vs );
            out.println( 
                v 
                + " -> "
                + v.getDate() 
            );
        }
        
/*        
    }
    
    
    @Test
    (   
        groups={"util.versioning"},
        dependsOnMethods={"versionSmokeTest"}
    )
    public void versionCompare()
    {
*/
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        
        out.println("comparing versions: should all be > 0");
        
        assert version_strings.length > 1;
        for ( int i = 1; i != version_strings.length; i++ )
        {
            String vs1 = version_strings[i-1];
            String vs2 = version_strings[i];
            
            Version v1 = new Version( vs1 );
            Version v2 = new Version( vs2 );
            
            int result = v1.compareTo( v2 );
            out.println( v1 + " <=> " + v2 + ": " + result );
            assert result > 0;
        }
        
    }
    
    
}



