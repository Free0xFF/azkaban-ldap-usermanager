package com.hfbank.azkaban.user;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import azkaban.user.User;
import azkaban.user.UserManagerException;
import azkaban.utils.Props;

public class AdLdapUserManagerTest {
	private LdapUserManager admin;
	private LdapUserManager admin_for_empty_group;
	
	@Rule
    public ExpectedException thrown = ExpectedException.none();
	
	@Before
	public void setUp() {
		Props props = new Props();
		props.put("user.manager.ldap.host","192.168.10.200");
		props.put("user.manager.ldap.port",389);
		props.put("user.manager.ldap.domain","finandata.com");
		props.put("user.manager.ldap.bind.name","CN=sys,OU=Domain Controllers,DC=finandata,DC=com");
		props.put("user.manager.ldap.bind.password","36Bbcbb801f5052739af8220c6ea51434");
		props.put("user.manager.ldap.name.tag","sAMAccountName");
		props.put("user.manager.ldap.email.tag","mail");
		props.put("user.manager.ldap.group.tag","memberof");
		props.put("user.manager.ldap.allowed.groups","cn=azexe,cn=azadmin");
		admin = new LdapUserManager(props);
		
		props.put("user.manager.ldap.allowed.groups","");
		admin_for_empty_group = new LdapUserManager(props);
	}
	
	@Test
	public void testGetUserNoUserFound() throws UserManagerException {
		thrown.expect(UserManagerException.class);
		thrown.expectMessage("No user found!");
		admin.getUser("hello", "123456");
	}
	
	@Test
	public void testGetUserNotMemberOfAllowedGroups() throws UserManagerException {
		thrown.expect(UserManagerException.class);
		thrown.expectMessage("User is not member of allowed groups!");
		admin.getUser("chen.chen", "Initial0");
	}
	
	@Test
	public void testGetUserPasswordWrong() throws UserManagerException {
		thrown.expect(UserManagerException.class);
		thrown.expectMessage("Bind error: username or password was wrong!");
		admin.getUser("yongmao.gui", "123456");
	}
	
	@Test
	public void testGetUserSucceed() throws UserManagerException {
		User expected = new User("yongmao.gui");
		User actual = admin.getUser("yongmao.gui", "Initial0");
		assertEquals(expected,actual);
	}
	
	@Test
	public void testValidateUser() {
		boolean actual = admin.validateUser("hello");
		assertFalse(actual);
		
		actual = admin.validateUser("chen.chen");
		assertFalse(actual);
		
		actual = admin.validateUser("yongmao.gui");
		assertTrue(actual);
		
		//empty group
		actual = admin_for_empty_group.validateUser("hello");
		assertFalse(actual);
				
		actual = admin_for_empty_group.validateUser("chen.chen");
		assertTrue(actual);
			
		actual = admin_for_empty_group.validateUser("yongmao.gui");
		assertTrue(actual);
	}
	
	@Test
	public void testValidateGroup() {
		boolean actual = admin.validateGroup("azweb");
		assertFalse(actual);
		
		actual = admin.validateGroup("azadmin");
		assertTrue(actual);
		
		actual = admin_for_empty_group.validateGroup("any");
		assertTrue(actual);
	}
}
