CREATE UNIQUE INDEX taxonomy_ncbi_unique ON core.taxonomy (ncbi_id);

/**
I've dropped the constraint checks here, as the two tables may be using different versions of the NET
This is only a temporary solution.
*/
CREATE TABLE  core.relationships_taxonomy (
  ncbi_id INT NOT NULL, 
  rank VARCHAR(200) NOT NULL, 
  ncbi_id_taxa INT NOT NULL, 
  relationship VARCHAR(200) NOT NULL, 
  position INT NOT NULL,
  PRIMARY KEY(ncbi_id,ncbi_id_taxa)
);