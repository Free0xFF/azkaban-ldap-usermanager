package com.hfbank.azkaban.user.utils;

import java.io.IOException;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;

public class LdapDriver {
	private LdapConnection connection = null;

	public LdapDriver(String host, int port) {
		connection = createConnection(host, port);
	}

	public LdapConnection createConnection(String host, int port) {
		if (connection == null) {
			synchronized (LdapDriver.class) {
				if (connection == null) {
					connection = new LdapNetworkConnection(host, port);
				}
			}
		}
		return connection;
	}

	public void bind(String bindName, String bindPwd) throws LdapException {
		connection.bind(bindName, bindPwd);
	}

	/**
	 * 
	 * @param baseDn
	 *            The domain, like "dc=test,dc=com"
	 * @param nameTag
	 *            May be "uid",maybe "cn", maybe "sAMAccountName" and so on
	 * @param searchValue
	 *            The value corresponding to nameProperty
	 * @return cursor EntryCursor
	 * @throws LdapException
	 */
	public EntryCursor searchUserByName(String baseDn, String nameTag, String searchValue) throws LdapException {
		String filter = "(" + nameTag + "=" + searchValue + ")";
		EntryCursor cursor = connection.search(baseDn, filter, SearchScope.SUBTREE);
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
