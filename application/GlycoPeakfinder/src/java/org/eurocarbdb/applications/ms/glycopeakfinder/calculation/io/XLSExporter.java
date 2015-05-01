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
* 
*/
package org.eurocarbdb.applications.ms.glycopeakfinder.calculation.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.format.PageOrientation;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationDerivatisation;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationFragment;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationIon;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationMolecule;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationParameter;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationPeak;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.PeakAnnotation;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Persubstitution;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.SpectraType;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.util.AnnotationToString;

/**
* @author Logan
*
*/
public class XLSExporter 
{
    private CalculationParameter m_objParameter = null; 
    /**
     * @param parameter
     * @param stream
     * @throws IOException 
     * @throws WriteException 
     */
    public void export(CalculationParameter a_objParameter, ByteArrayOutputStream a_objStream) throws IOException, WriteException 
    {
        this.m_objParameter = a_objParameter;
        WritableWorkbook t_objWorkbook = Workbook.createWorkbook(a_objStream);
        WritableSheet t_objSheet = t_objWorkbook.createSheet("Settings", 0);
        this.formatSettingsPage(t_objSheet);
        this.createSettingSheet(t_objSheet);

        t_objSheet = t_objWorkbook.createSheet("Results", 1);
        this.formatResultPage(t_objSheet);
        this.createResultSheet(t_objSheet);
        t_objSheet.setPageSetup(PageOrientation.LANDSCAPE);

        t_objWorkbook.write();
        t_objWorkbook.close();        
    }

    private void formatResultPage(WritableSheet a_objSheet) throws RowsExceededException, WriteException 
    {
        // mass
        CellView t_objView = new CellView();
        t_objView.setSize(3000);
        WritableFont t_objFont = new WritableFont(WritableFont.ARIAL, 
                10, 
                WritableFont.NO_BOLD);
        WritableCellFormat t_objFormat = new WritableCellFormat(t_objFont);
        t_objView.setFormat(t_objFormat);
        a_objSheet.setColumnView(0, t_objView);
        // intensity
        t_objView = new CellView();
        t_objView.setSize(3000);
        t_objFont = new WritableFont(WritableFont.ARIAL, 
                10, 
                WritableFont.NO_BOLD);
        t_objFormat = new WritableCellFormat(t_objFont);
        t_objView.setFormat(t_objFormat);
        a_objSheet.setColumnView(1, t_objView);
        // composition
        t_objView = new CellView();
        t_objView.setSize(9000);
        t_objFont = new WritableFont(WritableFont.ARIAL, 
                10, 
                WritableFont.NO_BOLD);
        t_objFormat = new WritableCellFormat(t_objFont);
        t_objView.setFormat(t_objFormat);
        a_objSheet.setColumnView(2, t_objView);
        // small molecules
        t_objView = new CellView();
        t_objView.setSize(4000);
        t_objFont = new WritableFont(WritableFont.ARIAL, 
                10, 
                WritableFont.NO_BOLD);
        t_objFormat = new WritableCellFormat(t_objFont);
        t_objView.setFormat(t_objFormat);
        a_objSheet.setColumnView(3, t_objView);
        // charged ions
        t_objView = new CellView();
        t_objView.setSize(4000);
        t_objFont = new WritableFont(WritableFont.ARIAL, 
                10, 
                WritableFont.NO_BOLD);
        t_objFormat = new WritableCellFormat(t_objFont);
        t_objView.setFormat(t_objFormat);
        a_objSheet.setColumnView(4, t_objView);
        // ion type
        t_objView = new CellView();
        t_objView.setSize(4000);
        t_objFont = new WritableFont(WritableFont.ARIAL, 
                10, 
                WritableFont.NO_BOLD);
        t_objFormat = new WritableCellFormat(t_objFont);
        t_objView.setFormat(t_objFormat);
        a_objSheet.setColumnView(5, t_objView);
        // mass calculation
        t_objView = new CellView();
        t_objView.setSize(3000);
        t_objFont = new WritableFont(WritableFont.ARIAL, 
                10, 
                WritableFont.NO_BOLD);
        t_objFormat = new WritableCellFormat(t_objFont);
        t_objView.setFormat(t_objFormat);
        a_objSheet.setColumnView(6, t_objView);
        // deviation
        t_objView = new CellView();
        t_objView.setSize(3000);
        t_objFont = new WritableFont(WritableFont.ARIAL, 
                10, 
                WritableFont.NO_BOLD);
        t_objFormat = new WritableCellFormat(t_objFont);
        t_objView.setFormat(t_objFormat);
        a_objSheet.setColumnView(7, t_objView);
    }

