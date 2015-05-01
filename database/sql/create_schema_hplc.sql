--
-- PostgreSQL database dump
--

-- Started on 2007-02-19 16:42:54 GMT Standard Time

--
-- TOC entry 7 (class 2615 OID 25471)
-- Name: hplc_schema; Type: SCHEMA; Schema: -; Owner: root
--
-- author: matthew.campbell@nibrt.ie
--

CREATE SCHEMA hplc;


CREATE TABLE hplc.autogu (
    autogu_id serial PRIMARY KEY NOT NULL,
    glycan_id integer,
    product_id integer,
    enzyme varchar(10),
    profile_id integer,
    refined_id integer,
    digest_id integer
);


CREATE TABLE hplc.digest_single (
    glycan_id integer NOT NULL,
    name VARCHAR(250) NOT NULL,
    enzyme VARCHAR(250) NOT NULL,
    product_id integer NOT NULL,
    id serial PRIMARY KEY NOT NULL 
);

/*
Temp file use by autogu
*/
CREATE TABLE hplc.dis_refine (
    dis_ref_id serial PRIMARY KEY NOT NULL,
    profile_id integer,
    digest_id integer,
    glycan_id integer,
    enzyme VARCHAR(50),
    product VARCHAR(50),
    product_id integer
);

/*Temp table*/
CREATE TABLE hplc.disappeared (
    disappeared_id serial PRIMARY KEY NOT NULL,
    profile_id integer,
    digest_id integer,
    assigned_peak integer,
    peak_area double precision,
    gu double precision,
    db_gu double precision,
    name_abbreviation VARCHAR(50),
    glycan_id integer,
    refined integer,
    enzyme VARCHAR(50)
);




CREATE TABLE hplc.column (
    column_id               SERIAL PRIMARY KEY NOT NULL,
    manufacturer            VARCHAR(150) NOT NULL,
    model		    VARCHAR(100) NOT NULL,
    packing_material        VARCHAR(100) NOT NULL,
    column_size_width       DOUBLE PRECISION NOT NULL,
    column_size_length      DOUBLE PRECISION NOT NULL,
    particle_size           VARCHAR(20) NOT NULL
);


--
-- TOC entry 1221 (class 1259 OID 25563)
-- Dependencies: 7
-- Name: detector; Type: TABLE; Schema: hplc_schema; Owner: root; Tablespace: 
--

CREATE TABLE hplc.detector (
    detector_id             SERIAL PRIMARY KEY NOT NULL,
    manufacturer            VARCHAR(150),
    model                   VARCHAR(50),
    --manufacturer_id         INTEGER NOT NULL,
    --model_id                INTEGER NOT NULL,
    excitation              SMALLINT,
    emission                SMALLINT,
    bandwidth               DOUBLE PRECISION,
    sampling_rate           SMALLINT
);


--
-- TOC entry 1223 (class 1259 OID 25570)
-- Dependencies: 1566 1567 1568 1569 1570 1571 1572 1573 1574 1575 7
-- Name: digest; Type: TABLE; Schema: hplc_schema; Owner: root; Tablespace: 
--

CREATE TABLE hplc.digest (
    digest_id               SERIAL PRIMARY KEY NOT NULL,
    name                    VARCHAR(50),
    enzyme_one              VARCHAR(20) DEFAULT '0'::VARCHAR NOT NULL,
    target_one              INTEGER DEFAULT 0 NOT NULL,
    enzyme_two              VARCHAR(20) DEFAULT '0'::VARCHAR NOT NULL,
    target_two              INTEGER DEFAULT 0 NOT NULL,
    enzyme_three            VARCHAR(20) DEFAULT '0'::VARCHAR NOT NULL,
    target_three            INTEGER DEFAULT 0 NOT NULL,
    enzyme_four             VARCHAR(20) DEFAULT '0'::VARCHAR NOT NULL,
    target_four             INTEGER DEFAULT 0 NOT NULL,
    enzyme_five             VARCHAR(20) DEFAULT '0'::VARCHAR NOT NULL,
    target_five             INTEGER DEFAULT 0 NOT NULL,
    glycan_id		    INTEGER
);



