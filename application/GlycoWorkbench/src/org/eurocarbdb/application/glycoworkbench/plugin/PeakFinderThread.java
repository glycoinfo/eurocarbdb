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
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.util.*;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.*;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.*;

import java.util.*;
import java.util.regex.*;

public class PeakFinderThread extends Thread {

    static private Pattern crossring_pattern;
    static {
    crossring_pattern = Pattern.compile("^\\^\\{([0-9])\\,([0-9])\\}([AX])\\_\\{(\\S+)\\}$");
    }

    private AnnotatedPeakList annotated_peaks = null;
    private PeakList peaks = null;    
    private PeakFinderOptions pf_opt = null;
    private AnnotationOptions ann_opt = null;
    private FragmentOptions frag_opt = null;

    public PeakFinderThread(PeakList _peaks, PeakFinderOptions _pf_opt, AnnotationOptions _ann_opt, FragmentOptions _frag_opt) {    
    annotated_peaks = new AnnotatedPeakList();
    peaks = _peaks;
    pf_opt = _pf_opt;
    ann_opt = _ann_opt;
    frag_opt = _frag_opt;
    }
    
    public AnnotatedPeakList getAnnotatedPeaks() {
    return annotated_peaks;
    }

    public void setAnnotatedPeaks(AnnotatedPeakList apl) {
    annotated_peaks = apl;
    }

    public void run () {
    
    if( peaks==null || ann_opt==null ) {
        interrupt();
        return;
    }

    // annotate     
    try {
        // init parameters
        CalculationParameter pf_parameters = initParameters(peaks,pf_opt,ann_opt,frag_opt);
        
        // calculate
        GlycoPeakfinder calculator = new GlycoPeakfinder();
        CalculationParameter pf_results = calculator.calculate(pf_parameters);

        // retrieve results
        getResults(annotated_peaks,peaks,pf_results);
    }
    catch( Exception e) {
        LogUtils.report(e);
        interrupt();
    }
    }   
    
