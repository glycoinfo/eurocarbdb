----------------------
-- Drop the old schema
----------------------
DROP SCHEMA ms CASCADE;

-------------------
-- Create a new schema
-------------------
CREATE SCHEMA ms AUTHORIZATION postgres;
SET search_path TO ms;
--------------------------------------------------------------
--------------------------------------------------------------
-------------------
-- Device diagram
------------------
--------------------------------------------------------------
--------------------------------------------------------------

-------------------
-- manufacturer
-------------------
CREATE TABLE manufacturer
(
  manufacturer_id serial NOT NULL,
  "name" character varying(255) NOT NULL,
  url character varying(1000),
  CONSTRAINT manufacturer_pkey PRIMARY KEY (manufacturer_id)
);

-----------------
-- device
-----------------
CREATE TABLE device
(
  device_id serial NOT NULL,
  manufacturer_id integer NOT NULL,
  model character varying(200) NOT NULL,
  ionisation_type character varying(255) NOT NULL,
  CONSTRAINT device_pkey PRIMARY KEY (device_id),
  CONSTRAINT device_manufacturer_id_fkey FOREIGN KEY (manufacturer_id)
      REFERENCES manufacturer (manufacturer_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

------------------
-- analyzer
------------------
CREATE TABLE analyser
(
  analyser_id serial NOT NULL,
  device_id integer NOT NULL,
  model character varying(255),
  analyser_type character varying(255) NOT NULL,
  accuracy double precision NOT NULL,
  scan_rate double precision NOT NULL,
  scan_time double precision NOT NULL,
  scan_direction character varying(5) NOT NULL,
  scan_law character varying(20) NOT NULL,
  tof_path_length double precision,
  isolation_width double precision,
  magnetic_field_strengh double precision,
  final_ms_exponent integer NOT NULL,
  CONSTRAINT analyser_pkey PRIMARY KEY (analyser_id),
  CONSTRAINT analyser_device_id_fkey FOREIGN KEY (device_id)
      REFERENCES device (device_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

-----------------------
-- mass_detector
-----------------------
CREATE TABLE mass_detector
(
  mass_detector_id serial NOT NULL,
  device_id integer NOT NULL,
  model character varying(255),
  mass_detector_type character varying(255) NOT NULL,
  mass_detector_resolution double precision NOT NULL,
  digital_resolution double precision NOT NULL,
  sampling_frequency integer NOT NULL,
  CONSTRAINT mass_detector_pkey PRIMARY KEY (mass_detector_id),
  CONSTRAINT mass_detector_device_id_fkey FOREIGN KEY (device_id)
      REFERENCES device (device_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

----------------------
-- fragmentation_type
----------------------
CREATE TABLE fragmentation_type
(
  fragmentation_type_id serial NOT NULL,
  device_id integer NOT NULL,
  fragmentation_type character varying(255) NOT NULL,
  CONSTRAINT fragmentation_type_pkey PRIMARY KEY (fragmentation_type_id),
  CONSTRAINT fragmentation_type_device_id_fkey FOREIGN KEY (device_id)
      REFERENCES device (device_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

-----------------------
-- laser
-----------------------
CREATE TABLE laser
(
  laser_id serial NOT NULL,
  device_id integer NOT NULL,
  model character varying(255),
  laser_type character varying(255) NOT NULL,
  focus double precision NOT NULL,
  energy double precision,
  frequency double precision,
  wave_length double precision,
  CONSTRAINT laser_pkey PRIMARY KEY (laser_id),
  CONSTRAINT laser_device_id_fkey FOREIGN KEY (device_id)
      REFERENCES device (device_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

------------------------
--Source
------------------------

 CREATE TABLE source
(
  source_id serial NOT NULL,
  device_id integer NOT NULL,
  model character varying(255),
  source_type character varying(255) NOT NULL,
  CONSTRAINT source_pkey PRIMARY KEY (source_id),
  CONSTRAINT source_device_id_fkey FOREIGN KEY (device_id)
      REFERENCES device (device_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

--------------------------
-- acquesition
--------------------------
CREATE TABLE acquisition
(
  acquisition_id integer NOT NULL,
  device_id integer NOT NULL,
  filename character varying(256) NOT NULL,
  filetype character varying(50) NOT NULL,
  date_obtained timestamp without time zone NOT NULL,
  contributor_quality double precision NOT NULL,
  CONSTRAINT acquisition_pkey PRIMARY KEY (acquisition_id),
  CONSTRAINT acquisition_device_id_fkey FOREIGN KEY (device_id)
      REFERENCES device (device_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT acquisition_evidence_id_fkey FOREIGN KEY (acquisition_id)
      REFERENCES core.evidence (evidence_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

-------------------------
-- device settings
-------------------------
CREATE TABLE device_settings
(
  device_settings_id serial NOT NULL,
  acquisition_id integer NOT NULL,
  contributor_quality double precision NOT NULL,
  CONSTRAINT device_settings_pkey PRIMARY KEY (device_settings_id),
  CONSTRAINT device_settings_acquisition_id_fkey FOREIGN KEY (acquisition_id)
      REFERENCES acquisition (acquisition_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);
--------------------------
-- laser parameter
--------------------------
CREATE TABLE laser_parameter
(
  laser_parameter_id serial NOT NULL,
  device_settings_id integer NOT NULL,
  laser_id integer NOT NULL,
  laser_shoot_count integer NOT NULL,
  laser_frequency double precision NOT NULL,
  laser_intensity double precision NOT NULL,
  laser_focus double precision NOT NULL,
  ionisation_energy double precision,
  CONSTRAINT laser_parameter_pkey PRIMARY KEY (laser_parameter_id),
  CONSTRAINT laser_parameter_device_settings_id_fkey FOREIGN KEY (device_settings_id)
      REFERENCES device_settings (device_settings_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT laser_parameter_laser_id_fkey FOREIGN KEY (laser_id)
      REFERENCES laser (laser_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

-------------------------------
-- fragmentation parameters
-------------------------------
CREATE TABLE fragmentation_parameter
(
  fragmentation_parameter_id serial NOT NULL,
  device_settings_id integer NOT NULL,
  fragmentation_type_id integer NOT NULL,
  collision_gas character varying(100) NOT NULL,
  pressure double precision NOT NULL,
  collision_energie double precision NOT NULL,
  CONSTRAINT fragmentation_parameter_pkey PRIMARY KEY (fragmentation_parameter_id),
  CONSTRAINT fragmentation_parameter_device_settings_id_fkey FOREIGN KEY (device_settings_id)
      REFERENCES device_settings (device_settings_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fragmentation_parameter_fragmentation_type_id_fkey FOREIGN KEY (fragmentation_type_id)
      REFERENCES fragmentation_type (fragmentation_type_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

----------------------------
-- source parameter
----------------------------
CREATE TABLE source_parameter
(
  source_parameter_id serial NOT NULL,
  device_settings_id integer NOT NULL,
  source_id integer NOT NULL,
  CONSTRAINT source_parameter_pkey PRIMARY KEY (source_parameter_id),
  CONSTRAINT source_parameter_device_settings_id_fkey FOREIGN KEY (device_settings_id)
      REFERENCES device_settings (device_settings_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT source_parameter_source_id_fkey FOREIGN KEY (source_id)
      REFERENCES source (source_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

-------------------------
-- tandem scan method
-------------------------
CREATE TABLE tandem_scan_method
(
  tandem_scan_method_id serial NOT NULL,
  analyser_id integer NOT NULL,
  tandem_scan_method character varying(255) NOT NULL,
  CONSTRAINT tandem_scan_method_pkey PRIMARY KEY (tandem_scan_method_id),
  CONSTRAINT tandem_scan_method_analyser_id_fkey FOREIGN KEY (analyser_id)
      REFERENCES analyser (analyser_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

---------------------------
-- analyzer parameter
---------------------------
CREATE TABLE analyser_parameter
(
  analyser_parameter_id serial NOT NULL,
  device_settings_id integer NOT NULL,
  analyser_id integer NOT NULL,
  tandem_scan_method_id integer NOT NULL,
  resolution_type character varying(45) NOT NULL,
  resolution_method character varying(45) NOT NULL,
  resolution double precision NOT NULL,
  CONSTRAINT analyser_parameter_pkey PRIMARY KEY (analyser_parameter_id),
  CONSTRAINT analyser_parameter_analyser_id_fkey FOREIGN KEY (analyser_id)
      REFERENCES analyser (analyser_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT analyser_parameter_device_settings_id_fkey FOREIGN KEY (device_settings_id)
      REFERENCES device_settings (device_settings_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT analyser_parameter_tandem_scan_method_id_fkey FOREIGN KEY (tandem_scan_method_id)
      REFERENCES tandem_scan_method (tandem_scan_method_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

---------------------------
-- mass detector parameter
---------------------------
CREATE TABLE mass_detector_parameter
(
  mass_detector_parameter_id serial NOT NULL,
  device_settings_id integer NOT NULL,
  mass_detector_id integer NOT NULL,
  digital_resolution integer NOT NULL,
  sampling_frequency integer NOT NULL,
  CONSTRAINT mass_detector_parameter_pkey PRIMARY KEY (mass_detector_parameter_id),
  CONSTRAINT mass_detector_parameter_device_settings_id_fkey FOREIGN KEY (device_settings_id)
      REFERENCES device_settings (device_settings_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT mass_detector_parameter_mass_detector_id_fkey FOREIGN KEY (mass_detector_id)
      REFERENCES mass_detector (mass_detector_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

---------------------------------
-- esi parameter
---------------------------------
CREATE TABLE esi_parameter
(
  esi_parameter_id serial NOT NULL,
  device_settings_id integer NOT NULL,
  dry_gas character varying(50),
  flow_rate double precision,
  temperatur double precision,
  voltage_capillary double precision,
  voltage_end_plate double precision,
  solvent character varying(255) NOT NULL,
  CONSTRAINT esi_parameter_pkey PRIMARY KEY (esi_parameter_id),
  CONSTRAINT esi_parameter_device_settings_id_fkey FOREIGN KEY (device_settings_id)
      REFERENCES device_settings (device_settings_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

-------------------------------
-- maldi matrix
-------------------------------
CREATE TABLE maldi_matrix
(
  maldi_matrix_id serial NOT NULL,
  matrix character varying(200) NOT NULL,
  CONSTRAINT maldi_matrix_pkey PRIMARY KEY (maldi_matrix_id)
);

--------------------------------
-- maldi parameter
--------------------------------
CREATE TABLE maldi_parameter
(
  maldi_parameter_id serial NOT NULL,
  device_settings_id integer NOT NULL,
  maldi_matrix_id integer NOT NULL,
  spot_diameter double precision NOT NULL,
  spot_type character varying(255) NOT NULL,
  CONSTRAINT maldi_parameter_pkey PRIMARY KEY (maldi_parameter_id),
  CONSTRAINT maldi_parameter_device_settings_id_fkey FOREIGN KEY (device_settings_id)
      REFERENCES device_settings (device_settings_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT maldi_parameter_maldi_matrix_id_fkey FOREIGN KEY (maldi_matrix_id)
      REFERENCES maldi_matrix (maldi_matrix_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE SET NULL
);

--------------------------------
-- iontrap_parameter
--------------------------------
CREATE TABLE iontrap_parameter
(
  iontrap_parameter_id serial NOT NULL,
  device_settings_id integer NOT NULL,
  ion_count integer NOT NULL,
  CONSTRAINT iontrap_parameter_pkey PRIMARY KEY (iontrap_parameter_id),
  CONSTRAINT iontrap_parameter_device_settings_id_fkey FOREIGN KEY (device_settings_id)
      REFERENCES device_settings (device_settings_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

------------------------------------
-- tof parameter
------------------------------------
CREATE TABLE tof_parameter
(
  tof_parameter_id serial NOT NULL,
  device_settings_id integer NOT NULL,
  reflector_state boolean,
  accelerator_grid_voltage double precision,
  delay_extration_time double precision,
  CONSTRAINT tof_parameter_pkey PRIMARY KEY (tof_parameter_id),
  CONSTRAINT tof_parameter_device_settings_id_fkey FOREIGN KEY (device_settings_id)
      REFERENCES device_settings (device_settings_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

--------------------------------------------------------------
--------------------------------------------------------------
-------------------
-- Scan diagram
------------------
--------------------------------------------------------------
--------------------------------------------------------------

------------------------
-- method_of_combination
------------------------
CREATE TABLE method_of_combination
(
  method_of_combination_id serial NOT NULL,
  method_of_combination character varying(50) NOT NULL,
  CONSTRAINT method_of_combination_pkey PRIMARY KEY (method_of_combination_id),
  CONSTRAINT method_of_combination_Unique UNIQUE(method_of_combination)
);

----------------------------
-- software
----------------------------
CREATE TABLE software
(
  software_id serial NOT NULL,
  "name" character varying(255) NOT NULL,
  software_version character varying(50) NOT NULL,
  CONSTRAINT software_pkey PRIMARY KEY (software_id),
  CONSTRAINT software_Unique UNIQUE("name",software_version)
);

----------------------
-- software_type
----------------------
CREATE TABLE software_type
(
  software_type_id serial NOT NULL,
  software_type character varying(100) NOT NULL,
  CONSTRAINT software_type_pkey PRIMARY KEY (software_type_id),
  CONSTRAINT software_type_Unique UNIQUE(software_type)
);

-----------------------
-- data_processing
-----------------------
CREATE TABLE data_processing
(
  data_processing_id serial NOT NULL,
  software_type_id integer NOT NULL,
  software_id integer NOT NULL,
  intensity_cutoff double precision NOT NULL DEFAULT (0)::double precision,
  format character varying(20) NOT NULL,
  CONSTRAINT data_processing_pkey PRIMARY KEY (data_processing_id),
  CONSTRAINT data_processing_software_id_fkey FOREIGN KEY (software_id)
      REFERENCES software (software_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT data_processing_software_type_id_fkey FOREIGN KEY (software_type_id)
      REFERENCES software_type (software_type_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT data_processing_Unique UNIQUE(software_type_id, software_id,format)
  
);

--------------------
-- persubstitution
--------------------
CREATE TABLE persubstitution
(
  persubstitution_id serial NOT NULL,
  abbreviation character varying(10),
  "name" character varying(255),
  CONSTRAINT persubstitution_pkey PRIMARY KEY (persubstitution_id),
  CONSTRAINT persubstitution_name_Unique UNIQUE("name"),
  CONSTRAINT persubstitution_abbreviation_Unique UNIQUE(abbreviation)

);

------------------------
-- Scan 
------------------------
CREATE TABLE scan
(
  scan_id serial NOT NULL,
  acquisition_id integer NOT NULL,
  ms_exponent integer NOT NULL DEFAULT 1,
  polarity boolean NOT NULL,
  start_mz double precision NOT NULL,
  end_mz double precision NOT NULL,
  contributor_quality double precision NOT NULL,
  
  original_scan_id integer NOT NULL,
  CONSTRAINT scan_pkey PRIMARY KEY (scan_id),
  CONSTRAINT scan_acquisition_id_fkey FOREIGN KEY (acquisition_id)
      REFERENCES acquisition (acquisition_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  
  CONSTRAINT scan_Unique UNIQUE(acquisition_id,original_scan_id)
  );

---------------------------
-- scan_to_data_processing
---------------------------
CREATE TABLE scan_to_data_processing
(
  scan_to_data_processing_id serial NOT NULL,
  scan_id integer NOT NULL,
  data_processing_id integer NOT NULL,
  spot_integration boolean NOt NULL,
  software_order integer NOT NULL,
  CONSTRAINT scan_to_data_processing_pkey PRIMARY KEY (scan_to_data_processing_id),
  CONSTRAINT scan_to_data_processing_data_processing_fkey FOREIGN KEY (data_processing_id)
      REFERENCES data_processing (data_processing_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT scan_to_data_processing_scan_fkey FOREIGN KEY (scan_id)
      REFERENCES scan (scan_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT scan_to_data_processing_Unique UNIQUE(scan_id,data_processing_id)

);

-------------------
-- scan_image
-------------------
CREATE TABLE scan_image
(
  scan_image_id serial NOT NULL,
  full_size bytea NOT NULL,
  scan_id integer NOT NULL,
  medium_size bytea NOT NULL,
  thumbnail bytea NOT NULL,
  file_name character varying(256) NOT NULL,
  annotation_report bytea,
  CONSTRAINT scan_image_pkey PRIMARY KEY (scan_image_id),
  CONSTRAINT scan_image_scan_id_fkey FOREIGN KEY (scan_id)
      REFERENCES scan (scan_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT scan_image_Unique UNIQUE(scan_id,file_name)
);

----------------------------
-- sum_average_relationship
----------------------------
CREATE TABLE sum_average_relationship
(
  sum_average_relationship_id serial NOT NULL,
  scan_id integer NOT NULL,
  subset_scan_id integer NOT NULL,
  method_of_combination_id integer NOT NULL,
  CONSTRAINT sum_average_relationship_pkey PRIMARY KEY (sum_average_relationship_id),
  CONSTRAINT sum_average_relationship_method_of_combination_id_fkey FOREIGN KEY (method_of_combination_id)
      REFERENCES method_of_combination (method_of_combination_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT sum_average_relationship_scan_id_fkey FOREIGN KEY (scan_id)
      REFERENCES scan (scan_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT sum_average_relationship_subset_scan_id_fkey FOREIGN KEY (subset_scan_id)
      REFERENCES scan (scan_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT sum_average_Unique UNIQUE(scan_id,subset_scan_id)
);

-----------------
-- ms_ms_relationship
---------------
CREATE TABLE ms_ms_relationship
(
  ms_ms_relationship_id serial NOT NULL,
  scan_id integer NOT NULL,
  parent_id integer NOT NULL,
  precursor_mz double precision NOT NULL,
  precursor_intensity double precision NOT NULL,
  precursor_mass_window_low double precision,
  precursor_mass_window_high double precision,
  precursor_charge integer NOT NULL,
  ms_ms_methode character varying(100) NOT NULL,
  CONSTRAINT ms_ms_relationship_pkey PRIMARY KEY (ms_ms_relationship_id),
  CONSTRAINT ms_ms_relationship_parent_id_fkey FOREIGN KEY (parent_id)
      REFERENCES scan (scan_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT ms_ms_relationship_scan_id_fkey FOREIGN KEY (scan_id)
      REFERENCES scan (scan_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT ms_ms_relationship_Unique UNIQUE(scan_id,parent_id)
);

------------------------
-- peak_processing
------------------------
CREATE TABLE peak_processing
(
  peak_processing_id serial NOT NULL,
  peak_processing_type character varying(50),
  CONSTRAINT peak_processing_pkey PRIMARY KEY (peak_processing_id),
  CONSTRAINT peak_processing_Unique UNIQUE(peak_processing_type)
);

--------------------------
-- small_molecule
--------------------------
CREATE TABLE small_molecule
(
  small_molecule_id serial NOT NULL,
  "name" character varying(255) NOT NULL,
  CONSTRAINT small_molecule_pkey PRIMARY KEY (small_molecule_id),
  CONSTRAINT small_molecule_Unique UNIQUE("name")
);

-----------------------------
-- small_molecule_composition
-----------------------------
CREATE TABLE small_molecule_composition
(
  small_molecule_composition_id serial NOT NULL,
  small_molecule_id integer NOT NULL,
  atom_type character varying(2) NOT NULL,
  "number" integer NOT NULL,
  CONSTRAINT small_molecule_composition_pkey PRIMARY KEY (small_molecule_composition_id),
  CONSTRAINT small_molecule_composition_small_molecule_id_fkey FOREIGN KEY (small_molecule_id)
      REFERENCES small_molecule (small_molecule_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT small_molecule_composition_atom_type_fkey FOREIGN KEY (atom_type)
      REFERENCES resources_db.periodic (symbol) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT small_molecule_composition_Unique UNIQUE(small_molecule_id,atom_type)
);

--------------------------------
-- reducing_end
--------------------------------
CREATE TABLE reducing_end
(
  reducing_end_id serial NOT NULL,
  abbreviation character varying(10) NOT NULL,
  "name" character varying(255) NOT NULL,
  uri character varying(1024),
  CONSTRAINT reducing_end_pkey PRIMARY KEY (reducing_end_id),
  CONSTRAINT reducing_end_abbreviation_Unique UNIQUE(abbreviation),
  CONSTRAINT reducing_end_name_Unique UNIQUE("name")
);

--------------------------------
-- peak_list
--------------------------------
CREATE TABLE peak_list
(
  peak_list_id serial NOT NULL,
  scan_id integer NOT NULL,
  date_entered timestamp NOT NULL,
  deisotoped boolean NOT NULL,
  charge_deconvoluted boolean NOT NULL,
  base_peak_mz double precision,
  base_peak_intensity double precision,
  low_mz double precision NOT NULL,
  high_mz double precision NOT NULL,
  contributor_id integer NOT NULL,
  peak_processing_id integer NOT NULL,
  contributor_quality double precision NOT NULL,
  CONSTRAINT peak_list_pkey PRIMARY KEY (peak_list_id),
  CONSTRAINT peak_list_peak_processing_fkey FOREIGN KEY (peak_processing_id)
      REFERENCES peak_processing (peak_processing_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT peak_list_scan_fkey FOREIGN KEY (scan_id)
      REFERENCES scan (scan_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT peak_list_contributor_fkey FOREIGN KEY (contributor_id)
      REFERENCES core.contributor (contributor_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT peak_list_Unique UNIQUE(scan_id,date_entered,contributor_id)
);

--------------------------------
-- peak_list_to_data_processing
--------------------------------
CREATE TABLE peak_list_to_data_processing
(
  peak_list_to_data_processing_id serial NOT NULL,
  peak_list_id integer NOT NULL,
  data_processing_id integer NOT NULL,
  software_order integer NOT NULL,
  CONSTRAINT peak_list_to_data_processing_pkey PRIMARY KEY (peak_list_to_data_processing_id),
  CONSTRAINT peak_list_to_data_processing_data_processing_fkey FOREIGN KEY (data_processing_id)
      REFERENCES data_processing (data_processing_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT peak_list_to_data_processing_peak_list_fkey FOREIGN KEY (peak_list_id)
      REFERENCES peak_list (peak_list_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT peak_list_to_data_processing_Unique UNIQUE(peak_list_id,data_processing_id)

);

--------------------------------
-- peak_labeled
--------------------------------
CREATE TABLE peak_labeled
(
  peak_labeled_id serial NOT NULL,
  peak_list_id integer NOT NULL,
  mz_value double precision NOT NULL,
  intensity_value double precision NOT NULL,
  monoisotopic boolean NOT NULL,
  charge_count integer,
  fwhm double precision,
  signal_to_noise double precision,
  CONSTRAINT peak_labeled_pkey PRIMARY KEY (peak_labeled_id),
  CONSTRAINT peak_annotated_peak_list_id_fkey FOREIGN KEY (peak_list_id)
      REFERENCES peak_list (peak_list_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT peak_labeled_Unique UNIQUE(peak_list_id,mz_value,monoisotopic)
);

--------------------------------
-- peak annotated
--------------------------------
CREATE TABLE peak_annotated
(
  peak_annotated_id serial NOT NULL,
  peak_labeled_id integer NOT NULL,
  glyco_ct_id integer NOT NULL,
  sequence_gws character varying(15000) NOT NULL,
  formula character varying(255),
  calculated_mass double precision NOT NULL,
  contributor_quality double precision NOT NULL,
  reducing_end_id integer NOT NULL,
  date_entered timestamp without time zone NOT NULL,
  persubstitution_id integer NOT NULL,
  contributor_id integer NOT NULL, 
  CONSTRAINT peak_annotated_pkey PRIMARY KEY (peak_annotated_id),
  CONSTRAINT peak_annotated_peak_labeled_id_fkey FOREIGN KEY (peak_labeled_id)
      REFERENCES peak_labeled (peak_labeled_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT peak_annotated_reducing_end_id_fkey FOREIGN KEY (reducing_end_id)
      REFERENCES reducing_end (reducing_end_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT peak_annotated_contributor_fkey FOREIGN KEY (contributor_id)
      REFERENCES core.contributor (contributor_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
   CONSTRAINT peak_annotated_glycan_sequence_fkey FOREIGN KEY (glyco_ct_id)
      REFERENCES core.glycan_sequence (glycan_sequence_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
   CONSTRAINT scan_persubstitution_id_fkey FOREIGN KEY (persubstitution_id)
      REFERENCES persubstitution (persubstitution_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT peak_annonated_Unique UNIQUE(peak_labeled_id,sequence_gws,reducing_end_id,contributor_id)
);

------------------------------------
-- peak_annotated_to_small_molecule
------------------------------------
CREATE TABLE peak_annotated_to_small_molecule
(
  peak_annotated_to_small_molecule_id integer NOT NULL,
  small_molecule_id integer NOT NULL,
  peak_annotated_id integer NOT NULL,
  gain boolean NOT NULL,
  "number" integer NOT NULL,
  CONSTRAINT peak_annotated_to_small_molecule_pkey PRIMARY KEY (peak_annotated_to_small_molecule_id),
  CONSTRAINT peak_annotated_to_small_molecule_peak_annotated_id_fkey FOREIGN KEY (peak_annotated_id)
      REFERENCES peak_annotated (peak_annotated_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT peak_annotated_to_small_molecule_small_molecule_id_fkey FOREIGN KEY (small_molecule_id)
      REFERENCES small_molecule (small_molecule_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT peak_annonated_to_small_molecule_Unique UNIQUE(small_molecule_id,peak_annotated_id)
);

-------------------------------
-- fragmentation
-------------------------------
CREATE TABLE fragmentation
(
  fragmentation_id serial NOT NULL,
  peak_annotated_id integer NOT NULL,
  fragment_type character varying(1),
  fragment_dc character varying(255) NOT NULL,
  fragment_alt character varying(255),
  fragment_position integer,
  cleavage_one integer,
  cleavage_two integer,
  CONSTRAINT fragmentation_pkey PRIMARY KEY (fragmentation_id),
  CONSTRAINT fragmentation_peak_annotated_id_fkey FOREIGN KEY (peak_annotated_id)
      REFERENCES peak_annotated (peak_annotated_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

------------------------------
-- ion
------------------------------
CREATE TABLE ion
(
  ion_id serial NOT NULL,
  ion_type character varying(255) NOT NULL,
  charge integer NOT NULL,
  "positive" boolean NOT NULL,
  atomer boolean NOT NULL,
  CONSTRAINT ion_pkey PRIMARY KEY (ion_id),
  CONSTRAINT ion_Unique UNIQUE(ion_type,charge,positive)
);

----------------------------
-- peak anootated to ion
----------------------------
CREATE TABLE peak_annotated_to_ion
(
  peak_annotated_to_ion_id serial NOT NULL,
  peak_annotated_id integer NOT NULL,
  ion_id integer NOT NULL,
  "number" integer NOT NULL,
  gain boolean NOT NULL,
  CONSTRAINT peak_annotated_to_ion_pkey PRIMARY KEY (peak_annotated_to_ion_id),
  CONSTRAINT peak_annotated_to_ion_ion_id_fkey FOREIGN KEY (ion_id)
      REFERENCES ion (ion_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT peak_annotated_to_ion_peak_annotated_id_fkey FOREIGN KEY (peak_annotated_id)
      REFERENCES peak_annotated (peak_annotated_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT peak_annotated_to_ion_Unique UNIQUE(peak_annotated_id,ion_id,gain)
);

---------------------------
-- ion_composition
---------------------------
CREATE TABLE ion_composition
(
  ion_composition_id serial NOT NULL,
  ion_id integer NOT NULL,
  atom_type character varying(2) NOT NULL,
  "number" integer NOT NULL,
  CONSTRAINT ion_composition_pkey PRIMARY KEY (ion_composition_id),
  CONSTRAINT ion_composition_ion_id_fkey FOREIGN KEY (ion_id)
      REFERENCES ion (ion_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT small_molecule_composition_atom_type_fkey FOREIGN KEY (atom_type)
      REFERENCES resources_db.periodic (symbol) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT ion_composition_Unique UNIQUE(ion_id,atom_type)
);

------------------------------
-- acquisition_to_persubstitution
------------------------------
CREATE TABLE acquisition_to_persubstitution
(
  acquisition_to_persubstitution_id serial NOT NULL,
  acquisition_id integer NOT NULL,
  persubstitution_id integer NOT NULL,
  CONSTRAINT acquisition_to_persubstitution_id_pkey PRIMARY KEY (acquisition_to_persubstitution_id),
  CONSTRAINT acquisition_to_persubstitution_acquisition_id_fkey FOREIGN KEY (acquisition_id)
      REFERENCES acquisition (acquisition_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT acquisition_to_persubstitution_persubstitution_id_fkey FOREIGN KEY (persubstitution_id)
      REFERENCES persubstitution (persubstitution_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);













