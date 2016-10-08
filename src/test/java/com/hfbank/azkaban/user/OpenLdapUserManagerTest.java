package com.hfbank.azkaban.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import azkaban.user.User;
import azkaban.user.UserManagerException;
import azkaban.utils.Props;

public class OpenLdapUserManagerTest {
	private LdapUserManager admin;
	private LdapUserManager admin_for_empty_group;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Before
	public void setUp() {
		Props props = new Props();
		props.put("user.manager.ldap.host","192.168.137.200");
		props.put("user.manager.ldap.port",389);
		props.put("user.manager.ldap.domain","test.com");
		props.put("user.manager.ldap.bind.name","CN=Manager,DC=test,DC=com");
		props.put("user.manager.ldap.bind.password","123456");
		props.put("user.manager.ldap.name.tag","uid");
		props.put("user.manager.ldap.email.tag","mail");
		props.put("user.manager.ldap.group.tag","member");
		props.put("user.manager.ldap.allowed.groups","ou=finandata,ou=Marketing");
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
	public void testGetUserMoreThanOneUserFound() throws UserManagerException {
		thrown.expect(UserManagerException.class);
		thrown.expectMessage("More than one user found!");
		admin.getUser("yongmao.gui", "123456");
	}
	
	@Test
	public void testGetUserNotMemberOfAllowedGroups() throws UserManagerException {
		thrown.expect(UserManagerException.class);
		thrown.expectMessage("User is not member of allowed groups!");
		admin.getUser("chen.chen", "123456");
	}
	
	@Test
	public void testGetUserPasswordWrong() throws UserManagerException {
		thrown.expect(UserManagerException.class);
		thrown.expectMessage("Bind error: username or password was wrong!");
		admin.getUser("free.man", "654321");
	}
	
	@Test
	public void testGetUserSucceed() throws UserManagerException {
		User expected = new User("free.man");
		User actual = admin.getUser("free.man", "123456");
		assertEquals(expected,actual);
	}
	
	@Test
	public void testValidateUser() {
		boolean actual = admin.validateUser("hello");
		assertFalse(actual);
		
		actual = admin.validateUser("chen.chen");
		assertFalse(actual);
		
		actual = admin.validateUser("yongmao.gui");
		assertFalse(actual);
		
		actual = admin.validateUser("free.man");
		assertTrue(actual);
		
		//empty group
		actual = admin_for_empty_group.validateUser("hello");
		assertFalse(actual);
		
		actual = admin_for_empty_group.validateUser("chen.chen");
		assertTrue(actual);
		
		actual = admin_for_empty_group.validateUser("yongmao.gui");
		assertFalse(actual);
		
		actual = admin_for_empty_group.validateUser("free.man");
		assertTrue(actual);
	}
	
	@Test
	public void testValidateGroup() {
		boolean actual = admin.validateGroup("azweb");
		assertFalse(actual);
		
		actual = admin.validateGroup("Marketing");
		assertTrue(actual);
		
		actual = admin_for_empty_group.validateGroup("any");
		assertTrue(actual);
	}
}
