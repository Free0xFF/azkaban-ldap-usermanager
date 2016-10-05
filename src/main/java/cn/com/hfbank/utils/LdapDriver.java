package cn.com.hfbank.utils;

import java.io.IOException;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;

public class LdapDriver {
	private LdapConnection connection = null;
	
	public LdapDriver(String ldapHost, int ldapPort) {
		connection = createConnection(ldapHost, ldapPort);
	}
	
	public LdapConnection createConnection(String ldapHost, int ldapPort) {
		if(connection == null) {
			synchronized(LdapDriver.class) {
				if(connection == null) {
					connection = new LdapNetworkConnection(ldapHost, ldapPort);
				}
			}
		}
		return connection;
	}
	
	public void bind(String bindUser, String passwd) throws LdapException {
		connection.bind(bindUser, passwd);
	}
	
	public EntryCursor searchUserByName(String baseDn, String nameProperty, String searchValue) throws LdapException {
		String filter = "(" + nameProperty + "=" + searchValue + ")";
		EntryCursor cursor = connection.search( baseDn, filter, SearchScope.SUBTREE);
		return cursor;
	}
	
	public void unBind() throws LdapException {
		connection.unBind();
	}
	
	public void closeLdapConnection() throws IOException {
		connection.close();
	}
	
	public void setConnection(LdapConnection connection) {
		this.connection = connection;
	}
	
	public LdapConnection getConnection() {
		return this.connection;
	}
}
