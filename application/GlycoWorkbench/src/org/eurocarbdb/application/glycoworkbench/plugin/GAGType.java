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

package org.eurocarbdb.application.glycoworkbench.plugin;

import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;

import java.util.*;
import java.util.regex.*;

public class GAGType {

    protected String family;
    protected String motif;
    protected int unit_size;
    protected TreeSet<GAGPosition> acetyl_pos;
    protected TreeSet<GAGPosition> opt_acetyl_pos;
    protected TreeSet<GAGPosition> sulfate_pos;
    protected TreeSet<GAGPosition> cooccurring_pos;
    protected String description;

    public GAGType() {
    family = "";
    motif = "";
    unit_size = 0;
    acetyl_pos = new TreeSet<GAGPosition>();    
    opt_acetyl_pos = new TreeSet<GAGPosition>();    
    sulfate_pos = new TreeSet<GAGPosition>();
    cooccurring_pos = new TreeSet<GAGPosition>();
    description = "";
    }

    public GAGType(String init) throws Exception {
    Vector<String> tokens = TextUtils.tokenize(init,"\t");
    if( tokens.size()!=7 ) 
        throw new Exception("Invalid string format: " + init);

    family         = tokens.elementAt(0);
    motif          = tokens.elementAt(1);
    unit_size      = Integer.parseInt(tokens.elementAt(2));
    acetyl_pos     = parsePositions(tokens.elementAt(3));
    opt_acetyl_pos = parsePositions(tokens.elementAt(4));
    sulfate_pos    = parsePositions(tokens.elementAt(5));
    description    = tokens.elementAt(6);

    cooccurring_pos = intersect(acetyl_pos,sulfate_pos);
    }

    public GAGType clone() {
    GAGType ret = new GAGType();
    ret.family = this.family;
    ret.motif  = this.motif;
    ret.unit_size = this.unit_size;
    ret.acetyl_pos = new TreeSet(this.acetyl_pos);
    ret.opt_acetyl_pos = new TreeSet(this.opt_acetyl_pos);
    ret.sulfate_pos = new TreeSet(this.sulfate_pos);
    ret.cooccurring_pos = new TreeSet(this.cooccurring_pos);
    ret.description = this.description;
    return ret;
    }

    public GAGType allowUnlikelyAcetylation() {
    GAGType ret = this.clone();    
    ret.acetyl_pos = ret.opt_acetyl_pos;
    ret.opt_acetyl_pos = new TreeSet<GAGPosition>();    
    return ret;
    }

    public GAGType applyModifications(String[] modifications) {
    
    GAGType ret = this.clone();
    for( int i=0; i<modifications.length; i++ ) {
        if( modifications[i].equals(GAGOptions.DE_2_SULFATION) ) 
        removePosition(ret.sulfate_pos,'2');
        else if( modifications[i].equals(GAGOptions.DE_6_SULFATION) ) 
        removePosition(ret.sulfate_pos,'6');
        else if( modifications[i].equals(GAGOptions.DE_N_SULFATION) ) 
        removePosition(ret.sulfate_pos,'N');
        else if( modifications[i].equals(GAGOptions.RE_ACETYLATION) ) {
        removePosition(ret.sulfate_pos,'N');
        ret.acetyl_pos.addAll(ret.opt_acetyl_pos);
        }
    }
    ret.cooccurring_pos = intersect(ret.acetyl_pos,ret.sulfate_pos);

    return ret;
    }

    // member access
    
    public String getFamily() {
    return family;
    }

    // operations
    
    public Glycan getMotifStructure(GAGOptions opt) {

    try {
        String backbone = generateBackbone(1,opt.IS_REDUCED);

        Collection<GAGPosition> s_pos  = repeat(sulfate_pos,1);
        Collection<GAGPosition> ac_pos = new Vector<GAGPosition>();
        if( opt.containsModification(GAGOptions.RE_ACETYLATION) )
        ac_pos = repeat(acetyl_pos,1);

        return generateStructure(backbone,ac_pos,s_pos,1,0,opt.DERIVATIZATION,opt.IS_UNSATURATED);
    }
    catch(Exception e) {
        LogUtils.report(e);
        return new Glycan();
    }
    }

    public int getMaxNoAcetyls(int no_units) {
    no_units = Math.max(0,no_units);
    return (no_units*acetyl_pos.size());
    }

