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

package org.eurocarbdb.application.glycoworkbench.plugin.reporting;

import org.eurocarbdb.application.glycoworkbench.plugin.grammar.*;
import org.eurocarbdb.application.glycoworkbench.plugin.*;
import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;

import java.util.*;

public class ProfilesComparisonReportDocument {

    public static class Row {        

    public Glycan structure;
    public String name;
        public double mz_ratio;
        public double[] intensities_firstgroup;
        public double[] intensities_secondgroup;   

    public Row(Glycan g, String n, int fg_size, int sg_size) {
            structure = g.clone();
        name = n;
            mz_ratio = g.computeMZ();        

            intensities_firstgroup = new double[fg_size];
            intensities_secondgroup = new double[sg_size];
            
            Arrays.fill(intensities_firstgroup,0.);
            Arrays.fill(intensities_secondgroup,0.);
        }

        public Row(FragmentEntry fe, int fg_size, int sg_size) {
        this(fe.fragment,fe.name,fg_size,sg_size);
        }

        public double getColumn(int ind) {
            if( ind<intensities_firstgroup.length )
                return intensities_firstgroup[ind];
            return intensities_secondgroup[ind-intensities_firstgroup.length];
        }

    public int getFirstGroupSize() {
        return intensities_firstgroup.length;
    }

    public int getSecondGroupSize() {
        return intensities_secondgroup.length;
    }

    public void addIntensities(Row other) {
        addIntensities(other,1.);
    }
     
    public void addIntensities(Row other, double weight) {
        int fg_size = Math.min(this.getFirstGroupSize(),other.getFirstGroupSize());
        for( int i=0; i<fg_size; i++ )
        this.intensities_firstgroup[i] += weight * other.intensities_firstgroup[i];

        int sg_size = Math.min(this.getSecondGroupSize(),other.getSecondGroupSize());
        for( int i=0; i<sg_size; i++ )
        this.intensities_secondgroup[i] += weight * other.intensities_secondgroup[i];
    }

             
    }

    private ProfilesComparisonReportOptions theOptions;
    private Grammar theGrammar;
    
    private Vector<String> names;
    private Vector<String> namesFirstGroup;
    private Vector<String> namesSecondGroup;

    private Vector<Row> rows;

    public ProfilesComparisonReportDocument(Vector<Scan> firstGroup, Vector<Scan> secondGroup, Map<Scan,String> scanNameMap, Grammar grammar, ProfilesComparisonReportOptions options) throws Exception {
        theOptions = options;
    theGrammar = grammar;
        
        // collect data
        rows = collectData(firstGroup,secondGroup);

        // collect names
        namesFirstGroup = collectNames(firstGroup,scanNameMap);
        namesSecondGroup = collectNames(secondGroup,scanNameMap);
        names = new Union<String>(namesFirstGroup).and(namesSecondGroup);

        // normalize
        normalize(rows,options);
        
        // deconvolute
        rows = deconvolute(rows,grammar,options);
    
    // normalize by row
       if( options.NORMALIZEBYROW ) 
        normalizeByRow(rows);
    else 
        normalizeByTable(rows);
    }

    public Vector<String> getNames() {
        return names;
    }

    public Vector<String> getNamesFirstGroup() {
        return namesFirstGroup;
    }

    public Vector<String> getNamesSecondGroup() {
        return namesSecondGroup;
    }

    public Vector<Row> getRows() {
        return rows;
    }

    public int getNoRows() {
        return rows.size();
    }
    
    public int getNoColumns() {
        if( rows.size()==0 )
            return 0;
        int fg_size = rows.get(0).intensities_firstgroup.length;
        int sg_size = rows.get(0).intensities_secondgroup.length; 
        return fg_size+sg_size;
    }

    public int getNoColumnsFirstGroup() {
        if( rows.size()==0 )
            return 0;
        return rows.get(0).intensities_firstgroup.length;
    }

    public int getNoColumnsSecondGroup() {
        if( rows.size()==0 )
            return 0;
        return rows.get(0).intensities_secondgroup.length;
    }

    public int size() {
        return rows.size();
    }

