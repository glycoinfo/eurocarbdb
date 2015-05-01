File: /database/README.txt

The contents of this directory deals with the creation, population, and 
maintenance of the actual database of Eurocarb.

Directory structure:

---
/database

    /conf
        Various property files that control or alter how the DB is created
        and installed.
        
    /data
        Directory for downloaded reference data. This directory should be
        writeable by the user that Tomcat runs as in order for automatic
        downloading & updating of reference data to work.
    
    /doc
        Directory for various design and usage documents relating to the database.
        
    /scripts
        Various utility scripts for manipulating the database.
        
    /sql
        The SQL used to create the various schema comprising EurocarbDB
        live here. These files should not be modified lightly ;-)
---

Other files:

Files in /database with the extension *.txt relate to the most recent
installation of the database. They are mainly intended for debugging.



        
        
        
