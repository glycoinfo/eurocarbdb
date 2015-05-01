#
# The original sequence entered was:
# ACDEFGHIKLMNPQRSTVWY
#


# Expressed in NMR-STAR, this sequence is:

_Mol_residue_sequence
;
ACDEFGHIKLMNPQRSTVWY
;

loop_
	_Residue_seq_code
	_Residue_author_seq_code
	_Residue_label

1   @   ALA	2   @   CYS	3   @   ASP	4   @   GLU	5   @   PHE
6   @   GLY	7   @   HIS	8   @   ILE	9   @   LYS	10   @   LEU
11   @   MET	12   @   ASN	13   @   PRO	14   @   GLN	15   @   ARG
16   @   SER	17   @   THR	18   @   VAL	19   @   TRP	20   @   TYR


stop_

###################################################################
#      Chemical Shift Ambiguity Code Definitions                  #
#                                                                 #
#    Codes            Definition                                  #
#                                                                 #
#      1             Unique                                       #
#      2             Ambiguity of geminal atoms or geminal methyl #
#                         proton groups                           #
#      3             Aromatic atoms on opposite sides of the ring #
#                        (e.g. Tyr HE1 and HE2 protons)           #
#      4             Intraresidue ambiguities (e.g. Lys HG and    #
#                         HD protons)                             #
#      5             Interresidue ambiguities (Lys 12 vs. Lys 27) #
#      9             Ambiguous, specific ambiguity not defined    #
#                                                                 #
###################################################################


# INSTRUCTIONS
# 1) Replace the @-signs with appropriate values.
# 2) Text comments concerning the assignments can be
#    supplied in the full deposition.
# 3) Feel free to add or delete rows to the table as needed.
#    The row numbers (_Atom_shift_assign_ID values) will be
#    re-assigned to sequential values by BMRB


# The atom table chosen for this sequence is:

loop_
     _Atom_shift_assign_ID
     _Residue_seq_code
     _Residue_label
     _Atom_name
     _Atom_type
     _Chem_shift_value
     _Chem_shift_value_error
     _Chem_shift_ambiguity_code

#
#Atom	Residue
#shift	Seq	Residue	Atom	Atom	Shift/	Error/	Ambiguity
#assign	code	Label	Name	Type	ppm	ppm	Code
#---------------------------------------------------------------
#

