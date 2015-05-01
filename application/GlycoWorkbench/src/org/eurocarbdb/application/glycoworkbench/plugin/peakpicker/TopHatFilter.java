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

package org.eurocarbdb.application.glycoworkbench.plugin.peakpicker;

public class TopHatFilter {

    static public void filter(double[][] data, double struc_elem_size) {
    if( data==null || data[0].length<2 )
        return;

    int no_points = data[0].length;
    double first_mz = data[0][0];
    double last_mz = data[0][no_points-1];
    double spacing = (last_mz-first_mz) / (double)no_points;
        int struc_elem_no_points = (int)Math.ceil(struc_elem_size/spacing);

        // the number has to be odd
    struc_elem_no_points += ((struc_elem_no_points+1)%2);        

        // compute the erosion of raw data
    double[][] erosion_results = erosion(data,struc_elem_no_points);

        // compute the dilation of erosion_result
    double[][] dilation_results = dilatation(erosion_results,struc_elem_no_points);
       
        // subtract the result from the original data
    for( int i=0; i<no_points; i++ )
        data[1][i] -= dilation_results[1][i];    
    }

    static private double[][] dilatation(double[][] data, int l)
    {
    int first = 0;
    int last  = data[0].length;
        
    int length = data[0].length;
    int middle = l/2;
    
    double[][] results = new double[2][];
    results[0] = new double[length];
    results[1] = new double[length];

    double[] g = new double[l];
    double[] h = new double[l];    
    int k=length-(length%l)-1;

    //--------------van Herk's method of the dilatation --------------------

    calcGDilatation(data,first,last,l,g,true);
    calcHDilatation(data,first,l-1,l,h,true);
    
    int i;
    int it = 0;
    for( i=0; i<middle; ++i, ++it, ++first ) {
        results[0][it] = data[0][first]; 
        results[1][it] = g[i+middle];
    }

    int m = l-1;
    int n = 0;
    for( i=middle; i<(length-middle); ++i, ++it, ++first, ++m, ++n ) {
        if ((i%l)==(middle+1)) {
        if (i==k)
            calcGDilatation(data,first+middle,last,l,g,false);        
        else            
            calcGDilatation(data,(first+middle),last,l,g,true);        
        m=0;
        }
        if ((i%l)==middle && (i>middle)) {
        if (i>k)          
            calcHDilatation(data,first,last,l,h,false);           
        else          
            calcHDilatation(data,(first-middle),(first+middle),l,h,true);        
        n=0;
        }
        
        results[0][it] = data[0][first];
        results[1][it] = Math.max(g[m],h[n]);
    }
      
    double last_int = data[1][(first-1)];
    for (i=0; i<middle; ++i, ++it, ++first) {
        results[0][it] = data[0][first];
        results[1][it] = last_int;
    }

    return results;
    }



    /**Van Herk's method of the Erosion. The algorithm requires 3 min/max comparisons for every data point
       independent from the length of the structuring element.
       Basic idea of the erosion is "Does the structuring element fit completely in a given set?". The value of a data point
       \f$ x \f$ in the signal \f$s \f$ after an erosion is the minimal data point in a window which is represented by the
       structuring element \f$ B\f$, when the \f$ B\f$'s point of reference is at \f$ x \f$:
       \f[ [\epsilon_B(s)](x)=min_{b \in B} s(x+b). \f]
       \image html Erosion.png "Erosion with a structuring element of length 3"
    */
    
    static private double[][] erosion(double[][] data, int l)
    {

    int first = 0;
    int last  = data[0].length;
        
    int length = data[0].length;
    int middle = l/2;
    
    double[][] results = new double[2][];
    results[0] = new double[length];
    results[1] = new double[length];
    
    double[] g = new double[l];
    double[] h = new double[l];    
    int k=length-(length%l)-1;

    //-------------- van Herk's method of the erosion --------------------

    
    calcGErosion(data,first,last,l,g,true);
    calcHErosion(data,first+l-1,l,h,true);

    int i;
    int it = 0;
    for (i=0; i<middle; ++i, ++it, ++first) {
        results[0][it] = data[0][first];
        results[1][it] = 0.;
    }

    int m = l-1;
    int n = 0;
    for (i=middle; i<(length-middle); ++i, ++it, ++first, ++m, ++n) {
        if ((i%l)==(middle+1)) {
        if (i==k)
            calcGErosion(data,(first+middle),last,l,g,false);        
        else          
            calcGErosion(data,(first+middle),last,l,g,true);        
        m=0;
        }
        if ((i%l)==middle && (i>middle) ) {
        if (i>k)          
            calcHErosion(data,(first+middle),l,h,false);          
        else          
            calcHErosion(data,(first+middle),l,h,true);          
        n=0;
        }

        results[0][it] = data[0][first];
        results[1][it] = Math.min(g[m],h[n]);
    }  

    for (i=0; i<middle; ++i, ++it, ++first) {
        results[0][it] = data[0][first];
        results[1][it] = 0.;
    }

    return results;
    }

    /// Compute the auxiliary fields g and h for the erosion
    static private void calcGErosion(double[][] data, int first, int last, int l, double[] g, boolean b)
    {
    int i,j;
    
    if (b) {
        for (j=0; j<l; ++j) {
        if (first < last){
            if (j==0)            
            g[j]=data[1][first];            
            else            
            g[j]=Math.min(data[1][first],g[j-1]);        
            ++first;
        }
        else            
            break;        
        }
    }
    else {
        j=0;
        while (first!=last) {
        if (j==0)            
            g[j]=data[1][first];
        else          
            g[j]=Math.min(data[1][first],g[j-1]);        
        ++first;
        ++j;
        }

        for (i=j; i<l; ++i) {
        g[i]=0;
        }
    }
    }


    static private void calcHErosion(double[][] data, int first, int l, double[] h, boolean b) 
    {
 
    int j;
    if (b) {
        for (j=l-1; j>=0; --j) {
        if (j==(l-1))          
            h[j]=data[1][first];        
        else          
            h[j]=Math.min(data[1][first],h[j+1]);        
        --first;
        }
    }
    else {
        for (j=0;j<l;++j)        
        h[j]=0;        
    }
    }


    /// Compute the auxiliary fields g and h for the dilatation
    static private void calcGDilatation(double[][] data, int first, int last, int l, double[] g, boolean b)
    {
    int i,j;

    if (b) {
        for (j=0; j<l; ++j) {
        if (first < last) {
            if (j==0)            
            g[j]=data[1][first];        
            else            
            g[j]=Math.max(data[1][first],g[j-1]);        
            ++first;
        }
        else            
            break;        
        }
    }
    else {
        j=0;
        while (first!=last) {
        if (j==0)            
            g[j]=data[1][first];
        else          
            g[j]=Math.max(data[1][first],g[j-1]);        
        ++first;
        ++j;
        }
        for (i=j; i<l; ++i)        
        g[i]=g[j-1];        
    }
    }


    static private void calcHDilatation(double[][] data, int first, int last, int l, double[] h, boolean b)
    {
    int j;

    if (b) {
        for (j=l-1; j>=0; --j) {
        if (j==(l-1))            
            h[j]=data[1][last];        
        else          
            h[j]=Math.max(data[1][last],h[j+1]);        
        --last;
        }
    }
    else {
        j=(last-first)-1;
        h[j]=data[1][(--last)];
        while (last!=first) {
        --j;
        h[j]=Math.max(data[1][first],h[j+1]);
        --last;
        }
    }
    }

}