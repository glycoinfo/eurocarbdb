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

import org.eurocarbdb.application.glycoworkbench.plugin.*;
import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;

import java.util.*;
import java.io.*;
import org.w3c.dom.*;

public class Grammar implements StructureGenerator, StructureScorer {

    public static final int SCORE_DIST = 0;
    public static final int SCORE_PDIST = 1;
    public static final int SCORE_PROB = 2;
    public static final int SCORE_PROB_DIST = 3;

    // constants
    private static GrammarTree[] cores = new GrammarTree[8];
    private static GrammarTree[] cores_tagged = new GrammarTree[8];
    static {
    try {
        // big one first
        cores[0] = GrammarTree.fromString("(+(GlcNAc(Fuc)(GlcNAc(Man(Man(Man)(Man))(GlcNAc)(Man)))))");        
        cores[1] = GrammarTree.fromString("(+(GlcNAc(Fuc)(GlcNAc(Man(Man(Man)(Man))(Man)))))");        
        cores[2] = GrammarTree.fromString("(+(GlcNAc(Fuc)(GlcNAc(Man(Man)(GlcNAc)(Man)))))"); 
        cores[3] = GrammarTree.fromString("(+(GlcNAc(Fuc)(GlcNAc(Man(Man)(Man)))))");         
        cores[4] = GrammarTree.fromString("(+(GlcNAc(GlcNAc(Man(Man(Man)(Man))(GlcNAc)(Man)))))");        
        cores[5] = GrammarTree.fromString("(+(GlcNAc(GlcNAc(Man(Man(Man)(Man))(Man)))))");        
        cores[6] = GrammarTree.fromString("(+(GlcNAc(GlcNAc(Man(Man)(GlcNAc)(Man)))))"); 
        cores[7] = GrammarTree.fromString("(+(GlcNAc(GlcNAc(Man(Man)(Man)))))");         

        for( int i=0; i<cores.length; i++ ) {
        cores_tagged[i] = cores[i].clone();        
        tagCore(cores_tagged[i]);
        }
    }
    catch(Exception e) {
    }
    };
  
    // members
    private TreeMap<Rule,Double> rules = new TreeMap<Rule,Double>();
    private TreeMap<GrammarTree,RuleProfile> seeds = new TreeMap<GrammarTree,RuleProfile>();
    
    private GrammarOptions options = new GrammarOptions();

    // generation
    private MassOptions mass_opt = null;
    private LinkedList<GrammarTree> pool = new LinkedList<GrammarTree>();
    private TreeSet<String> visited = new TreeSet<String>();
    private GrammarTree last_structure = null;

    public Grammar() {
    }

    public Grammar(String filename, boolean on_file_system) throws Exception {
    load(filename,on_file_system);
    }
    
    public Collection<Rule> getRules() {
    return rules.keySet();
    }

    public String getScorerType() {
    return "Grammar";
    }

    // i/o
    
    public void load(String filename, boolean on_file_system) throws Exception {
    load(FileUtils.open(this.getClass(),filename,!on_file_system));
    }

    public void load(InputStream is) throws Exception {
    Document d = XMLUtils.read(is);
    this.fromXML(XMLUtils.assertChild(d,"Grammar"),false);
    }

    public void save(String filename) throws Exception {
    save(new FileOutputStream(filename));
    }

    public void save(OutputStream os) throws Exception {
    Document d = XMLUtils.newDocument();
    d.appendChild(toXML(d));
    XMLUtils.write(os,d);
    }

    // learning 
    
    public static Collection<Rule> extractRules(Glycan structure, GrammarOptions opt) throws Exception {
    Vector<Rule> ret = new Vector<Rule>();
    extractRules(ret,structure,opt);
    return ret;
    }

    public static Collection<Rule> extractRules(GrammarTree structure, GrammarOptions opt) {
    Vector<Rule> ret = new Vector<Rule>();
    extractRules(ret,structure,opt);
    return ret;
    }

    public static void extractRules(Collection<Rule> buffer, Glycan structure, GrammarOptions opt) throws Exception {

    // parse the tree
    GrammarTree gt = GrammarTree.fromGlycan(structure, opt.ADD_LINKAGE_INFO);
    // tag the core
    if( opt.TAG_CORES )
        tagCore(gt);     
    
    // extract the rules
    extractRules(buffer,gt,opt);    
    }

    private static void extractRules(Collection<Rule> buffer, GrammarTree gt, GrammarOptions opt) {
    if( buffer==null )
        return;

    // add new rule
    Rule toadd = Rule.createRule(gt,opt);
    if( toadd!=null ) 
        buffer.add(toadd);

    // recurse
    for( GrammarTree gtc : gt.getChildren() ) 
        extractRules(buffer,gtc,opt);
    
    return;
    }
  