    private static Vector<Row> collectData(Vector<Scan> firstGroup, Vector<Scan> secondGroup) {
        int fg_size = (firstGroup!=null) ?firstGroup.size() :0;
        int sg_size = (secondGroup!=null) ?secondGroup.size() :0;
        TreeMap<FragmentEntry,Row> ret = new TreeMap<FragmentEntry,Row>();

        // collect data for first group
        for( int i=0; i<fg_size; i++ ) {
            Scan s = firstGroup.get(i);
            PeakAnnotationCollection pac = s.getAnnotatedPeakList().getPeakAnnotationCollection(0);
            for( PeakAnnotation pa : pac.getPeakAnnotations() ) {
                if( pa.isAnnotated() ) {
                    Row row = ret.get(pa.getAnnotation().getFragmentEntry());
                    if( row==null ) {
                        row = new Row(pa.getAnnotation().getFragmentEntry(),fg_size,sg_size);
            row.name = GlycanRenderer.makeCompositionTextPlain(row.structure.getComposition(false));
                        ret.put(pa.getAnnotation().getFragmentEntry(),row);
                    }
                    row.intensities_firstgroup[i] += pa.getPeak().getIntensity();
                }
            }
        }

        // collect data for second group
        for( int i=0; i<sg_size; i++ ) {
            Scan s = secondGroup.get(i);
            PeakAnnotationCollection pac = s.getAnnotatedPeakList().getPeakAnnotationCollection(0);
            for( PeakAnnotation pa : pac.getPeakAnnotations() ) {
                if( pa.isAnnotated() ) {
                    Row row = ret.get(pa.getAnnotation().getFragmentEntry());
                    if( row==null ) {
                        row = new Row(pa.getAnnotation().getFragmentEntry(),fg_size,sg_size);
            row.name = GlycanRenderer.makeCompositionTextPlain(row.structure.getComposition(false));
                        ret.put(pa.getAnnotation().getFragmentEntry(),row);
                    }
                    row.intensities_secondgroup[i] += pa.getPeak().getIntensity();
                }
            }
        }

        return new Vector<Row>(ret.values());        
    }

    private static Vector<String> collectNames(Vector<Scan> group, Map<Scan,String> scanNameMap) {
        Vector<String> ret = new Vector<String>();
        if( group!=null ) {
            for( Scan s : group )
                ret.add(scanNameMap.get(s));
        }
        return ret;
    }

    private static void normalize(Vector<Row> rows, ProfilesComparisonReportOptions options) {
        if( rows.size()==0 ) 
            return;

        int fg_size = rows.get(0).intensities_firstgroup.length;
        for( int i=0; i<fg_size; i++ ) {
            double div = 0.;
            for( Row row : rows ) {
                if( options.NORMALIZATION == options.BASEPEAK )
                    div = Math.max(div,row.intensities_firstgroup[i]);
                else if( options.NORMALIZATION == options.SUM )
                    div += row.intensities_firstgroup[i];
                else if( options.NORMALIZATION == options.AVERAGE )
                    div += row.intensities_firstgroup[i]/(double)rows.size();
            }
            for( Row row : rows ) 
                row.intensities_firstgroup[i] /= div;
        }

        int sg_size = rows.get(0).intensities_secondgroup.length;
        for( int i=0; i<sg_size; i++ ) {
            double div = 0.;
            for( Row row : rows ) {
                if( options.NORMALIZATION == options.BASEPEAK )
                    div = Math.max(div,row.intensities_secondgroup[i]);
                else if( options.NORMALIZATION == options.SUM )
                    div += row.intensities_secondgroup[i];
                else if( options.NORMALIZATION == options.AVERAGE )
                    div += row.intensities_secondgroup[i]/(double)rows.size();
            }
            for( Row row : rows ) 
                row.intensities_secondgroup[i] /= div;
        }
    }

    private static void normalizeByRow(Vector<Row> rows) {
        if( rows.size()==0 ) 
            return;

        int fg_size = rows.get(0).intensities_firstgroup.length;
        int sg_size = rows.get(0).intensities_secondgroup.length; 
        for( Row row : rows ) {
            double min = 0.;
            double max = 0.;
            for( int i=0; i<fg_size; i++ ) {
                max = Math.max(row.intensities_firstgroup[i],max);
                min = Math.min(row.intensities_firstgroup[i],min);
            }
            for( int i=0; i<sg_size; i++ ) {
                max = Math.max(row.intensities_secondgroup[i],max);
                min = Math.min(row.intensities_secondgroup[i],min);
            }

            double center = (max+min)/2;
            double deviation = (max-min)/2;
            for( int i=0; i<fg_size; i++ )
                row.intensities_firstgroup[i] = (row.intensities_firstgroup[i]-center)/deviation;
            for( int i=0; i<sg_size; i++ )
                row.intensities_secondgroup[i] = (row.intensities_secondgroup[i]-center)/deviation;
        }
    }

