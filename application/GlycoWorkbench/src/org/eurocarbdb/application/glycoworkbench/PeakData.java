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

package org.eurocarbdb.application.glycoworkbench;

import org.eurocarbdb.application.glycoworkbench.plugin.peakpicker.*;
import org.eurocarbdb.application.glycanbuilder.*;


import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import java.nio.MappedByteBuffer;
import java.nio.DoubleBuffer;
import org.jfree.data.Range;
import org.xml.sax.SAXException;
import javax.xml.transform.sax.TransformerHandler;
import org.xml.sax.helpers.AttributesImpl;

public class PeakData implements SAXUtils.SAXWriter {     

    protected int no_peaks;
    protected double min_mz;
    protected double max_mz;
    protected MMFCreator.Pointer theData;
 
    public PeakData() {
    initData();
    }

    public PeakData(org.systemsbiology.jrap.Scan s, MMFCreator mmfc) throws Exception {
    if( s!=null )
        setData(s.getMassIntensityList(),mmfc);
    else 
        initData();    
    }

    public PeakData(org.proteomecommons.io.PeakList p, MMFCreator mmfc) throws Exception {
    if( p!=null )
        setData(p.getPeaks(),mmfc);
    else 
        initData();
    }    

    public PeakData(List<Peak> peaks, MMFCreator mmfc) throws Exception {
    setData(peaks,mmfc);
    }
    
    private void initData() {
    no_peaks = 0;
    min_mz = max_mz = 0.;
    theData = null;
    }

    private void setData(org.proteomecommons.io.Peak[] data, MMFCreator mmfc) throws Exception {
    // init
    initData();

    // check for empty data
    if( data==null || data.length==0 ) 
        return;    

    // skip trailing zeros
    int start = 0;
    for( ; start<data.length && data[start].getMassOverCharge()==0.; start++ );
    if( start==data.length )
        return;

    //add data
    for( int i=start; i<data.length; i++ ) {
        mmfc.addDouble(data[i].getMassOverCharge());
        mmfc.addDouble(data[i].getIntensity());    
    }
    
    no_peaks = data.length-start;
    min_mz = data[start].getMassOverCharge();
    max_mz = data[data.length-1].getMassOverCharge();
    theData = mmfc.getPointerFromLast();
    }
    
    private void setData(float[][] data, MMFCreator mmfc) throws Exception {
    // init
    initData();

    // check for empty data
    if( data==null || data[0].length==0 ) 
        return;


    // skip trailing zeros
    int start = 0;
    for( ; start<data[0].length && data[0][start]==0.; start++ );
    if( start==data[0].length )
        return;

    // add data
    for( int i=start; i<data[0].length; i++ ) {
        mmfc.addDouble(data[0][i]);
        mmfc.addDouble(data[1][i]);
    }
    
    no_peaks = data[0].length-start;
    min_mz = data[0][start];
    max_mz = data[0][data[0].length-1];
    theData = mmfc.getPointerFromLast();
    }   


    private void setData(List<Peak> data, MMFCreator mmfc) throws Exception {
    // init
    initData();

    // check for empty data
    if( data==null || data.size()==0 ) 
        return;
    int data_peaks = data.size();

    // skip trailing zeros
    int start = 0;
    for( ; start<data_peaks && data.get(start).getMZ()==0.; start++ );
    if( start==data_peaks )
        return;

    // add data
    for( int i=start; i<data_peaks; i++ ) {
        mmfc.addDouble(data.get(i).getMZ());
        mmfc.addDouble(data.get(i).getIntensity());
    }
    
    no_peaks = data_peaks - start;
    min_mz = data.get(start).getMZ();
    max_mz = data.get(data_peaks-1).getMZ();
    theData = mmfc.getPointerFromLast();
    }   

    public int getPeakCount() {
    return no_peaks;
    }

    public double getMinMZ() {
    return min_mz;
    }

    public double getMaxMZ() {
    return max_mz;
    }

    public Range getMZRange() {
    return new Range(min_mz,max_mz);
    }        

    // retrieve data

