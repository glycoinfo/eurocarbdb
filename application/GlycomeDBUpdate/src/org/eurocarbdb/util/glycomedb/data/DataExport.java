
package org.eurocarbdb.util.glycomedb.data;

import java.util.Date;

/** 
 * Schema fragment(s) for this class:
 * <pre>
 * &lt;xs:element xmlns:xs="http://www.w3.org/2001/XMLSchema" name="data_export">
 *   &lt;xs:complexType>
 *     &lt;xs:choice minOccurs="1" maxOccurs="1">
 *       &lt;xs:element type="taxonTreeType" name="taxon_tree" minOccurs="1" maxOccurs="1"/>
 *       &lt;xs:element type="errorType" name="error" minOccurs="1" maxOccurs="1"/>
 *       &lt;xs:element type="structureListType" name="structure_list" minOccurs="1" maxOccurs="1"/>
 *       &lt;xs:element type="databaseListType" name="database_list" minOccurs="1" maxOccurs="1"/>
 *     &lt;/xs:choice>
 *     &lt;xs:attribute type="xs:dateTime" use="required" name="date"/>
 *   &lt;/xs:complexType>
 * &lt;/xs:element>
 * </pre>
 */
public class DataExport
{
    private int choiceSelect = -1;
    private static final int TAXON_TREE_CHOICE = 0;
    private static final int ERROR_CHOICE = 1;
    private static final int STRUCTURE_LIST_CHOICE = 2;
    private static final int DATABASE_LIST_CHOICE = 3;
    private TaxonTreeType taxonTree;
    private ErrorType error;
    private StructureListType structureList;
    private DatabaseListType databaseList;
    private Date date;

    private void setChoiceSelect(int choice) {
        if (choiceSelect == -1) {
            choiceSelect = choice;
        } else if (choiceSelect != choice) {
            throw new IllegalStateException(
                    "Need to call clearChoiceSelect() before changing existing choice");
        }
    }

    /** 
     * Clear the choice selection.
     */
    public void clearChoiceSelect() {
        choiceSelect = -1;
    }

    /** 
     * Check if TaxonTree is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifTaxonTree() {
        return choiceSelect == TAXON_TREE_CHOICE;
    }

    /** 
     * Get the 'taxon_tree' element value.
     * 
     * @return value
     */
    public TaxonTreeType getTaxonTree() {
        return taxonTree;
    }

    /** 
     * Set the 'taxon_tree' element value.
     * 
     * @param taxonTree
     */
    public void setTaxonTree(TaxonTreeType taxonTree) {
        setChoiceSelect(TAXON_TREE_CHOICE);
        this.taxonTree = taxonTree;
    }

    /** 
     * Check if Error is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifError() {
        return choiceSelect == ERROR_CHOICE;
    }

    /** 
     * Get the 'error' element value.
     * 
     * @return value
     */
    public ErrorType getError() {
        return error;
    }

    /** 
     * Set the 'error' element value.
     * 
     * @param error
     */
    public void setError(ErrorType error) {
        setChoiceSelect(ERROR_CHOICE);
        this.error = error;
    }

    /** 
     * Check if StructureList is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifStructureList() {
        return choiceSelect == STRUCTURE_LIST_CHOICE;
    }

    /** 
     * Get the 'structure_list' element value.
     * 
     * @return value
     */
    public StructureListType getStructureList() {
        return structureList;
    }

    /** 
     * Set the 'structure_list' element value.
     * 
     * @param structureList
     */
    public void setStructureList(StructureListType structureList) {
        setChoiceSelect(STRUCTURE_LIST_CHOICE);
        this.structureList = structureList;
    }

    /** 
     * Check if DatabaseList is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifDatabaseList() {
        return choiceSelect == DATABASE_LIST_CHOICE;
    }

    /** 
     * Get the 'database_list' element value.
     * 
     * @return value
     */
    public DatabaseListType getDatabaseList() {
        return databaseList;
    }

    /** 
     * Set the 'database_list' element value.
     * 
     * @param databaseList
     */
    public void setDatabaseList(DatabaseListType databaseList) {
        setChoiceSelect(DATABASE_LIST_CHOICE);
        this.databaseList = databaseList;
    }

    /** 
     * Get the 'date' attribute value.
     * 
     * @return value
     */
    public Date getDate() {
        return date;
    }

    /** 
     * Set the 'date' attribute value.
     * 
     * @param date
     */
    public void setDate(Date date) {
        this.date = date;
    }
}