--
-- TOC entry 1227 (class 1259 OID 25592)
-- Dependencies: 7
-- Name: enzyme; Type: TABLE; Schema: hplc_schema; Owner: root; Tablespace: 
--

CREATE TABLE hplc.enzyme (
    enzyme_id               SERIAL PRIMARY KEY NOT NULL,
    name                    VARCHAR(100) NOT NULL,
    abbreviation_id         VARCHAR(20) NOT NULL UNIQUE,
    accession_number        VARCHAR(50) NOT NULL UNIQUE,
    description             VARCHAR(200) NOT NULL,
    supplier                VARCHAR(100)
);


--
-- TOC entry 1229 (class 1259 OID 25599)
-- Dependencies: 7
-- Name: glycan; Type: TABLE; Schema: hplc_schema; Owner: root; Tablespace: 
--

CREATE TABLE hplc.glycan (
    glycan_id               SERIAL PRIMARY KEY NOT NULL,
    name                    VARCHAR(50) NOT NULL,
    gu                      DOUBLE PRECISION NOT NULL,
    ms                      VARCHAR(5),
    ms_ms                   VARCHAR(5),
    hex                     SMALLINT,
    hexnac                  SMALLINT,
    neunac                  SMALLINT,
    fucose                  SMALLINT,
    xylose                  SMALLINT,
    neugc                   SMALLINT,
    a1                      SMALLINT,
    a2                      SMALLINT,
    a3                      SMALLINT,
    a4                      SMALLINT,
    s                       SMALLINT,
    f_6                     SMALLINT,
    b                       SMALLINT,
    bgal                    SMALLINT,
    agal                    SMALLINT,
    galnac                  SMALLINT,
    polylac                 SMALLINT,
    fouterarm               SMALLINT,
    hybrid                  SMALLINT,
    mannose                 SMALLINT,
    all_groups              SMALLINT,
    plant                   SMALLINT,
    serum                   SMALLINT,
    normal_igg              SMALLINT,
    std			    FLOAT,
    ogbitranslation         INTEGER 
);


--
-- TOC entry 1231 (class 1259 OID 25604)
-- Dependencies: 7
-- Name: instrument; Type: TABLE; Schema: hplc_schema; Owner: root; Tablespace: 
--

CREATE TABLE hplc.instrument (
    instrument_id           SERIAL PRIMARY KEY NOT NULL,
    manufacturer            VARCHAR(150) NOT NULL,
    model                   VARCHAR(50) NOT NULL,
    temperature             DOUBLE PRECISION,
    solvent_a               VARCHAR(100),
    solvent_b               VARCHAR(100),
    solvent_c               VARCHAR(100),
    solvent_d               VARCHAR(100),
    flow_rate               DOUBLE PRECISION,
    flow_gradient           VARCHAR(50)
);



--

-- TOC entry 1234 (class 1259 OID 25615)
-- Dependencies: 7
-- Name: profile; Type: TABLE; Schema: hplc_schema; Owner: root; Tablespace: 
--

CREATE TABLE hplc.profile (
    profile_id              SERIAL PRIMARY KEY NOT NULL,
 
    evidence_id             INT REFERENCES core.evidence
                            ON UPDATE CASCADE ON DELETE CASCADE,

    parent_profile_id       INTEGER 
                            REFERENCES hplc.profile
                            ON DELETE CASCADE ON UPDATE CASCADE
                            INITIALLY DEFERRED,
    instrument_id           INTEGER NOT NULL
                            REFERENCES hplc.instrument,
    column_id               INTEGER --NOT NULL
                            REFERENCES hplc.column,
    detector_id             INTEGER NOT NULL
                            REFERENCES hplc.detector,
    
    acq_sw_version          VARCHAR(50) NOT NULL,
    operator                VARCHAR(100) NOT NULL,
    date_acquired           DATE NOT NULL,
    dextran_standard        VARCHAR(50) NOT NULL,
    sequential_digest       VARCHAR(100),
    user_comments           VARCHAR(255),
    wax_undigested          VARCHAR(4)
);


