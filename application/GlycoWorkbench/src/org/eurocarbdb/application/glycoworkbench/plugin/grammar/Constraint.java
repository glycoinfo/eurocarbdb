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
/**
   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

package org.eurocarbdb.application.glycoworkbench.plugin.grammar;

import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;
import java.util.*;
import org.w3c.dom.*;

public class Constraint {
        
    private Constraint parent = null;
    private String node_type = "";
    private String children_types = "";
    
    public Constraint() {
    }
    
    public static Constraint createConstraint(GrammarTree current, GrammarOptions opt) {
    return createConstraint(current,null,false,0,opt);
    }

    public static Constraint createConstraint(GrammarTree current, GrammarTree skip, boolean add_children, int level, GrammarOptions opt) {
    if( current==null )
        return null;
    if( level>=opt.MAX_LEVEL )
        return null;
    
    Constraint ret = new Constraint();
    
    // get parent type
    ret.node_type = current.getLabel();
    
    // convert children to string and sort     
    if( add_children ) 
        ret.children_types = getChildrenTypes(current,skip,opt);
    
    // recurse
    ret.parent = createConstraint(current.getParent(),current,opt.ADD_UNCLES,level+1,opt);
    
    return ret;
    }

    private static String getChildrenTypes(GrammarTree current, GrammarTree skip, GrammarOptions opt) {
    LinkedList<String> list = new LinkedList<String>();
    
    for( GrammarTree child : current.getChildren() ) {
        if( child!=skip ) {
        String type = child.getLabel();
        int index = Collections.binarySearch(list, type);   
        if (index < 0) list.add(-index-1, type);
        else list.add(index, type);    
        }
    }                        

    return TextUtils.toString(list,',');
    }


    public boolean matches(GrammarTree current, GrammarOptions opt) {
    return matches(current,null,false,opt);
    }

    public boolean matches(GrammarTree current, GrammarTree skip, boolean match_children, GrammarOptions opt) {
    if( !node_type.equals(current.getLabel()) )
        return false;
    if( match_children && !children_types.equals(getChildrenTypes(current,skip,opt)) )
        return false;
    if( parent!=null && current==null )
        return false;
    return (parent==null || parent.matches(current.getParent(),current,opt.ADD_UNCLES,opt));
    }

    public String toString() {
    String ret = "";
    
    ret += node_type;
    if( children_types.length()>0 ) 
        ret += "(" + children_types + ")";
    if( parent!=null )
        ret += " " + parent.toString();
    
    return ret;
    }

    static public Constraint fromXML(Node c_node) throws Exception {
    
    Constraint ret = new Constraint();

    // read values
    ret.node_type = XMLUtils.getAttribute(c_node,"node_type");
    if( ret.node_type==null )
        ret.node_type = "";
    
    ret.children_types = XMLUtils.getAttribute(c_node,"children_types");
    if( ret.children_types==null )
        ret.children_types = "";

    // read parent
    Node p_node = XMLUtils.findChild(c_node,"Constraint");
    if( p_node!=null )
        ret.parent  = Constraint.fromXML(p_node);

    return ret;
    }
  

    public Element toXML(Document document) {
    if( document==null )
        return null;
    
    // create root node
    Element c_node = document.createElement("Constraint");

    // set values
    c_node.setAttribute("node_type", "" + node_type);
    c_node.setAttribute("children_types", "" + children_types);

    // add parent node
    if( parent!=null )
        c_node.appendChild(parent.toXML(document));
    
    return c_node;
    }
    
}
          
