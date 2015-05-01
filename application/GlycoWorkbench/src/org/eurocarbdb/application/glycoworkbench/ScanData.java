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

import org.eurocarbdb.application.glycanbuilder.*;

import java.util.Collection;
import java.util.Map;
import java.util.Vector;
import java.util.TreeMap;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import javax.xml.transform.sax.TransformerHandler;

public class ScanData implements SAXUtils.SAXWriter {

    protected int num = 0;
    protected Integer parent_num = null;

    protected Integer ms_level = null;
    protected Double precursor_mz = null;
    protected Integer precursor_charge = null;
  
    protected Boolean positive_mode = null;
    protected Boolean centroided = null;
    protected Boolean deisotoped = null;
    protected Boolean charge_deconvoluted = null;
    protected Double  retention_time = null;

    protected Double base_peak_mz = null;
    protected Double base_peak_intensity = null;
    protected Double start_mz = null;
    protected Double end_mz = null;
    protected Double low_mz = null;
    protected Double high_mz = null;
    protected Double total_ion_current = null;

    public ScanData() {
    }

    public ScanData(int _num) {
    num = _num;

    ms_level = 1;
    precursor_mz = 0.;
    precursor_charge = 0;
    parent_num = 0;    
    }


    public ScanData(org.systemsbiology.jrap.Scan s) {
    num = s.getNum();

    ms_level = s.getMsLevel();
    precursor_mz = (double)s.getPrecursorMz();
    precursor_charge = s.getPrecursorCharge();
    parent_num = s.getPrecursorScanNum();
    
    positive_mode = (s.getPolarity()!=null) ?s.getPolarity().equals("+") :null;
    centroided = (s.getCentroided()==1);           
    charge_deconvoluted = (s.getChargeDeconvoluted()==1);
    deisotoped = (s.getDeisotoped()==1);
    retention_time = (s.getRetentionTime()!=null) ?s.getDoubleRetentionTime() :null;
    
    base_peak_intensity = (double)s.getBasePeakIntensity();
    base_peak_mz = (double)s.getBasePeakMz();           
    start_mz = (double)s.getStartMz();
    end_mz = (double)s.getEndMz();
    low_mz = (double)s.getLowMz();
    high_mz = (double)s.getHighMz();           
    total_ion_current = (double)s.getTotIonCurrent();           
    }

