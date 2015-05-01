-------------------------------------------------------------------------------
-- Script to insert the required values to persubstitution table
-- Should be run after dropping and recreating MS schema by using r1916.sql
-- Data for Name: persubstitution; Type: TABLE DATA; Schema: ms; Owner: postgres
-- By: Khalifeh Al Jadda
-------------------------------------------------------------------------------

SET search_path = ms,core;


SELECT pg_catalog.setval('persubstitution_persubstitution_id_seq', 5, true);

INSERT INTO persubstitution (persubstitution_id, abbreviation, name) VALUES (1, 'Und', 'Underivatised');
INSERT INTO persubstitution (persubstitution_id, abbreviation, name) VALUES (2, 'perMe', 'Per-methylation');
INSERT INTO persubstitution (persubstitution_id, abbreviation, name) VALUES (3, 'perDMe', 'Per-deuteromethylation');
INSERT INTO persubstitution (persubstitution_id, abbreviation, name) VALUES (4, 'perAc', 'Per-acetylation');
INSERT INTO persubstitution (persubstitution_id, abbreviation, name) VALUES (5, 'perDAc', 'Per-deuteroacetylation');

------------------------------------------------------------------------------------
-- Filling reducing_end table
------------------------------------------------------------------------------------
SELECT pg_catalog.setval('reducing_end_reducing_end_id_seq', 18, true);

INSERT INTO reducing_end (reducing_end_id, abbreviation, name, uri) VALUES (18, 'freeEnd', 'freeEnd', NULL);

-------------------------------------------------------------------------------------
-- Filling ion table
-------------------------------------------------------------------------------------
SELECT pg_catalog.setval('ion_ion_id_seq', 33, true);


INSERT INTO ion (ion_id, ion_type, charge, positive, atomer) VALUES (1, 'Na', 1, true, true);
INSERT INTO ion (ion_id, ion_type, charge, positive, atomer) VALUES (2, 'H', 1, true, true);
INSERT INTO ion (ion_id, ion_type, charge, positive, atomer) VALUES (3, 'H', 1, false, true);
INSERT INTO ion (ion_id, ion_type, charge, positive, atomer) VALUES (24, 'Na', 2, true, true);
INSERT INTO ion (ion_id, ion_type, charge, positive, atomer) VALUES (25, 'H', 2, true, true);
INSERT INTO ion (ion_id, ion_type, charge, positive, atomer) VALUES (26, 'K', 1, true, true);
INSERT INTO ion (ion_id, ion_type, charge, positive, atomer) VALUES (27, 'Li', 2, true, true);
INSERT INTO ion (ion_id, ion_type, charge, positive, atomer) VALUES (28, 'Li', 3, true, true);
INSERT INTO ion (ion_id, ion_type, charge, positive, atomer) VALUES (29, 'Li', 4, true, true);
INSERT INTO ion (ion_id, ion_type, charge, positive, atomer) VALUES (30, 'Li', 6, true, true);
INSERT INTO ion (ion_id, ion_type, charge, positive, atomer) VALUES (31, 'Li', 7, true, true);
INSERT INTO ion (ion_id, ion_type, charge, positive, atomer) VALUES (32, 'Li', 5, true, true);
INSERT INTO ion (ion_id, ion_type, charge, positive, atomer) VALUES (33, 'Li', 1, true, true);

-------------------------------------------------------------------------------------------
-- Filling Peak_Processing table
-------------------------------------------------------------------------------------------
SELECT pg_catalog.setval('peak_processing_peak_processing_id_seq', 3, true);

INSERT INTO peak_processing (peak_processing_id, peak_processing_type) VALUES (1, 'Unknown');
INSERT INTO peak_processing (peak_processing_id, peak_processing_type) VALUES (2, 'Centroid');
INSERT INTO peak_processing (peak_processing_id, peak_processing_type) VALUES (3, 'deisotoped');


