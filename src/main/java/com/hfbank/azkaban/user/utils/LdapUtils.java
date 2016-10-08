package com.hfbank.azkaban.user.utils;

import java.util.List;

public class LdapUtils {

	/**
	 * Creating a domain base using domain string
	 * 
	 * @param domain
	 *            such as "example.com"
	 * @return domainBase if domain is null or empty, return "",otherwise such
	 *         as "DC=EXAMPLE,DC=COM"
	 */
	public static String buildDomainBase(String domain) {
		if (domain == null || domain.isEmpty()) {
			return "";
		}
		String domainBase = "DC=";
		for (char ch : domain.toCharArray()) {
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
	 * @param userDn
	 *            The entry,for example
	 *            "cn=Gui Yongmao,ou=test,ou=Users,dc=test,dc=com"
	 * @param allowedGroups
	 *            The allowed groups defined by config file
	 * @return Return true if belong to,otherwise false
	 */
	public static boolean memberOfAllowedGroups(String userDn, List<String> allowedGroups) {
		if (allowedGroups == null || allowedGroups.isEmpty()) {
			return true;
		}
		String dn = userDn.toUpperCase();
		for (String group : allowedGroups) {
			if (dn.contains(group.toUpperCase())) {
				return true;
			}
		}
		return false;
	}
}