CREATE TABLE hplc.method_run
(
  method_run_id serial PRIMARY KEY NOT NULL,
  profile integer NOT NULL
          REFERENCES hplc.profile,
         -- ON DELETE CASCADE ON UPDATE CASCADE,
  temperature double precision,
  solvent_a character varying(100),
  solvent_b character varying(100),
  solvent_c character varying(100),
  solvent_d character varying(100),
  flow_rate double precision,
  flow_gradient character varying(50)
);
    
--
-- TOC entry 1225 (class 1259 OID 25585)
-- Dependencies: 7
-- Name: digest_profile; Type: TABLE; Schema: hplc_schema; Owner: root; Tablespace: 
--

CREATE TABLE hplc.digest_profile (
    digest_profile_id       SERIAL PRIMARY KEY NOT NULL,
    digest_id		    INTEGER,
    profile_id              INTEGER NOT NULL,
                            -- REFERENCES hplc.profile,
    acq_sw_version          VARCHAR(50), --NOT NULL,
    operator                VARCHAR(100), -- NOT NULL,
    date_acquired           DATE, -- NOT NULL,
    dextran_standard        VARCHAR(50), -- NOT NULL,
    sequential_digest       VARCHAR(100),
    wax_digested            VARCHAR(4),
    wax_undigested          VARCHAR(4),
    neutral_separation      VARCHAR(4),
    mono_separation         VARCHAR(4),
    di_separation           VARCHAR(4),
    tri_separation          VARCHAR(4),
    tetra_separation        VARCHAR(4),
    user_comments           VARCHAR(250),
    
    UNIQUE( digest_profile_id)
);



--
-- TOC entry 1232 (class 1259 OID 25609)
-- Dependencies: 7
-- Name: integration_method; Type: TABLE; Schema: hplc_schema; Owner: root; Tablespace: 
--

CREATE TABLE hplc.integration_method (
    integration_method      SERIAL PRIMARY KEY NOT NULL,
    profile_id              INTEGER NOT NULL
                            REFERENCES hplc.profile,
    digest_id               INTEGER NOT NULL
                            REFERENCES hplc.digest,
    
    /** End retention time */
    rt_range_end            SMALLINT NOT NULL,
    
    /** Start retention time */
    rt_range_start          SMALLINT NOT NULL,
    peak_width              DOUBLE PRECISION NOT NULL,
    peak_threshold          DOUBLE PRECISION,
    peak_min_height         DOUBLE PRECISION NOT NULL,
    peak_min_area           DOUBLE PRECISION NOT NULL,
    calibration_curve_type  VARCHAR(100) NOT NULL,
    
    UNIQUE( profile_id, digest_id )
);



--
-- TOC entry 1235 (class 1259 OID 25620)
-- Dependencies: 7
-- Name: hplc_peaks_annotated; Type: TABLE; Schema: hplc_schema; Owner: root; Tablespace: 
--

CREATE TABLE hplc.hplc_peaks_annotated (
    hplc_peaks_annotated_id SERIAL PRIMARY KEY NOT NULL,
--    evidence_id             INT REFERENCES core.evidence
--                            ON UPDATE CASCADE ON DELETE CASCADE,
                            
    profile_id              INTEGER NOT NULL,
                         --   REFERENCES hplc.profile,
    digest_id               INTEGER NOT NULL,
                    --        REFERENCES hplc.digest,
                            
    /** User-assigned peak number */
    assigned_peak           INTEGER,
    peak_area               DOUBLE PRECISION,
    gu                      DOUBLE PRECISION NOT NULL,
    db_gu                   DOUBLE PRECISION NOT NULL, 
    name_abbreviation       VARCHAR NOT NULL,
    glycan_id               INTEGER,
                         --   REFERENCES hplc.glycan,
    refined		    INTEGER
    
    
);


