Upgrade from "ebi_2nd_dec_2009" to r1804. (contact david@nixbioinf.org for help with this upgrade)

You can either do a clean upgrade from a fresh install of a database dump, or apply this upgrade to an existing database.
-----------------------------------------------------------
Clean upgrade (recommended)
a)Download the database dump taken by the EBI on 2nd December 2009 and copy it to the file ${project.dir}/database/data/ebi_2nd_dec_2009.sql.  This dump is currently only available on request from the developers, please email david@nixbioinf.org to obtain the file.
>cd ${project.dir}
b)Run the ant task upgrade-ebi_2nd_dec_2009-r1804-clean
>cd database/scripts/org/eurocarb/database/upgrade/
>ant upgrade-ebi_2nd_dec_2009-r1804-clean
c)Enter the password as prompted, which is "flipper" unless you have changed it.

The new system requires new tables for the NCBI Entrez Taxonomy and UniProtKB, these tables can take several hours to build.  To skip their construction add the property "skip.long" to the ant command.
>ant task upgrade-ebi_2nd_dec_2009-r1804-clean -Dskip.long=1
------------------------------------------------------------

------------------------------------------------------------
Patches an existing database
a)Run the ant task upgrade-ebi_2nd_dec_2009-r1804-schema 
>ant upgrade-ebi_2nd_dec_2009-r1804-schema
c)Enter the password as prompted, which is "flipper" unless you have changed it.

The new system requires new tables for the NCBI Entrez Taxonomy and UniProtKB, these tables can take several hours to build.  To skip their construction you can call upgrade-ebi_2nd_dec_2009-r1804-schema-only instead
>ant upgrade-ebi_2nd_dec_2009-r1804-schema-only
------------------------------------------------------------

Updates:
a)Extraction of the one-to-many relationship between bc and contributor onto separate tables, to aid Java code.
  
Unfortunately seven tables depend on the column biological_context.biological_context_id; data within the dependent tables is backed up as the upgrade proceeds and restored with the correct biological_context_id.  Duplicate rows are detected in a dependent table as those which contain identical values for all columns except for, date_entered (if present) and biological_context_id; as long as the biological contexts refered to only differ by comment,date_entered or contributor id.  Therefore a duplicate biological context is detected when the taxonomy_id and tissue_taxonomy_id are identical.  If multiple rows are to be merged, the value for the date_entered field (if present) is taken to be the minimum date avaiable for the merged rows.

b)Rows are merged which have been contributed by a contributor with the value "guest" for contributor_name, but have different contributor_ids.   Or put another way, those rows which are identical except for contributor_id for-which the associated contributor_name is "guest" for them all are merged into one row.
