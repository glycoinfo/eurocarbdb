SET search_path TO ms;

INSERT INTO manufacturer (manufacturer_id, name, url) VALUES (1, 'Agilent', NULL);
INSERT INTO manufacturer (manufacturer_id, name, url) VALUES (2, 'Bruker Daltonik', NULL);
INSERT INTO manufacturer (manufacturer_id, name, url) VALUES (3, 'Applied Biosystem', NULL);
INSERT INTO manufacturer (manufacturer_id, name, url) VALUES (4, 'Waters', NULL);

INSERT INTO device (device_id, manufacturer_id, model, ionisation_type) VALUES (1, 1, 'XCT', 'ESI');
INSERT INTO device (device_id, manufacturer_id, model, ionisation_type) VALUES (2, 2, 'Ultraflex I', 'MALDI');
INSERT INTO device (device_id, manufacturer_id, model, ionisation_type) VALUES (3, 2, 'Ultraflex II', 'MALDI');
INSERT INTO device (device_id, manufacturer_id, model, ionisation_type) VALUES (4, 2, 'Esquire3000', 'ESI');
INSERT INTO device (device_id, manufacturer_id, model, ionisation_type) VALUES (5, 2, 'HCT ultra', 'ESI');
INSERT INTO device (device_id, manufacturer_id, model, ionisation_type) VALUES (6, 2, 'MicrOTOF', 'ESI');
INSERT INTO device (device_id, manufacturer_id, model, ionisation_type) VALUES (7, 2, 'MicrOTOF-Q', 'ESI');
INSERT INTO device (device_id, manufacturer_id, model, ionisation_type) VALUES (8, 2, 'Apex IV', 'ESI');
INSERT INTO device (device_id, manufacturer_id, model, ionisation_type) VALUES (9, 3, 'Voyager', 'MALDI');
INSERT INTO device (device_id, manufacturer_id, model, ionisation_type) VALUES (10, 3, '4800 TOF/TOF', 'MALDI');
INSERT INTO device (device_id, manufacturer_id, model, ionisation_type) VALUES (11, 3, 'Q-Star', 'ESI');
INSERT INTO device (device_id, manufacturer_id, model, ionisation_type) VALUES (12, 4, 'Q-TOF', 'ESI');
INSERT INTO device (device_id, manufacturer_id, model, ionisation_type) VALUES (13, 4, 'Q-TOF premiere', 'ESI');

INSERT INTO persubstitution (persubstitution_id, abbreviation, name) VALUES (1, 'Und', 'Underivatised');
INSERT INTO persubstitution (persubstitution_id, abbreviation, name) VALUES (2, 'perMe', 'Per-methylation');
INSERT INTO persubstitution (persubstitution_id, abbreviation, name) VALUES (3, 'perDMe', 'Per-deuteromethylation');
INSERT INTO persubstitution (persubstitution_id, abbreviation, name) VALUES (4, 'perAc', 'Per-acetylation');
INSERT INTO persubstitution (persubstitution_id, abbreviation, name) VALUES (5, 'perDAc', 'Per-deuteroacetylation');

INSERT INTO reducing_end (reducing_end_id, abbreviation, name) VALUES (1, 'freeEnd', 'Free reducing end');
INSERT INTO reducing_end (reducing_end_id, abbreviation, name) VALUES (2, 'redEnd', 'Reduced reducing end');
INSERT INTO reducing_end (reducing_end_id, abbreviation, name) VALUES (3, 'PA', '2-Aminopyridine');
INSERT INTO reducing_end (reducing_end_id, abbreviation, name) VALUES (4, '2AP', '2-Aminopyridine');
INSERT INTO reducing_end (reducing_end_id, abbreviation, name) VALUES (5, '2AB', '2-Aminobenzamide');
INSERT INTO reducing_end (reducing_end_id, abbreviation, name) VALUES (6, 'AA', 'Anthranilic Acid');
INSERT INTO reducing_end (reducing_end_id, abbreviation, name) VALUES (7, 'DAP', '2,6-Diaminopyridine');
INSERT INTO reducing_end (reducing_end_id, abbreviation, name) VALUES (8, '4AB', '4-Aminobenzamidine');
INSERT INTO reducing_end (reducing_end_id, abbreviation, name) VALUES (9, 'DAPMAB', '4-(N-[2,4-Diamino-6-pteridinylmethyl]amino)benzoic acid');
INSERT INTO reducing_end (reducing_end_id, abbreviation, name) VALUES (10, 'AMC', '7-Amino-4-methylcoumarin');
INSERT INTO reducing_end (reducing_end_id, abbreviation, name) VALUES (11, '6AQ', '6-Aminoquinoline');
INSERT INTO reducing_end (reducing_end_id, abbreviation, name) VALUES (12, '2AAc', '2-Aminoacridone');
INSERT INTO reducing_end (reducing_end_id, abbreviation, name) VALUES (13, 'FMC', '9-Fluorenylmethyl carbazate');
INSERT INTO reducing_end (reducing_end_id, abbreviation, name) VALUES (14, 'DH', 'Dansylhydrazine');

INSERT INTO ion (ion_id, ion_type) VALUES (1, 'H');
INSERT INTO ion (ion_id, ion_type) VALUES (2, 'Na');
INSERT INTO ion (ion_id, ion_type) VALUES (3, 'Li');
INSERT INTO ion (ion_id, ion_type) VALUES (4, 'K');