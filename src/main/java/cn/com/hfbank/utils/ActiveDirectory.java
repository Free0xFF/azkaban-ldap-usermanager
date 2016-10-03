/**
 * LICENSE
 * 
 * 
 * 
 * 
 */

package cn.com.hfbank.utils;

import java.util.Properties;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * Query Microsoft Active Directory using Java
 * 
 * @filiename ActiveDirectory.java
 * @author    free.0xff
 * @copyright hfbank
 * 
 */ 
public class ActiveDirectory {
	//Loggerge
	private static final Logger LOG = Logger.getLogger(ActiveDirectory.class.getName());
	
	//final string
	protected final String CONNECTION_TIMEOUT = "com.sun.jndi.ldap.connect.timeout";
	protected final String BASE_FILTER = "(&((&(objectCategory=Person)(objectClass=User)))";
	protected final String[] RETURN_ATTRIBUTES = { "sAMAccountName", "givenName", "cn", "mail", "memberof" };
	
	//private variables
	private Properties properties;
	private DirContext dirCtx;
	private SearchControls searchCtls;
	private String domainBase;
	
	/**
	 * Constructor with empty parameter, it should be given
	 */
	public ActiveDirectory() {
		
	}
	
	/**
	 * Contructor with parameters for initialize Microsoft Active Directory context
	 * 
	 * @param adHost	AD server host name or ip, like 192.168.10.1
	 * @param adPort	AD server port, always 389
	 * @param bindUser	The user'name to bind the server for user profile, usually have admin privilege
	 * @param bindPwd	The password for binding user
	 * @param domain	the domain controller, like "example.com"
	 * @throws NamingException 
	 */
	public ActiveDirectory(String adHost, int adPort, String bindUser, String bindPwd, String domain) throws NamingException {
		this.properties = new Properties();
		
		//connection parameters
		properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	    properties.put(Context.PROVIDER_URL, "LDAP://" + adHost + ":"+adPort);
	    properties.put(Context.SECURITY_PRINCIPAL, bindUser + "@" + domain);
	    properties.put(Context.SECURITY_CREDENTIALS, bindPwd);
	    properties.put(CONNECTION_TIMEOUT, "5000");
	    
	    //initialize AD ldap connection
	    dirCtx = new InitialDirContext(properties);
	    
	    //convert domain to proper format for search
	    this.domainBase = this.buildDomainBase(domain);
	    
	    //initialize search controller
	    searchCtls = new SearchControls();
	    searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    searchCtls.setReturningAttributes(RETURN_ATTRIBUTES);
	}
	 
	/**
	 * Creating a domain base using domain string
	 * 
	 * @param  domain		such as "example.com"
	 * @return domainBase	such as "DC=EXAMPLE,DC=COM"
	 */
	public String buildDomainBase(String domain) {
		if(domain == null || domain.isEmpty()) {
			return "";
		}
		String domainBase = "DC=";
		for(char ch : domain.toUpperCase().toCharArray()) {
			if (ch == '.') {
				domainBase += ",DC=";
			}
			else {
				domainBase += ch;
			}
		}
		return domainBase;
	}
	
	/**
	 * 
	 * @param searchBy			Support two types at present, "email" and "username"
	 * @param searchValue		The searching value corresponding to searchBy type
	 * @param searchBase		Null or a String value like "example.com"
	 * @return searchResults	The search results
	 * @throws NamingException 
	 */
	public NamingEnumeration<SearchResult> searchUser(String searchBy, String searchValue, String searchBase) throws NamingException {
		String filter = this.buildFilter(BASE_FILTER, searchBy, searchValue);
		String baseDomain = (searchBase == null) ? domainBase : buildDomainBase(searchBase);
		NamingEnumeration<SearchResult> searchResults = dirCtx.search(baseDomain, filter, searchCtls);
		return searchResults;
	}
	
	/**
	 * Contructor search filter for AD by email or username
	 * 
	 * @param searchBy		Support two types at present, "email" and "username"
	 * @param searchValue	The searching value corresponding to searchBy type
	 * @return searchFilter corresponding filter
	 */
	public String buildFilter(String baseFilter, String searchBy, String searchValue) {
		String searchFilter = baseFilter;
		if("email".equals(searchBy)) {
			searchFilter += ("(mail="+searchValue+"))");
		} else if("username".equals(searchBy)) {
			searchFilter += ("(samaccountname="+searchValue+"))");
		}
		return searchFilter;
	}
	
	/**
	 * Close AD connection
	 */
	public void closeDirContext() {
		if(dirCtx != null) {
			try {
				dirCtx.close();
			} catch (NamingException e) {
				LOG.severe(e.getMessage());
			}
		}
	}
	
}