    public static Grammar createGrammar(Collection<Glycan> structures, GrammarOptions opt) {
    if( structures==null || opt==null )
        return new Grammar();

    // set options
    Grammar ret = new Grammar();
    ret.options = opt;    

    try {
        // extract all rules
        RuleProfile all_rules = new RuleProfile();
        for( Glycan s : structures ) {
        // parse the tree
        GrammarTree gt = GrammarTree.fromGlycan(s, ret.options.ADD_LINKAGE_INFO);            
        if( !ret.seeds.containsKey(gt) ) {
            // tag the core
            if( opt.TAG_CORES )
            tagCore(gt);    
            
            // extract the rules
            Collection<Rule> extracted = extractRules(gt,ret.options);
            all_rules.addAll(extracted);
            
            // save the seed
            extracted = extractRules(gt,ret.options);
            ret.seeds.put(gt,new RuleProfile(extracted));
        }
        }

        // normalize
        HashMap<String,Double> normalizations = new HashMap<String,Double>();
        for( Map.Entry<Rule,Double> e : all_rules.getEntries() ) {
        String nkey = e.getKey().leftSide();

        Double old_val = normalizations.get(nkey);
        if( old_val==null )
            normalizations.put(nkey,e.getValue());
        else
            normalizations.put(nkey,old_val + e.getValue());
        }
        for( Map.Entry<Rule,Double> e : all_rules.getEntries() ) {
        String nkey = e.getKey().leftSide();
        ret.rules.put(e.getKey(),e.getValue()/normalizations.get(nkey));        
        }        
    }
    catch(Exception ex){
        LogUtils.report(ex);
    }
    
    return ret;
    }
    
    private static void tagCore(GrammarTree gt) {
    for( int i=0; i<cores.length; i++ ) {
        HashSet<GrammarTree> matchings = new HashSet<GrammarTree>();
        if( subtreeMatches(gt,cores[i],matchings) ) {
        GrammarTree.tag(gt,""+i,matchings);
        return;
        }
    }
    }

    public static int whichCore(GrammarTree gt) {
    for( int i=0; i<cores.length; i++ ) {
        HashSet<GrammarTree> matchings = new HashSet<GrammarTree>();
        if( subtreeMatches(gt,cores[i],matchings) ) 
        return i;
    }
    return -1;
    }
    
    private static boolean subtreeMatches(GrammarTree structure, GrammarTree core, Collection<GrammarTree> matchings) {
    
    if( !structure.getLabel().equals(core.getLabel()) )
        return false;
    if( core.getNoChildren()==0 ) {
        matchings.add(structure);
        return true;
    }

    // try all possible combinations of children, to prevent ordering
    for( Collection<GrammarTree> combination : getAllCombinations(structure.getChildren(),core.getNoChildren()) ) {

        // match all the subtrees
        Vector<GrammarTree> children_matchings = new Vector<GrammarTree>();        
        Iterator<GrammarTree> i=combination.iterator(), l=core.getChildren().iterator();        
        
        boolean matches = true;
        while(i.hasNext()) {
        if( !subtreeMatches(i.next(),l.next(),children_matchings) )
            matches = false;
        }
           

        // if all match
        if( matches ) {
        matchings.add(structure);
        matchings.addAll(children_matchings);
        return true;
        }
    }
    return false;        
    }

    private static Vector<Vector<GrammarTree>> getAllCombinations(Collection<GrammarTree> src, int size) {
    Vector<Vector<GrammarTree>> ret = new Vector<Vector<GrammarTree>>();
    getAllCombinations(ret,src,new Union<GrammarTree>(),size);
    return ret;
    }

    private static void getAllCombinations(Vector<Vector<GrammarTree>> buffer, Collection<GrammarTree> src, Union<GrammarTree> combination, int size) {
    if( (src.size()-combination.size())<size )
        return;
    if( size==0 && combination.size()>0 ) 
        buffer.add(combination);
    for( GrammarTree o : src ) {
        if( !combination.contains(o) )
        getAllCombinations(buffer,src,combination.and(o),size-1);
    }
    }

    // generation

    public boolean accept(Glycan structure) throws Exception {
    if( structure==null || structure.isEmpty() )
        return true;

    // parse the tree
    GrammarTree gt = GrammarTree.fromGlycan(structure, options.ADD_LINKAGE_INFO);            
    if( seeds.containsKey(gt) )
        return true;
    
    // tag the core
    if( options.TAG_CORES )
        tagCore(gt);    
    
    // extract the rules
    Collection<Rule> extracted = extractRules(gt,options);
    
    // check if the structure components are all recognized
    for( Rule r : extracted ) {
        if( !rules.containsKey(r) )
        return false;
    }
    return true;    
    }
       