1	 1	ALA	H 	H	@ 	@ 	@ 
2	 1	ALA	HA 	H	@ 	@ 	@ 
3	 1	ALA	HB1 	H	@ 	@ 	@ 
4	 1	ALA	HB2 	H	@ 	@ 	@ 
5	 1	ALA	HB3 	H	@ 	@ 	@ 
6	 1	ALA	C 	C	@ 	@ 	@ 
7	 1	ALA	CA 	C	@ 	@ 	@ 
8	 1	ALA	CB 	C	@ 	@ 	@ 
9	 1	ALA	N 	N	@ 	@ 	@ 
10	 1	ALA	O 	O	@ 	@ 	@ 
11	 2	CYS	H 	H	@ 	@ 	@ 
12	 2	CYS	HA 	H	@ 	@ 	@ 
13	 2	CYS	HB2 	H	@ 	@ 	@ 
14	 2	CYS	HB3 	H	@ 	@ 	@ 
15	 2	CYS	HG 	H	@ 	@ 	@ 
16	 2	CYS	C 	C	@ 	@ 	@ 
17	 2	CYS	CA 	C	@ 	@ 	@ 
18	 2	CYS	CB 	C	@ 	@ 	@ 
19	 2	CYS	N 	N	@ 	@ 	@ 
20	 2	CYS	O 	O	@ 	@ 	@ 
21	 2	CYS	SG 	S	@ 	@ 	@ 
22	 3	ASP	H 	H	@ 	@ 	@ 
23	 3	ASP	HA 	H	@ 	@ 	@ 
24	 3	ASP	HB2 	H	@ 	@ 	@ 
25	 3	ASP	HB3 	H	@ 	@ 	@ 
26	 3	ASP	HD2 	H	@ 	@ 	@ 
27	 3	ASP	C 	C	@ 	@ 	@ 
28	 3	ASP	CA 	C	@ 	@ 	@ 
29	 3	ASP	CB 	C	@ 	@ 	@ 
30	 3	ASP	CG 	C	@ 	@ 	@ 
31	 3	ASP	N 	N	@ 	@ 	@ 
32	 3	ASP	O 	O	@ 	@ 	@ 
33	 3	ASP	OD1 	O	@ 	@ 	@ 
34	 3	ASP	OD2 	O	@ 	@ 	@ 
35	 4	GLU	H 	H	@ 	@ 	@ 
36	 4	GLU	HA 	H	@ 	@ 	@ 
37	 4	GLU	HB2 	H	@ 	@ 	@ 
38	 4	GLU	HB3 	H	@ 	@ 	@ 
39	 4	GLU	HG2 	H	@ 	@ 	@ 
40	 4	GLU	HG3 	H	@ 	@ 	@ 
41	 4	GLU	HE2 	H	@ 	@ 	@ 
42	 4	GLU	C 	C	@ 	@ 	@ 
43	 4	GLU	CA 	C	@ 	@ 	@ 
44	 4	GLU	CB 	C	@ 	@ 	@ 
45	 4	GLU	CG 	C	@ 	@ 	@ 
46	 4	GLU	CD 	C	@ 	@ 	@ 
47	 4	GLU	N 	N	@ 	@ 	@ 
48	 4	GLU	O 	O	@ 	@ 	@ 
49	 4	GLU	OE1 	O	@ 	@ 	@ 
50	 4	GLU	OE2 	O	@ 	@ 	@ 
51	 5	PHE	H 	H	@ 	@ 	@ 
52	 5	PHE	HA 	H	@ 	@ 	@ 
53	 5	PHE	HB2 	H	@ 	@ 	@ 
54	 5	PHE	HB3 	H	@ 	@ 	@ 
55	 5	PHE	HD1 	H	@ 	@ 	@ 
56	 5	PHE	HD2 	H	@ 	@ 	@ 
57	 5	PHE	HE1 	H	@ 	@ 	@ 
58	 5	PHE	HE2 	H	@ 	@ 	@ 
59	 5	PHE	HZ 	H	@ 	@ 	@ 
60	 5	PHE	C 	C	@ 	@ 	@ 
61	 5	PHE	CA 	C	@ 	@ 	@ 
62	 5	PHE	CB 	C	@ 	@ 	@ 
63	 5	PHE	CG 	C	@ 	@ 	@ 
64	 5	PHE	CD1 	C	@ 	@ 	@ 
65	 5	PHE	CD2 	C	@ 	@ 	@ 
66	 5	PHE	CE1 	C	@ 	@ 	@ 
67	 5	PHE	CE2 	C	@ 	@ 	@ 
68	 5	PHE	CZ 	C	@ 	@ 	@ 
69	 5	PHE	N 	N	@ 	@ 	@ 
70	 5	PHE	O 	O	@ 	@ 	@ 
71	 6	GLY	H 	H	@ 	@ 	@ 
72	 6	GLY	HA2 	H	@ 	@ 	@ 
73	 6	GLY	HA3 	H	@ 	@ 	@ 
74	 6	GLY	C 	C	@ 	@ 	@ 
75	 6	GLY	CA 	C	@ 	@ 	@ 
76	 6	GLY	N 	N	@ 	@ 	@ 
77	 6	GLY	O 	O	@ 	@ 	@ 
78	 7	HIS	H 	H	@ 	@ 	@ 
79	 7	HIS	HA 	H	@ 	@ 	@ 
80	 7	HIS	HB2 	H	@ 	@ 	@ 
81	 7	HIS	HB3 	H	@ 	@ 	@ 
82	 7	HIS	HD1 	H	@ 	@ 	@ 
83	 7	HIS	HD2 	H	@ 	@ 	@ 
84	 7	HIS	HE1 	H	@ 	@ 	@ 
85	 7	HIS	HE2 	H	@ 	@ 	@ 
86	 7	HIS	C 	C	@ 	@ 	@ 
87	 7	HIS	CA 	C	@ 	@ 	@ 
88	 7	HIS	CB 	C	@ 	@ 	@ 
89	 7	HIS	CG 	C	@ 	@ 	@ 
90	 7	HIS	CD2 	C	@ 	@ 	@ 
91	 7	HIS	CE1 	C	@ 	@ 	@ 
92	 7	HIS	N 	N	@ 	@ 	@ 
93	 7	HIS	ND1 	N	@ 	@ 	@ 
94	 7	HIS	NE2 	N	@ 	@ 	@ 
95	 7	HIS	O 	O	@ 	@ 	@ 
96	 8	ILE	H 	H	@ 	@ 	@ 
97	 8	ILE	HA 	H	@ 	@ 	@ 
98	 8	ILE	HB 	H	@ 	@ 	@ 
99	 8	ILE	HG12 	H	@ 	@ 	@ 
100	 8	ILE	HG13 	H	@ 	@ 	@ 
101	 8	ILE	HG21 	H	@ 	@ 	@ 
102	 8	ILE	HG22 	H	@ 	@ 	@ 
103	 8	ILE	HG23 	H	@ 	@ 	@ 
104	 8	ILE	HD11 	H	@ 	@ 	@ 
105	 8	ILE	HD12 	H	@ 	@ 	@ 
106	 8	ILE	HD13 	H	@ 	@ 	@ 
107	 8	ILE	C 	C	@ 	@ 	@ 
108	 8	ILE	CA 	C	@ 	@ 	@ 
109	 8	ILE	CB 	C	@ 	@ 	@ 
110	 8	ILE	CG1 	C	@ 	@ 	@ 
111	 8	ILE	CG2 	C	@ 	@ 	@ 
112	 8	ILE	CD1 	C	@ 	@ 	@ 
113	 8	ILE	N 	N	@ 	@ 	@ 
114	 8	ILE	O 	O	@ 	@ 	@ 
115	 9	LYS	H 	H	@ 	@ 	@ 
116	 9	LYS	HA 	H	@ 	@ 	@ 
117	 9	LYS	HB2 	H	@ 	@ 	@ 
118	 9	LYS	HB3 	H	@ 	@ 	@ 
119	 9	LYS	HG2 	H	@ 	@ 	@ 
120	 9	LYS	HG3 	H	@ 	@ 	@ 
121	 9	LYS	HD2 	H	@ 	@ 	@ 
122	 9	LYS	HD3 	H	@ 	@ 	@ 
123	 9	LYS	HE2 	H	@ 	@ 	@ 
124	 9	LYS	HE3 	H	@ 	@ 	@ 
125	 9	LYS	HZ1 	H	@ 	@ 	@ 
126	 9	LYS	HZ2 	H	@ 	@ 	@ 
127	 9	LYS	HZ3 	H	@ 	@ 	@ 
128	 9	LYS	C 	C	@ 	@ 	@ 
129	 9	LYS	CA 	C	@ 	@ 	@ 
130	 9	LYS	CB 	C	@ 	@ 	@ 
131	 9	LYS	CG 	C	@ 	@ 	@ 
132	 9	LYS	CD 	C	@ 	@ 	@ 
133	 9	LYS	CE 	C	@ 	@ 	@ 
134	 9	LYS	N 	N	@ 	@ 	@ 
135	 9	LYS	NZ 	N	@ 	@ 	@ 
136	 9	LYS	O 	O	@ 	@ 	@ 
137	 10	LEU	H 	H	@ 	@ 	@ 
138	 10	LEU	HA 	H	@ 	@ 	@ 
139	 10	LEU	HB2 	H	@ 	@ 	@ 
140	 10	LEU	HB3 	H	@ 	@ 	@ 
141	 10	LEU	HG 	H	@ 	@ 	@ 
142	 10	LEU	HD11 	H	@ 	@ 	@ 
143	 10	LEU	HD12 	H	@ 	@ 	@ 
144	 10	LEU	HD13 	H	@ 	@ 	@ 
145	 10	LEU	HD21 	H	@ 	@ 	@ 
146	 10	LEU	HD22 	H	@ 	@ 	@ 
147	 10	LEU	HD23 	H	@ 	@ 	@ 
148	 10	LEU	C 	C	@ 	@ 	@ 
149	 10	LEU	CA 	C	@ 	@ 	@ 
150	 10	LEU	CB 	C	@ 	@ 	@ 
151	 10	LEU	CG 	C	@ 	@ 	@ 
152	 10	LEU	CD1 	C	@ 	@ 	@ 
153	 10	LEU	CD2 	C	@ 	@ 	@ 
154	 10	LEU	N 	N	@ 	@ 	@ 
155	 10	LEU	O 	O	@ 	@ 	@ 
156	 11	MET	H 	H	@ 	@ 	@ 
157	 11	MET	HA 	H	@ 	@ 	@ 
158	 11	MET	HB2 	H	@ 	@ 	@ 
159	 11	MET	HB3 	H	@ 	@ 	@ 
160	 11	MET	HG2 	H	@ 	@ 	@ 
161	 11	MET	HG3 	H	@ 	@ 	@ 
162	 11	MET	HE1 	H	@ 	@ 	@ 
163	 11	MET	HE2 	H	@ 	@ 	@ 
164	 11	MET	HE3 	H	@ 	@ 	@ 
165	 11	MET	C 	C	@ 	@ 	@ 
166	 11	MET	CA 	C	@ 	@ 	@ 
167	 11	MET	CB 	C	@ 	@ 	@ 
168	 11	MET	CG 	C	@ 	@ 	@ 
169	 11	MET	CE 	C	@ 	@ 	@ 
170	 11	MET	N 	N	@ 	@ 	@ 
171	 11	MET	O 	O	@ 	@ 	@ 
172	 11	MET	SD 	S	@ 	@ 	@ 
173	 12	ASN	H 	H	@ 	@ 	@ 
174	 12	ASN	HA 	H	@ 	@ 	@ 
175	 12	ASN	HB2 	H	@ 	@ 	@ 
176	 12	ASN	HB3 	H	@ 	@ 	@ 
177	 12	ASN	HD21 	H	@ 	@ 	@ 
178	 12	ASN	HD22 	H	@ 	@ 	@ 
179	 12	ASN	C 	C	@ 	@ 	@ 
180	 12	ASN	CA 	C	@ 	@ 	@ 
181	 12	ASN	CB 	C	@ 	@ 	@ 
182	 12	ASN	CG 	C	@ 	@ 	@ 
183	 12	ASN	N 	N	@ 	@ 	@ 
184	 12	ASN	ND2 	N	@ 	@ 	@ 
185	 12	ASN	O 	O	@ 	@ 	@ 
186	 12	ASN	OD1 	O	@ 	@ 	@ 
187	 13	PRO	HA 	H	@ 	@ 	@ 
188	 13	PRO	HB2 	H	@ 	@ 	@ 
189	 13	PRO	HB3 	H	@ 	@ 	@ 
190	 13	PRO	HG2 	H	@ 	@ 	@ 
191	 13	PRO	HG3 	H	@ 	@ 	@ 
192	 13	PRO	HD2 	H	@ 	@ 	@ 
193	 13	PRO	HD3 	H	@ 	@ 	@ 
194	 13	PRO	C 	C	@ 	@ 	@ 
195	 13	PRO	CA 	C	@ 	@ 	@ 
196	 13	PRO	CB 	C	@ 	@ 	@ 
197	 13	PRO	CG 	C	@ 	@ 	@ 
198	 13	PRO	CD 	C	@ 	@ 	@ 
199	 13	PRO	N 	N	@ 	@ 	@ 
200	 13	PRO	O 	O	@ 	@ 	@ 
201	 14	GLN	H 	H	@ 	@ 	@ 
202	 14	GLN	HA 	H	@ 	@ 	@ 
203	 14	GLN	HB2 	H	@ 	@ 	@ 
204	 14	GLN	HB3 	H	@ 	@ 	@ 
205	 14	GLN	HG2 	H	@ 	@ 	@ 
206	 14	GLN	HG3 	H	@ 	@ 	@ 
207	 14	GLN	HE21 	H	@ 	@ 	@ 
208	 14	GLN	HE22 	H	@ 	@ 	@ 
209	 14	GLN	C 	C	@ 	@ 	@ 
210	 14	GLN	CA 	C	@ 	@ 	@ 
211	 14	GLN	CB 	C	@ 	@ 	@ 
212	 14	GLN	CG 	C	@ 	@ 	@ 
213	 14	GLN	CD 	C	@ 	@ 	@ 
214	 14	GLN	N 	N	@ 	@ 	@ 
215	 14	GLN	NE2 	N	@ 	@ 	@ 
216	 14	GLN	O 	O	@ 	@ 	@ 
217	 14	GLN	OE1 	O	@ 	@ 	@ 
218	 15	ARG	H 	H	@ 	@ 	@ 
219	 15	ARG	HA 	H	@ 	@ 	@ 
220	 15	ARG	HB2 	H	@ 	@ 	@ 
221	 15	ARG	HB3 	H	@ 	@ 	@ 
222	 15	ARG	HG2 	H	@ 	@ 	@ 
223	 15	ARG	HG3 	H	@ 	@ 	@ 
224	 15	ARG	HD2 	H	@ 	@ 	@ 
225	 15	ARG	HD3 	H	@ 	@ 	@ 
226	 15	ARG	HE 	H	@ 	@ 	@ 
227	 15	ARG	HH11 	H	@ 	@ 	@ 
228	 15	ARG	HH12 	H	@ 	@ 	@ 
229	 15	ARG	HH21 	H	@ 	@ 	@ 
230	 15	ARG	HH22 	H	@ 	@ 	@ 
231	 15	ARG	C 	C	@ 	@ 	@ 
232	 15	ARG	CA 	C	@ 	@ 	@ 
233	 15	ARG	CB 	C	@ 	@ 	@ 
234	 15	ARG	CG 	C	@ 	@ 	@ 
235	 15	ARG	CD 	C	@ 	@ 	@ 
236	 15	ARG	CZ 	C	@ 	@ 	@ 
237	 15	ARG	N 	N	@ 	@ 	@ 
238	 15	ARG	NE 	N	@ 	@ 	@ 
239	 15	ARG	NH1 	N	@ 	@ 	@ 
240	 15	ARG	NH2 	N	@ 	@ 	@ 
241	 15	ARG	O 	O	@ 	@ 	@ 
242	 16	SER	H 	H	@ 	@ 	@ 
243	 16	SER	HA 	H	@ 	@ 	@ 
244	 16	SER	HB2 	H	@ 	@ 	@ 
245	 16	SER	HB3 	H	@ 	@ 	@ 
246	 16	SER	HG 	H	@ 	@ 	@ 
247	 16	SER	C 	C	@ 	@ 	@ 
248	 16	SER	CA 	C	@ 	@ 	@ 
249	 16	SER	CB 	C	@ 	@ 	@ 
250	 16	SER	N 	N	@ 	@ 	@ 
251	 16	SER	O 	O	@ 	@ 	@ 
252	 16	SER	OG 	O	@ 	@ 	@ 
253	 17	THR	H 	H	@ 	@ 	@ 
254	 17	THR	HA 	H	@ 	@ 	@ 
255	 17	THR	HB 	H	@ 	@ 	@ 
256	 17	THR	HG1 	H	@ 	@ 	@ 
257	 17	THR	HG21 	H	@ 	@ 	@ 
258	 17	THR	HG22 	H	@ 	@ 	@ 
259	 17	THR	HG23 	H	@ 	@ 	@ 
260	 17	THR	C 	C	@ 	@ 	@ 
261	 17	THR	CA 	C	@ 	@ 	@ 
262	 17	THR	CB 	C	@ 	@ 	@ 
263	 17	THR	CG2 	C	@ 	@ 	@ 
264	 17	THR	N 	N	@ 	@ 	@ 
265	 17	THR	O 	O	@ 	@ 	@ 
266	 17	THR	OG1 	O	@ 	@ 	@ 
267	 18	VAL	H 	H	@ 	@ 	@ 
268	 18	VAL	HA 	H	@ 	@ 	@ 
269	 18	VAL	HB 	H	@ 	@ 	@ 
270	 18	VAL	HG11 	H	@ 	@ 	@ 
271	 18	VAL	HG12 	H	@ 	@ 	@ 
272	 18	VAL	HG13 	H	@ 	@ 	@ 
273	 18	VAL	HG21 	H	@ 	@ 	@ 
274	 18	VAL	HG22 	H	@ 	@ 	@ 
275	 18	VAL	HG23 	H	@ 	@ 	@ 
276	 18	VAL	C 	C	@ 	@ 	@ 
277	 18	VAL	CA 	C	@ 	@ 	@ 
278	 18	VAL	CB 	C	@ 	@ 	@ 
279	 18	VAL	CG1 	C	@ 	@ 	@ 
280	 18	VAL	CG2 	C	@ 	@ 	@ 
281	 18	VAL	N 	N	@ 	@ 	@ 
282	 18	VAL	O 	O	@ 	@ 	@ 
283	 19	TRP	H 	H	@ 	@ 	@ 
284	 19	TRP	HA 	H	@ 	@ 	@ 
285	 19	TRP	HB2 	H	@ 	@ 	@ 
286	 19	TRP	HB3 	H	@ 	@ 	@ 
287	 19	TRP	HD1 	H	@ 	@ 	@ 
288	 19	TRP	HE1 	H	@ 	@ 	@ 
289	 19	TRP	HE3 	H	@ 	@ 	@ 
290	 19	TRP	HZ2 	H	@ 	@ 	@ 
291	 19	TRP	HZ3 	H	@ 	@ 	@ 
292	 19	TRP	HH2 	H	@ 	@ 	@ 
293	 19	TRP	C 	C	@ 	@ 	@ 
294	 19	TRP	CA 	C	@ 	@ 	@ 
295	 19	TRP	CB 	C	@ 	@ 	@ 
296	 19	TRP	CG 	C	@ 	@ 	@ 
297	 19	TRP	CD1 	C	@ 	@ 	@ 
298	 19	TRP	CD2 	C	@ 	@ 	@ 
299	 19	TRP	CE2 	C	@ 	@ 	@ 
300	 19	TRP	CE3 	C	@ 	@ 	@ 
301	 19	TRP	CZ2 	C	@ 	@ 	@ 
302	 19	TRP	CZ3 	C	@ 	@ 	@ 
303	 19	TRP	CH2 	C	@ 	@ 	@ 
304	 19	TRP	N 	N	@ 	@ 	@ 
305	 19	TRP	NE1 	N	@ 	@ 	@ 
306	 19	TRP	O 	O	@ 	@ 	@ 
307	 20	TYR	H 	H	@ 	@ 	@ 
308	 20	TYR	HA 	H	@ 	@ 	@ 
309	 20	TYR	HB2 	H	@ 	@ 	@ 
310	 20	TYR	HB3 	H	@ 	@ 	@ 
311	 20	TYR	HD1 	H	@ 	@ 	@ 
312	 20	TYR	HD2 	H	@ 	@ 	@ 
313	 20	TYR	HE1 	H	@ 	@ 	@ 
314	 20	TYR	HE2 	H	@ 	@ 	@ 
315	 20	TYR	HH 	H	@ 	@ 	@ 
316	 20	TYR	C 	C	@ 	@ 	@ 
317	 20	TYR	CA 	C	@ 	@ 	@ 
318	 20	TYR	CB 	C	@ 	@ 	@ 
319	 20	TYR	CG 	C	@ 	@ 	@ 
320	 20	TYR	CD1 	C	@ 	@ 	@ 
321	 20	TYR	CD2 	C	@ 	@ 	@ 
322	 20	TYR	CE1 	C	@ 	@ 	@ 
323	 20	TYR	CE2 	C	@ 	@ 	@ 
324	 20	TYR	CZ 	C	@ 	@ 	@ 
325	 20	TYR	N 	N	@ 	@ 	@ 
326	 20	TYR	O 	O	@ 	@ 	@ 
327	 20	TYR	OH 	O	@ 	@ 	@ 

stop_

# The following loop is used to define sets of Atom-shift assignment IDs that
# represent related ambiguous assignments taken from the above list of
# assigned chemical shifts.  Each element in the set should be separated by a
# comma, as shown in the example below, and is the assignment ID for a chemical
# shift assignment that has been given as ambiguity code of 4 or 5.  Each set
# indicates that the observed chemical shifts are related to the defined 
# atoms, but have not been assigned uniquely to a specific atom in the set.

loop_
  _Atom_shift_assign_ID_ambiguity   

#
#    Sets of Atom-shift Assignment Ambiguities
               #              
#    ------------------------------------------
# Example:    5,4,7
#
                @
stop_

