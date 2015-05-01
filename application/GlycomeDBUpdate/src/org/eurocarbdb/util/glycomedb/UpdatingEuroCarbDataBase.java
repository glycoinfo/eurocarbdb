package org.eurocarbdb.util.glycomedb;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eurocarbdb.dataaccess.HibernateEntityManager;
import org.eurocarbdb.dataaccess.core.Contributor;
import org.eurocarbdb.dataaccess.core.GlycanSequence;
import org.eurocarbdb.dataaccess.core.GlycanSequenceReference;
import org.eurocarbdb.dataaccess.core.Reference;
import org.eurocarbdb.util.glycomedb.data.DataExport;
import org.eurocarbdb.util.glycomedb.data.StructureListType.Structure;
import org.eurocarbdb.util.glycomedb.data.StructureListType.Structure.Resource;
import org.hibernate.Query;

public class UpdatingEuroCarbDataBase {
	private Contributor m_user = null;
	private HibernateEntityManager m_manager = new HibernateEntityManager();
	private HashMap<String, CrossReference> m_hashDatabaseNames = new HashMap<String, CrossReference>();
	private HashMap<String, Integer> m_glycanSequences = new HashMap<String, Integer>();

	Configuration m_config;
	
	public void startLoading(DataExport de, Configuration a_config){
		
		//start hibernate manager
		m_manager.beginUnitOfWork();
		this.m_config = a_config;
		
		/*checking if user exists or not
		*if not exist, then add up current user to the database
		*/
		if(userexist(a_config) != null){
			m_user = userexist(a_config);
		}
		else{
			this.m_user = new Contributor();
			m_user.setContributorName(a_config.getUsername());
			m_user.setEmail(a_config.getUserEmailAddress());
			m_user.setFullName(a_config.getUserFullName());
			m_user.setIsBlocked(true);
			m_user.setIsActivated(true);
			m_user.setPassword("asgkjq34kjhfdslkj");
			this.m_manager.store(this.m_user);
		}
		
		//hash build reference
		
		buildHashMapWithReference(gettingReferencebyuser());
		buildHashMapWithSequences(gettingSequences());

		//end of hibernate manager
		m_manager.endUnitOfWork();
		
		if(de.ifError()){
			System.err.println("ifError");
			return;
		}
		else{
			if(!de.ifStructureList()){
				System.err.println("ifStructureListError");
				return;
			}
			else{
				for(Structure structure1 : de.getStructureList().getStructures()){
					if(structure1.getSequenceFormat().equals("glycoct_condensed")){
						m_manager.beginUnitOfWork();
						Integer t_hibernateSequence=this.getSequenceCT(structure1.getSequenceString());
						if(t_hibernateSequence == null){
							System.out.print("\nsequence format in xml does not match to anyone in the database\n");
						}
						else{
							System.out.print("\nsequence format in xml matches to one in the database\nUpdating....\n");
							//start updating database
							this.updatedatabase(t_hibernateSequence,structure1,a_config);
						}
						m_manager.endUnitOfWork();
					}
				}
				m_manager.beginUnitOfWork();
				//look up hashMap if false found, then delete entries from database.
				this.deleteUnusedEntries();
				m_manager.endUnitOfWork();
			}
		}
	}
	/*
	 * lookup m_hashDatabaseNames
	 * if false found, then delete data from database
	 */
	private void deleteUnusedEntries() {
		
		for(String t_key : m_hashDatabaseNames.keySet() )
		{
			CrossReference t_ref = m_hashDatabaseNames.get(t_key);
			if(t_ref.isUsed()){
				HashMap<Integer,CrossReferenceToStructure> hashGetSequence = t_ref.getSeqeunces();
				for(Integer t_IntKey : hashGetSequence.keySet()){
					CrossReferenceToStructure t_refStruc = hashGetSequence.get(t_IntKey);
					if(!t_refStruc.isUsed()){
						this.m_manager.remove( this.m_manager.lookup(GlycanSequenceReference.class, t_refStruc.getId()));
					}
				}
			}
			else{
				this.m_manager.remove( this.m_manager.lookup(Reference.class, t_ref.getID()));
			}
		}
		
	}

	private void buildHashMapWithSequences(List<Object[]> gettingSequences) {
		for(Object[] t_gly : gettingSequences){
			m_glycanSequences.put((String)t_gly[1], (Integer)t_gly[0]);
		}
	}

	private List<Object[]> gettingSequences() {
		Query t_query = m_manager.getQuery("org.eurocarbdb.dataaccess.core.GlycanSequence.GET_ALL_ID_SEQUENCE");
		@SuppressWarnings("unchecked")
		List <Object[]> t = (List<Object[]>) t_query.list();
		return t;
	}

	public void buildHashMapWithReference(List<Object[]> ref)
	{
		for(Object[] t_ref : ref)
		{
			String t_key = ((String)t_ref[1]) + "+" + ((String)t_ref[2]);
			CrossReference cre = this.m_hashDatabaseNames.get(t_key);
			if ( cre == null )
			{
				cre = new CrossReference();
				cre.setID((Integer)t_ref[0]);
				this.m_hashDatabaseNames.put(t_key, cre);
			}
			// add structure to crossreference
			HashMap<Integer,CrossReferenceToStructure> t_seq = cre.getSeqeunces();
			CrossReferenceToStructure t_instance = new CrossReferenceToStructure();
			t_instance.setId((Integer)t_ref[3]);
			t_seq.put((Integer)t_ref[4],t_instance);
		}
	}
	