    public void start(MassOptions _mass_opt) {
    mass_opt = _mass_opt;
    
    if( seeds.size()==0 || !options.USE_SEEDS ) {
        for( int i=0; i<cores_tagged.length; i++ )
        pool.add(cores_tagged[i]);
        //generateStructures(pool,null);
    }
    else
        pool.addAll(seeds.keySet());

    last_structure = null;
    visited.clear();
    }


    public FragmentEntry next(boolean backtrack) {
    if( last_structure!=null && !backtrack) 
        generateStructures(pool,last_structure);    

    while( pool.size()>0 ) {
        if( seeds.size()==0 || !options.USE_SEEDS ) {
        // remove the back of the stack (depth first)
        last_structure = pool.getLast(); 
        pool.removeLast();
        }
        else {
        // remove the top of the stack (breadth first)
        last_structure = pool.getFirst(); 
        pool.removeFirst();
        }

        // check for duplicates
        String str_structure = last_structure.toString(true);
        if( !visited.contains(str_structure) ) {
        visited.add(str_structure);

        try {
            Glycan s = last_structure.toGlycan(mass_opt);
            if( options.USE_SEEDS && seeds.containsKey(last_structure) )
            return new FragmentEntry(s,"seed");        
            return new FragmentEntry(s,"generated");
        }
        catch(Exception e) {
            LogUtils.report(e);
            return null;
        }
        }
    }
    return null;
    }


    public double computeScore(Glycan structure) {
    return computeScore(structure,SCORE_DIST);
    }
    
    public double computeMinDist(RuleProfile rp) {
    double min_dist = Double.MAX_VALUE;
    for( Map.Entry<GrammarTree,RuleProfile> e : seeds.entrySet() ) {
        double dist = rp.intersection(e.getValue()).absSum();
        min_dist = Math.min(min_dist,dist);
    }

    return min_dist;    
    }

    public double computeMinPDist(RuleProfile rp) {
    double min_pdist = Double.MAX_VALUE;
    for( Map.Entry<GrammarTree,RuleProfile> e : seeds.entrySet() ) {
        double dist = -computeLogProb(rp.intersection(e.getValue()));
        min_pdist = Math.min(min_pdist,dist);
    }

    return min_pdist;    
    }

    public double computeLogProb(RuleProfile rp) {
    double ret = 0.;
    for( Map.Entry<Rule,Double> e : rp.getEntries() ) 
        ret += Math.log(rules.get(e.getKey())) * Math.abs(e.getValue());
    return ret;
    }

    public double computeScore(Glycan structure, int type) {
    
    try {
        RuleProfile rp = new RuleProfile(extractRules(structure,options));

        if( type==SCORE_DIST )
        return -computeMinDist(rp);
        if( type==SCORE_PDIST ) 
        return -computeMinPDist(rp);
        if( type==SCORE_PROB )
        return computeLogProb(rp);
        if( type==SCORE_PROB_DIST ) 
        return computeMinDist(rp)*computeLogProb(rp);        
        throw new Exception("Invalid score type");
    }
    catch(Exception e) {
        LogUtils.report(e);
        return 0.;
    }
    }

    public StructureDictionary generateDictionary(String dict_name, String s_type, String source, double max_mass) {
    StructureDictionary dict = new StructureDictionary(dict_name);
    dict.setScorer(this);

    // generate all structures
    boolean backtrack = false;
    FragmentEntry fe = null;

    start(MassOptions.empty());    
    while( (fe = next(backtrack))!=null ) {
        dict.add(new StructureType(dict_name,s_type,source,fe.structure));
        backtrack = (fe.mass>=max_mass);    
    }

    return dict;    
    }

    public StructureDictionary generateDictionary(String dict_name, String s_type, String source, double max_mass, int max_res, int score_type) {
    StructureDictionary dict = new StructureDictionary(dict_name);
    dict.setScorer(this);

    // generate all structures, order by mass
    TreeMap<Double,ArrayList<FragmentEntry>> generated = new TreeMap<Double,ArrayList<FragmentEntry>>();

    System.out.println("generating structures");

    boolean backtrack = false;
    FragmentEntry gen = null;
    start(MassOptions.empty());    
    while( (gen = next(backtrack))!=null ) {
        if( gen.mass<=max_mass ) {
        // compute score
        gen.score = computeScore(gen.fragment,score_type); // decreasing order
        gen.fragment = null; // free memory

        // add to mass group
        ArrayList<FragmentEntry> mass_group = generated.get(gen.mass);
        if( mass_group==null ) {
            mass_group = new ArrayList<FragmentEntry>();
            generated.put(gen.mass,mass_group);        
        }
        mass_group.add(gen);
        }
        backtrack = (gen.mass>=max_mass);    
    }

    System.out.println("adding to dict");

    // add structures to dict order by rank
    for( ArrayList<FragmentEntry> mass_group : generated.values() ) {

        // order mass group by rank
        TreeMap<Double,ArrayList<FragmentEntry>> ranked = new TreeMap<Double,ArrayList<FragmentEntry>>();
        for( FragmentEntry fe : mass_group ) {                    
        
        ArrayList<FragmentEntry> rank_group = ranked.get(-fe.score);       
        if( rank_group == null ) {
            rank_group = new ArrayList<FragmentEntry>();
            ranked.put(-fe.score,rank_group);        
        }
        rank_group.add(fe);
        }
        
        // add to dictionary
        int rank = 0;
        for( ArrayList<FragmentEntry> rank_group: ranked.values() ) {
        if( rank>max_res )
            break;
        for( FragmentEntry fe : rank_group ) 
            dict.add(new StructureType(dict_name,s_type,source,fe.structure));                    
        rank += rank_group.size();
        }
    }


    return dict;    
    }

