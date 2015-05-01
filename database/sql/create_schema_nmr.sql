CREATE SCHEMA nmr;

CREATE TABLE nmr.ccpn_project
(
  id serial NOT NULL,
  project oid,
  CONSTRAINT pk_ccpn_project PRIMARY KEY (id)
)
WITHOUT OIDS;

CREATE TABLE nmr.experiment
(
  id serial NOT NULL,
  id_spectrometer integer,
  "name" character varying,
  dimensions integer,
  "comment" character varying,
  id_nmr_project integer
)
WITHOUT OIDS;

CREATE TABLE nmr.link
(
  id serial NOT NULL,
  "name" character varying,
  id_link_next integer,
  CONSTRAINT pk_link PRIMARY KEY (id)
)
WITHOUT OIDS;

CREATE TABLE nmr.nmr_evidence
(
  evidence_id integer NOT NULL,
  ccpn_project_id integer,
  nmr_evidence_id serial NOT NULL,
  CONSTRAINT fk_ccpn_project_id FOREIGN KEY (ccpn_project_id)
      REFERENCES nmr.ccpn_project (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITHOUT OIDS;

CREATE TABLE nmr.nmr_project
(
  id serial NOT NULL,
  id_project integer
)
WITHOUT OIDS;

CREATE TABLE nmr.project
(
  id serial NOT NULL,
  "name" character varying,
  id_ccpn_project integer,
  CONSTRAINT pk_project PRIMARY KEY (id)
)
WITHOUT OIDS;

CREATE TABLE nmr.residue
(
  id serial NOT NULL,
  id_project integer NOT NULL,
  "name" character varying,
  moltype character varying,
  id_link integer,
  CONSTRAINT pk_residue PRIMARY KEY (id),
  CONSTRAINT fk_link FOREIGN KEY (id_link)
      REFERENCES nmr.link (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_project FOREIGN KEY (id_project)
      REFERENCES nmr.project (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITHOUT OIDS;

CREATE TABLE nmr.shift
(
  id serial NOT NULL,
  "value" double precision,
  error double precision,
  atom character varying,
  id_residue integer NOT NULL,
  CONSTRAINT pk_shift PRIMARY KEY (id),
  CONSTRAINT fk_residue FOREIGN KEY (id_residue)
      REFERENCES nmr.residue (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITHOUT OIDS;

CREATE TABLE nmr.spectrometer
(
  id serial NOT NULL,
  "name" character varying,
  frequency double precision
)
WITHOUT OIDS;

