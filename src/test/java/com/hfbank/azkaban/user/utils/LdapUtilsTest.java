package com.hfbank.azkaban.user.utils;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import azkaban.utils.Props;

public class LdapUtilsTest {
	
	@Test
	public void testBuildDomainBase() {
		String actual = LdapUtils.buildDomainBase(null);
		String expected = "";
		assertEquals(expected, actual);

		actual = LdapUtils.buildDomainBase("");
		assertEquals(expected, actual);

		actual = LdapUtils.buildDomainBase("test");
		expected = "DC=test";
		assertEquals(expected, actual);

		actual = LdapUtils.buildDomainBase("test.com");
		expected = "DC=test,DC=com";
		assertEquals(expected, actual);

	}
	@Test
	public void testMemberOfAllowedGroups() {
		String userDn = "cn=Gui Yongmao,ou=finandata,ou=Users,dc=test,dc=com";
		List<String> groups = null;
		boolean actual = LdapUtils.memberOfAllowedGroups(userDn, groups);
		assertTrue(actual);
		
		groups = Collections.emptyList();
		actual = LdapUtils.memberOfAllowedGroups(userDn, groups);
		assertTrue(actual);
		
		Props props = new Props();
		props.put("groups","ou=finandata,ou=Marketing");
		actual = LdapUtils.memberOfAllowedGroups(userDn, props.getStringList("groups"));
		assertTrue(actual);
		
		userDn = "cn=Gui Yongmao,ou=spark,ou=Users,dc=test,dc=com";
		actual = LdapUtils.memberOfAllowedGroups(userDn, props.getStringList("groups"));
		assertFalse(actual);
	}
}