    public double[] findNearestPeak(double mz) {
    // get data
    DoubleBuffer buffer = null;
    try {
        buffer = theData.getBuffer(true).asDoubleBuffer();
    }
    catch(Exception e) {
        LogUtils.report(e);
        return null;
    }

    // estimate position
    int best_pos = (int)((mz-min_mz)/(max_mz-min_mz));
    if( best_pos<0 )
        return new double[] {buffer.get(0),buffer.get(1)};            
    if( best_pos>=no_peaks )
        return new double[] {buffer.get(2*no_peaks-2),buffer.get(2*no_peaks-1)};            
    
    double pos_mz = buffer.get(2*best_pos);
    double min_dist = Math.abs(pos_mz-mz);
    if( pos_mz>mz ) {
        for( int i=best_pos-1; i>=0; i-- ) {
        double dist = Math.abs(buffer.get(2*i)-mz);
        if( dist>min_dist )
            break;
        min_dist = dist;
        best_pos = i;
        }
    }
    else if( pos_mz<mz ) {
        for( int i=best_pos+1; i<no_peaks; i++ ) {
        double dist = Math.abs(buffer.get(2*i)-mz);
        if( dist>min_dist )
            break;
        min_dist = dist;
        best_pos = i;
        }        
    }

    return new double[] {buffer.get(2*best_pos),buffer.get(2*best_pos+1)};            
    }

    public double[][] getData() {
    return getData(0.);
    }

    public double[][] getData(double from_mz, double to_mz) {
    return getData(from_mz,to_mz,0.);
    }

    public double[][] getData(double mz_toll) {    
    return getData(getMinMZ(),getMaxMZ(),mz_toll,false);
    }

    public double[][] getData(double mz_toll, boolean rel_int) {    
    return getData(getMinMZ(),getMaxMZ(),mz_toll,rel_int);
    }

    public double[][] getData(Range range, double mz_toll) {
    return getData(range.getLowerBound(),range.getUpperBound(),mz_toll,false);
    }

    public double[][] getData(Range range, double mz_toll, boolean rel_int) {
    return getData(range.getLowerBound(),range.getUpperBound(),mz_toll,rel_int);
    }

    public double[][] getData(double from_mz, double to_mz, double mz_toll) {    
    return getData(from_mz,to_mz,mz_toll,false);
    }

