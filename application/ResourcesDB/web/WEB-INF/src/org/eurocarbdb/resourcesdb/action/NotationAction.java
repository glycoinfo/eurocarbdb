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
package org.eurocarbdb.resourcesdb.action;

public class NotationAction extends MsdbDefaultAction {

    private static final long serialVersionUID = 1L;
    
    public static final String TOPIC_MONOSACC = "monosacc";
    public static final String TOPIC_BASETYPE = "basetype";
    public static final String TOPIC_SUBST = "subst";
    public static final String TOPIC_AGLYCA = "aglyca";
    public static final String TOPIC_SCHEMES = "schemes";
    public static final String TOPIC_START = "start";
    public static final String TOPIC_COREMOD = "coremod";
    public static final String TOPIC_STEREOCODE = "stereocode";
    public static final String TOPIC_LINKTYPE = "linktype";
    
    private String topic = null;
    
    public String getTopic() {
        return topic;
    }

    public void setTopic(String theTopic) {
        if(theTopic != null) {
            theTopic = theTopic.toLowerCase();
        }
        this.topic = theTopic;
    }
    
    String testParam;
    
    public String getTestParam() {
        return this.testParam;
    }
    
    public void setTestParam(String valueStr) {
        this.testParam = valueStr;
    }
    
    public String execute() {
        this.setMainMenuItems();
        this.setSubMenuItems(EMenu.NOTATION);
        if(this.getTopic() == null) {
            this.setTopic(TOPIC_START);
        }
        if(this.getTopic().equals(TOPIC_MONOSACC)) {
            this.setTitle(MSDB_TITLE + " - Monosaccharide Notation");
            this.setCurrentSubMenuItem(EMenu.NOTATION_MONOSACC);
            return TOPIC_MONOSACC;
        }
        if(this.getTopic().equals(TOPIC_BASETYPE) || this.getTopic().equals(TOPIC_COREMOD) || this.getTopic().equals(TOPIC_STEREOCODE)) {
            this.setTitle(MSDB_TITLE + " - Monosaccharide Basetype Notation");
            this.setCurrentSubMenuItem(EMenu.NOTATION_BASETYPE);
            return TOPIC_BASETYPE;
        }
        if(this.getTopic().equals(TOPIC_SUBST) || this.getTopic().equals(TOPIC_LINKTYPE)) {
            this.setTitle(MSDB_TITLE + " - Substituent Notation");
            this.setCurrentSubMenuItem(EMenu.NOTATION_SUBST);
            return TOPIC_SUBST;
        }
        /*if(this.getTopic().equals(TOPIC_AGLYCA)) {
            this.setTitle(MSDB_TITLE + " - Aglycon Notation");
            this.setCurrentSubMenuItem(EMenu.NOTATION_AGLYCA);
            return TOPIC_AGLYCA;
        }*/
        if(this.getTopic().equals(TOPIC_SCHEMES)) {
            this.setTitle(MSDB_TITLE + " - Notation Schemes");
            this.setCurrentSubMenuItem(EMenu.NOTATION_SCHEMES);
            return TOPIC_SCHEMES;
        }
        this.setTitle(MSDB_TITLE + " - Notation");
        this.setCurrentSubMenuItem(EMenu.NOTATION_START);
        return TOPIC_START;
    }
}
