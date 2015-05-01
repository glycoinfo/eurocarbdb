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
package org.eurocarbdb.applications.ms.glycopeakfinder.util;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;



public class Configuration 
{
    private Document doc;
    
    public Configuration(URL xmlfile) throws JDOMException, IOException 
    {        
        doc = new SAXBuilder().build(xmlfile);
    }      
    
    public String resultXpathSingleAttribute (String query, String element) throws JDOMException 
    {
        XPath xpath = null;
        xpath = XPath.newInstance(query);
        List resultSet =  xpath.selectNodes( doc );
        Iterator b = resultSet.iterator();
        if (b.hasNext())
        {
            Element oNode = (Element) b.next();
            return (oNode.getAttributeValue(element));
        }
        return null;
    }

    public String getBaseUrl() throws JDOMException
    {
        return this.resultXpathSingleAttribute("/configuration", "base_url");
    }
}