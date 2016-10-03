package cn.com.hfbank.utils;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ActiveDirectoryTest {
	
	private ActiveDirectory ad;
	
	@Before
	public void setUp() {
		ad = new ActiveDirectory();
	}
	
	@Test
	public void testBuildDomainBase() {
		String domain = null;
		assertNotNull(ad.buildDomainBase(domain));
		assertEquals("", ad.buildDomainBase(domain));
		
		domain = "";
		assertNotNull(ad.buildDomainBase(domain));
		assertEquals("", ad.buildDomainBase(domain));
		
		domain = "example";
		assertEquals("DC=EXAMPLE", ad.buildDomainBase(domain));
		
		domain = "example.com";
		assertEquals("DC=EXAMPLE,DC=COM", ad.buildDomainBase(domain));
		
		domain = "example.com.cn";
		assertEquals("DC=EXAMPLE,DC=COM,DC=CN", ad.buildDomainBase(domain));
	}
	
	@Test
	public void testBuildFilter() {
		String baseFilter = "(&((&(objectCategory=Person)(objectClass=User)))";
		String emailBaseFilter = "(&((&(objectCategory=Person)(objectClass=User)))(mail=test@example.com))";
		String accountBaseFilter = "(&((&(objectCategory=Person)(objectClass=User)))(samaccountname=test))";
		assertEquals(emailBaseFilter, ad.buildFilter(ad.BASE_FILTER, "email", "test@example.com"));
		assertEquals(accountBaseFilter, ad.buildFilter(ad.BASE_FILTER, "username", "test"));
	}
}
