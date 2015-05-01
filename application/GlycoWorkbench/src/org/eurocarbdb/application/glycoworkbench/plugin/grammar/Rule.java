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

public class Rule implements Comparable{      
    
    private Vector<String> children = new Vector<String>();            
    private Constraint root_constraint = null;
    
    public Rule() {             
    }

    public int compareTo(Object o) {
    return this.toString().compareTo("" + o);
    }


    public static Rule createRule(GrammarTree current, GrammarOptions opt) {
    if( current==null || current.getNoChildren()==0 || current.hasChildrenTagged() )
        return null;
    
    Rule created = new Rule();
        
    // select sugars to add with this rule
    for(GrammarTree child : current.getChildren())                 
        created.children.add(child.getLabel());        
    
    // create constraint        
    created.root_constraint = Constraint.createConstraint(current,opt);                
        
    return created;
    }     

    public GrammarTree generateStructure(GrammarTree current, GrammarOptions opt) {
    
    if( current!=null && current.getNoChildren()!=0 )
        return null;
    if( current==null ) 
        current = new GrammarTree(GrammarTree.END);

    if( root_constraint!=null && root_constraint.matches(current,opt) ) {
        // create the new leaf
        GrammarTree leaf = new GrammarTree(current.getLabel());
        for( String str_child : children ) 
        leaf.addChild(new GrammarTree(str_child));

        // create the structure
        return current.getRoot().clone(current,leaf);
    }
    return null;
    }


    public String leftSide() {
    return root_constraint.toString();
    }

    public String rightSide() {
    return TextUtils.toString(children,',');
    }
    
    public String toString() {
    String ret = "";
        
    if( root_constraint!=null )
        ret += root_constraint.toString();
    else
        ret += "*";
    ret += "->" + TextUtils.toString(children,',');
    
    return ret;
    }



    static public Rule fromXML(Node r_node) throws Exception {
    
    Rule ret = new Rule();

    // read children
    Vector<Node> child_nodes = XMLUtils.findAllChildren(r_node, "Child");
    for( Node c_node : child_nodes) {
        String type = XMLUtils.getAttribute(c_node,"type");
        if( type!=null )
        ret.children.add(type);
    }

    // read constraint
    Node rc_node = XMLUtils.findChild(r_node,"Constraint");
    if( rc_node!=null )
        ret.root_constraint  = Constraint.fromXML(rc_node);

    return ret;
    }
  

    public Element toXML(Document document) {
    if( document==null )
        return null;
    
    // create root node
    Element r_node = document.createElement("Rule");

    // add children nodes
    for( String c : children ) {
        Element c_node = document.createElement("Child");
        c_node.setAttribute("type", c);
        r_node.appendChild(c_node);
    }
    
    // add constraint node
    if( root_constraint!=null ) 
        r_node.appendChild(root_constraint.toXML(document));
    
    return r_node;
    }
}