    public int getMaxNoSulfates(int no_units, int no_acetyls) {    

    no_units = Math.max(0,no_units);       
    int max_no_acetyls = no_units*acetyl_pos.size();
    int max_no_sulfates = no_units*sulfate_pos.size();
    int no_cooccurring = no_units*cooccurring_pos.size();
    no_acetyls = Math.min(no_acetyls,max_no_acetyls);
    
    return max_no_sulfates - Math.max(0,no_acetyls - (max_no_acetyls - no_cooccurring));
    }

    public FragmentCollection generateStructures(GAGOptions opt) {
    FragmentCollection ret = new FragmentCollection();
    generateStructures(ret,null,opt);
    return ret;
    }

    public void generateStructures(GeneratorListener listener, GAGOptions opt) {
    generateStructures(null,listener,opt);
    }

    public void generateStructures(FragmentCollection buffer, GeneratorListener listener, GAGOptions opt) {    

    for( int no_units=opt.MIN_NO_UNITS; no_units<=opt.MAX_NO_UNITS; no_units++ ) {
        if( no_units>0 ) {
        // generate backbone
        String backbone = generateBackbone(no_units,opt.IS_REDUCED);
        
        // establish limits
        int max_no_acetyls = 0;
        int min_no_acetyls = 0;
        if( opt.containsModification(GAGOptions.RE_ACETYLATION) ) 
            min_no_acetyls = max_no_acetyls = getMaxNoAcetyls(no_units);
        else {
            max_no_acetyls = Math.min(opt.MAX_NO_ACETYLS,getMaxNoAcetyls(no_units));
            min_no_acetyls = Math.min(opt.MIN_NO_ACETYLS,max_no_acetyls);
        }

        // generate
        for( int no_acetyls = min_no_acetyls; no_acetyls<=max_no_acetyls; no_acetyls++ ) { 

            // establish limits
            int max_no_sulfates = Math.min(opt.MAX_NO_SULFATES,getMaxNoSulfates(no_units,no_acetyls));
            int min_no_sulfates = Math.min(opt.MIN_NO_SULFATES,max_no_sulfates);
            
            // generate
            for( int no_sulfates=min_no_sulfates; no_sulfates<=max_no_sulfates; no_sulfates++ ) {
            if( !generateStructures(buffer,listener,backbone,no_units,no_acetyls,no_sulfates,opt.DERIVATIZATION,opt.IS_UNSATURATED,opt.ALLOW_REDEND_LOSS) )
                return;
            }
        }        
        }
    }
    }    
    
    private void removePosition( Collection<GAGPosition> pos_coll, char link_pos ) {
    for( Iterator<GAGPosition> i = pos_coll.iterator(); i.hasNext(); ) {
        GAGPosition pos = i.next();
        if( pos.linkage_pos==link_pos )
        i.remove();
    }
    }
    

    private boolean generateStructures(FragmentCollection buffer, GeneratorListener listener, String backbone, int no_units, int no_acetyl, int no_sulfates, String derivatization, boolean unsaturated, boolean allow_redend_loss) {

    // create positions
    Vector<Union<GAGPosition>> combinations = new Vector<Union<GAGPosition>>();
    enumerateCombinations(combinations, new Union<GAGPosition>(), repeat(acetyl_pos,no_units), 0, no_acetyl );

    Union<GAGPosition> all_s_pos = repeat(sulfate_pos,no_units);       
    for(Union<GAGPosition> ac_pos : combinations) {
        // generate structure
        Glycan structure = generateStructure(backbone,ac_pos,all_s_pos,no_units,no_sulfates,derivatization,unsaturated);

        // generate name
        String name = "dp" + (no_units*2) + (new TypePattern().and("Ac",ac_pos.size()).and("S",no_sulfates).toString());

        // add to list
        if( buffer!=null )
        buffer.addFragment(structure,name);
        if( listener!=null ) {
        if( !listener.generatorCallback(new FragmentEntry(structure,name)) )
            return false;    
        }

        if( allow_redend_loss ) {
        // remove redend
        structure = removeReducingEnd(structure);

        // count remaining labiles
        int no_ac = 0;
        for( GAGPosition pos : ac_pos ) {
            if( pos.residue_id!=0 ) 
            no_ac++;
        }    
        int no_s  = structure.countResidues("S");
        
        // generate name
        name = "dp" + (no_units*2-1) + (new TypePattern().and("Ac",no_ac).and("S",no_s).toString());

        // add to list
        if( buffer!=null )
            buffer.addFragment(structure,name);
        if( listener!=null ) {
            if( !listener.generatorCallback(new FragmentEntry(structure,name)) )
            return false;    
        }
        }        
    }
    return true;
    }