    private void formatSettingsPage(WritableSheet a_objSheet) throws RowsExceededException, WriteException 
    {
        // spalte 1
        CellView t_objView = new CellView();
        t_objView.setSize(10000);
        WritableFont t_objFont = new WritableFont(WritableFont.ARIAL, 
                10, 
                WritableFont.BOLD);
        WritableCellFormat t_objFormat = new WritableCellFormat(t_objFont);
        t_objFormat.setAlignment( Alignment.LEFT );
        t_objView.setFormat(t_objFormat);
        a_objSheet.setColumnView(0, t_objView);
        // spalte 2
        t_objView = new CellView();
        t_objView.setSize(6000);
        t_objFont = new WritableFont(WritableFont.ARIAL, 
                10, 
                WritableFont.NO_BOLD);
        t_objFormat = new WritableCellFormat(t_objFont);
        t_objFormat.setAlignment( Alignment.LEFT );
        t_objView.setFormat(t_objFormat);
        a_objSheet.setColumnView(1, t_objView);
        // spalte 3
        t_objView = new CellView();
        t_objView.setSize(4000);
        t_objFont = new WritableFont(WritableFont.ARIAL, 
                10, 
                WritableFont.NO_BOLD);
        t_objFormat = new WritableCellFormat(t_objFont);
        t_objFormat.setAlignment( Alignment.LEFT );
        t_objView.setFormat(t_objFormat);
        a_objSheet.setColumnView(2, t_objView);
        // spalte 4
        t_objView = new CellView();
        t_objView.setSize(3000);
        t_objFont = new WritableFont(WritableFont.ARIAL, 
                10, 
                WritableFont.NO_BOLD);
        t_objFormat = new WritableCellFormat(t_objFont);
        t_objFormat.setAlignment( Alignment.LEFT );
        t_objView.setFormat(t_objFormat);
        a_objSheet.setColumnView(3, t_objView);
        // headline
        a_objSheet.mergeCells(0, 0, 3, 0);
    }

