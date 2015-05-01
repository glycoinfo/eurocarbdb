CREATE SCHEMA ms;

SET search_path TO ms;

CREATE TABLE scan_image
(
  scan_image_id SERIAL PRIMARY KEY NOT NULL,
  scan_id INT NOT NULL REFERENCES scan ON DELETE CASCADE ON UPDATE CASCADE,
  full_size bytea NOT NULL,
  medium_size bytea NOT NULL,
  thumbnail bytea NOT NULL,
  annotation_report bytea NOT NULL,
  -- CONSTRAINT file_pkey PRIMARY KEY (file_id)
);

CREATE TABLE manufacturer (
  manufacturer_id SERIAL PRIMARY KEY NOT NULL,
  name VARCHAR(255) NOT NULL,
  url VARCHAR(1000) NULL
  -- CONSTRAINT secondary_manufacturer UNIQUE(name)
);

CREATE TABLE device (
  device_id SERIAL PRIMARY KEY NOT NULL,
  manufacturer_id INT NOT NULL REFERENCES manufacturer ON DELETE CASCADE ON UPDATE CASCADE,
  model VARCHAR(200) NOT NULL,
  ionisation_type VARCHAR(255) NOT NULL
  -- CONSTRAINT secondary_device UNIQUE(model)
);

CREATE TABLE analyser (
  analyser_id SERIAL PRIMARY KEY NOT NULL,
  device_id INT NOT NULL REFERENCES device ON DELETE CASCADE ON UPDATE CASCADE,
  model VARCHAR(255) NULL,
  analyser_type VARCHAR(255) NOT NULL,
  accuracy FLOAT NOT NULL,
  scan_rate FLOAT NOT NULL,
  scan_time FLOAT NOT NULL,
  scan_direction VARCHAR(5) NOT NULL,
  scan_law VARCHAR(20) NOT NULL,
  tof_path_length FLOAT NULL,
  isolation_width FLOAT NULL,
  magnetic_field_strengh FLOAT NULL,
  final_ms_exponent INT NOT NULL
);

CREATE TABLE mass_detector (
  mass_detector_id SERIAL PRIMARY KEY NOT NULL,
  device_id INT NOT NULL REFERENCES device ON DELETE CASCADE ON UPDATE CASCADE,  
  model VARCHAR(255) NULL,
  mass_detector_type VARCHAR(255) NOT NULL,
  mass_detector_resolution FLOAT NOT NULL,
  digital_resolution FLOAT NOT NULL,
  sampling_frequency INT NOT NULL
  -- CONSTRAINT secondary_mass_detector UNIQUE(mass_detector_type, mass_detector_resolution, digital_resolution, sampling_frequency, device_id)
);

CREATE TABLE laser (
  laser_id SERIAL PRIMARY KEY NOT NULL,
  device_id INT NOT NULL REFERENCES device ON DELETE CASCADE ON UPDATE CASCADE,
  model VARCHAR(255) NULL,
  laser_type VARCHAR(255) NOT NULL,
  focus FLOAT NOT NULL,
  energy FLOAT NULL,
  frequency FLOAT NULL,
  wave_length FLOAT NULL
);

CREATE TABLE tandem_scan_method (
  tandem_scan_method_id SERIAL PRIMARY KEY NOT NULL,
  analyser_id INT NOT NULL REFERENCES analyser ON DELETE CASCADE ON UPDATE CASCADE,
  tandem_scan_method VARCHAR(255) NOT NULL
  --CONSTRAINT secondary_tandem_scan_method UNIQUE(tandem_scan_method)
);

CREATE TABLE fragmentation_type (
  fragmentation_type_id SERIAL PRIMARY KEY NOT NULL,
  device_id INT NOT NULL REFERENCES device ON DELETE CASCADE ON UPDATE CASCADE,
  fragmentation_type VARCHAR(255) NOT NULL
  --CONSTRAINT secondary_fragmentation_type UNIQUE(fragmentation_type,device_id)
);

CREATE TABLE software_type (
  software_type_id SERIAL PRIMARY KEY NOT NULL,
  software_type VARCHAR(100) NOT NULL
  --CONSTRAINT secondary_software_type UNIQUE(software_type)
);