--
-- TOC entry 1236 (class 1259 OID 25624)
-- Dependencies: 7
-- Name: hplc_peaks_integrated; Type: TABLE; Schema: hplc_schema; Owner: root; Tablespace: 
--

CREATE TABLE hplc.hplc_peaks_integrated (
    hplc_peaks_integrated_id SERIAL PRIMARY KEY NOT NULL,
    profile_id              INTEGER NOT NULL
                            REFERENCES hplc.profile,
    digest_id               INTEGER 
                            REFERENCES hplc.digest,

    /** User-assigned peak number */
    assigned_peak           INTEGER,
    peak_area               DOUBLE PRECISION NOT NULL,
    gu                      INTEGER NOT NULL,
    
    UNIQUE( profile_id, digest_id )
);


--
-- TOC entry 1237 (class 1259 OID 25628)
-- Dependencies: 7
-- Name: pictorial_representation; Type: TABLE; Schema: hplc_schema; Owner: root; Tablespace: 
--

CREATE TABLE hplc.pictorial_representation (
    glycan_id               INTEGER PRIMARY KEY NOT NULL,
    pictorial_representation VARCHAR(50)
);


--
-- TOC entry 1238 (class 1259 OID 25630)
-- Dependencies: 7
-- Name: profile_data; Type: TABLE; Schema: hplc_schema; Owner: root; Tablespace: 
--

CREATE TABLE hplc.profile_data (
    profile_data_id         SERIAL PRIMARY KEY NOT NULL,
    profile_id              INTEGER NOT NULL
                            REFERENCES hplc.profile,
    digest_id               INTEGER NOT NULL
                            REFERENCES hplc.digest,
    x_coord                 DOUBLE PRECISION,
    y_coord                 DOUBLE PRECISION
);


--
-- TOC entry 1241 (class 1259 OID 25644)
-- Dependencies: 7
-- Name: ref_link; Type: TABLE; Schema: hplc_schema; Owner: root; Tablespace: 
--

CREATE TABLE hplc.ref_link (
    ref_id                  INTEGER NOT NULL,
    glycan_id               INTEGER NOT NULL,
--                            REFERENCES hplc.glycan,
    paper_gu                DOUBLE PRECISION,
    ms                      VARCHAR(5),
    ms_ms                   VARCHAR(5)
);


 CREATE TABLE hplc.ref (
     ref_id                  INTEGER PRIMARY KEY NOT NULL,
     author		            VARCHAR(255),
     title                   VARCHAR(255),
     journal                 VARCHAR(255),
     abstract	            TEXT,
     pub_year                SMALLINT,
     pub_date                VARCHAR(12),
     volume                  VARCHAR(25),
     issue                   VARCHAR(10),
     pages                   VARCHAR(20),
     med_ui                  INTEGER,
     ogbi_id	                SMALLINT NOT NULL

 );

CREATE TABLE hplc.ref_tax_link
(
  ref_tax_id serial NOT NULL,
  ogbi_link_id integer NOT NULL,
  mesh_pert_id integer,
  mesh_disease_id integer,
  mesh_tissue_id integer,
  tax_order_id integer,
  tax_species_id integer,
  pert_name character varying(250),
  disease_name character varying(250),
  tissue_name character varying(250),
  CONSTRAINT ref_tax_id PRIMARY KEY (ref_tax_id)
) 
;