    public double[][] getData(double from_mz, double to_mz, double mz_toll, boolean rel_int) {    
        
    DoubleBuffer buffer = null;
    try {
        buffer = theData.getBuffer(true).asDoubleBuffer();
    }
    catch(Exception e) {
        LogUtils.report(e);
        return null;
    }
        
    double max_int = 0;
    Vector<Peak> peaks = new Vector<Peak>();
    for( int i=0; ; ) {
        // skip initial peaks
        if( buffer.get(2*i)<from_mz ) {
        i++;
        continue;
        }
        
        // get highest peak in the tolerange
        double last_mz = buffer.get(2*i);
        double add_mz = buffer.get(2*i);
        double add_int = buffer.get(2*i+1);
        for( ++i; i<no_peaks && buffer.get(2*i)<(last_mz+mz_toll) && buffer.get(2*i)<=to_mz ; i++ ) {
        if( buffer.get(2*i+1)>add_int ) {
            add_mz = buffer.get(2*i);
            add_int = buffer.get(2*i+1);
        }
        }

        // add peak
        max_int = Math.max(max_int,add_int);
        peaks.add(new Peak(add_mz,add_int));

        // skip last peaks
        if( i>=no_peaks || buffer.get(2*i)>to_mz )
        break;
    }

    // return (normalized) value
           double[][] ret = new double[2][];
    ret[0] = new double[peaks.size()];
    ret[1] = new double[peaks.size()];

    double div = (rel_int) ?(max_int/100) :1.;
    for( int i=0; i<peaks.size(); i++ ) {
        ret[0][i] = peaks.get(i).getMZ();
        ret[1][i] = peaks.get(i).getIntensity()/div;
    }
     
    return ret;
    }
    /*
    public double[][] getData(Range range, double mz_toll, boolean rel_int) {
    
    double div = 1.;
    if( rel_int )
        div = getMaxIntensity(range)/100.;

    mz_toll = Math.abs(mz_toll);
    
    // restrict to range
    java.util.SortedMap<Double,Double> submap = theData.subMap(range.getLowerBound(),range.getUpperBound());        
    if( submap.size()==0 ) {
        double[][] ret = new double[2][];
        ret[0] = new double[0];
        ret[1] = new double[0];
        return ret;
    }

    // count entries
    int count = 1;
    double last_mz = submap.firstKey();
    if( mz_toll>0. )
        last_mz = getMinMZ() + (int)((last_mz-getMinMZ())/mz_toll)*mz_toll;
    for( Map.Entry<Double,Double> e : submap.entrySet() ) {
        if( (e.getKey()-last_mz)>mz_toll ) {
        count++;
        last_mz = e.getKey();
        }        
    }

    // allocate space
    double[][] ret = new double[2][];
    ret[0] = new double[count];
    ret[1] = new double[count];
    
    // save entries
    int i = 0;
    last_mz = submap.firstKey();
    if( mz_toll>0. )
        last_mz = getMinMZ() + (int)((last_mz-getMinMZ())/mz_toll)*mz_toll;
    double max_int = submap.get(submap.firstKey());
    for( Map.Entry<Double,Double> e : submap.entrySet() ) {        
        if( (e.getKey()-last_mz)>mz_toll ) {
        ret[0][i] = last_mz;
        ret[1][i] = max_int/div;
        last_mz = e.getKey();
        max_int = e.getValue();
        i++;
        }
        else
        max_int = Math.max(max_int,e.getValue());
    }
    if( i<count ) {
        ret[0][i] = last_mz;
        ret[1][i] = max_int/div;
    }

    return ret;
    }   
    */
   