    private void createResultSheet(WritableSheet a_objPage) throws RowsExceededException, WriteException 
    {
        AnnotationToString t_objAnnotationToString = new AnnotationToString();
        int t_iPeakLineNumber = 0;
        CalculationPeak t_objPeak = null;
        Label t_objLabel = null;
        Number t_objNumber = null;
        if ( this.m_objParameter.getSpectraType() == SpectraType.MS2 && this.m_objParameter.getScan().getPrecusorMass() != null )
        {
            t_objLabel = new Label(0,t_iPeakLineNumber,"Precursor:" );
            a_objPage.addCell(t_objLabel);
            t_iPeakLineNumber++;
            int t_iLineCount = this.writeHeadline(a_objPage,t_iPeakLineNumber);
            t_iPeakLineNumber += t_iLineCount+1;
            for (Iterator<CalculationPeak> t_iterPre = this.getPrecursorPeaks().iterator(); t_iterPre.hasNext();) 
            {
                t_objPeak = t_iterPre.next();
                t_objNumber = new Number(0,t_iPeakLineNumber,t_objPeak.getMz());
                a_objPage.addCell(t_objNumber);
                if ( t_objPeak.getIntensity() != 0 )
                {
                    t_objNumber = new Number(1,t_iPeakLineNumber,t_objPeak.getMz());
                    a_objPage.addCell(t_objNumber);
                }
                else
                {
                    t_objLabel = new Label(1,t_iPeakLineNumber,"n/a");
                    a_objPage.addCell(t_objLabel);
                }
                for (Iterator<PeakAnnotation> t_iterAnnotation = t_objPeak.getAnnotation().iterator(); t_iterAnnotation.hasNext();) 
                {
                    PeakAnnotation t_objAnno = t_iterAnnotation.next();
                    if ( t_objAnno.getFragments().size() == 0 )
                    {
                        t_objLabel = new Label(2,t_iPeakLineNumber,t_objAnnotationToString.composition(t_objAnno));
                        a_objPage.addCell(t_objLabel);
                        t_objLabel = new Label(3,t_iPeakLineNumber,t_objAnnotationToString.smallMolecules(t_objAnno));
                        a_objPage.addCell(t_objLabel);
                        t_objLabel = new Label(4,t_iPeakLineNumber,t_objAnnotationToString.chargedIon(t_objAnno));
                        a_objPage.addCell(t_objLabel);
                        t_objLabel = new Label(5,t_iPeakLineNumber,t_objAnnotationToString.ion(t_objAnno));
                        a_objPage.addCell(t_objLabel);
                        t_objNumber = new Number(6,t_iPeakLineNumber,t_objAnno.getMass());
                        a_objPage.addCell(t_objNumber);
                        t_objNumber = new Number(7,t_iPeakLineNumber,this.getDeviation(t_objAnno.getMass(),t_objPeak.getMz()));
                        a_objPage.addCell(t_objNumber);
                        t_iPeakLineNumber++;
                    }
                }
                if ( t_objPeak.getAnnotationCount() != t_objPeak.getAnnotation().size() )
                {
                    // not all were calculated
                    t_objLabel = new Label(2,t_iPeakLineNumber,
                            String.format("This peak has %d annotations. Only the first %d are shown.",t_objPeak.getAnnotationCount(),t_objPeak.getAnnotation().size()));
                    a_objPage.addCell(t_objLabel);
                    WritableCell t_objCell = a_objPage.getWritableCell(2, t_iPeakLineNumber);
                    WritableFont t_objFont = new WritableFont(WritableFont.ARIAL, 
                            10, 
                            WritableFont.BOLD);
                    t_objFont.setColour(Colour.RED);
                    WritableCellFormat t_objFormat = new WritableCellFormat(t_objFont);
                    t_objCell.setCellFormat(t_objFormat);
                    a_objPage.mergeCells(2, t_iPeakLineNumber, 7, t_iPeakLineNumber);
                }
                if ( t_objPeak.getAnnotation().size() == 0 )
                {
                    t_iPeakLineNumber++;
                }
                t_iPeakLineNumber++;
            }
            t_objLabel = new Label(0,t_iPeakLineNumber,"Peaklist:" );
            a_objPage.addCell(t_objLabel);
            t_iPeakLineNumber++;
        }
        int t_iLineCount = this.writeHeadline(a_objPage,t_iPeakLineNumber);
        t_iPeakLineNumber += t_iLineCount+1;
        for (Iterator<CalculationPeak> t_iterPeaks = this.m_objParameter.getScan().getPeaks().iterator(); t_iterPeaks.hasNext();) 
        {
            t_objPeak = t_iterPeaks.next();
            t_objNumber = new Number(0,t_iPeakLineNumber,t_objPeak.getMz());
            a_objPage.addCell(t_objNumber);
            if ( t_objPeak.getIntensity() != 0 )
            {
                t_objNumber = new Number(1,t_iPeakLineNumber,t_objPeak.getMz());
                a_objPage.addCell(t_objNumber);
            }
            else
            {
                t_objLabel = new Label(1,t_iPeakLineNumber,"n/a");
                a_objPage.addCell(t_objLabel);
            }
            for (Iterator<PeakAnnotation> t_iterAnnotation = t_objPeak.getAnnotation().iterator(); t_iterAnnotation.hasNext();) 
            {
                PeakAnnotation t_objAnno = t_iterAnnotation.next();
                if ( this.m_objParameter.getSpectraType() != SpectraType.MS2 || t_objAnno.getFragments().size() != 0 )
                {
                    t_objLabel = new Label(2,t_iPeakLineNumber,t_objAnnotationToString.composition(t_objAnno));
                    a_objPage.addCell(t_objLabel);
                    t_objLabel = new Label(3,t_iPeakLineNumber,t_objAnnotationToString.smallMolecules(t_objAnno));
                    a_objPage.addCell(t_objLabel);
                    t_objLabel = new Label(4,t_iPeakLineNumber,t_objAnnotationToString.chargedIon(t_objAnno));
                    a_objPage.addCell(t_objLabel);
                    t_objLabel = new Label(5,t_iPeakLineNumber,t_objAnnotationToString.ion(t_objAnno));
                    a_objPage.addCell(t_objLabel);
                    t_objNumber = new Number(6,t_iPeakLineNumber,t_objAnno.getMass());
                    a_objPage.addCell(t_objNumber);
                    t_objNumber = new Number(7,t_iPeakLineNumber,this.getDeviation(t_objAnno.getMass(),t_objPeak.getMz()));
                    a_objPage.addCell(t_objNumber);
                    t_iPeakLineNumber++;
                }
            }
            if ( t_objPeak.getAnnotationCount() != t_objPeak.getAnnotation().size() )
            {
                // not all were calculated
                t_objLabel = new Label(2,t_iPeakLineNumber,
                        String.format("This peak has %d annotations. Only the first %d are shown.",t_objPeak.getAnnotationCount(),t_objPeak.getAnnotation().size()));
                a_objPage.addCell(t_objLabel);
                WritableCell t_objCell = a_objPage.getWritableCell(2, t_iPeakLineNumber);
                WritableFont t_objFont = new WritableFont(WritableFont.ARIAL, 
                        10, 
                        WritableFont.BOLD);
                t_objFont.setColour(Colour.RED);
                WritableCellFormat t_objFormat = new WritableCellFormat(t_objFont);
                t_objCell.setCellFormat(t_objFormat);
                a_objPage.mergeCells(2, t_iPeakLineNumber, 7, t_iPeakLineNumber);
                if ( t_objPeak.getAnnotation().size() > 0 )
                {
                    t_iPeakLineNumber++;
                }
            }
            if ( t_objPeak.getAnnotation().size() == 0 )
            {
                t_iPeakLineNumber++;
            }
            t_iPeakLineNumber++;
        }
    }