    private CalculationParameter initParameters(PeakList peaks, PeakFinderOptions pf_opt, AnnotationOptions ann_opt, FragmentOptions frag_opt) throws Exception {

    CalculationParameter ret = new CalculationParameter();    
    DefaultMasses defaultMasses = new DefaultMasses(this.getClass().getResource("/conf/peak_finder_masses.xml"),
                            this.getClass().getResource("/conf/peak_finder_ax.xml"));

     
    // Global settings.
        boolean monoisotopic = true;
        Persubstitution persubstitution = null;
    if( pf_opt.DERIVATIZATION.equals("Und") )
        persubstitution = Persubstitution.None;
     else if( pf_opt.DERIVATIZATION.equals("perMe") )
        persubstitution = Persubstitution.Me;
     else if( pf_opt.DERIVATIZATION.equals("perDMe") )
        persubstitution = Persubstitution.DMe;
     else if( pf_opt.DERIVATIZATION.equals("perAc") )
        persubstitution = Persubstitution.Ac;
     else if( pf_opt.DERIVATIZATION.equals("perDAc") )
        persubstitution = Persubstitution.DAc;
    
    // Set parameters for calculation.
    if( frag_opt==null )
        ret.setSpectraType(SpectraType.Profile);        
    else
        ret.setSpectraType(SpectraType.Fragmented);        
        ret.setAccuracy(ann_opt.MASS_ACCURACY);
        ret.setAccuracyPpm(ann_opt.MASS_ACCURACY_UNIT.equals(ann_opt.MASS_ACCURACY_PPM));
        ret.setMassShift(0);

        // spectra/scan
        org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Scan scan = new org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Scan();
        scan.setId(1);
        scan.setPrecursorMass(null);
    
        ArrayList<CalculationPeak> scan_peaks = new ArrayList<CalculationPeak>();
    for( Peak p : peaks.getPeaks() ) 
        scan_peaks.add(new CalculationPeak(p.getMZ(), p.getIntensity()));
        scan.setPeaks(scan_peaks);
        ret.setScan(scan);

        // residues
        ArrayList<CalculationMolecule> residues = new ArrayList<CalculationMolecule>();
        if( pf_opt.MAX_PEN>0 ) residues.add( new CalculationMolecule("Pen", defaultMasses.getResidueMass("pen",persubstitution,monoisotopic),pf_opt.MIN_PEN,pf_opt.MAX_PEN) );
        if( pf_opt.MAX_HEX>0 ) residues.add( new CalculationMolecule("Hex", defaultMasses.getResidueMass("hex",persubstitution,monoisotopic),pf_opt.MIN_HEX,pf_opt.MAX_HEX) );
        if( pf_opt.MAX_HEP>0 ) residues.add( new CalculationMolecule("Hept", defaultMasses.getResidueMass("hep",persubstitution,monoisotopic),pf_opt.MIN_HEP,pf_opt.MAX_HEP) );
        if( pf_opt.MAX_HEXN>0 ) residues.add( new CalculationMolecule("HexN", defaultMasses.getResidueMass("hexn",persubstitution,monoisotopic),pf_opt.MIN_HEXN,pf_opt.MAX_HEXN) );
        if( pf_opt.MAX_HEXNAC>0 ) residues.add( new CalculationMolecule("HexNAc", defaultMasses.getResidueMass("hexnac",persubstitution,monoisotopic),pf_opt.MIN_HEXNAC,pf_opt.MAX_HEXNAC) );
        if( pf_opt.MAX_DPEN>0 ) residues.add( new CalculationMolecule("dPen", defaultMasses.getResidueMass("dpen",persubstitution,monoisotopic),pf_opt.MIN_DPEN,pf_opt.MAX_DPEN) );
        if( pf_opt.MAX_DHEX>0 ) residues.add( new CalculationMolecule("dHex", defaultMasses.getResidueMass("dhex",persubstitution,monoisotopic),pf_opt.MIN_DHEX,pf_opt.MAX_DHEX) );
        if( pf_opt.MAX_DDHEX>0 ) residues.add( new CalculationMolecule("ddHex", defaultMasses.getResidueMass("ddhex",persubstitution,monoisotopic),pf_opt.MIN_DDHEX,pf_opt.MAX_DDHEX) );
        if( pf_opt.MAX_MEHEX>0 ) residues.add( new CalculationMolecule("MeH", defaultMasses.getResidueMass("(4mehex)",persubstitution,monoisotopic),pf_opt.MIN_MEHEX,pf_opt.MAX_MEHEX) );

        if( pf_opt.MAX_OR1>0 && pf_opt.OR1_MASS>0. ) residues.add( new CalculationMolecule(pf_opt.OR1_NAME, pf_opt.OR1_MASS, pf_opt.MIN_OR1,pf_opt.MAX_OR1) );
        if( pf_opt.MAX_OR2>0 && pf_opt.OR1_MASS>0. ) residues.add( new CalculationMolecule(pf_opt.OR2_NAME, pf_opt.OR2_MASS, pf_opt.MIN_OR2,pf_opt.MAX_OR2) );
        if( pf_opt.MAX_OR3>0 && pf_opt.OR1_MASS>0. ) residues.add( new CalculationMolecule(pf_opt.OR3_NAME, pf_opt.OR3_MASS, pf_opt.MIN_OR3,pf_opt.MAX_OR3) );

        if( pf_opt.MAX_HEXA>0 ) residues.add( new CalculationMolecule("HexA", defaultMasses.getResidueMass("hexa",persubstitution,monoisotopic),pf_opt.MIN_HEXA,pf_opt.MAX_HEXA) );
        if( pf_opt.MAX_DHEXA>0 ) residues.add( new CalculationMolecule("dHexA", defaultMasses.getResidueMass("dhexa",persubstitution,monoisotopic),pf_opt.MIN_DHEXA,pf_opt.MAX_DHEXA) );
        if( pf_opt.MAX_NEU5GC>0 ) residues.add( new CalculationMolecule("NeuGc", defaultMasses.getResidueMass("neu5gc",persubstitution,monoisotopic),pf_opt.MIN_NEU5GC,pf_opt.MAX_NEU5GC) );
    if( pf_opt.MAX_NEU5AC>0 ) residues.add( new CalculationMolecule("NeuAc", defaultMasses.getResidueMass("neu5ac",persubstitution,monoisotopic),pf_opt.MIN_NEU5AC,pf_opt.MAX_NEU5AC) );
        if( pf_opt.MAX_NEU5GCLAC>0 ) residues.add( new CalculationMolecule("NeuGcLac", defaultMasses.getResidueMass("neu5gc-lac",persubstitution,monoisotopic),pf_opt.MIN_NEU5GCLAC,pf_opt.MAX_NEU5GCLAC) );
        if( pf_opt.MAX_NEU5ACLAC>0 ) residues.add( new CalculationMolecule("NeuAcLac", defaultMasses.getResidueMass("neu5ac-lac",persubstitution,monoisotopic),pf_opt.MIN_NEU5ACLAC,pf_opt.MAX_NEU5ACLAC) );
        if( pf_opt.MAX_KDO>0 ) residues.add( new CalculationMolecule("KDO", defaultMasses.getResidueMass("kdo",persubstitution,monoisotopic),pf_opt.MIN_KDO,pf_opt.MAX_KDO) );
        if( pf_opt.MAX_KDN>0 ) residues.add( new CalculationMolecule("KDN", defaultMasses.getResidueMass("kdn",persubstitution,monoisotopic),pf_opt.MIN_KDN,pf_opt.MAX_KDN) );
        if( pf_opt.MAX_MUR>0 ) residues.add( new CalculationMolecule("MurNAc", defaultMasses.getResidueMass("mur",persubstitution,monoisotopic),pf_opt.MIN_MUR,pf_opt.MAX_MUR) );

        if( pf_opt.MAX_S>0 && defaultMasses.getResidueMass("s",persubstitution,monoisotopic)>0. ) 
        residues.add( new CalculationMolecule("S", defaultMasses.getResidueMass("s",persubstitution,monoisotopic),pf_opt.MIN_S,pf_opt.MAX_S) );
        if( pf_opt.MAX_P>0 && defaultMasses.getResidueMass("p",persubstitution,monoisotopic)>0. ) 
        residues.add( new CalculationMolecule("P", defaultMasses.getResidueMass("p",persubstitution,monoisotopic),pf_opt.MIN_P,pf_opt.MAX_P) );
        if( pf_opt.MAX_AC>0 &&  defaultMasses.getResidueMass("ac",persubstitution,monoisotopic)>0. ) 
        residues.add( new CalculationMolecule("Ac", defaultMasses.getResidueMass("ac",persubstitution,monoisotopic),pf_opt.MIN_AC,pf_opt.MAX_AC) );
        if( pf_opt.MAX_PYR>0 && defaultMasses.getResidueMass("pyr",persubstitution,monoisotopic)>0. ) 
        residues.add( new CalculationMolecule("Pyr", defaultMasses.getResidueMass("pyr",persubstitution,monoisotopic),pf_opt.MIN_PYR,pf_opt.MAX_PYR) );
        if( pf_opt.MAX_PC>0 && defaultMasses.getResidueMass("pc",persubstitution,monoisotopic)>0. ) 
        residues.add( new CalculationMolecule("PC", defaultMasses.getResidueMass("pc",persubstitution,monoisotopic),pf_opt.MIN_PC,pf_opt.MAX_PC) );

        ret.setResidues(residues);

    //System.out.println("Residues");
    //for( CalculationMolecule cm : ret.getResidues() )
    //System.out.println("\t" + cm.getId() + " " + cm.getMass() + " " + cm.getMin() + " " + cm.getMax());


    // fragments
    if( frag_opt!=null ) {
        // reducing end
        ArrayList<CalculationFragment> fragmentsRed = new ArrayList<CalculationFragment>();
        if( frag_opt.ADD_BFRAGMENTS ) fragmentsRed.add( new CalculationFragment("B",null, defaultMasses.getGlycosidicFragmentMass("b",monoisotopic)) );
        if( frag_opt.ADD_CFRAGMENTS ) fragmentsRed.add( new CalculationFragment("C",null, defaultMasses.getGlycosidicFragmentMass("c",monoisotopic)) );        
        if( frag_opt.ADD_AFRAGMENTS ) {
        for( CalculationMolecule cm : ret.getResidues() ) {
            String res = cm.getId();
            String lc_res = cm.getId().toLowerCase();
            for( int i=0; i<=3; i++ ) {
            for( int l=i+2; l<=5; l++ ) {
                try {
                String name = "^{" + i + "," + l + "}A_{" + res + "}";
                fragmentsRed.add( new CalculationFragment(name,res,defaultMasses.getCrossringFragmentMass("A",persubstitution,monoisotopic,lc_res,i,l) ) );
                }
                catch(Exception e){
                }
            }
            }
        }
        }
        ret.setFragmentsRed(fragmentsRed);
      
        //System.out.println("Reducing end fragments");
        //for( CalculationFragment cf : ret.getFragmentsRed() )
        //System.out.println("\t" + cf.getId() + " " + cf.getMass());

        // non reducing end
        ArrayList<CalculationFragment> fragmentsNonRed = new ArrayList<CalculationFragment>();
        if( frag_opt.ADD_YFRAGMENTS ) fragmentsNonRed.add( new CalculationFragment("Y",null, defaultMasses.getGlycosidicFragmentMass("y",monoisotopic)) );
        if( frag_opt.ADD_ZFRAGMENTS ) fragmentsNonRed.add( new CalculationFragment("Z",null, defaultMasses.getGlycosidicFragmentMass("z",monoisotopic)) );
        if( frag_opt.ADD_XFRAGMENTS ) {
        for( CalculationMolecule cm : ret.getResidues() ) {
            String res = cm.getId();
            String lc_res = cm.getId().toLowerCase();
            for( int i=0; i<=3; i++ ) {
            for( int l=i+2; l<=5; l++ ) {
                try {
                String name = "^{" + i + "," + l + "}X_{" + res + "}";
                fragmentsNonRed.add( new CalculationFragment(name,res,defaultMasses.getCrossringFragmentMass("X",persubstitution,monoisotopic,lc_res,i,l) ) );
                }
                catch(Exception e){
                }
            }
            }
        }
        }
        ret.setFragmentsNonRed(fragmentsNonRed);
      
        //System.out.println("Non-Reducing end fragments");
        //for( CalculationFragment cf : ret.getFragmentsNonRed() )
        //System.out.println("\t" + cf.getId() + " " + cf.getMass());
        
        // number of fragmentations
        ArrayList<Integer> nofragments = new ArrayList<Integer>();
        for( int i=1; i<=frag_opt.MAX_NO_CLEAVAGES; i++ )
        nofragments.add(i);
        ret.setMultiFragments(nofragments);

        //System.out.println("No fragments");
        //for( Integer i : ret.getMultiFragments() )
        //System.out.println("\t" + i);
    }

        // ions
        ArrayList<CalculationIon> ions = new ArrayList<CalculationIon>();
    if( ann_opt.NEGATIVE_MODE ) {
        ions.add( new CalculationIon("-H", -defaultMasses.getIonMass("h",monoisotopic), 1) );
    }
    else {
        if( ann_opt.MAX_NO_H_IONS>0 )  ions.add( new CalculationIon("H", defaultMasses.getIonMass("h",monoisotopic), 1) );
        if( ann_opt.MAX_NO_NA_IONS>0 ) ions.add( new CalculationIon("Na", defaultMasses.getIonMass("na",monoisotopic), 1) );
        if( ann_opt.MAX_NO_LI_IONS>0 ) ions.add( new CalculationIon("Li", defaultMasses.getIonMass("li",monoisotopic), 1) );
        if( ann_opt.MAX_NO_K_IONS>0 )  ions.add( new CalculationIon("K", defaultMasses.getIonMass("k",monoisotopic), 1) );
    }
        ret.setIons(ions);

    //System.out.println("Ions");
    //for( CalculationIon ci : ret.getIons() )
    //System.out.println("\t" + ci.getId() + " " + ci.getMass() + " " + ci.getCharge());

    // charges
        ArrayList<Integer> nocharges = new ArrayList<Integer>();
    for( int i=1; i<=ann_opt.MAX_NO_CHARGES; i++ )
        nocharges.add(i);
        ret.setCharges(nocharges);

    //System.out.println("Charges");
    //for( Integer i : ret.getCharges() )
    //System.out.println("\t" + i);

        // ion exchange
    int max_no_exchanges = 0;
        ArrayList<CalculationIon> exchanges = new ArrayList<CalculationIon>();
    if( ann_opt.COMPUTE_EXCHANGES ) {
        if( ann_opt.MAX_EX_NA_IONS>0 ) exchanges.add( new CalculationIon("Na", defaultMasses.getIonMass("na",monoisotopic), 1) );
        if( ann_opt.MAX_EX_LI_IONS>0 ) exchanges.add( new CalculationIon("Li", defaultMasses.getIonMass("li",monoisotopic), 1) );
        if( ann_opt.MAX_EX_K_IONS>0 )  exchanges.add( new CalculationIon("K", defaultMasses.getIonMass("k",monoisotopic), 1) );
        max_no_exchanges = Math.max(Math.max(ann_opt.MAX_EX_NA_IONS,ann_opt.MAX_EX_LI_IONS),ann_opt.MAX_EX_K_IONS);
    }
        ret.setIonExchangeIon(exchanges);

    //System.out.println("Exchanges");
    //for( CalculationIon ci : ret.getIonExchangeIon() )
    //System.out.println("\t" + ci.getId() + " " + ci.getMass() + " " + ci.getCharge());

        // number of exchanges
    max_no_exchanges = Math.min(max_no_exchanges,20);
        ArrayList<Integer> exchangeNumbers = new ArrayList<Integer>();
    for( int i=1; i<=max_no_exchanges; i++ )
        exchangeNumbers.add(i);
        ret.setIonExchangeCount(exchangeNumbers);

    //System.out.println("ExchangeCount");
    //for( Integer i : ret.getIonExchangeCount() )
    //System.out.println("\t" + i);

    // derivatisation = Modification at the reducing end ... also none must be set
    //System.out.println(pf_opt.REDUCING_END);
        ArrayList<CalculationDerivatisation> derivatization = new ArrayList<CalculationDerivatisation>();
    if( !pf_opt.REDUCING_END.equals("XXX") )
        derivatization.add( new CalculationDerivatisation(pf_opt.REDUCING_END, defaultMasses.getDerivatisationMass(pf_opt.REDUCING_END,persubstitution,monoisotopic)) );
    else 
        derivatization.add( new CalculationDerivatisation(pf_opt.OTHER_REDEND_NAME, pf_opt.OTHER_REDEND_MASS) );    
        ret.setDerivatisation(derivatization);
    
    //System.out.println("Derivatisation");
    //for( CalculationDerivatisation cd : ret.getDerivatisation() )
    //System.out.println("\t" + cd.getId() + " " + cd.getMass() );

        // completion
        ret.setCompletionNonRed(defaultMasses.getCompletionMass("nonred",persubstitution,monoisotopic));
    if( !pf_opt.REDUCING_END.equals("XXX") )
        ret.setCompletionRed(defaultMasses.getCompletionMass("red",persubstitution,monoisotopic));
    else
        ret.setCompletionRed(defaultMasses.getCompletionMass("red",Persubstitution.None,monoisotopic));

        ret.setNonReducingDifference(defaultMasses.getNonReducingDifference(persubstitution,monoisotopic));
        ret.setExchangeIonMass(defaultMasses.getExchangeIonMass(monoisotopic));

    return ret;    
    }


