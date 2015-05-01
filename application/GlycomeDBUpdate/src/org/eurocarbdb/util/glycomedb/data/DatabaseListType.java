
package org.eurocarbdb.util.glycomedb.data;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/** 
 * Schema fragment(s) for this class:
 * <pre>
 * &lt;xs:complexType xmlns:xs="http://www.w3.org/2001/XMLSchema" name="databaseListType">
 *   &lt;xs:sequence>
 *     &lt;xs:element name="database" minOccurs="0" maxOccurs="unbounded">
 *       &lt;!-- Reference to inner class Database -->
 *     &lt;/xs:element>
 *   &lt;/xs:sequence>
 *   &lt;xs:attribute type="xs:integer" use="required" name="count"/>
 * &lt;/xs:complexType>
 * </pre>
 */
public class DatabaseListType
{
    private List<Database> databaseList = new ArrayList<Database>();
    private BigInteger count;

    /** 
     * Get the list of 'database' element items.
     * 
     * @return list
     */
    public List<Database> getDatabases() {
        return databaseList;
    }

    /** 
     * Set the list of 'database' element items.
     * 
     * @param list
     */
    public void setDatabases(List<Database> list) {
        databaseList = list;
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
     * &lt;xs:element xmlns:xs="http://www.w3.org/2001/XMLSchema" name="database" minOccurs="0" maxOccurs="unbounded">
     *   &lt;xs:complexType>
     *     &lt;xs:attribute type="xs:string" use="required" name="name"/>
     *     &lt;xs:attribute type="xs:string" use="required" name="identifier"/>
     *     &lt;xs:attribute type="xs:string" use="optional" name="url"/>
     *     &lt;xs:attribute type="xs:string" use="required" name="abbr"/>
     *     &lt;xs:attribute type="xs:integer" use="required" name="count"/>
     *     &lt;xs:attribute type="xs:string" use="required" name="countType"/>
     *   &lt;/xs:complexType>
     * &lt;/xs:element>
     * </pre>
     */
    public static class Database
    {
        private String name;
        private String identifier;
        private String url;
        private String abbr;
        private BigInteger count;
        private String countType;

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
         * Get the 'identifier' attribute value.
         * 
         * @return value
         */
        public String getIdentifier() {
            return identifier;
        }

        /** 
         * Set the 'identifier' attribute value.
         * 
         * @param identifier
         */
        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        /** 
         * Get the 'url' attribute value.
         * 
         * @return value
         */
        public String getUrl() {
            return url;
        }

        /** 
         * Set the 'url' attribute value.
         * 
         * @param url
         */
        public void setUrl(String url) {
            this.url = url;
        }

        /** 
         * Get the 'abbr' attribute value.
         * 
         * @return value
         */
        public String getAbbr() {
            return abbr;
        }

        /** 
         * Set the 'abbr' attribute value.
         * 
         * @param abbr
         */
        public void setAbbr(String abbr) {
            this.abbr = abbr;
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
         * Get the 'countType' attribute value.
         * 
         * @return value
         */
        public String getCountType() {
            return countType;
        }

        /** 
         * Set the 'countType' attribute value.
         * 
         * @param countType
         */
        public void setCountType(String countType) {
            this.countType = countType;
        }
    }
}
