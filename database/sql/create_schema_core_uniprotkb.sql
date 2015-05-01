CREATE TABLE core.Static_taxonomy_sk_proteome_ranking (
  ncbi_id INT PRIMARY KEY NOT NULL, /**REFERENCES core.taxonomy(ncbi_id),*/
  ncbi_id_sk INT NOT NULL ,/*REFERENCES core.taxonomy(ncbi_id),*/
  rank_pos INT NOT NULL
);

CREATE TABLE core.Static_taxonomy_proteome_ranking (
  ncbi_id INT PRIMARY KEY NOT NULL, /**REFERENCES core.taxonomy(ncbi_id),*/
  rank_pos INT NOT NULL
);

