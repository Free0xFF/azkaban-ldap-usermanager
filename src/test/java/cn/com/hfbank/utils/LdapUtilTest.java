package cn.com.hfbank.utils;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class LdapUtilTest {
	@Test
	public void testBuildDomainBase() {
		String actual = LdapUtil.buildDomainBase(null);
		String expected = "";
		assertEquals(expected, actual);
		
		actual = LdapUtil.buildDomainBase("");
		assertEquals(expected, actual);
		
		actual = LdapUtil.buildDomainBase("test");
		expected = "DC=TEST";
		assertEquals(expected, actual);
		
		actual = LdapUtil.buildDomainBase("test.com");
		expected = "DC=TEST,DC=COM";
		assertEquals(expected, actual);
		
	}
	
	@Test
	public void testMemberOfAllowedGroupsTest() {
		String ad_type = "ActiveDirectory";
		String open_type = "OpenLDAP";
		String ad_dn = "cn=Gui Yongmao,cn=finandata,cn=Users,dc=test,dc=com";
		String open_dn = "cn=Gui Yongmao,ou=finandata,ou=Users,dc=test,dc=com";
		List<String> allowedGroups = new ArrayList<String>();
		boolean actual = LdapUtil.memberOfAllowedGroups(ad_dn, allowedGroups, ad_type);
		assertTrue(actual);
		
		actual = LdapUtil.memberOfAllowedGroups(open_dn, allowedGroups, open_type);
		assertTrue(actual);
		
		allowedGroups.add("Sales");
		actual = LdapUtil.memberOfAllowedGroups(ad_dn, allowedGroups, ad_type);
		assertFalse(actual);
		
		actual = LdapUtil.memberOfAllowedGroups(open_dn, allowedGroups, open_type);
		assertFalse(actual);
		
		allowedGroups.add("finandata");
		actual = LdapUtil.memberOfAllowedGroups(ad_dn, allowedGroups, ad_type);
		assertTrue(actual);
		
		actual = LdapUtil.memberOfAllowedGroups(open_dn, allowedGroups, open_type);
		assertTrue(actual);
		
		allowedGroups.remove(allowedGroups.indexOf("finandata"));
		allowedGroups.add("FINANDATA");
		actual = LdapUtil.memberOfAllowedGroups(ad_dn, allowedGroups, ad_type);
		assertTrue(actual);
		
		actual = LdapUtil.memberOfAllowedGroups(open_dn, allowedGroups, open_type);
		assertTrue(actual);
		
		allowedGroups.remove(allowedGroups.indexOf("FINANDATA"));
		allowedGroups.add("FinanData");
		actual = LdapUtil.memberOfAllowedGroups(ad_dn, allowedGroups, ad_type);
		assertTrue(actual);
		
		actual = LdapUtil.memberOfAllowedGroups(open_dn, allowedGroups, open_type);
		assertTrue(actual);
		
	}
	
	@Test
	public void testGetAllowedGroups() {
		List<String> expected = new ArrayList<String>();
		List<String> actual = LdapUtil.getAllowedGroups("");
		assertEquals(expected, actual);
		 
		String allowedGroups = "Marketing";
		actual = LdapUtil.getAllowedGroups(allowedGroups);
		expected.add("Marketing");
		assertEquals(expected, actual);
		
		allowedGroups += ",finandata";
		actual = LdapUtil.getAllowedGroups(allowedGroups);
		expected.add("finandata");
		assertEquals(expected, actual);
		
	}
}
