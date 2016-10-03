package cn.com.hfbank.azkaban;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import azkaban.utils.Props;

public class AdLdapUserManagerTest {
	private AdLdapUserManager admin;

	@Before
	public void setUp() {
		Props props = new Props();
		props.put(AdLdapUserManager.AD_HOST, "192.168.10.226");
		props.put(AdLdapUserManager.AD_PORT, 389);
		props.put(AdLdapUserManager.AD_DOMAIN, "example.com");
		props.put(AdLdapUserManager.AD_BIND_ACCOUNT, "yongmao.gui");
		props.put(AdLdapUserManager.AD_BIND_PASSWORD, "123456");
		props.put(AdLdapUserManager.AD_ALLOWEDGROUPS, "azexe,azadmin");
		admin = new AdLdapUserManager(props);
	}

	@Test
	public void testGetAllowedGroups() {
		List<String> actual = new ArrayList<String>();
		actual.add("azexe");
		List<String> expected = admin.getAllowedGroups("azexe");
		assertEquals(actual, expected);

		actual.add("azadmin");
		expected = admin.getAllowedGroups("azexe,azadmin");
		assertEquals(actual, expected);
	}
}
