package cn.com.hfbank.utils;

import java.util.ArrayList;
import java.util.List;

public class LdapUtil {

	/**
	 * Creating a domain base using domain string
	 * 
	 * @param domain
	 *            such as "example.com"
	 * @return domainBase if domain is null or empty, return "",otherwise such as "DC=EXAMPLE,DC=COM"
	 */
	public static String buildDomainBase(String domain) {
		if (domain == null || domain.isEmpty()) {
			return "";
		}
		String domainBase = "DC=";
		for (char ch : domain.toUpperCase().toCharArray()) {
			if (ch == '.') {
				domainBase += ",DC=";
			} else {
				domainBase += ch;
			}
		}
		return domainBase;
	}
	
	/**
	 * Check whether the user is part of allowed groups
	 * 
	 * @param dn				The entry,for example "cn=Gui Yongmao,ou=test,ou=Users,dc=test,dc=com"
	 * @param allowedGroups		The allowed groups defined by config file
	 * @param type				Corresponding ldap production, supporting Microsoft AD, OpenLDAP at present
	 * @return					Return true if belong to,otherwise false
	 */
	public static boolean memberOfAllowedGroups(String dn, List<String> allowedGroups, String type) {
		if(allowedGroups == null || allowedGroups.size() == 0) {
			return true;
		}
		
		String base = null;
		if(type == null || type == "ActiveDirectory") {
			base = "CN=";
		}
		else if(type == "OpenLDAP") {
			base = "OU=";
		}
		
		String userDn = dn.toUpperCase();
		for (String group : allowedGroups) {
			String groupEntry = base+group;
			if (userDn.contains(groupEntry.toUpperCase())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Parse group name from a string
	 * 
	 * @param adAllowedGroups
	 *            A String like "admin,system",which were separated by commas
	 * @return groups A List contains many group names
	 */
	public static List<String> getAllowedGroups(String adAllowedGroups) {
		List<String> groups = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();
		for (char ch : adAllowedGroups.toCharArray()) {
			if (ch == ',') {
				groups.add(sb.toString());
				sb.delete(0, sb.length());
			} else {
				sb.append(ch);
			}
		}
		if(sb.toString().length() != 0) {
			groups.add(sb.toString());
		}
		return groups;
	}
}