    /**
     * @param mass
     * @param mz
     * @return
     */
    private double getDeviation(double a_dMassAnno, double a_dMassPeak) 
    {
        if ( a_dMassAnno > 0 )
        {
            return ( ( (a_dMassAnno - a_dMassPeak) * 1000000.0 ) / a_dMassAnno );
        }
        else
        {
            return 0;
        }
    }

    /**
     * @param page
     * @param lineNumber
     * @return
     * @throws WriteException 
     * @throws RowsExceededException 
     */
    private int writeHeadline(WritableSheet a_objPage, int a_iLineNumber) throws RowsExceededException, WriteException 
    {
        WritableFont t_objFont = new WritableFont(WritableFont.ARIAL, 
                10, 
                WritableFont.BOLD);
        WritableCellFormat t_objFormat = new WritableCellFormat(t_objFont);
        t_objFormat.setBackground(Colour.LIGHT_ORANGE );
        Label t_objLabel = new Label(0,a_iLineNumber,"Mass");
        a_objPage.addCell(t_objLabel);
        WritableCell t_objCell = a_objPage.getWritableCell(0, a_iLineNumber);
        t_objCell.setCellFormat(t_objFormat);
        t_objLabel = new Label(1,a_iLineNumber,"Intensity");
        a_objPage.addCell(t_objLabel);
        t_objCell = a_objPage.getWritableCell(1, a_iLineNumber);
        t_objCell.setCellFormat(t_objFormat);
        t_objLabel = new Label(2,a_iLineNumber,"Composition");
        a_objPage.addCell(t_objLabel);
        t_objCell = a_objPage.getWritableCell(2, a_iLineNumber);
        t_objCell.setCellFormat(t_objFormat);
        t_objLabel = new Label(3,a_iLineNumber,"Small\r\nmolecules");
        a_objPage.addCell(t_objLabel);
        t_objCell = a_objPage.getWritableCell(3, a_iLineNumber);
        t_objCell.setCellFormat(t_objFormat);
        t_objLabel = new Label(4,a_iLineNumber,"Charged\r\nIon(s)");
        a_objPage.addCell(t_objLabel);
        t_objCell = a_objPage.getWritableCell(4, a_iLineNumber);
        t_objCell.setCellFormat(t_objFormat);
        t_objLabel = new Label(5,a_iLineNumber,"Ion\r\ntype");
        a_objPage.addCell(t_objLabel);
        t_objCell = a_objPage.getWritableCell(5, a_iLineNumber);
        t_objCell.setCellFormat(t_objFormat);
        t_objLabel = new Label(6,a_iLineNumber,"Mass\r\ncalculated");
        a_objPage.addCell(t_objLabel);
        t_objCell = a_objPage.getWritableCell(6, a_iLineNumber);
        t_objCell.setCellFormat(t_objFormat);
        t_objLabel = new Label(7,a_iLineNumber,"Deviation\r\n(ppm)");
        a_objPage.addCell(t_objLabel);
        t_objCell = a_objPage.getWritableCell(7, a_iLineNumber);
        t_objCell.setCellFormat(t_objFormat);
        return 1;
    }