    public void baselineCorrection() {

    try {
        DoubleBuffer buffer = theData.getBuffer(false).asDoubleBuffer();
      
        // put data in memory
        double[][] data = new double[2][];
        data[0] = new double[no_peaks];
        data[1] = new double[no_peaks];
        for( int i=0; i<no_peaks; i++ ) {
        data[0][i] = buffer.get(2*i);
        data[1][i] = buffer.get(2*i+1);
        }
        
        // compute baseline correction
        TopHatFilter.filter(data,2.5);

        // save corrected data
        for( int i=0; i<no_peaks; i++ ) {
        buffer.put(2*i,data[0][i]);
        buffer.put(2*i+1,data[1][i]);
        }
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }

    public void recalibrate(List<Double> params) {
    if( params==null || params.size()<2 )
        return;

    try {
        // save corrected data
        DoubleBuffer buffer = theData.getBuffer(false).asDoubleBuffer();
        for( int i=0; i<no_peaks; i++ ) {
        double old_mz = buffer.get(2*i);
        double new_mz = recalibrate(old_mz,params);
        buffer.put(2*i,new_mz);        
        }

        // recalibrate min/max
        min_mz = recalibrate(min_mz,params);        
        max_mz = recalibrate(max_mz,params);
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }

    private double recalibrate(double mz, List<Double> params) {
    double ret = mz;
    double mul = 1;
    for( Double param : params ) {
        ret += param * mul;
        mul *= mz;
    }
    return ret;
    }
     
    public void noiseFilter() {
    noiseFilter(0.8);
    }

    public void noiseFilter(double width) {

    try {
        DoubleBuffer buffer = theData.getBuffer(false).asDoubleBuffer();
      
        // put data in memory
        double[][] data = new double[2][];
        data[0] = new double[no_peaks];
        data[1] = new double[no_peaks];
        for( int i=0; i<no_peaks; i++ ) {
        data[0][i] = buffer.get(2*i);
        data[1][i] = buffer.get(2*i+1);
        }
        
        // compute gaussian filtering
        GaussFilter gf = new GaussFilter();
        gf.setKernelWidth(width);
        data = gf.filter(data);

        // save corrected data
        for( int i=0; i<no_peaks; i++ ) {
        buffer.put(2*i,data[0][i]);
        buffer.put(2*i+1,data[1][i]);
        }
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }

    // Serialization

    public void write(File f) throws Exception {
    write(new FileOutputStream(f));
    }

    public void write(OutputStream os) throws Exception {
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));    

    DoubleBuffer buffer = theData.getBuffer(true).asDoubleBuffer();
    for( int i=0; i<no_peaks; i++ ) {
        String str = "" + buffer.get(2*i) + " " + buffer.get(2*i+1);
        bw.write(str,0,str.length());
        bw.newLine();
    }

    bw.close();
    }

    static public PeakData fromXML(Node pd_node, MMFCreator mmfc) throws Exception {
    PeakData ret = new PeakData();
    if( pd_node==null )
        return ret;
    
    // get text representation of peaks
    String text = XMLUtils.getText(pd_node);
    if( text==null || text.length()==0 )
        return ret;

    // decode peaks from string
    byte[] decoded = Base64.decode(text);
    for( int i=0; i<decoded.length; i++ )
        mmfc.addByte(decoded[i]);
    
    // setting data
    ret.theData = mmfc.getPointerFromLast();
    ret.no_peaks = (int)(8*ret.theData.getSize()/Double.SIZE)/2;

    DoubleBuffer buffer = ret.theData.getBuffer(true).asDoubleBuffer();
    ret.min_mz = buffer.get(0);
    ret.max_mz = buffer.get(2*(ret.no_peaks-1));
    
    return ret;
    }
    
    public Element toXML(Document document) {    
    if( document==null )
        return null;
    
    // create root node
    Element pd_node = document.createElement("PeakData");

    // create string encoding of peaks
    try {
        // create string encoding of peaks
        MappedByteBuffer buffer = theData.getBuffer(true);
        byte[] toencode = new byte[buffer.capacity()];
        for( int i=0; i<buffer.capacity(); i++ ) 
        toencode[i] = buffer.get(i);

        // store encoding
        XMLUtils.setText(pd_node,Base64.encodeToString(toencode,false));
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    
    
    return pd_node;    
    }

    public static class SAXHandler extends SAXUtils.ObjectTreeHandler {
    
    private MMFCreator mmfc;

    public SAXHandler(MMFCreator _mmfc) {
        mmfc = _mmfc;
    }


    public boolean isElement(String namespaceURI, String localName, String qName) {
        return qName.equals(getNodeElementName());
    }

    public static String getNodeElementName() {
        return "PeakData";
    }    

    protected void addWhiteSpace() {
    }

    protected Object finalizeContent(String namespaceURI, String localName, String qName) throws SAXException{

        PeakData ret = new PeakData();
    
        try {
        // decode peaks from string
        byte[] decoded = Base64.decode(text.toString());
        for( int i=0; i<decoded.length; i++ )
            mmfc.addByte(decoded[i]);

        // set data
        ret.theData = mmfc.getPointerFromLast();
        ret.no_peaks = (int)(8*ret.theData.getSize()/Double.SIZE)/2;

        DoubleBuffer buffer = ret.theData.getBuffer(true).asDoubleBuffer();
        ret.min_mz = buffer.get(0);
        ret.max_mz = buffer.get(2*(ret.no_peaks-1));
        }
        catch(Exception e) {
        throw new SAXException(e);
        }
        
        return (object = ret);
    }
    }            

    public void write(TransformerHandler th) throws SAXException {
    th.startElement("","","PeakData",new AttributesImpl());

    char[] text = new char[0];
    try {
        // create string encoding of peaks
        MappedByteBuffer buffer = theData.getBuffer(true);
        byte[] toencode = new byte[buffer.capacity()];
        for( int i=0; i<buffer.capacity(); i++ ) 
        toencode[i] = buffer.get(i);

        // store encoding
        text = Base64.encodeToChar(toencode,true);
    }
    catch(Exception e) {
        LogUtils.report(e);
    }

    th.characters(text,0,text.length);
    th.endElement("","","PeakData");
    }

}