    private Glycan removeReducingEnd(Glycan structure) {
    Glycan ret = structure.clone();
    Residue toremove = ret.getRoot().firstChild();    
    
    // remove substituents and placeholders
    int removed_placeholders = 0;
    Vector<Residue> toremove_children = new Vector<Residue>();
    for( Linkage l : toremove.getChildrenLinkages() ) {
        if( !l.getChildResidue().isSaccharide() )
        toremove_children.add(l.getChildResidue()); // prevent concurrent exception
        if( l.getChildResidue().isLCleavage() )
        removed_placeholders++;
    }     
    for(Residue r : toremove_children ) 
        toremove.removeChild(r);
    
    // remove reducing end
    ret.getRoot().removeChild(ret.getRoot().firstChild());            
    
    // fix labiles
    if( removed_placeholders>0 ) { 
        TypePattern detached = ret.getDetachedLabilesPattern();
        if( detached.size()>0 && detached.contains(ret.getLabilePositionsPattern()) ) 
        ret = ret.removeDetachedLabiles().reattachAllLabileResidues();        
    }
    return ret;
    }
    

    private Glycan generateStructure(String backbone, Collection<GAGPosition> ac_pos, Collection<GAGPosition> s_pos, int no_units, int no_sulfates, String derivatization, boolean unsaturated) {
    try {
        Glycan structure = Glycan.fromString(backbone,createMassOptions(derivatization));
        if( unsaturated )
        makeUnsaturated(structure); 
        
        int id = 0;
        boolean attach_sulfates = (no_sulfates == getMaxNoSulfates(no_units,ac_pos.size()));
        for( Residue nav = structure.getRoot().firstSaccharideChild(); nav!=null; nav = nav.firstSaccharideChild() ) {
        // add acetyl
        for( GAGPosition pos : ac_pos ) {
            if( id==pos.residue_id ) {
            if( pos.linkage_pos!='N' || !toHexNAc(nav) )
                nav.addChild(ResidueDictionary.newResidue("Ac"),pos.linkage_pos);            
            }
        }
                
        // add sulfates      
        for( GAGPosition pos : s_pos ) {
            if( id==pos.residue_id && !ac_pos.contains(pos) ) {
            if( attach_sulfates ) {
                // add sulfate
                nav.addChild(ResidueDictionary.newResidue("S"),pos.linkage_pos);            
            }            
            else {
                // add placeholder
                Residue l_cleav = new Residue(ResidueType.createLCleavage());
                l_cleav.setCleavedResidue(ResidueDictionary.newResidue("S"));
                nav.addChild(l_cleav,pos.linkage_pos);
            }
            }
        }        
        
        id++;
        }
                
        
        if( !attach_sulfates ) {
        // add sulfates                      
        for( int i=0; i<no_sulfates; i++ )
            structure.addAntenna(ResidueDictionary.newResidue("S"));
        }

        return structure;    
    }
    catch(Exception e) {
        LogUtils.report(e);
        return new Glycan();
    }
    }

    public String generateBackbone(int nounits, boolean reduced) {
    String backbone = (reduced) ?"redEnd" :"freeEnd";
    for( int i=0; i<nounits; i++ )
        backbone += "--" + motif;    
    return backbone;
    }

    public void makeUnsaturated(Glycan structure) {
    if( structure==null || structure.getRoot()==null )
        return;

    // find last residue       
    Residue nav = structure.getRoot();
    while(true) {
        // search for a saccharide children
        Residue child = null;        
        for( Linkage l : nav.getChildrenLinkages() ) {
        if( l.getChildResidue().isSaccharide() ) {
            child = l.getChildResidue();
            break;
        }
        }
        
        if( child==null )
        break;
        nav = child;
    }

    // make unsaturation
    char pos = '?';
    if( structure.getRoot().firstLinkage()!=null )
        pos = structure.getRoot().firstLinkage().getParentPositionsSingle();
        
    try { 
        if( ResidueDictionary.findResidueType(pos + "u" + nav.getTypeName())!=null ) 
        nav.setType(ResidueDictionary.getResidueType(pos + "u" + nav.getTypeName()));
        else 
        nav.addChild(ResidueDictionary.newResidue("un"),pos);
    }
    catch( Exception e ) {
        LogUtils.report(e);
    }
    }
       
    
    public Union<GAGPosition> repeat(Collection<GAGPosition> positions, int times) { 
    Union<GAGPosition> ret = new Union<GAGPosition>();
    
    for( int i=0; i<times; i++ ) 
        for( GAGPosition pos : positions ) 
        ret.add(pos.translate(i*unit_size));
    
    return ret;
    }

