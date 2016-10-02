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
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

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
	
	//private variables
	private Properties properties;
	private DirContext dirCtx;
	private SearchControls searchCtls;
	private String[] returnAttributes;
	private String domainBase;
	private String baseFilter;
	private final String CONNECTION_TIMEOUT = "com.sun.jndi.ldap.connect.timeout";
	
	/**
	 * Contructor with parameters for initialize Microsoft Active Directory context
	 * 
	 * @param adHost	AD server host name or ip, like 192.168.10.1
	 * @param adPort	AD server port, always 389
	 * @param bindUser	The user'name to bind the server for user profile, usually have admin privilege
	 * @param bindPwd	The password for binding user
	 * @param domain	the domain controller, like "example.com"
	 */
	public ActiveDirectory(String adHost, 
							int adPort, 
							String bindUser, 
							String bindPwd, 
							String domain) {
		this.properties = new Properties();
		//connection parameters
		properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	    properties.put(Context.PROVIDER_URL, "LDAP://" + adHost + ":"+adPort);
	    properties.put(Context.SECURITY_PRINCIPAL, bindUser + "@" + domain);
	    properties.put(Context.SECURITY_CREDENTIALS, bindPwd);
	    properties.put(CONNECTION_TIMEOUT, "5000");
	    
	    //this.domainBase = 
	}
	 
	/**
	 * Creating a domain base using domain string
	 * 
	 * @param domain		like "example.com"
	 * @return domainBase	like "DC=EXAMPLE,DC=COM"
	 */
	public String buildDomainBase(String domain) {
		String domainBase =  "";
		
		return domainBase;
	}
	
	
}