CREATE TABLE peak_processing (
  peak_processing_id SERIAL PRIMARY KEY NOT NULL,
  peak_processing_type VARCHAR(50) NULL
  --CONSTRAINT secondary_peak_processing UNIQUE(peak_processing_type)
);

CREATE TABLE method_of_combination (
  method_of_combination_id SERIAL PRIMARY KEY NOT NULL,
  method_of_combination VARCHAR(50) NOT NULL
  --CONSTRAINT secondary_method_of_combination UNIQUE(method_of_combination)
);

CREATE TABLE maldi_matrix (
  maldi_matrix_id SERIAL PRIMARY KEY NOT NULL,
  matrix VARCHAR(200) NOT NULL
  --CONSTRAINT secondary_maldi_matrix UNIQUE( matrix )
);

CREATE TABLE ion (
  ion_id SERIAL PRIMARY KEY NOT NULL,
  ion_type VARCHAR(255) NOT NULL
  --CONSTRAINT secondary_ion UNIQUE(ion_type)
);

CREATE TABLE software (
  software_id SERIAL PRIMARY KEY NOT NULL,
  name VARCHAR(255) NOT NULL,
  software_version VARCHAR(50) NOT NULL
  --CONSTRAINT secondary_software UNIQUE(name, software_version)
);


CREATE TABLE source (
  source_id SERIAL PRIMARY KEY NOT NULL,
  device_id INT NOT NULL REFERENCES device ON DELETE CASCADE ON UPDATE CASCADE,
  model VARCHAR(255) NULL,
  source_type VARCHAR(255) NOT NULL
);


CREATE TABLE acquisition (
  acquisition_id SERIAL PRIMARY KEY NOT NULL,
  evidence_id INT REFERENCES core.evidence ON DELETE CASCADE ON UPDATE CASCADE,
  device_id INT  NOT NULL REFERENCES device ON DELETE SET NULL ON UPDATE CASCADE,
  contributor_id INT NOT NULL REFERENCES core.contributor ON DELETE SET NULL ON UPDATE CASCADE,  
  filename VARCHAR(256) NOT NULL,
  filetype VARCHAR(50) NOT NULL,
  date_obtained TIMESTAMP NOT NULL,
  contributor_quality FLOAT NOT NULL
  
);

CREATE TABLE scan (
  scan_id SERIAL PRIMARY KEY NOT NULL,
  acquisition_id INT NOT NULL REFERENCES acquisition ON DELETE CASCADE ON UPDATE CASCADE,
  peak_processing_id INT NOT NULL REFERENCES peak_processing ON DELETE SET NULL ON UPDATE CASCADE,
  scan_image_id INT REFERENCES scan_image ON DELETE SET NULL ON UPDATE CASCADE,
  ms_exponent INT NOT NULL DEFAULT '1',
  polarity BOOL NOT NULL,
  deisotoped BOOL NOT NULL,
  charge_deconvoluted BOOL NOT NULL,
  base_peak_mz FLOAT NOT NULL,
  base_peak_intensity FLOAT NOT NULL,
  start_mz FLOAT NOT NULL,
  end_mz FLOAT NOT NULL,
  low_mz FLOAT NOT NULL,
  high_mz FLOAT NOT NULL,
  contributor_quality FLOAT NOT NULL
);
 

CREATE TABLE peak_labeled (
  peak_labeled_id SERIAL PRIMARY KEY NOT NULL,
  scan_id INT NOT NULL REFERENCES scan ON DELETE CASCADE ON UPDATE CASCADE,
  mz_value FLOAT NOT NULL,
  intensity_value FLOAT NOT NULL,
  monoisotopic BOOL NOT NULL,
  charge_count INT NOT NULL,
  FWHM FLOAT NOT NULL,
  signal_to_noise FLOAT NULL,
  contributor_quality FLOAT NOT NULL
  --CONSTRAINT secondary_peak_labeled UNIQUE(scan_id, mz_value)
);