    public ScanData(int _num, org.proteomecommons.io.PeakList p) {
    num = _num;

    ms_level = p.getTandemCount();
    precursor_mz = (p.getParentPeak()!=null) ?p.getParentPeak().getMassOverCharge() :null;    
    precursor_charge = (p.getParentPeak()!=null) ?p.getParentPeak().getCharge() :null;    

    if( p instanceof org.proteomecommons.io.mzxml.v2_1.MzXMLPeakList ) {
        org.proteomecommons.io.mzxml.v2_1.MzXMLPeakList s = (org.proteomecommons.io.mzxml.v2_1.MzXMLPeakList)p;
        num = (s.getNum()!=null) ?Integer.valueOf(s.getNum()) :_num;
        parent_num = (s.getPrecursorScanNum()!=null) ?Integer.valueOf(s.getPrecursorScanNum()) :-1;
        positive_mode = (s.getPolarity()!=null) ?s.getPolarity().equals("+") :null;
        centroided = (s.getCentroided()!=null) ?(s.getCentroided().equals("1")) :null;           
        charge_deconvoluted = (s.getChargeDeconvoluted()!=null) ?(s.getChargeDeconvoluted().equals("1")) :null;           
        deisotoped = (s.getDeisotoped()!=null) ?(s.getDeisotoped().equals("1")) :null;           
        retention_time = (s.getRetentionTime()!=null) ?Double.valueOf(s.getRetentionTime()) :null;
        
        base_peak_intensity = (s.getBasePeakIntensity()!=null) ?Double.valueOf(s.getBasePeakIntensity()) :null;
        base_peak_mz = (s.getBasePeakMz()!=null) ?Double.valueOf(s.getBasePeakMz()) :null;           
        start_mz = (s.getStartMz()!=null) ?Double.valueOf(s.getStartMz()) :null;
        end_mz = (s.getEndMz()!=null) ?Double.valueOf(s.getEndMz()) :null;
        low_mz = (s.getLowMz()!=null) ?Double.valueOf(s.getLowMz()) :null;
        high_mz = (s.getHighMz()!=null) ?Double.valueOf(s.getHighMz()) :null;
        total_ion_current = (s.getTotIonCurrent()!=null) ?Double.valueOf(s.getTotIonCurrent()) :null;    
    }
    else if( p instanceof org.proteomecommons.io.mzxml.v2_0.MzXMLPeakList ) {
        org.proteomecommons.io.mzxml.v2_0.MzXMLPeakList s = (org.proteomecommons.io.mzxml.v2_0.MzXMLPeakList)p;
        num = (s.getNum()!=null) ?Integer.valueOf(s.getNum()) :_num;
        parent_num = (s.getPrecursorScanNum()!=null) ?Integer.valueOf(s.getPrecursorScanNum()) :-1;
        positive_mode = (s.getPolarity()!=null) ?s.getPolarity().equals("+") :null;
        centroided = (s.getCentroided()!=null) ?(s.getCentroided().equals("1")) :null;           
        charge_deconvoluted = (s.getChargeDeconvoluted()!=null) ?(s.getChargeDeconvoluted().equals("1")) :null;           
        deisotoped = (s.getDeisotoped()!=null) ?(s.getDeisotoped().equals("1")) :null;           
        retention_time = (s.getRetentionTime()!=null) ?Double.valueOf(s.getRetentionTime()) :null;
           
        base_peak_intensity = (s.getBasePeakIntensity()!=null) ?Double.valueOf(s.getBasePeakIntensity()) :null;
        base_peak_mz = (s.getBasePeakMz()!=null) ?Double.valueOf(s.getBasePeakMz()) :null;           
        start_mz = (s.getStartMz()!=null) ?Double.valueOf(s.getStartMz()) :null;
        end_mz = (s.getEndMz()!=null) ?Double.valueOf(s.getEndMz()) :null;
        low_mz = (s.getLowMz()!=null) ?Double.valueOf(s.getLowMz()) :null;
        high_mz = (s.getHighMz()!=null) ?Double.valueOf(s.getHighMz()) :null;
        total_ion_current = (s.getTotIonCurrent()!=null) ?Double.valueOf(s.getTotIonCurrent()) :null;    
    }
    else if( p instanceof org.proteomecommons.io.mzxml.v1_1_1.MzXMLPeakList ) {
        org.proteomecommons.io.mzxml.v1_1_1.MzXMLPeakList s = (org.proteomecommons.io.mzxml.v1_1_1.MzXMLPeakList)p;
        num = (s.getNum()!=null) ?Integer.valueOf(s.getNum()) :_num;
        parent_num = (s.getPrecursorScanNum()!=null) ?Integer.valueOf(s.getPrecursorScanNum()) :-1;
        positive_mode = (s.getPolarity()!=null) ?s.getPolarity().equals("+") :null;
        centroided = (s.getCentroided()!=null) ?(s.getCentroided().equals("1")) :null;           
        charge_deconvoluted = (s.getChargeDeconvoluted()!=null) ?(s.getChargeDeconvoluted().equals("1")) :null;           
        deisotoped = (s.getDeisotoped()!=null) ?(s.getDeisotoped().equals("1")) :null;           
        retention_time = (s.getRetentionTime()!=null) ?Double.valueOf(s.getRetentionTime()) :null;
           
        base_peak_intensity = (s.getBasePeakIntensity()!=null) ?Double.valueOf(s.getBasePeakIntensity()) :null;
        base_peak_mz = (s.getBasePeakMz()!=null) ?Double.valueOf(s.getBasePeakMz()) :null;           
        start_mz = (s.getStartMz()!=null) ?Double.valueOf(s.getStartMz()) :null;
        end_mz = (s.getEndMz()!=null) ?Double.valueOf(s.getEndMz()) :null;
        low_mz = (s.getLowMz()!=null) ?Double.valueOf(s.getLowMz()) :null;
        high_mz = (s.getHighMz()!=null) ?Double.valueOf(s.getHighMz()) :null;
        total_ion_current = (s.getTotIonCurrent()!=null) ?Double.valueOf(s.getTotIonCurrent()) :null;    
    }
    }

    // members access

    public int getNum() {
    return num;
    }
    
    public void setNum(int n) {
    num = n;
    }

    public Integer getParentNum() {
    return parent_num;
    }

    public void setParentNum(Integer v) {
    parent_num = v;
    }


    public Integer getMSLevel() {
    return ms_level;
    }    
    
    public void setMSLevel(Integer v) {
    ms_level = v;
    }

    public Double getPrecursorMZ() {
    return precursor_mz;
    }

    public void setPrecursorMZ(Double v) {
    precursor_mz = v;
    }

    public Integer getPrecursorCharge() {
    return precursor_charge;
    }

    public void setPrecursorCharge(Integer v) {
    precursor_charge = v;
    }

    public Boolean getPositiveMode() {   
    return positive_mode;
    }
    
    public void setPositiveMode(Boolean v) {
    positive_mode = v;
    }
    
    public Boolean getCentroided() {
    return centroided;
    }

    public void setCentroided(Boolean v) {
    centroided = v;
    }

    public Boolean getDeisotoped() {
    return deisotoped;
    }

    public void setDeisotoped(Boolean v) {
    deisotoped = v;
    }