    /**
     * @return
     */
    private ArrayList<CalculationPeak> getPrecursorPeaks() 
    {
        ArrayList<CalculationPeak> t_aResult = new ArrayList<CalculationPeak>();
        CalculationPeak t_objPeak;
        for (Iterator<CalculationPeak> t_iterPeak = this.m_objParameter.getScan().getPeaks().iterator(); t_iterPeak.hasNext();) 
        {
            t_objPeak = t_iterPeak.next();
            if ( this.containsProfileInformation(t_objPeak) )
            {
                t_aResult.add(t_objPeak);                
            }
        }
        return t_aResult;
    }

    /**
     * @return
     */
    private boolean containsProfileInformation(CalculationPeak a_objPeak) 
    {
        for (Iterator<PeakAnnotation> t_iterAnno = a_objPeak.getAnnotation().iterator(); t_iterAnno.hasNext();) 
        {
            if ( t_iterAnno.next().getFragments().size() == 0 )
            {
                return true;
            }            
        }
        return false;
    }

    private void createSettingSheet(WritableSheet a_objPage) throws RowsExceededException, WriteException
    {
        int t_iLineNumber = 0 ;
        Date t_objDate = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern( "yyyy.MM.dd-hh.mm" );
        // Headline
        Label t_objLabel = new Label(0,t_iLineNumber,"Glyco-Peakfinder Excel Report\r\n" + sdf.format(t_objDate) );
        a_objPage.addCell(t_objLabel);
        WritableCell t_objCell = a_objPage.getWritableCell(0, 0);
        WritableFont t_objFont = new WritableFont(WritableFont.ARIAL, 
                18, 
                WritableFont.BOLD);
        WritableCellFormat t_objFormat = new WritableCellFormat(t_objFont);
        t_objFormat.setAlignment( Alignment.CENTRE );
        t_objCell.setCellFormat(t_objFormat);
        t_iLineNumber += 2;
        // mass settings
        t_objLabel = new Label(0,t_iLineNumber,"Spectrum type:");
        a_objPage.addCell(t_objLabel);
        t_objLabel = new Label(1,t_iLineNumber,this.getSpectrumTypeName(this.m_objParameter.getSpectraType()));
        a_objPage.addCell(t_objLabel);
        t_iLineNumber += 2;
        // mass tpye
        t_objLabel = new Label(0,t_iLineNumber,"Mass type:");
        a_objPage.addCell(t_objLabel);
        if ( this.m_objParameter.getMonoisotopic() )
        {
            t_objLabel = new Label(1,t_iLineNumber,"monoisotopic");    
        }
        else
        {
            t_objLabel = new Label(1,t_iLineNumber,"average");
        }
        a_objPage.addCell(t_objLabel);
        t_iLineNumber += 2;        
        // accuracy
        t_objLabel = new Label(0,t_iLineNumber,"Accuracy:");
        a_objPage.addCell(t_objLabel);
        Number t_objNumber = new Number(1,t_iLineNumber,this.m_objParameter.getAccuracy());
        a_objPage.addCell(t_objNumber);
        if ( this.m_objParameter.getAccuracyPpm() )
        {
            t_objLabel = new Label(2,t_iLineNumber,"ppm");
        }
        else
        {
            t_objLabel = new Label(2,t_iLineNumber,"u");
        }
        a_objPage.addCell(t_objLabel);
        t_iLineNumber += 2;
        // mass shift
        if ( this.m_objParameter.getMassShift() != 0 )
        {
            t_objLabel = new Label(0,t_iLineNumber,"Mass shift:");
            a_objPage.addCell(t_objLabel);
            t_objNumber = new Number(1,t_iLineNumber,this.m_objParameter.getMassShift());
            a_objPage.addCell(t_objNumber);
            t_iLineNumber += 2;                
        }
        // persub
        t_objLabel = new Label(0,t_iLineNumber,"Modification of complete structure:");
        a_objPage.addCell(t_objLabel);
        t_objLabel = new Label(1,t_iLineNumber,this.getPersubstTypeName(this.m_objParameter.getPersubstitution()));
        a_objPage.addCell(t_objLabel);
        t_iLineNumber += 2;
        // modification
        t_objLabel = new Label(0,t_iLineNumber,"Reducing end:");
        a_objPage.addCell(t_objLabel);
        for (Iterator<CalculationDerivatisation> t_iterRed = this.m_objParameter.getDerivatisation().iterator(); t_iterRed.hasNext();) 
        {
            CalculationDerivatisation t_objDeri = t_iterRed.next();
            t_objLabel = new Label(1,t_iLineNumber,t_objDeri.getId());
            a_objPage.addCell(t_objLabel);
            t_objNumber = new Number(2,t_iLineNumber,t_objDeri.getMass());
            a_objPage.addCell(t_objNumber);
            t_iLineNumber++;
        }
        t_iLineNumber += 2;
        // peaks
        t_objLabel = new Label(0,t_iLineNumber,"Peaks:");
        a_objPage.addCell(t_objLabel);
        t_objLabel = new Label(1,t_iLineNumber,
                String.format("%d peak(s)", this.m_objParameter.getScan().getPeaks().size()));
        a_objPage.addCell(t_objLabel);
        t_objLabel = new Label(2,t_iLineNumber,this.getMinMaxPeakString());
        a_objPage.addCell(t_objLabel);
        t_iLineNumber++;
        if ( this.m_objParameter.getSpectraType() == SpectraType.MSxMS || this.m_objParameter.getSpectraType() == SpectraType.MS2 )
        {
            if ( this.m_objParameter.getScan().getPrecusorMass() != null )
            {
                t_objLabel = new Label(0,t_iLineNumber,"Precursor:");
                a_objPage.addCell(t_objLabel);
                t_objNumber = new Number(1,t_iLineNumber,this.m_objParameter.getScan().getPrecusorMass());
                a_objPage.addCell(t_objNumber);
            }
        }
        t_iLineNumber += 2;
        // residues
        t_objLabel = new Label(0,t_iLineNumber,"Residues:");
        a_objPage.addCell(t_objLabel);
        for (Iterator<CalculationMolecule> t_iterResidue = this.m_objParameter.getResidues().iterator(); t_iterResidue.hasNext();) 
        {
            CalculationMolecule t_objResidue = t_iterResidue.next();
            t_objLabel = new Label(1,t_iLineNumber,t_objResidue.getId());
            a_objPage.addCell(t_objLabel);
            t_objNumber = new Number(2,t_iLineNumber,t_objResidue.getMass());
            a_objPage.addCell(t_objNumber);
            t_objLabel = new Label(3,t_iLineNumber,
                    String.format("%d - %d",t_objResidue.getMin(),t_objResidue.getMax()));
            a_objPage.addCell(t_objLabel);
            t_iLineNumber++;
        }
        // charged ions
        t_iLineNumber++;
        t_objLabel = new Label(0,t_iLineNumber,"Charged ions:");
        a_objPage.addCell(t_objLabel);
        for (Iterator<CalculationIon> t_iterIons = this.m_objParameter.getIons().iterator(); t_iterIons.hasNext();) 
        {
            CalculationIon t_objIon = t_iterIons.next();
            t_objLabel = new Label(1,t_iLineNumber,t_objIon.getId());
            a_objPage.addCell(t_objLabel);
            t_objNumber = new Number(2,t_iLineNumber,t_objIon.getMass());
            a_objPage.addCell(t_objNumber);
            t_objNumber = new Number(3,t_iLineNumber,t_objIon.getCharge());
            a_objPage.addCell(t_objNumber);
            t_iLineNumber++;
        }
        // charge state
        t_iLineNumber++;
        t_objLabel = new Label(0,t_iLineNumber,"Charge state:");
        a_objPage.addCell(t_objLabel);
        for (Iterator<Integer> t_iterIons = this.m_objParameter.getCharges().iterator(); t_iterIons.hasNext();) 
        {
            t_objNumber = new Number(1,t_iLineNumber,t_iterIons.next());
            a_objPage.addCell(t_objNumber);
            t_iLineNumber++;
        }
        // ion exchange
        if ( this.m_objParameter.getIonExchangeIon().size() > 0 )
        {
            t_iLineNumber++;
            t_objLabel = new Label(0,t_iLineNumber,"Ion exchange ions:");
            a_objPage.addCell(t_objLabel);
            for (Iterator<CalculationIon> t_iterIons = this.m_objParameter.getIonExchangeIon().iterator(); t_iterIons.hasNext();) 
            {
                CalculationIon t_objIon = t_iterIons.next();
                t_objLabel = new Label(1,t_iLineNumber,t_objIon.getId());
                a_objPage.addCell(t_objLabel);
                t_objNumber = new Number(2,t_iLineNumber,t_objIon.getMass());
                a_objPage.addCell(t_objNumber);
                t_objNumber = new Number(3,t_iLineNumber,t_objIon.getCharge());
                a_objPage.addCell(t_objNumber);
                t_iLineNumber++;
            }
            // state
            t_iLineNumber++;
            t_objLabel = new Label(0,t_iLineNumber,"Number of exchanges:");
            a_objPage.addCell(t_objLabel);
            for (Iterator<Integer> t_iterIons = this.m_objParameter.getIonExchangeCount().iterator(); t_iterIons.hasNext();) 
            {
                t_objNumber = new Number(1,t_iLineNumber,t_iterIons.next());
                a_objPage.addCell(t_objNumber);
                t_iLineNumber++;
            }    
        }
        // fragments
        if ( this.m_objParameter.getSpectraType() != SpectraType.Profile )
        {
            t_iLineNumber++;
            t_objLabel = new Label(0,t_iLineNumber,"Fragments:");
            a_objPage.addCell(t_objLabel);
            ArrayList<String> t_aFragments = this.getFragmentList();
            for (Iterator<String> t_iterFrag = t_aFragments.iterator(); t_iterFrag.hasNext();) 
            {
                t_objLabel = new Label(1,t_iLineNumber,t_iterFrag.next());
                a_objPage.addCell(t_objLabel);
                t_iLineNumber++;
            }
        }
        // small molecules
        if ( this.m_objParameter.getGainMolecules().size() > 0 )
        {
            t_iLineNumber++;
            t_objLabel = new Label(0,t_iLineNumber,"Gain of molecule:");
            a_objPage.addCell(t_objLabel);
            for (Iterator<CalculationMolecule> t_iterIons = this.m_objParameter.getGainMolecules().iterator(); t_iterIons.hasNext();) 
            {
                CalculationMolecule t_objMol = t_iterIons.next();
                t_objLabel = new Label(1,t_iLineNumber,t_objMol.getId());
                a_objPage.addCell(t_objLabel);
                t_objNumber = new Number(2,t_iLineNumber,t_objMol.getMass());
                a_objPage.addCell(t_objNumber);
                t_objLabel = new Label(3,t_iLineNumber,
                        String.format("%d - %d",t_objMol.getMin(),t_objMol.getMax()));
                a_objPage.addCell(t_objLabel);
                t_iLineNumber++;
            }    
        }
        if ( this.m_objParameter.getLossMolecules().size() > 0 )
        {
            t_iLineNumber++;
            t_objLabel = new Label(0,t_iLineNumber,"Lose of molecule:");
            a_objPage.addCell(t_objLabel);
            for (Iterator<CalculationMolecule> t_iterIons = this.m_objParameter.getLossMolecules().iterator(); t_iterIons.hasNext();) 
            {
                CalculationMolecule t_objMol = t_iterIons.next();
                t_objLabel = new Label(1,t_iLineNumber,t_objMol.getId());
                a_objPage.addCell(t_objLabel);
                t_objNumber = new Number(2,t_iLineNumber,t_objMol.getMass());
                a_objPage.addCell(t_objNumber);
                t_objLabel = new Label(3,t_iLineNumber,
                        String.format("%d - %d",t_objMol.getMin(),t_objMol.getMax()));
                a_objPage.addCell(t_objLabel);
                t_iLineNumber++;
            }    
        }
    }

