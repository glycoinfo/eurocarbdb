
package org.eurocarbdb.util.glycomedb.data;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/** 
 * Schema fragment(s) for this class:
 * <pre>
 * &lt;xs:complexType xmlns:xs="http://www.w3.org/2001/XMLSchema" name="taxonTreeType">
 *   &lt;xs:sequence>
 *     &lt;xs:element name="taxon" minOccurs="0" maxOccurs="unbounded">
 *       &lt;!-- Reference to inner class Taxon -->
 *     &lt;/xs:element>
 *   &lt;/xs:sequence>
 *   &lt;xs:attribute type="xs:integer" use="required" name="count"/>
 * &lt;/xs:complexType>
 * </pre>
 */
public class TaxonTreeType
{
    private List<Taxon> taxonList = new ArrayList<Taxon>();
    private BigInteger count;

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
     * &lt;xs:element xmlns:xs="http://www.w3.org/2001/XMLSchema" name="taxon" minOccurs="0" maxOccurs="unbounded">
     *   &lt;xs:complexType>
     *     &lt;xs:sequence>
     *       &lt;xs:element name="parent" minOccurs="0" maxOccurs="unbounded">
     *         &lt;!-- Reference to inner class Parent -->
     *       &lt;/xs:element>
     *     &lt;/xs:sequence>
     *     &lt;xs:attribute type="xs:integer" use="required" name="ncbi"/>
     *     &lt;xs:attribute type="xs:string" use="required" name="name"/>
     *     &lt;xs:attribute type="xs:string" use="optional" name="rank"/>
     *   &lt;/xs:complexType>
     * &lt;/xs:element>
     * </pre>
     */
    public static class Taxon
    {
        private List<Parent> parentList = new ArrayList<Parent>();
        private BigInteger ncbi;
        private String name;
        private String rank;

        /** 
         * Get the list of 'parent' element items.
         * 
         * @return list
         */
        public List<Parent> getParents() {
            return parentList;
        }

        /** 
         * Set the list of 'parent' element items.
         * 
         * @param list
         */
        public void setParents(List<Parent> list) {
            parentList = list;
        }

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

        /** 
         * Get the 'name' attribute value.
         * 
         * @return value
         */
        public String getName() {
            return name;
        }

        /** 
         * Set the 'name' attribute value.
         * 
         * @param name
         */
        public void setName(String name) {
            this.name = name;
        }

        /** 
         * Get the 'rank' attribute value.
         * 
         * @return value
         */
        public String getRank() {
            return rank;
        }

        /** 
         * Set the 'rank' attribute value.
         * 
         * @param rank
         */
        public void setRank(String rank) {
            this.rank = rank;
        }
        /** 
         * Schema fragment(s) for this class:
         * <pre>
         * &lt;xs:element xmlns:xs="http://www.w3.org/2001/XMLSchema" name="parent" minOccurs="0" maxOccurs="unbounded">
         *   &lt;xs:complexType>
         *     &lt;xs:attribute type="xs:integer" use="required" name="ncbi"/>
         *   &lt;/xs:complexType>
         * &lt;/xs:element>
         * </pre>
         */
        public static class Parent
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
    }
}
