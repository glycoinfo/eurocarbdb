
Welcome to the EUROCarb project!

The Eurocarb project is an EU-funded initiative for the development of
software for the collection, processing, and analysis of carbohydrate
structures and experimental data. The project includes various standalone 
carbohydrate tools and resources, as well as a unifying web-based application.

The canonical public version of Eurocarb is available at http://www.ebi.ac.uk/eurocarb

The epicentre of development of Eurocarb revolves around http://code.google.com/p/eurocarb


Licensing
---------
All code and data in this distribution is licensed under the terms of the open source
Lesser Gnu Public License version 3.0 (LGPL), which is included in this distribution.
See LICENSE.txt for details. As a courtesy, if you download/use/extend the source version 
we'd love to hear to about it.


Documentation
-------------
The best place to start is the googlecode project wiki, at http://code.google.com/p/eurocarb


Requirements
------------
Installation and compilation of Eurocarb requires Java version 1.5+ (http://java.sun.com), 
Ant (http://ant.apache.org), and the open source relational database PostgreSQL version 7.3+
(http://postgres.org).


How to create a local/private version
-------------------------------------
These instructions are for the code + data. If you just want the data, check the
wiki and downloads area at http://code.google.com/p/eurocarb/downloads/list

At present, the best way to get Eurocarb is to checkout a source version
and build it locally. The choices are:

1) Checkout a release branch. Work out which branch you want first by surfing 
to http://code.google.com/p/eurocarb/source/browse/#svn/branches, then:
> svn checkout http://eurocarb.googlecode.com/svn/branches/[version-you-want] eurocarb-[version]

2) Checkout the main development branch. Note that while this is always the most
up-to-date version, it may be unstable from time-to-time.
> svn checkout http://eurocarb.googlecode.com/svn/trunk eurocarb-trunk

Note that in both cases these will be read-only checkouts. You'll need to have 
a google account and be accepted by one of the project admins as a project 
developer in order to be able to commit changes back to the project.


Installation instructions
-------------------------




Authors
-------

Matt Harrison (mjh, glycoslave): 

    * primary author of core-api, sugar-api, and EurocarbDB webapp

Alessio Ceroni (aceroni): 

    * primary author of GlycanBuilder, GlycoWorkBench
    * primary author of core-api mass spec module
    * significant contributor to EurocarbDB webapp in general
    
Matthew Campbell

    * primary author of Eurocarbdb HPLC module
    
Hiren Joshi

    * contributor to core-api and EurocarbDB webapp

Magnus Lundborg

    * primary author of NMR tools for Eurocarbdb webapp

Thomas Lutteke

    * primary author of ResourcesDB 
    
Rene Ranziger

    * primary author of GlycoPeakFinder and MolecularFramework library

Kai Maass

    * contributor to ResourcesDB and EurocarbDB mass spec module

Siegfried Schloissnig

    * primary author of core-api NMR module
    
===
$Version: $

