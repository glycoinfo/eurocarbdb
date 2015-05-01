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
import org.eurocarbdb.application.glycanbuilder.*;
import org.eurocarbdb.application.glycoworkbench.*;


import java.util.*;

public class MSUtils {

    public static final class IsotopeList {

    private double mass_tol;
    private TreeMap<Double,Double> mz_int_map;

    public IsotopeList(boolean show_all) {
        mass_tol = (show_all) ?0.0001 :0.2;    
        mz_int_map = new TreeMap<Double,Double>();
    }

    private Map.Entry<Double,Double> getEntry(double mz) {
        for( Map.Entry<Double,Double> e : mz_int_map.entrySet() ) {
        double emz = e.getKey();
        if( emz>(mz+mass_tol) )
            return null;
        if( emz>(mz-mass_tol) )
            return e;        
        }
        return null;
    }

    public double get(double mz) {
        Map.Entry<Double,Double> e = getEntry(mz);
        return (e==null) ?0. :e.getValue();
    }

    public void add(double mz, double intensity, boolean sum) {
        Map.Entry<Double,Double> e = getEntry(mz);
        if( e==null )
        mz_int_map.put(mz,intensity);
        else if( sum ) 
        mz_int_map.put(e.getKey(),e.getValue()+intensity);
        else
        mz_int_map.put(e.getKey(),intensity);
    }

    public void add(double[][] data, boolean sum) {
        for( int i=0; i<data[0].length; i++ )
        add(data[0][i],data[1][i],sum);
    }


    public void adjust(double[][] data, double mz_ratio, double intensity) {
        double mass_shift = 0.;
        for( int i=0; i<data[0].length; i++ ) {
        if( data[1][i]==1. ) {
            mass_shift = mz_ratio - data[0][i];
            break;
        }
        }
        
        double int_shift = get(mz_ratio); 
        for( int i=0; i<data[0].length; i++ ) {
        double new_mz = data[0][i] + mass_shift;
        double new_int = data[1][i]*(intensity-int_shift)+get(new_mz);
        data[0][i] = new_mz;
        data[1][i] = new_int;
        }
    }    
    }

    private static final double OUTPUT_TOLERANCE = 0.01;
    private static final double COMPUTE_TOLERANCE = 0.000001; 

    public static double[][] average(Collection<double[][]> curves, boolean show_all) {
    
    if( curves.size()==0 )
        return null;

    // average multiple curves    
    double mass_tol = (show_all) ?0.0001 :0.5;    
    Vector<Pair<Double,Double>> accumulated = new Vector<Pair<Double,Double>>();    
    for( double[][] curve : curves ) {
        int i=0;
        for( int l=0; l<curve[0].length; l++ ) {
        for(;;i++) {
            if( i>=accumulated.size() || curve[0][l]<(accumulated.get(i).getFirst()-mass_tol) ) {
            accumulated.insertElementAt(new Pair<Double,Double>(curve[0][l],curve[1][l]),i);
            break;
            }
            if( curve[0][l]<=(accumulated.get(i).getFirst()+mass_tol) ) {
            Pair<Double,Double> dest = accumulated.get(i);
            dest.setSecond(dest.getSecond()+curve[1][l]);
            break;
            }
        }
        }    
    }

    // make array
    int count = accumulated.size();
    int no_curves = curves.size();

    int i=0;
    double[][] ret = new double[2][];
    ret[0] = new double[count];
    ret[1] = new double[count];
    for( Pair<Double,Double> min : accumulated ) {
        ret[0][i] = min.getFirst();
        ret[1][i] = min.getSecond()/(double)no_curves;            
        i++;        
    }

    return ret;
    }
    

    // +0 has intensity = 1

    static public void adjust(double[][] data, double mz_ratio, double intensity) {
    double mass_shift = 0.;
    for( int i=0; i<data[0].length; i++ ) {
        if( data[1][i]==1. ) {
        mass_shift = mz_ratio - data[0][i];
        break;
        }
    }

    for( int i=0; i<data[0].length; i++ ) {
        data[0][i] += mass_shift;
        data[1][i] *= intensity;
    }
    }

    public static double[][] getIsotopesCurve(int no_molecules, String molecule, boolean show_all) throws Exception {    
    return getIsotopesCurve(no_molecules,new Molecule(molecule),show_all);
    }