    private static void normalizeByTable(Vector<Row> rows) {
        int fg_size = rows.get(0).intensities_firstgroup.length;
        int sg_size = rows.get(0).intensities_secondgroup.length; 

        double max = 0.;;
        for( Row row : rows ) {
            for( int i=0; i<fg_size; i++ ) 
                max = Math.max(Math.abs(row.intensities_firstgroup[i]),max);
            for( int i=0; i<sg_size; i++ ) 
                max = Math.max(Math.abs(row.intensities_secondgroup[i]),max);
        }

        for( Row row : rows ) {
            for( int i=0; i<fg_size; i++ )
                row.intensities_firstgroup[i] /= max;
            for( int i=0; i<sg_size; i++ )
                row.intensities_secondgroup[i] /= max;
        }
    }    

    private static Vector<Row> deconvolute(Vector<Row> rows, Grammar grammar, ProfilesComparisonReportOptions options) throws Exception {

    if( rows.size()==0 || options.DECONVOLUTION == ProfilesComparisonReportOptions.NONE )
        return rows;

    int fg_size = rows.get(0).getFirstGroupSize();        
        int sg_size = rows.get(0).getSecondGroupSize();
        TreeMap<FragmentEntry,Row> ret = new TreeMap<FragmentEntry,Row>();
    
    // place uncertain antennae in fuzzy structures
    if( options.DECONVOLUTION == ProfilesComparisonReportOptions.DISACCHARIDES ||
        options.DECONVOLUTION == ProfilesComparisonReportOptions.TERMINALS )
        rows = resolveFuzzyStructures(rows,grammar);

    for( Row r : rows ) {    
        Map<FragmentEntry,Integer> components;
        if( options.DECONVOLUTION == ProfilesComparisonReportOptions.MONOSACCHARIDES ) 
        components = getMonosaccharides(r.structure);    
        else if( options.DECONVOLUTION == ProfilesComparisonReportOptions.DISACCHARIDES ) 
        components = getDisaccharides(r.structure);    
        else if( options.DECONVOLUTION == ProfilesComparisonReportOptions.CORES ) 
        components = getCore(r.structure);    
        else if( options.DECONVOLUTION == ProfilesComparisonReportOptions.TERMINALS ) 
        components = getTerminals(r.structure);
        else
        throw new Exception("Uknown deconvolution type");
        
        for( Map.Entry<FragmentEntry,Integer> e : components.entrySet() ) {
        Row dest = ret.get(e.getKey());
        if( dest==null ) {
            dest = new Row(e.getKey(),fg_size,sg_size);
            ret.put(e.getKey(),dest);
        }
        dest.addIntensities(r,(int)e.getValue());
        }
    }    
    
        return new Vector<Row>(ret.values());
    }

    private static Vector<Row> resolveFuzzyStructures(Vector<Row> rows, Grammar grammar) throws Exception {
    if( rows.size()==0 )
        return rows;
    int fg_size = rows.get(0).getFirstGroupSize();        
        int sg_size = rows.get(0).getSecondGroupSize();

    // create grammar from non-fuzzy structure if necessary
    //if( grammar == null ) {

        Vector<Glycan> nonfuzzy_structures = new Vector<Glycan>();
        for( Row r : rows ) {
        if( !r.structure.isFuzzy() )
            nonfuzzy_structures.add(r.structure);
        }
        
        GrammarOptions options = new GrammarOptions();
        options.ADD_LINKAGE_INFO = false;
        options.MAX_LEVEL = 2;
        options.ADD_UNCLES = false;
        options.USE_SEEDS = false;
        options.TAG_CORES = false;
        
        grammar = Grammar.createGrammar(nonfuzzy_structures,options);
        //}
    
    // collect rows
    TreeMap<Glycan,Row> ret = new TreeMap<Glycan,Row>();
    for( Row r : rows ) {
        if( !r.structure.isFuzzy() ) {
        ret.put(r.structure,r);
        }
        else {
        if( r.structure.getNoAntennae()>3 ) {
            continue;
        }

        // resolve fuzzy structure
        Vector<Glycan> all = r.structure.placeAntennae();

        Vector<Glycan> resolved = new Vector<Glycan>();
        for( Glycan g : all ) {
            if( grammar.accept(g) )
            resolved.add(g);
        }

        if( resolved.size()==0 )
            System.err.println("Cannot resolve some fuzzy structures");
            //throw new Exception("Cannot resolve some fuzzy structures");
        
        // distribute intensities
        double weight = 1./(double)resolved.size();
        for( Glycan g : resolved ) {
            Row dest = ret.get(g);
            if( dest==null ) {
            String name = GlycanRenderer.makeCompositionTextPlain(g.getComposition(false));
            dest = new Row(g,name,fg_size,sg_size);
            ret.put(g,dest);
            }
            dest.addIntensities(r,weight);
        }
        }
    }
    return new Vector<Row>(ret.values());    
    }