    private ArrayList<String> getFragmentList() 
    {
        ArrayList<String> t_aList = new ArrayList<String>();
        for (Iterator<CalculationFragment> t_iterFrag = this.m_objParameter.getFragmentsRed().iterator(); t_iterFrag.hasNext();) 
        {
            CalculationFragment t_objFrag = t_iterFrag.next();
            if ( !t_aList.contains(t_objFrag.getFragmentType()) )
            {
                t_aList.add(t_objFrag.getFragmentType());
            }
        }
        for (Iterator<CalculationFragment> t_iterFrag = this.m_objParameter.getFragmentsNonRed().iterator(); t_iterFrag.hasNext();) 
        {
            CalculationFragment t_objFrag = t_iterFrag.next();
            if ( !t_aList.contains(t_objFrag.getFragmentType()) )
            {
                t_aList.add(t_objFrag.getFragmentType());
            }
        }
        return t_aList;
    }

    private String getMinMaxPeakString() 
    {
        double t_dMin = 0;
        double t_dMax = 0;
        boolean t_bFirst = true;
        for (Iterator<CalculationPeak> t_iterPeak = this.m_objParameter.getScan().getPeaks().iterator(); t_iterPeak.hasNext();) 
        {
            CalculationPeak    t_objPeak = t_iterPeak.next();
            if ( t_bFirst )
            {
                t_dMax = t_objPeak.getMz();
                t_dMin = t_objPeak.getMz();
                t_bFirst = false;
            }
            else
            {
                if ( t_dMax < t_objPeak.getMz() )
                {
                    t_dMax = t_objPeak.getMz();
                }
                if ( t_dMin > t_objPeak.getMz() )
                {
                    t_dMin = t_objPeak.getMz();
                }
            }
        }
        return String.format("%.4f - %.4f", t_dMin,t_dMax);
    }

    private String getPersubstTypeName(Persubstitution persubstitution) 
    {
        return persubstitution.getName();
    }

    private String getSpectrumTypeName(SpectraType a_enumSpectraType) 
    {
        return a_enumSpectraType.getName();
    }
}