--glycan_sequence table created here due to manual creation of some hplc structure
--due to glycoct support for two + sets of unknown linkages!
CREATE TABLE hplc.glycan_sequence_temp
(

    glycan_sequence_id                  INTEGER NOT NULL,
    sequence_iupac                      VARCHAR(65535) UNIQUE NOT NULL,
    sequence_ct                         VARCHAR(65535) UNIQUE NOT NULL,
    sequence_ct_condensed		VARCHAR(65535) NOT NULL,
    residue_count                       SMALLINT DEFAULT NULL,
    mass_monoisotopic                   NUMERIC,
    mass_average                        NUMERIC,
    composition                         VARCHAR(64),
    date_entered                        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_contributed                    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    contributor_id                      INT NOT NULL

)
;

CREATE TABLE hplc.multipleglycoct
(
	id				SERIAL PRIMARY KEY NOT NULL,
	glycan_id			INT,
	sequence_id			INT

)
;

CREATE TABLE hplc.glycan_source_link
(
	  source_id serial NOT NULL,
	  glycan_id int4 NOT NULL,
	  ref_id int4 NOT NULL,
	  mesh_pert_id int4,
	  mesh_disease_id int4,
	  mesh_tissue_id int4,
	  tax_species_id int4,
	  biological_context_id int4,
--additions
	  pert_mesh_full_id VARCHAR(250),
	  disease_mesh_full_id VARCHAR(250),
	  tissue_mesh_full_id VARCHAR(250),
	  CONSTRAINT source_id PRIMARY KEY (source_id)
);

--
-- TOC entry 1591 (class 1259 OID 25655)
-- Dependencies: 1221 1221
-- Name: detector_index; Type: INDEX; Schema: hplc_schema; Owner: root; Tablespace: 
--

CREATE INDEX detector_index ON hplc.detector USING btree (manufacturer, model);


--
-- TOC entry 1592 (class 1259 OID 25658)
-- Dependencies: 1223
-- Name: digest_index; Type: INDEX; Schema: hplc_schema; Owner: root; Tablespace: 
--

CREATE INDEX digest_index ON hplc.digest USING btree (name);


--
-- TOC entry 1599 (class 1259 OID 25661)
-- Dependencies: 1229
-- Name: glycan_index; Type: INDEX; Schema: hplc_schema; Owner: root; Tablespace: 
--

CREATE INDEX glycan_index ON hplc.glycan USING btree (name);


--
-- TOC entry 1602 (class 1259 OID 25662)
-- Dependencies: 1231 1231
-- Name: instrument_index; Type: INDEX; Schema: hplc_schema; Owner: root; Tablespace: 
--

CREATE INDEX instrument_index ON hplc.instrument USING btree (manufacturer, model);


--
-- TOC entry 1586 (class 1259 OID 25648)
-- Dependencies: 1219
-- Name: manufacturer_index; Type: INDEX; Schema: hplc_schema; Owner: root; Tablespace: 
--

CREATE INDEX manufacturer_index ON hplc.column USING btree (manufacturer);


--
-- TOC entry 1620 (class 1259 OID 25666)
-- Dependencies: 1241
-- Name: ref_link_index; Type: INDEX; Schema: hplc_schema; Owner: root; Tablespace: 
--

--TEMPCHANGE CREATE INDEX ref_link_index ON hplc.ref_link USING btree (glycan_id);


-- Completed on 2007-02-19 16:42:54 GMT Standard Time

--
-- PostgreSQL database dump complete
--
-- COPY hplc.glycan from '/hplc_data/glycan_share.txt';
-- COPY hplc.digest from '/hplc_data/digest_share.txt';
-- COPY hplc.ref_link from '/hplc_data/reflink_share.txt';
-- COPY hplc.ref from '/hplc_data/ref_share.txt';

--dummy data

insert into hplc.detector values (1, 'Waters', 2475, 200, 300, 2, 2);
insert into hplc.column values (1, 'Tosoh', 'TSKgel Amide 80', 'Amide', 2,5,3);
insert into hplc.column values (2, 'Tosoh', 'TSKgel Amide 80', 'Amide', 2,5,5); 