    public static double[][] getIsotopesCurve(int no_molecules, Molecule m, boolean show_all) throws Exception {    
        
    // init step
    TreeMap<Double,Double> mol_massint = new TreeMap<Double,Double>();
    mol_massint.put(0.,1.);
    
    for( Map.Entry<Atom,Integer> a : m.getAtoms() ) {
        // compute mass/int pairs for each atom
        double[][] atom_massint = getAtomIsotopesCurve(no_molecules*a.getValue(),a.getKey().getSymbol());
        
        // combine with the previous atoms
        TreeMap<Double,Double> new_mol_massint = new TreeMap<Double,Double>();
        for( Map.Entry<Double,Double> mi : mol_massint.entrySet() ) {
        double old_mass = mi.getKey();
        double old_int = mi.getValue();
        for( int i=0; i<atom_massint[0].length; i++ ) {
            double new_mass = old_mass + atom_massint[0][i];
            double new_int = old_int*atom_massint[1][i];
            
            Double cur_int = new_mol_massint.get(new_mass);
            if( cur_int==null )
            new_mol_massint.put(new_mass,new_int);
            else
            new_mol_massint.put(new_mass,cur_int + new_int);
        }           
        }
        mol_massint = new_mol_massint;
    }
    
    
    // accumulate masses
    double last_mass = 0.;
    double mass_tol = (show_all) ?0.0001 :0.5;
    int no_charges = no_molecules*m.getNoCharges();
    Vector<Pair<Double,Double>> mol_accumulated = new Vector<Pair<Double,Double>>();
    for( Map.Entry<Double,Double> mi : mol_massint.entrySet() ) {
        double mass = mi.getKey();    
        if( no_charges>0 ) {
        mass -= no_charges * MassUtils.electron.getMainMass();
        mass /= Math.abs(no_charges);
        }
        
        double rel_int = mi.getValue();
        if( mol_accumulated.size()==0 || (mass-last_mass)>mass_tol ) {
        mol_accumulated.add(new Pair<Double,Double>(mass,rel_int));
        last_mass = mass;
        }
        else {
        double old_rel_int = mol_accumulated.get(mol_accumulated.size()-1).getSecond();
        mol_accumulated.get(mol_accumulated.size()-1).setSecond(old_rel_int+rel_int);
        }
    }
    
    // make array
    int count = 0;
    for( Pair<Double,Double> mi : mol_accumulated ) {
        if( mi.getSecond()>OUTPUT_TOLERANCE ) 
        count++;
    }

    int i=0;
    double[][] ret = new double[2][];
    ret[0] = new double[count];
    ret[1] = new double[count];
    for( Pair<Double,Double> mi : mol_accumulated ) {
        if( mi.getSecond()>OUTPUT_TOLERANCE ) {
        ret[0][i] = mi.getFirst();
        ret[1][i] = mi.getSecond();            
        i++;
        }
    }
    
    return ret;
    }

    // +0 has intensity = 1
    private static double[][] getAtomIsotopesCurve(int no_atoms, String atom_name) throws Exception {
    if( no_atoms==0 )
        return new double[0][];

    // init vars
       Atom atom = MassUtils.getAtom(atom_name);
       if( atom==null )
        throw new Exception("Invalid atom name: " + atom_name);

    Isotope main_isotope = MassUtils.getMainIsotope(atom);
    if( main_isotope==null )
        throw new Exception("No isotopic information for atom: " + atom_name);

    Vector<Isotope> all_isotopes = MassUtils.getAllIsotopes(atom);
    int no_isotopes = all_isotopes.size();

    // compute base log-probability
    double lpn = no_atoms*Math.log(main_isotope.getAbundance());
    
    // compute probabilities    
    Vector<Pair<Double,Double>> massint_pairs = new Vector<Pair<Double,Double>>();
    for( int r=0;;r++ ) {
        // get all combinations of isotope with n-i times the main isotope
        Vector<int[]> combinations = getAllCombinations(no_atoms,no_isotopes,r);

        // compute masses and intensities
        boolean added = false;
        for( int[] x : combinations ) {
        // compute combination log-probability
        double lp = logMultinomialCoefficient(no_atoms,x);
        for( int i=0; i<no_isotopes; i++ ) 
            lp += x[i] * Math.log(all_isotopes.get(i).getAbundance());        
        
        // compute combination intensity relative to the main isotope
        double rel_int = Math.exp(lp-lpn);
        if( rel_int>COMPUTE_TOLERANCE ) {
            // compute combination mass
            double mass = 0.;
            for( int i=0; i<no_isotopes; i++ ) 
            mass += x[i]*all_isotopes.get(i).getMass();
            
            massint_pairs.add(new Pair<Double,Double>(mass,rel_int));
            added = true;
        }
        }

        if( no_isotopes==1 || !added )
        break;
    }

    // make array
    double[][] ret = new double[2][];
    ret[0] = new double[massint_pairs.size()];
    ret[1] = new double[massint_pairs.size()];

    for( int i=0; i<massint_pairs.size(); i++ ) {
        ret[0][i] = massint_pairs.get(i).getFirst();
        ret[1][i] = massint_pairs.get(i).getSecond();
    }
     
    return ret;    
    }

    private static double logMultinomialCoefficient(int n, int[] x) {
    double ret = Math.log(binomialCoefficient(n,x[0]));
    for( int i=1; i<x.length; i++ )
        ret -= Math.log(factorial(x[i]));
    return ret;
    }
    
    private static double factorial(int n) {
    return binomialCoefficient(n,1);
    }

    private static double binomialCoefficient(int n, int k) {
    double ret = 1;
    for( int i=n; i>k; i-- )
        ret *= i;
    return ret;
    }

    private static Vector<int[]> getAllCombinations(int n, int k, int r) {
    Vector<int[]> ret = new Vector<int[]>();

    int[] root = new int[k];
    root[0] = n; Arrays.fill(root,1,k,0);
    getAllCombinationsRecursive(ret,root,r); 

    return ret;
    }

    private static void getAllCombinationsRecursive(Vector<int[]> buffer, int[] current, int r) {
    if( r==0 ) {
        buffer.add(current);
        return;
    }
    
    for( int i=1; i<current.length; i++ ) {
        int[] new_current = clone(current);
        new_current[0]--; new_current[i]++;
        getAllCombinationsRecursive(buffer,new_current,r-1);
    }    
    }

    private static int[] clone(int[] array) {
    int[] ret = new int[array.length];
    for( int c=0; c<array.length; c++ )
        ret[c] = array[c];
    return ret;
    }               


}