CREATE TABLE persubstitution (
  persubstitution_id SERIAL PRIMARY KEY NOT NULL,
  abbreviation VARCHAR(10),
  name VARCHAR(255)
  -- CONSTRAINT secondary_persubstitution UNIQUE(abbreviation)		
);

CREATE TABLE reducing_end (
  reducing_end_id SERIAL PRIMARY KEY NOT NULL, 
  abbreviation VARCHAR(10),
  name VARCHAR(255)		
  -- CONSTRAINT secondary_reducing_end UNIQUE(abbreviation)		
);

CREATE TABLE annotation (
  annotation_id SERIAL PRIMARY KEY NOT NULL,	
  scan_id INT NOT NULL REFERENCES scan ON DELETE CASCADE ON UPDATE CASCADE,
  contributor_id INT NOT NULL REFERENCES core.contributor ON DELETE SET NULL ON UPDATE CASCADE,  
  parent_structure_id INT NULL REFERENCES core.glycan_sequence ON DELETE SET NULL ON UPDATE CASCADE,
  persubstitution_id INT NOT NULL REFERENCES persubstitution ON DELETE SET NULL ON UPDATE CASCADE,  
  reducing_end_id INT NOT NULL REFERENCES reducing_end ON DELETE SET NULL ON UPDATE CASCADE,  
  date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  contributor_quality FLOAT NOT NULL
);