    private static void add(Map<FragmentEntry,Integer> ret, Glycan g, String name, int quantity) {    
    FragmentEntry fe = new FragmentEntry(g,name);
    Integer old_count = ret.get(fe);
    if( old_count==null )
        ret.put(fe,quantity);
    else
        ret.put(fe,old_count+quantity);
    }

    private static Map<FragmentEntry,Integer> getMonosaccharides(Glycan structure) {
    HashMap<FragmentEntry,Integer> ret = new HashMap<FragmentEntry,Integer>();
    getMonosaccharides(ret,structure.getRoot());
    return ret;
    }

    private static void getMonosaccharides(Map<FragmentEntry,Integer> ret, Residue node) {
    if( node.isSaccharide() || node.isSubstituent() ) {
        // make structure
        Residue r = node.cloneResidue();
        r.setPreferredPlacement(null);
        Glycan g = new Glycan(r,true,new MassOptions());
        String name = node.getTypeName();
        add(ret,g,name,1);
    }
    for( Linkage l : node.getChildrenLinkages() )
        getMonosaccharides(ret,l.getChildResidue());
    }

    private static Map<FragmentEntry,Integer> getDisaccharides(Glycan structure) throws Exception {
    if( structure.isFuzzy() )
        throw new Exception("Cannot get disaccharides from fuzzy structures");            

    HashMap<FragmentEntry,Integer> ret = new HashMap<FragmentEntry,Integer>();
    getDisaccharides(ret,structure.getRoot());
    return ret;
    }

    private static void getDisaccharides(Map<FragmentEntry,Integer> ret, Residue node) {
    Residue parent = node.getParent();
    if( parent!=null &&
        ((parent.isSaccharide() || parent.isSubstituent()) &&                          
         (node.isSaccharide() || node.isSubstituent())) ) {
        // make structure
        Residue rp = parent.cloneResidue();
        rp.setPreferredPlacement(null);
        Residue rc = node.cloneResidue();
        rc.setPreferredPlacement(null);
        rp.addChild(rc,node.getParentLinkage().getBonds());
        
        Glycan g = new Glycan(rp,true,new MassOptions());
        String name = rc.getTypeName() + node.getParentLinkage().toIupac() + rp.getTypeName();
        add(ret,g,name,1);
    }
    for( Linkage l : node.getChildrenLinkages() )
        getDisaccharides(ret,l.getChildResidue());
    }   

    private static Map<FragmentEntry,Integer> getCore(Glycan structure) throws Exception {
    HashMap<FragmentEntry,Integer> ret = new HashMap<FragmentEntry,Integer>();        

    // sort cores by mass descending
    GWSParser parser = new GWSParser();
    Vector<FragmentEntry> cores_ordered = new Vector<FragmentEntry>();
    for( CoreType ct : CoreDictionary.getCores() ) {
        Glycan core = new Glycan(ct.newCore(),true,new MassOptions());
        cores_ordered.add(new FragmentEntry(core,ct.getDescription()));
    }
    Collections.sort(cores_ordered, new Comparator<FragmentEntry>() {
        public int compare(FragmentEntry f1, FragmentEntry f2) {
            if( f1.mz_ratio>f2.mz_ratio )
            return -1;
            if( f1.mz_ratio<f2.mz_ratio )
            return 1;
            return 0;
        }
        });

    // match structures with all cores
    for( FragmentEntry core : cores_ordered ) {
        if( structure.contains(core.fragment,true,false) ) {
        ret.put(core,1);
        return ret;
        }
    }

    return ret;
    }

    private static Map<FragmentEntry,Integer> getTerminals(Glycan structure) throws Exception {
    HashMap<FragmentEntry,Integer> ret = new HashMap<FragmentEntry,Integer>();

    // sort terminals by mass descending
    GWSParser parser = new GWSParser();
    Vector<FragmentEntry> terminals_ordered = new Vector<FragmentEntry>();
    for( TerminalType tt : TerminalDictionary.getTerminals() ) {
        Glycan terminal = new Glycan(tt.newTerminal(),true,new MassOptions());
        terminals_ordered.add(new FragmentEntry(terminal,tt.getDescription()));
    }
    Collections.sort(terminals_ordered, new Comparator<FragmentEntry>() {
        public int compare(FragmentEntry f1, FragmentEntry f2) {
            if( f1.mz_ratio>f2.mz_ratio )
            return -1;
            if( f1.mz_ratio<f2.mz_ratio )
            return 1;
            return 0;
        }
        });

    // match structures with all terminals
    for( FragmentEntry terminal : terminals_ordered ) {
        int count = structure.count(terminal.fragment,false,true);
        if( count>0 )
        ret.put(terminal,count);
    }
    
    return ret;
    }
    
}