    public Boolean getChargeDeconvoluted() {
    return charge_deconvoluted;
    }

    public void setChargeDeconvoluted(Boolean v) {
    charge_deconvoluted = v;
    }
    
    public Double getRetentionTime() {
    return retention_time;
    }

    public void setRetentionTime(Double v) {
    retention_time = v;
    }
    
    public Double getBasePeakMZ() {
    return base_peak_mz;
    }

    public void setBasePeakMZ(Double v) {
    base_peak_mz = v;
    }
    
    public Double getBasePeakIntensity() {
    return base_peak_intensity;
    }

    public void setBasePeakIntensity(Double v) {
    base_peak_intensity = v;
    }
    
    public Double getStartMZ() {
    return start_mz;
    }
    
    public void setStartMZ(Double v) {
    start_mz = v;
    }

    public Double getEndMZ() {
    return end_mz;
    }
    
    public void setEndMZ(Double v) {
    end_mz = v;
    }
    
    public Double getLowMZ() {
    return low_mz;
    }
    
    public void setLowMZ(Double v) {
    low_mz = v;
    }
    
    public Double getHighMZ() {
    return high_mz;
    }
    
    public void setHighMZ(Double v) {
    high_mz = v;
    }    

    public Double getTotalIonCurrent() {
    return total_ion_current;
    }
    
    public void setTotalIonCurrent(Double v){
    total_ion_current = v;
    }

    public String toString() {
    String ret = "Scan " + num;
        
    if( ms_level==1 ) 
        ret += ", MS";
    else if( ms_level==2 ) 
        ret += ", MS/MS";
    else if( ms_level>2 ) 
        ret += ", MS" + ms_level;

    if( ms_level>1 ) 
        ret += ", precursor= " + new java.text.DecimalFormat("0.0000").format(precursor_mz) + " Da";    
    return ret;
    }

    public static ScanData fromXML(Node sd_node) throws Exception {        
    if( sd_node==null )
        throw new Exception("empty node");

    ScanData ret = new ScanData();
    ret.num = XMLUtils.getIntegerAttribute(sd_node,"num");
    ret.parent_num = XMLUtils.getIntegerAttribute(sd_node,"parent_num");

    ret.ms_level = XMLUtils.getIntegerAttribute(sd_node,"ms_level");
    ret.precursor_mz = XMLUtils.getDoubleAttribute(sd_node,"precursor_mz");
    if( ret.precursor_mz==null )
        ret.precursor_mz = XMLUtils.getDoubleAttribute(sd_node,"parent_mz"); // backward compatibility
    ret.precursor_charge = XMLUtils.getIntegerAttribute(sd_node,"precursor_charge");

    ret.positive_mode = XMLUtils.getBooleanAttribute(sd_node,"positive_mode");
    ret.centroided = XMLUtils.getBooleanAttribute(sd_node,"centroided");
    ret.deisotoped = XMLUtils.getBooleanAttribute(sd_node,"deisotoped");
    ret.charge_deconvoluted = XMLUtils.getBooleanAttribute(sd_node,"charge_deconvoluted");
    ret.retention_time = XMLUtils.getDoubleAttribute(sd_node,"retention_time");

    ret.base_peak_mz = XMLUtils.getDoubleAttribute(sd_node,"base_peak_mz");
    ret.base_peak_intensity = XMLUtils.getDoubleAttribute(sd_node,"base_peak_intensity");
    ret.start_mz = XMLUtils.getDoubleAttribute(sd_node,"start_mz");
    ret.end_mz = XMLUtils.getDoubleAttribute(sd_node,"end_mz");
    ret.low_mz = XMLUtils.getDoubleAttribute(sd_node,"low_mz");
    ret.high_mz = XMLUtils.getDoubleAttribute(sd_node,"high_mz");
    ret.total_ion_current = XMLUtils.getDoubleAttribute(sd_node,"total_ion_current");

    return ret;
    }
    
    public Element toXML(Document document) {    
    if( document==null )
        return null;
    
    // create root node
    Element sd_node = document.createElement("ScanData");
    
    // add values
    sd_node.setAttribute("num",""+num);
    if( parent_num!=null )
        sd_node.setAttribute("parent_num",""+parent_num);

    if( ms_level!=null )
        sd_node.setAttribute("ms_level",""+ms_level);
    if( precursor_mz!=null )
        sd_node.setAttribute("precursor_mz",""+precursor_mz);
    if( precursor_charge!=null )
        sd_node.setAttribute("precursor_charge",""+precursor_charge);

    if( positive_mode!=null )
        sd_node.setAttribute("positive_mode",""+positive_mode);
    if( centroided!=null )
        sd_node.setAttribute("centroided",""+centroided);
    if( deisotoped!=null )
        sd_node.setAttribute("deisotoped",""+deisotoped);
    if( charge_deconvoluted!=null )
        sd_node.setAttribute("charge_deconvoluted",""+charge_deconvoluted);
    if( retention_time!=null )
        sd_node.setAttribute("retention_time",""+retention_time);

    if( base_peak_mz!=null )
        sd_node.setAttribute("base_peak_mz",""+base_peak_mz);
    if( base_peak_intensity!=null )
        sd_node.setAttribute("base_peak_intensity",""+base_peak_intensity);
    if( start_mz!=null )
        sd_node.setAttribute("start_mz",""+start_mz);
    if( end_mz!=null )
        sd_node.setAttribute("end_mz",""+end_mz);
    if( low_mz!=null )
        sd_node.setAttribute("low_mz",""+low_mz);
    if( high_mz!=null )
        sd_node.setAttribute("high_mz",""+high_mz);
    if( total_ion_current!=null )
        sd_node.setAttribute("total_ion_current",""+total_ion_current);

    return sd_node;    
    }