	private CrossReference addReferenceToHash(Reference a_ref) 
	{
		String tmp = a_ref.getExternalReferenceName() + "+" + a_ref.getExternalReferenceId();
		CrossReference cre = new CrossReference();
		cre.setID(a_ref.getId());
		HashMap<Integer,CrossReferenceToStructure> t_seq = cre.getSeqeunces();
		Set<GlycanSequenceReference> t_seqRef= a_ref.getGlycanSequenceReferences();
		for(GlycanSequenceReference gly : t_seqRef){
			CrossReferenceToStructure t_instance = new CrossReferenceToStructure();
			t_instance.setId(gly.getGlycanSequenceReferenceId());
			t_seq.put(gly.getGlycanSequence().getGlycanSequenceId(),t_instance);
		}
		m_hashDatabaseNames.put(tmp, cre);
		return cre;
	}

	public void updatedatabase(Integer a_hibernateSequence, Structure a_jibxStructure, Configuration m_config){
		for(Resource res: a_jibxStructure.getResources())
		{
			if ( ! m_config.isIgnoreDatabase( res.getDb() ) )
			{
				this.addReferenceToDb(res.getDb(), res.getId(),a_hibernateSequence);
			}
		}
		this.addReferenceToDb(a_jibxStructure.getDatabase(), a_jibxStructure.getId(),a_hibernateSequence);
	}
	
	public void addReferenceToDb(String a_resourceName, String a_resourceId,Integer a_hibernateSequence)
	{
		CrossReference t_crossReference = this.lookupReferenceByHashMap(a_resourceName,a_resourceId);
		
		if (t_crossReference == null )
		{
			Reference t_reference = new Reference();
			t_reference.setReferenceType("database");
			t_reference.setExternalReferenceId(a_resourceId);
			String t_name = m_config.getDatabaseName(a_resourceName);
			if ( t_name == null )
			{	
				t_reference.setExternalReferenceName(a_resourceName);
			}
			else
			{
				t_reference.setExternalReferenceName(t_name);
				if(m_config.getDatabaseUrl(a_resourceName)!= null){
					t_reference.setUrl(m_config.getDatabaseUrl(a_resourceName)+t_reference.getExternalReferenceId());
				}
				else{
					t_reference.setUrl(null);					
				}
			}
			t_reference.setContributor(this.m_user);
			m_manager.store(t_reference);
			t_crossReference = this.addReferenceToHash(t_reference);
		}
		t_crossReference.setUsed(true);
		if ( !this.existsGlycanSequenceToReference(a_hibernateSequence,t_crossReference) )
		{
			this.createGlycanSequenceToReference(a_hibernateSequence,t_crossReference);	
		}
		else
		{
			t_crossReference.getSeqeunces().get(a_hibernateSequence).setUsed(true);
		}
	}
	
	private boolean existsGlycanSequenceToReference(Integer a_hibernateSequence, CrossReference t_reference) 
	{
		CrossReferenceToStructure t = t_reference.getSeqeunces().get(a_hibernateSequence);
		if(t!= null){
			return true;
		}
		return false;
	}

	private void createGlycanSequenceToReference(
		Integer a_hibernateSequence, CrossReference a_reference) {
		GlycanSequenceReference glycanSequenceReference = new GlycanSequenceReference();
		glycanSequenceReference.setContributor(this.m_user);
		glycanSequenceReference.setGlycanSequence(this.m_manager.lookup(GlycanSequence.class, a_hibernateSequence));
		glycanSequenceReference.setReference(this.m_manager.lookup(Reference.class, a_reference.getID()));
		m_manager.store(glycanSequenceReference);	
		CrossReferenceToStructure t_refStruc = new CrossReferenceToStructure();
		t_refStruc.setId(glycanSequenceReference.getId());
		t_refStruc.setUsed(true);
		a_reference.getSeqeunces().put(a_hibernateSequence,t_refStruc);
	}

	public List<Object[]> gettingReferencebyuser(){
		Query t_query = m_manager.getQuery("org.eurocarbdb.dataaccess.core.Reference.GET_CROSSREFERENCE_BY_USER");
		t_query = t_query.setParameter("cont", m_user);
		@SuppressWarnings("unchecked")
		List<Object[]> t = (List<Object[]>) t_query.list();
		return t;
	}
	
	public CrossReference lookupReferenceByHashMap(String a_name, String a_id){
		String a_namePlusId = this.m_config.getDatabaseName(a_name) + "+" + a_id;
		CrossReference t_ref = m_hashDatabaseNames.get(a_namePlusId);
		if( t_ref != null){
			return t_ref;
		}
		return null;
	}
	
	public Reference lookupReference(String a_name, String a_id)
	{	
		Query t_query = m_manager.getQuery("org.eurocarbdb.dataaccess.core.Reference.IDLOOKUP");
		t_query = t_query.setParameter("ext_name", m_config.getDatabaseName(a_name));
		t_query = t_query.setParameter("ext_id", a_id);
		t_query = t_query.setParameter("cont", m_user);
		Reference t = (Reference) t_query.uniqueResult();
		return t;
	}
	
	private Contributor userexist(Configuration m_config) {
		Query t_query = m_manager.getQuery("org.eurocarbdb.dataaccess.core.Contributor.BY_EXACT_NAME");
		t_query = t_query.setParameter("name", m_config.getUsername());
		Contributor t = (Contributor) t_query.uniqueResult();
		return t;
	}
	
	public Integer getSequenceCT(String sequence)
	{
		 return m_glycanSequences.get(sequence);
	}
}