    public void enumerateCombinations(Vector<Union<GAGPosition>> buffer, Union<GAGPosition> combination, Union<GAGPosition> collection, int ind, int combination_size) {
    if( combination.size()==combination_size ) {
        buffer.add(combination);
        return;
    }
    
    // add
    enumerateCombinations(buffer,combination.and(collection.elementAt(ind)),collection,ind+1,combination_size);    

    // don't add (if possible)
    if( (collection.size()-ind)>(combination_size-combination.size()) ) 
        enumerateCombinations(buffer,combination,collection,ind+1,combination_size);

    return;
    }

    public boolean toHexNAc(Residue r) {    
    if( r==null )
        return false;

    try {
        if( r.getTypeName().equals("HexN") ) {
        r.setType(ResidueDictionary.getResidueType("HexNAc"));
        return true;
        }
        if( r.getTypeName().equals("GalN") ) {
        r.setType(ResidueDictionary.getResidueType("GalNAc"));
        return true;
        }
        if( r.getTypeName().equals("GlcN") ) {
        r.setType(ResidueDictionary.getResidueType("GlcNAc"));
        return true;
        }
        if( r.getTypeName().equals("ManN") ) {
        r.setType(ResidueDictionary.getResidueType("ManNAc"));    
        return true;
        }
        return false;
    }
    catch(Exception e) {
        LogUtils.report(e);
        return false;
    }    
    }

    private MassOptions createMassOptions(String derivatization) {
    MassOptions ret = new MassOptions();

    ret.DERIVATIZATION = derivatization;
    ret.ION_CLOUD = new IonCloud();

    return ret;
    }

    // serialization

    private TreeSet<GAGPosition> intersect(TreeSet<GAGPosition> s1, TreeSet<GAGPosition> s2) {
    TreeSet<GAGPosition> ret = new TreeSet<GAGPosition>();
    for( GAGPosition p : s1 ) 
        if( s2.contains(p) )
        ret.add(p);
    return ret;
    }
    
    private TreeSet<GAGPosition> parsePositions(String str) throws Exception {
    TreeSet<GAGPosition> ret = new TreeSet<GAGPosition>();
    if( str.equals("-") )
        return ret;

    Vector<String> tokens = TextUtils.tokenize(str,",");
    for( String token : tokens ) 
        ret.add(GAGPosition.fromString(token));    
    return ret;
    }
    
    public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(family);
    sb.append('\t');
    sb.append(motif);
    sb.append('\t');
    sb.append(TextUtils.toString(acetyl_pos,','));
    sb.append('\t');
    sb.append(TextUtils.toString(sulfate_pos,','));
    return sb.toString();    
    }
    
}

class GAGPosition implements Comparable<GAGPosition> {
    
    static Pattern pattern;
    static {
    pattern = Pattern.compile("^([0-9]+)\\#([1-9N])$");
    }

    public int residue_id = 0;
    public char linkage_pos = 'N';

    public GAGPosition() {
    }

    public boolean equals(Object obj) {
    if( !(obj instanceof GAGPosition) )
        return false;
    
    GAGPosition o = (GAGPosition)obj;
    return ( residue_id==o.residue_id && linkage_pos==o.linkage_pos );
    }

    public int compareTo(GAGPosition o) {
    if( o==null )
        return 1;
    
    if( residue_id>o.residue_id ) 
        return 1;
    if( residue_id==o.residue_id && linkage_pos>o.linkage_pos )
        return 1;
    if( residue_id==o.residue_id && linkage_pos==o.linkage_pos )
        return 0;
    return -1;
    }

    public GAGPosition translate(int by) {
    GAGPosition ret = new GAGPosition();
    ret.residue_id = this.residue_id+by;
    ret.linkage_pos = this.linkage_pos;
    return ret;
    }

    static GAGPosition fromString(String str) throws Exception {
    GAGPosition ret = new GAGPosition();    
    
    Matcher m = pattern.matcher(str);
    if( !m.matches() )
        throw new Exception("Invalid format for position: " + str);
    
    ret.residue_id = Integer.parseInt(m.group(1));
    ret.linkage_pos = m.group(2).charAt(0);

    return ret;
    }

    public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(residue_id);
    sb.append('#');
    sb.append(linkage_pos);    
    return sb.toString();
    }
}
