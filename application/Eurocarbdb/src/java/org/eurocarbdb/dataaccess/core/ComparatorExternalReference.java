package org.eurocarbdb.dataaccess.core;

import org.eurocarbdb.dataaccess.core.Reference;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

public class ComparatorExternalReference implements Comparator <Reference>  
{
	private List<Reference> toBeDeleted = new ArrayList<Reference>();

     /* (non-Javadoc)
     * @see java.util.Comparator#compare(T, T)
     */
    public int compare(Reference ref1, Reference ref2)
   {
	int t_result = ref1.getExternalReferenceName().compareTo(ref2.getExternalReferenceName());

       if ( t_result == 0 )
       {
          //same external database name
		try 
		{
			Integer t_one = Integer.parseInt(ref1.getExternalReferenceId());
			Integer t_two = Integer.parseInt(ref2.getExternalReferenceId());
			t_result = t_one.compareTo(t_two);
			if ( t_result == 0 )
			{
				this.toBeDeleted.add(ref2);
			}
			return t_result;
		} 
		catch (Exception e) 
		{
	 		t_result = ref1.getExternalReferenceId().compareTo(ref2.getExternalReferenceId());
			if ( t_result == 0 )
			{
				this.toBeDeleted.add(ref2);
			}
			return t_result;
		}
	  
       }
       return t_result;
    }

	public List<Reference> getToBeDeleted(){
		return this.toBeDeleted;	
	}
}