CREATE TABLE peak_annotation (
  peak_annotation_id SERIAL PRIMARY KEY NOT NULL,
  peak_labeled_id INT NOT NULL REFERENCES peak_labeled ON DELETE CASCADE ON UPDATE CASCADE,
  annotation_id INT NOT NULL REFERENCES annotation ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE peak_annotated (
  peak_annotated_id SERIAL PRIMARY KEY NOT NULL,
  peak_annotation_id INT NOT NULL REFERENCES peak_annotation ON DELETE CASCADE ON UPDATE CASCADE,
  glyco_ct_id INT NOT NULL,
  sequence_gws VARCHAR(255) NOT NULL,
  formula VARCHAR(255) NOT NULL,
  formula_carry FLOAT NOT NULL,
  calculated_mass FLOAT NOT NULL,
  contributor_quality FLOAT NOT NULL
);

CREATE TABLE fragmentation (
  fragmentation_id SERIAL PRIMARY KEY NOT NULL,
  peak_annotated_id INT NOT NULL REFERENCES peak_annotated ON DELETE CASCADE ON UPDATE CASCADE,
  fragment_type VARCHAR(1) NULL,
  fragment_dc VARCHAR(255) NOT NULL,
  fragment_alt VARCHAR(255) NULL,
  fragment_position INT NULL,
  cleavage_one INT NULL,
  cleavage_two INT NULL
  --CONSTRAINT secondary_fragment UNIQUE(fragment_dc, peak_annotated_id)
);

CREATE TABLE ms_ms_relationship (
  ms_ms_relationship_id SERIAL PRIMARY KEY NOT NULL,
  scan_id INT NOT NULL REFERENCES scan ON DELETE CASCADE ON UPDATE CASCADE,
  parent_id INT NOT NULL REFERENCES scan ON DELETE CASCADE ON UPDATE CASCADE,
  precursor_mz FLOAT NOT NULL,
  precursor_intensity FLOAT NOT NULL,
  precursor_mass_window_low FLOAT NULL,
  precursor_mass_window_high FLOAT NOT NULL,
  precursor_charge INT NOT NULL,
  ms_ms_methode VARCHAR(100) NOT NULL
  --CONSTRAINT secondary_ms_ms_relationship UNIQUE(parent_id),
  --CONSTRAINT tertiar_ms_ms_relationship UNIQUE(scan_id,parent_id)
);

CREATE TABLE sum_average_relationship (
  sum_average_relationship_id SERIAL PRIMARY KEY NOT NULL,
  scan_id INT NOT NULL REFERENCES scan ON DELETE CASCADE ON UPDATE CASCADE,
  subset_scan_id INT NOT NULL REFERENCES scan ON DELETE CASCADE ON UPDATE CASCADE,
  method_of_combination_id INT NOT NULL REFERENCES method_of_combination ON DELETE SET NULL ON UPDATE CASCADE
  --CONSTRAINT secondary_sum_average_relationship UNIQUE(scan_id),
  --CONSTRAINT tertiar_sum_average_relationship UNIQUE(scan_id,subset_scan_id)
);

CREATE TABLE data_processing (
  data_processing_id SERIAL PRIMARY KEY NOT NULL,
  software_type_id INT NOT NULL REFERENCES software_type ON DELETE SET NULL ON UPDATE CASCADE,
  software_id INT NOT NULL REFERENCES software ON DELETE CASCADE ON UPDATE CASCADE,
  scan_id INT NOT NULL REFERENCES scan ON DELETE CASCADE ON UPDATE CASCADE,
  spot_integration BOOL NOT NULL,
  intensity_cutoff FLOAT NOT NULL DEFAULT '0',
  format VARCHAR(20) NOT NULL,
  software_order INT NOT NULL DEFAULT '1'
  --CONSTRAINT secondary_data_processing UNIQUE(software_order, scan_id)
);

CREATE TABLE peak_annotated_to_ion (
  peak_annotated_to_ion_id SERIAL PRIMARY KEY NOT NULL,
  peak_annotated_id INT NOT NULL REFERENCES peak_annotated ON DELETE CASCADE ON UPDATE CASCADE,
  ion_id INT NOT NULL REFERENCES ion ON DELETE CASCADE ON UPDATE CASCADE,
  charge INT NOT NULL,
  gain BOOL NOT NULL
  --CONSTRAINT secondary_peak_annotated_to_ion UNIQUE(ion_id, peak_annotated_id)
);


CREATE TABLE device_settings (
  device_settings_id SERIAL PRIMARY KEY NOT NULL,
  acquisition_id INT NOT NULL REFERENCES acquisition ON DELETE CASCADE ON UPDATE CASCADE,
  contributor_quality FLOAT NOT NULL
  --CONSTRAINT secondary_device_settings UNIQUE(acquisition_id)
);

CREATE TABLE maldi_parameter (
  maldi_parameter_id SERIAL PRIMARY KEY NOT NULL,
  device_settings_id INT NOT NULL REFERENCES device_settings ON DELETE CASCADE ON UPDATE CASCADE,
  maldi_matrix_id INT NOT NULL REFERENCES maldi_matrix ON DELETE SET NULL ON UPDATE CASCADE,
  spot_diameter FLOAT NOT NULL,
  spot_type VARCHAR(255) NOT NULL
  --CONSTRAINT secondary_maldi_parameter UNIQUE(device_settings_id)
);

CREATE TABLE laser_parameter (
  laser_parameter_id SERIAL PRIMARY KEY NOT NULL,
  device_settings_id INT NOT NULL REFERENCES device_settings ON DELETE CASCADE ON UPDATE CASCADE,
  laser_id INT NOT NULL REFERENCES laser ON DELETE CASCADE ON UPDATE CASCADE,
  laser_shoot_count INT NOT NULL,
  laser_frequency FLOAT NOT NULL,
  laser_intensity FLOAT NOT NULL,
  laser_focus FLOAT NOT NULL,
  ionisation_energy FLOAT NULL
  --CONSTRAINT secondary_laser_parameter UNIQUE(device_settings_id)
);

CREATE TABLE tof_parameter (
  tof_parameter_id SERIAL PRIMARY KEY NOT NULL,
  device_settings_id INT NOT NULL REFERENCES device_settings ON DELETE CASCADE ON UPDATE CASCADE,
  reflector_state BOOL NULL,
  accelerator_grid_voltage FLOAT NULL,
  delay_extration_time FLOAT NULL
  --CONSTRAINT secondary_tof_parameter UNIQUE(device_settings_id)
);

CREATE TABLE iontrap_parameter (
  iontrap_parameter_id SERIAL PRIMARY KEY NOT NULL,
  device_settings_id INT NOT NULL REFERENCES device_settings ON DELETE CASCADE ON UPDATE CASCADE,
  ion_count INT NOT NULL
  --CONSTRAINT secondary_iontrap_parameter UNIQUE(device_settings_id)
);

CREATE TABLE fragmentation_parameter (
  fragmentation_parameter_id SERIAL PRIMARY KEY NOT NULL,
  device_settings_id INT NOT NULL REFERENCES device_settings ON DELETE CASCADE ON UPDATE CASCADE,
  fragmentation_type_id INT NOT NULL REFERENCES fragmentation_type ON DELETE CASCADE ON UPDATE CASCADE,
  collision_gas VARCHAR(100) NOT NULL,
  pressure FLOAT NOT NULL,
  collision_energie FLOAT NOT NULL
  --CONSTRAINT secondary_cid_parameter UNIQUE(device_settings_id)
);

CREATE TABLE esi_parameter (
  esi_parameter_id SERIAL PRIMARY KEY NOT NULL,
  device_settings_id INT NOT NULL REFERENCES device_settings ON DELETE CASCADE ON UPDATE CASCADE,
  dry_gas VARCHAR(50) NULL,
  flow_rate FLOAT NULL,
  temperatur FLOAT NULL,
  voltage_capillary FLOAT NULL,
  voltage_end_plate FLOAT NULL,
  solvent VARCHAR(255) NOT NULL
  --CONSTRAINT secondary_esi_parameter UNIQUE(device_settings_id)
);

CREATE TABLE mass_detector_parameter (
  mass_detector_parameter_id SERIAL PRIMARY KEY NOT NULL,
  device_settings_id INT NOT NULL REFERENCES device_settings ON DELETE CASCADE ON UPDATE CASCADE,
  mass_detector_id INT NOT NULL REFERENCES mass_detector ON DELETE CASCADE ON UPDATE CASCADE,
  digital_resolution INT NOT NULL,
  sampling_frequency INT NOT NULL
  --CONSTRAINT secondary_mass_detector_parameter UNIQUE(device_settings_id)
);

CREATE TABLE source_parameter (
  source_parameter_id SERIAL PRIMARY KEY NOT NULL,
  device_settings_id INT NOT NULL REFERENCES device_settings ON DELETE CASCADE ON UPDATE CASCADE,
  source_id INT NOT NULL REFERENCES source ON DELETE CASCADE ON UPDATE CASCADE
  --CONSTRAINT secondary_source_parameter UNIQUE(device_settings_id)
);

CREATE TABLE small_molecule (
  small_molecule_id SERIAL PRIMARY KEY NOT NULL,
  name VARCHAR(255) NOT NULL
  --CONSTRAINT secondary_small_molecule UNIQUE(name)
);

CREATE TABLE peak_annotated_to_small_molecule (
  peak_annotated_to_small_molecule_id SERIAL PRIMARY KEY NOT NULL,
  small_molecule_id INT NOT NULL REFERENCES small_molecule ON DELETE CASCADE ON UPDATE CASCADE,
  peak_annotated_id INT NOT NULL REFERENCES peak_annotated ON DELETE CASCADE ON UPDATE CASCADE,
  operation VARCHAR(255) NOT NULL
);

CREATE TABLE small_molecule_composition (
  small_molecule_composition_id SERIAL PRIMARY KEY NOT NULL,
  small_molecule_id INT NOT NULL REFERENCES small_molecule ON DELETE CASCADE ON UPDATE CASCADE,
  atom_type INT NOT NULL,
  number INT NOT NULL
);

CREATE TABLE analyser_parameter (
  analyser_parameter_id SERIAL PRIMARY KEY NOT NULL,
  device_settings_id INT NOT NULL REFERENCES device_settings ON DELETE CASCADE ON UPDATE CASCADE,
  analyser_id INT NOT NULL REFERENCES analyser ON DELETE CASCADE ON UPDATE CASCADE,
  tandem_scan_method_id INT NOT NULL REFERENCES tandem_scan_method ON DELETE CASCADE ON UPDATE CASCADE,
  resolution_type VARCHAR(45) NOT NULL,
  resolution_method VARCHAR(45) NOT NULL,
  resolution FLOAT NOT NULL
  --CONSTRAINT secondary_analyser_parameter UNIQUE(device_settings_id,analyser_id)
);