    public static class SAXHandler extends SAXUtils.ObjectTreeHandler {
    
    public boolean isElement(String namespaceURI, String localName, String qName) {
        return qName.equals(getNodeElementName());
    }

    public static String getNodeElementName() {
        return "ScanData";
    }

    protected void initContent(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        super.initContent(namespaceURI,localName,qName,atts);

        ScanData ret = new ScanData();
        ret.num = integerAttribute(atts,"num");
        ret.parent_num = integerAttribute(atts,"parent_num");

        ret.ms_level = integerAttribute(atts,"ms_level");
        ret.precursor_mz = doubleAttribute(atts,"precursor_mz");
        if( ret.precursor_mz==null )
        ret.precursor_mz = doubleAttribute(atts,"parent_mz"); // backward compatibility
        ret.precursor_charge = integerAttribute(atts,"precursor_charge");

        ret.positive_mode = booleanAttribute(atts,"positive_mode");
        ret.centroided = booleanAttribute(atts,"centroided");
        ret.deisotoped = booleanAttribute(atts,"deisotoped");
        ret.charge_deconvoluted = booleanAttribute(atts,"charge_deconvoluted");
        ret.retention_time = doubleAttribute(atts,"retention_time");
        
        ret.base_peak_mz = doubleAttribute(atts,"base_peak_mz");
        ret.base_peak_intensity = doubleAttribute(atts,"base_peak_intensity");
        ret.start_mz = doubleAttribute(atts,"start_mz");
        ret.end_mz = doubleAttribute(atts,"end_mz");
        ret.low_mz = doubleAttribute(atts,"low_mz");
        ret.high_mz = doubleAttribute(atts,"high_mz");
        ret.total_ion_current = doubleAttribute(atts,"total_ion_current");
        
        object = ret;
    }
    }
     
    public void write(TransformerHandler th) throws SAXException {
    AttributesImpl atts = new AttributesImpl();
    
    atts.addAttribute("","","num","CDATA",""+num);
    if( parent_num!=null )
        atts.addAttribute("","","parent_num","CDATA",""+parent_num);

    if( ms_level!=null )
        atts.addAttribute("","","ms_level","CDATA",""+ms_level);
    if( precursor_mz!=null )
        atts.addAttribute("","","precursor_mz","CDATA",""+precursor_mz);
    if( precursor_charge!=null )
        atts.addAttribute("","","precursor_charge","CDATA",""+precursor_charge);

    if( positive_mode!=null )
        atts.addAttribute("","","positive_mode","CDATA",""+positive_mode);
    if( centroided!=null )
        atts.addAttribute("","","centroided","CDATA",""+centroided);
    if( deisotoped!=null )
        atts.addAttribute("","","deisotoped","CDATA",""+deisotoped);
    if( charge_deconvoluted!=null )
        atts.addAttribute("","","charge_deconvoluted","CDATA",""+charge_deconvoluted);
    if( retention_time!=null )
        atts.addAttribute("","","retention_time","CDATA",""+retention_time);

    if( base_peak_mz!=null )
        atts.addAttribute("","","base_peak_mz","CDATA",""+base_peak_mz);
    if( base_peak_intensity!=null )
        atts.addAttribute("","","base_peak_intensity","CDATA",""+base_peak_intensity);
    if( start_mz!=null )
        atts.addAttribute("","","start_mz","CDATA",""+start_mz);
    if( end_mz!=null )
        atts.addAttribute("","","end_mz","CDATA",""+end_mz);
    if( low_mz!=null )
        atts.addAttribute("","","low_mz","CDATA",""+low_mz);
    if( high_mz!=null )
        atts.addAttribute("","","high_mz","CDATA",""+high_mz);
    if( total_ion_current!=null )
        atts.addAttribute("","","total_ion_current","CDATA",""+total_ion_current);

    th.startElement("","","ScanData",atts);
    th.endElement("","","ScanData");
    }
    
    
   
}
