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

package org.eurocarbdb.application.glycoworkbench.plugin.peakpicker;
import org.eurocarbdb.application.glycanbuilder.*;
import org.eurocarbdb.application.glycoworkbench.*;

/**
   A base class for all classes handling default parameters.
     
     This class facilitates the handling of parameters:
     - it manages default parameter (defaults_)
     - it checks for wrong/misspelled parameters
     - subsections that are passed to other classes can be excluded from the check (subsections_)
     - it keeps member variables in syncronicity with the parameters stored in param_
     - it helps to automatically create a doxygen documentation page for the parameters
     
     Extra member variables are needed if getting the value from param_ would be too slow
     e.g. when they are used in methods that are called very often.
     
     No matter if you have extra variables or not, do the following:
     - Set defaults_ and subsections_ in the derived classes' default constructor.
     - Call defaultsToParam_() at the end of derived classes' default constructor.
     It copies the defaults to param_ (and calls updateMembers_()).
     
     If you have extra member variables you need to syncronize with param_, do the following:
     - Implement the updateMembers_() method. It is used after each change of param_
     in order to update the extra member variables. If the base class is a DefaultParamHandler as well
     make sure to call the updateMembers_() method of the base class in the updateMembers_() method.
     - Call updateMembers_() at the end of the derived classes' copy constructor and assignment operator.
     - If you need mutable access to the extra member variables, provide a set-method and make sure to set
     the corresponding value in param_ as well!
        
     @b Base @b classes: @n
     If you create a class @a A that is derived from DefaultParamHandler and derive another class @a B
     for @a A, you should set use the setName(String) method to set the name used for error messages to @a B.
     
     @b Parameter @b documentation: @n
     Each default parameter has to be documented in a comprehensive way. This is done using the
     Param::setValue methods and the Param::setDescription method.
     
     In order to avoid having to document the parameters in the code and in the doxygen documentation,
     there is a mechanism for creating the doxygen documentation automatically. You simply have to add your
     class to the program @a OpenMS/doc/doxygen/parameters/DefaultParamHandlerDocumenter.C. This program
     generates a doxygen subpage for the parameters. The subpage can than be included into the 
     class documentation using the following doxygen command:
     @code
     @ref <class name>_Parameters
     @endcode
     You can test if everything worked by calling @a make @a paramdoc in @a OpenMS/doc/.
     The parameters documentation is written to @a OpenMS/docdoxygen/parameters/DefaultParameters.doxygen.
     
     @todo Transform the docuementation to a tree, as soon as the internal representation in Param is a tree as well (Marc, Stefan Rink)
     
     @ingroup Datastructures
*/
    
import java.util.*;

public class DefaultParamHandler {
   

    ///Container for current parameters
    protected Param param_;

    /**
       Container for default paramters. This member should be filled in the constructor of derived classes!       
       @note call the setParam_() method at the end of the constructor in order to copy the defaults to param_.
    */
    protected Param defaults_;

    /**
       Container for registered subsections. This member should be filled in the constructor of derived classes!
       @note Do not add a ':' character at the end of subsections.
    */
    protected Vector<String> subsections_;

    /// Name that is displayed in error messages during the parameter checking
    protected String error_name_;

    /**
       If this member is set to false no checking if parameters in done;
       The only reason to set this member to false is that the derived class has no parameters!
    */
    protected boolean check_defaults_;

    // ------------------------

    
    /// Constructor with name that is diplayed in error messages
    public DefaultParamHandler(String name) {
    param_ = new Param();
    defaults_ = new Param();
    subsections_ = new Vector<String>();
    error_name_ = name;
    check_defaults_ = true;
    }

    /// Copy constructor
    public DefaultParamHandler clone() {
    DefaultParamHandler ret = new DefaultParamHandler(this.error_name_);
    ret.copy(this);
    return ret;
    }

    /// Assignment operator.
    public void copy(DefaultParamHandler rhs) {
    this.param_.copy(rhs.param_);
    this.defaults_.copy(rhs.defaults_);
    this.subsections_ = (Vector<String>)rhs.subsections_.clone();
    error_name_ = rhs.error_name_;
    check_defaults_ = rhs.check_defaults_;
    }

    /// Equality operator
    public boolean equals(DefaultParamHandler rhs) {
    return (param_.equals(rhs.param_) &&
        defaults_.equals(rhs.defaults_) &&
        subsections_.equals(rhs.subsections_) &&
        error_name_.equals(rhs.error_name_) &&
        check_defaults_ == rhs.check_defaults_);
    }

    /**
       Sets the parameters       
       It also applies the default parameters to @p param and checks for unknown parameters.
    */
    public void setParameters(Param param) {
    //set defaults and apply new parameters
    Param tmp = param.clone();
    tmp.setDefaults(defaults_);
    param_ = tmp;
        
    if (check_defaults_) {
        if (defaults_.size()==0) {
        System.out.println("Warning: no default parameters for DefaultParameterHandler '" + error_name_ + "' specified!");
        }
            
        //remove registered subsections
        for(String s : subsections_ )            
        tmp.remove(s + ':');    
            
        //check defaults
        tmp.checkDefaults(error_name_,defaults_);
    }
        
    //do necessary changes to other member variables
    updateMembers_();    
    }    

    /// Non-mutable access to the parameters
    public Param getParameters() {
    return param_;
    }

    /// Non-mutable access to the default parameters
    public Param getDefaults() {
    return defaults_;
    }
    
    /// Non-mutable access to the name
    public String getName() {
    return error_name_;
    }

    /// Mutable access to the name
    public void setName(String name) {
    error_name_ = name;
    }

    /// Non-mutable access to the registered subsections
    public Vector<String> getSubsections() {
    return subsections_;
    }

    /**
       This method is used to update extra member variables at the end of the setParam() method.       
       Also call it at the end of the derived classes' copy constructor and assignment operator.       
       The default implementation is empty.
    */
    
    protected void updateMembers_() {
    }

    ///Updates the parameters after the defaults have been set in the constructor
    protected void defaultsToParam_() {
    //check if a description id given for all defaults
    boolean description_missing = false;
    String missing_parameters = "";
    for(Map.Entry<String,Object> e : defaults_.getValues().entrySet() ) {        
        //cout << "Name: " << it->first << endl;
        if( defaults_.getDescription(e.getKey()).length()==0 ) {
        description_missing = true;
        missing_parameters += e.getKey()+",";
        break;
        }
    }
    if (description_missing) {
        System.out.println( "Warning: no default parameter description for parameters '" + missing_parameters + "' of DefaultParameterHandler '" + error_name_ + "' given!");
    }
    
    param_.setDefaults(defaults_);
    updateMembers_();    
    }

    public void store(Configuration config) {
    param_.store(error_name_,config);
    }

    public void retrieve(Configuration config) {
    param_.retrieve(error_name_,config);
    updateMembers_();
    }


}