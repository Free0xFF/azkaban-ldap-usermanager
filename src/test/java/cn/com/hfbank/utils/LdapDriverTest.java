package cn.com.hfbank.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;

public class LdapDriverTest {
	private LdapDriver driver;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void testCreateConnection() {
		driver = new LdapDriver("192.168.137.201",389);
		assertNotNull(driver.getConnection());
		assertFalse(driver.getConnection().isConnected());
		assertFalse(driver.getConnection().isAuthenticated());
	}
	
	@Test
	public void testBind() throws LdapException {
		driver = new LdapDriver("192.168.137.200",389);
		String admin = "cn=Manager,dc=test,dc=com";
		String pwd = "123456";
		driver.bind(admin, pwd);
		assertNotNull(driver.getConnection());
		assertTrue(driver.getConnection().isConnected());
		assertTrue(driver.getConnection().isAuthenticated());
		
		admin = "cn=Manager111,dc=test,dc=com";
		thrown.expect(LdapException.class);
		driver.bind(admin, pwd);
		
		admin = "cn=Manager,dc=test,dc=com";
		pwd = "12345678";
		thrown.expect(LdapException.class);
		driver.bind(admin, pwd);
		
	}
	
	@Test
	public void testSearchUserByName() throws LdapException, CursorException {
		driver = new LdapDriver("192.168.137.200",389);
		String admin = "cn=Manager,dc=test,dc=com";
		String pwd = "123456";
		driver.bind(admin, pwd);
		
		String baseDn = "dc=test,dc=com";
		EntryCursor cursor = driver.searchUserByName(baseDn, "uid", "free.man");
		assertTrue(cursor.next());
		
		Entry entry = cursor.get();
		assertFalse(cursor.next());
		
		String actual = entry.get("mail").getString();
		assertEquals("free.man@163.com", actual);
		
		cursor = driver.searchUserByName(baseDn, "uid", "yongmao.gui");
		assertTrue(cursor.next());
		
		entry = cursor.get();
		assertTrue(cursor.next());
		actual = entry.get("mail").getString();
		assertEquals("yongmao.gui@test.com", actual);
		
		entry = cursor.get();
		actual = entry.get("mail").getString();
		assertEquals("free.0xff@outlook.com", actual);
		
		assertFalse(cursor.next());
	}
	
	@Test
	public void testUnBind() throws LdapException {
		driver = new LdapDriver("192.168.137.200",389);
		String admin = "cn=Manager,dc=test,dc=com";
		String pwd = "123456";
		driver.bind(admin, pwd);
		assertTrue(driver.getConnection().isConnected());
		assertTrue(driver.getConnection().isAuthenticated());
		
		driver.unBind();
		assertFalse(driver.getConnection().isConnected());
		assertFalse(driver.getConnection().isAuthenticated());
	}
}
