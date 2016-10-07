package cn.com.hfbank.azkaban;

import static org.junit.Assert.*;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import azkaban.user.User;
import azkaban.user.UserManagerException;
import azkaban.utils.Props;

public class OpenLdapUserManagerTest {
	
	private OpenldapUserManager openldap;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Before
	public void setUp() {
		Props props = new Props();
		String ldapHost = "192.168.137.200";
		int ldapPort = 389;
		String domain = "test.com";
		String bindUser = "cn=Manager,dc=test,dc=com";
		String bindPwd = "123456";
		String allowedGroups = "finandata,Marketing";
		String nameAlias = "uid";
		String emailAlias = "mail";
		String ldapType = "OpenLDAP";
		props.put(OpenldapUserManager.LDAP_HOST,ldapHost);
		props.put(OpenldapUserManager.LDAP_PORT,ldapPort);
		props.put(OpenldapUserManager.LDAP_DOMAIN,domain);
		props.put(OpenldapUserManager.LDAP_BIND_ACCOUNT,bindUser);
		props.put(OpenldapUserManager.LDAP_BIND_PASSWORD,bindPwd);
		props.put(OpenldapUserManager.LDAP_ALLOWEDGROUPS,allowedGroups);
		props.put(OpenldapUserManager.LDAP_NAME_ALIAS,nameAlias);
		props.put(OpenldapUserManager.LDAP_EMAIL_ALIAS,emailAlias);
		props.put(OpenldapUserManager.LDAP_TYPE,ldapType);
		openldap = new OpenldapUserManager(props);
	}
	
	@Test
	public void testGetUser() throws UserManagerException, LdapException {
		thrown.expect(UserManagerException.class);
		thrown.expectMessage("No user found!");
		User actualUser = openldap.getUser("hello", "123456");
		
		thrown.expect(UserManagerException.class);
		thrown.expectMessage("More than one user found!");
		actualUser = openldap.getUser("yongmao.gui", "123456");
		
		thrown.expect(UserManagerException.class);
		thrown.expectMessage("User is not member of allowed groups.");
		actualUser = openldap.getUser("laiqun.li", "123456");
		
		User expectedUser = openldap.getUser("free.man", "123456");
		actualUser = new User("free.man");
		assertEquals(expectedUser,actualUser);
	}
	
	@Test
	public void testValidateUser() {
		boolean actual = openldap.validateUser("hello");
		assertFalse(actual);
		
		actual = openldap.validateUser("yongmao.gui");
		assertFalse(actual);
		
		actual = openldap.validateUser("laiqun.li");
		assertFalse(actual);
		
		actual = openldap.validateUser("free.man");
		assertTrue(actual);
	}
	
	@Test
	public void testValidateGroup(){
		boolean actual = openldap.validateGroup("testgroup");
		assertFalse(actual);
		
		actual = openldap.validateGroup("Sales");
		assertFalse(actual);
		
		actual = openldap.validateGroup("finandata");
		assertTrue(actual);
	}
}