    private void getResults(AnnotatedPeakList dest, PeakList peaks, CalculationParameter pf_results) {
    try { 
        // retrieve results
        dest.clear();
        Glycan motif = new Glycan();
        for( org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationPeak cp : pf_results.getScan().getPeaks() ) {
        Peak p = new Peak(cp.getMz(), cp.getIntensity());
        for( org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.PeakAnnotation pa : cp.getAnnotation() ) {
            org.eurocarbdb.application.glycanbuilder.FragmentEntry fe = createFragmentEntry(pa);
            if( IonCloudUtils.isRealistic(fe) ) // check exchanges
            annotated_peaks.addPeakAnnotation(motif,new org.eurocarbdb.application.glycoworkbench.PeakAnnotation(p,fe),true);
        }
        }
        
        // check if all peaks have matched
        PeakAnnotationCollection pac = annotated_peaks.getPeakAnnotationCollection(motif);
        for( Peak p : peaks.getPeaks() ) {
        if( pac==null || !pac.isAnnotated(p) ) 
            annotated_peaks.addPeakAnnotation(motif,new org.eurocarbdb.application.glycoworkbench.PeakAnnotation(p),true);        
        }
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }

    private org.eurocarbdb.application.glycanbuilder.FragmentEntry createFragmentEntry(org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.PeakAnnotation pa) throws Exception {

    // create composition object
    Glycan fragment = getCompositionAsGlycan(pa);
    String name = getFragmentName(pa);    

    // get ions
    IonCloud charges = new IonCloud();
    for( AnnotationEntity ae : pa.getIons()) {
        if( ae.getId().startsWith("-") )
        charges.add(ae.getId().substring(1),-ae.getNumber());    
        else
        charges.add(ae.getId(),ae.getNumber());    
    }
    fragment.setCharges(charges);

    // get exchanges
    int no_exchanges = 0;
    IonCloud exchanges = new IonCloud();
    for( AnnotationEntity ae : pa.getIonExchange()) {
        exchanges.add(ae.getId(),ae.getNumber());
        no_exchanges += ae.getNumber();
    }
    exchanges.add(MassOptions.ION_H,-no_exchanges);
    fragment.setNeutralExchanges(exchanges);
        
    return new FragmentEntry(fragment,name);    
    }


    private Glycan getCompositionAsGlycan(org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.PeakAnnotation pa) throws Exception {
    Glycan ret;
    if( pf_opt.REDUCING_END.equals("XXX") ) {
        ResidueType re_type = ResidueType.createOtherReducingEnd(pf_opt.OTHER_REDEND_NAME,pf_opt.OTHER_REDEND_MASS);
        ret = Glycan.createComposition(new MassOptions(pf_opt.DERIVATIZATION,re_type.getName()));
    }
    else
        ret = Glycan.createComposition(new MassOptions(pf_opt.DERIVATIZATION,pf_opt.REDUCING_END));

    // add residues
    for( AnnotationEntity ae : pa.getResidues()) {
        for( int i=0; i<ae.getNumber(); i++ ) {
        if( ae.getId().equals(pf_opt.OR1_NAME) )
            ret.addAntenna(new Residue(ResidueType.createOtherResidue(pf_opt.OR1_NAME,pf_opt.OR1_MASS)));
        else if( ae.getId().equals(pf_opt.OR2_NAME) )
            ret.addAntenna(new Residue(ResidueType.createOtherResidue(pf_opt.OR2_NAME,pf_opt.OR2_MASS)));
        else if( ae.getId().equals(pf_opt.OR3_NAME) )            
            ret.addAntenna(new Residue(ResidueType.createOtherResidue(pf_opt.OR3_NAME,pf_opt.OR3_MASS)));
        else
            ret.addAntenna(ResidueDictionary.newResidue(ae.getId()));
        }
    }

    // add cleavages
    boolean first = true;
    for( AnnotationEntity ae : pa.getFragments()) {
        for( int i=0; i<ae.getNumber(); i++ ) {
        Residue cleavage = getCleavage(ae);
        if( cleavage.getCleavedResidue()==null )
            cleavage.setCleavedResidue(ResidueDictionary.newResidue("Hex"));

        if( cleavage.canBeReducingEnd() )
            ret.setRoot(cleavage);
        else
            ret.addAntenna(cleavage);
        }
    }

    return ret;
    }

    private Residue getCleavage(AnnotationEntity ae) throws Exception {
    if( ae.getId().equals("B") ) return ResidueDictionary.createBCleavage();
    else if( ae.getId().equals("C") ) return ResidueDictionary.createCCleavage();
    else if( ae.getId().equals("Y") ) return ResidueDictionary.createYCleavage();    
    else if( ae.getId().equals("Z") ) return ResidueDictionary.createZCleavage();    

    Matcher m = crossring_pattern.matcher(ae.getId());
    if( m.matches() ) {
        int first_pos = Integer.valueOf(m.group(1));
        int last_pos = Integer.valueOf(m.group(2));
        char type = m.group(3).charAt(0);
        
        return CrossRingFragmentDictionary.newCrossRingFragment(type,first_pos,last_pos,ResidueDictionary.newResidue(m.group(4)));
    }

    throw new Exception("Invalid cleavage type: " + ae.getId());
    }


    private String getCompositionAsString(org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.PeakAnnotation pa) {
    // create name from composition
    StringBuilder sb_name = new StringBuilder();
    
    if( pa.getDerivatisation()!=null && !pa.getDerivatisation().equals("freeEnd") ) {
        sb_name.append(pa.getDerivatisation());
        sb_name.append('-');
    }
    
    for( AnnotationEntity ae : pa.getResidues()) {
        if( ae.getNumber()>0 ) {
        sb_name.append(ae.getId());
        sb_name.append("" + ae.getNumber());
        }
    }
    
    boolean first = true;
    for( AnnotationEntity ae : pa.getFragments()) {
        if( ae.getNumber()>0 ) {
        if( first ) 
            sb_name.append('/');
        else
            sb_name.append(',');
        
        sb_name.append(ae.getId());
        if( ae.getNumber()>1 )
            sb_name.append("" + ae.getNumber());

        first = false;
        }
    }

    return sb_name.toString();
    }


    private String getFragmentName(org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.PeakAnnotation pa) {
    StringBuilder sb_name = new StringBuilder();
    

    for( AnnotationEntity ae : pa.getFragments()) {
        if( ae.getNumber()>0 ) {
        sb_name.append(ae.getId());
        if( ae.getNumber()>1 )
            sb_name.append("" + ae.getNumber());
        }
    }

    return sb_name.toString();
    }
}
