
package org.eurocarbdb.util.glycomedb.data;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/** 
 * Schema fragment(s) for this class:
 * <pre>
 * &lt;xs:complexType xmlns:xs="http://www.w3.org/2001/XMLSchema" name="structureListType">
 *   &lt;xs:sequence>
 *     &lt;xs:element name="structure" minOccurs="0" maxOccurs="unbounded">
 *       &lt;!-- Reference to inner class Structure -->
 *     &lt;/xs:element>
 *   &lt;/xs:sequence>
 *   &lt;xs:attribute type="xs:integer" use="required" name="count"/>
 * &lt;/xs:complexType>
 * </pre>
 */
public class StructureListType
{
    private List<Structure> structureList = new ArrayList<Structure>();
    private BigInteger count;

    /** 
     * Get the list of 'structure' element items.
     * 
     * @return list
     */
    public List<Structure> getStructures() {
        return structureList;
    }

    /** 
     * Set the list of 'structure' element items.
     * 
     * @param list
     */
    public void setStructures(List<Structure> list) {
        structureList = list;
    }

    /** 
     * Get the 'count' attribute value.
     * 
     * @return value
     */
    public BigInteger getCount() {
        return count;
    }

    /** 
     * Set the 'count' attribute value.
     * 
     * @param count
     */
    public void setCount(BigInteger count) {
        this.count = count;
    }
    /** 
     * Schema fragment(s) for this class:
     * <pre>
     * &lt;xs:element xmlns:xs="http://www.w3.org/2001/XMLSchema" name="structure" minOccurs="0" maxOccurs="unbounded">
     *   &lt;xs:complexType>
     *     &lt;xs:sequence>
     *       &lt;xs:element name="taxon" minOccurs="0" maxOccurs="unbounded">
     *         &lt;!-- Reference to inner class Taxon -->
     *       &lt;/xs:element>
     *       &lt;xs:element name="resource" minOccurs="0" maxOccurs="unbounded">
     *         &lt;!-- Reference to inner class Resource -->
     *       &lt;/xs:element>
     *       &lt;xs:element name="deleted_mapping" minOccurs="0" maxOccurs="unbounded">
     *         &lt;!-- Reference to inner class DeletedMapping -->
     *       &lt;/xs:element>
     *       &lt;xs:element name="sequence" minOccurs="1" maxOccurs="1">
     *         &lt;xs:complexType>
     *           &lt;xs:simpleContent>
     *             &lt;xs:extension base="xs:string">
     *               &lt;xs:attribute type="xs:string" use="required" name="format"/>
     *             &lt;/xs:extension>
     *           &lt;/xs:simpleContent>
     *         &lt;/xs:complexType>
     *       &lt;/xs:element>
     *     &lt;/xs:sequence>
     *     &lt;xs:attribute type="xs:string" use="required" name="database"/>
     *     &lt;xs:attribute type="xs:string" use="required" name="id"/>
     *   &lt;/xs:complexType>
     * &lt;/xs:element>
     * </pre>
     */
    public static class Structure
    {
        private List<Taxon> taxonList = new ArrayList<Taxon>();
        private List<Resource> resourceList = new ArrayList<Resource>();
        private List<DeletedMapping> deletedMappingList = new ArrayList<DeletedMapping>();
        private String sequenceString;
        private String sequenceFormat;
        private String database;
        private String id;

        /** 
         * Get the list of 'taxon' element items.
         * 
         * @return list
         */
        public List<Taxon> getTaxons() {
            return taxonList;
        }

        /** 
         * Set the list of 'taxon' element items.
         * 
         * @param list
         */
        public void setTaxons(List<Taxon> list) {
            taxonList = list;
        }

        /** 
         * Get the list of 'resource' element items.
         * 
         * @return list
         */
        public List<Resource> getResources() {
            return resourceList;
        }

        /** 
         * Set the list of 'resource' element items.
         * 
         * @param list
         */
        public void setResources(List<Resource> list) {
            resourceList = list;
        }

        /** 
         * Get the list of 'deleted_mapping' element items.
         * 
         * @return list
         */
        public List<DeletedMapping> getDeletedMappings() {
            return deletedMappingList;
        }