    public TreeMap<Glycan,Double> generateStructures(double mass, int score_type) {
    boolean backtrack = false;
    FragmentEntry fe = null;
    TreeMap<Glycan,Double> ret = new TreeMap<Glycan,Double>();

    start(MassOptions.empty());
    while( (fe = next(backtrack))!=null ) {
        if( Math.abs(fe.mass-mass)<0.1 ) {
        double score = computeScore(fe.fragment,score_type);
        ret.put(fe.fragment,score);
        }
        
        backtrack = (fe.mass>=mass);
    }
    
    return ret;
    }

    
    public Collection<GrammarTree> generateStructures(GrammarTree seed) {
    
    Vector<GrammarTree> ret = new Vector<GrammarTree>();
    generateStructures(ret,seed);
    return ret;
    }

    public void generateStructures(Collection<GrammarTree> buffer, GrammarTree seed) {
    if( seed==null )
        seed = new GrammarTree(GrammarTree.END);
    
    for( Rule r : rules.keySet() ) 
        generateStructures(buffer,seed,r,options);    
    }


    public static void generateStructures(Collection<GrammarTree> buffer, GrammarTree seed, Rule r, GrammarOptions opt) {
    if( r==null || buffer==null )
        return;
    if( seed==null )
        seed = new GrammarTree(GrammarTree.END);
    
    GrammarTree toadd = r.generateStructure(seed,opt);
    if( toadd!=null ) 
        buffer.add(toadd);
    
    for( GrammarTree c : seed.getChildren() ) 
        generateStructures(buffer,c,r,opt);    
    }
    

    // serialization

    public StructureScorer fromXML(Node g_node) throws Exception {
    Grammar ret = new Grammar();
    ret.fromXML(g_node,false);
    return ret;
    }

    public void fromXML(Node g_node, boolean merge) throws Exception {
    if( !merge ) 
        rules.clear();
    
    // read options
    Node c_node = XMLUtils.assertChild(g_node,"Configuration");
    Configuration conf = new Configuration();
    conf.fromXML(c_node);
    options.retrieve(conf);

    // read rules
    Vector<Node> r_nodes = XMLUtils.findAllChildren(g_node, "Rule");
    for( Node r_node : r_nodes) {
        Rule r = Rule.fromXML(r_node);
        String p = XMLUtils.getAttribute(r_node,"probability");
        if( p!=null )
        rules.put(r,Double.valueOf(p));
        else
        rules.put(r,1.);
    }

    // read seeds
    Vector<Node> s_nodes = XMLUtils.findAllChildren(g_node, "Seed");
    for( Node s_node : s_nodes) {
        GrammarTree gt = GrammarTree.fromString(XMLUtils.getAttribute(s_node,"structure"));
        RuleProfile rp = new RuleProfile(extractRules(gt,options));
        seeds.put(gt,rp);
    }
    }

    public Element toXML(Document document) {
    if( document==null )
        return null;
    
    // create root node
    Element g_node = document.createElement("Grammar");
    if( g_node==null )
        return null;

    // add options
    Configuration conf = new Configuration();
    options.store(conf);
    g_node.appendChild(conf.toXML(document));

    // add rules
    for( Map.Entry<Rule,Double> e : rules.entrySet() ) {
        Element r_node = e.getKey().toXML(document);
        if( r_node!=null ) {
        r_node.setAttribute("probability", e.getValue().toString());
        g_node.appendChild(r_node);
        }
    }

    // add seeds
    for( GrammarTree s : seeds.keySet() ) {
        Element s_node = document.createElement("Seed");
        s_node.setAttribute("structure", s.toString(false));
        if( s_node!=null )
        g_node.appendChild(s_node);
    }
    
    return g_node;
    }
    
}