        /** 
         * Set the list of 'deleted_mapping' element items.
         * 
         * @param list
         */
        public void setDeletedMappings(List<DeletedMapping> list) {
            deletedMappingList = list;
        }

        /** 
         * Get the extension value.
         * 
         * @return value
         */
        public String getSequenceString() {
            return sequenceString;
        }

        /** 
         * Set the extension value.
         * 
         * @param sequenceString
         */
        public void setSequenceString(String sequenceString) {
            this.sequenceString = sequenceString;
        }

        /** 
         * Get the 'format' attribute value.
         * 
         * @return value
         */
        public String getSequenceFormat() {
            return sequenceFormat;
        }

        /** 
         * Set the 'format' attribute value.
         * 
         * @param sequenceFormat
         */
        public void setSequenceFormat(String sequenceFormat) {
            this.sequenceFormat = sequenceFormat;
        }

        /** 
         * Get the 'database' attribute value.
         * 
         * @return value
         */
        public String getDatabase() {
            return database;
        }

        /** 
         * Set the 'database' attribute value.
         * 
         * @param database
         */
        public void setDatabase(String database) {
            this.database = database;
        }

        /** 
         * Get the 'id' attribute value.
         * 
         * @return value
         */
        public String getId() {
            return id;
        }

        /** 
         * Set the 'id' attribute value.
         * 
         * @param id
         */
        public void setId(String id) {
            this.id = id;
        }
        /** 
         * Schema fragment(s) for this class:
         * <pre>
         * &lt;xs:element xmlns:xs="http://www.w3.org/2001/XMLSchema" name="taxon" minOccurs="0" maxOccurs="unbounded">
         *   &lt;xs:complexType>
         *     &lt;xs:attribute type="xs:integer" use="required" name="ncbi"/>
         *   &lt;/xs:complexType>
         * &lt;/xs:element>
         * </pre>
         */
        public static class Taxon
        {
            private BigInteger ncbi;

            /** 
             * Get the 'ncbi' attribute value.
             * 
             * @return value
             */
            public BigInteger getNcbi() {
                return ncbi;
            }

            /** 
             * Set the 'ncbi' attribute value.
             * 
             * @param ncbi
             */
            public void setNcbi(BigInteger ncbi) {
                this.ncbi = ncbi;
            }
        }
        /** 
         * Schema fragment(s) for this class:
         * <pre>
         * &lt;xs:element xmlns:xs="http://www.w3.org/2001/XMLSchema" name="resource" minOccurs="0" maxOccurs="unbounded">
         *   &lt;xs:complexType>
         *     &lt;xs:attribute type="xs:string" use="required" name="db"/>
         *     &lt;xs:attribute type="xs:string" use="optional" name="id"/>
         *   &lt;/xs:complexType>
         * &lt;/xs:element>
         * </pre>
         */
        public static class Resource
        {
            private String db;
            private String id;

            /** 
             * Get the 'db' attribute value.
             * 
             * @return value
             */
            public String getDb() {
                return db;
            }

            /** 
             * Set the 'db' attribute value.
             * 
             * @param db
             */
            public void setDb(String db) {
                this.db = db;
            }

            /** 
             * Get the 'id' attribute value.
             * 
             * @return value
             */
            public String getId() {
                return id;
            }

            /** 
             * Set the 'id' attribute value.
             * 
             * @param id
             */
            public void setId(String id) {
                this.id = id;
            }
        }
        /** 
         * Schema fragment(s) for this class:
         * <pre>
         * &lt;xs:element xmlns:xs="http://www.w3.org/2001/XMLSchema" name="deleted_mapping" minOccurs="0" maxOccurs="unbounded">
         *   &lt;xs:complexType>
         *     &lt;xs:attribute type="xs:integer" use="required" name="id"/>
         *   &lt;/xs:complexType>
         * &lt;/xs:element>
         * </pre>
         */
        public static class DeletedMapping
        {
            private BigInteger id;

            /** 
             * Get the 'id' attribute value.
             * 
             * @return value
             */
            public BigInteger getId() {
                return id;
            }

            /** 
             * Set the 'id' attribute value.
             * 
             * @param id
             */
            public void setId(BigInteger id) {
                this.id = id;
            }
        }
    }
}
