package cn.com.hfbank.azkaban;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import azkaban.user.Permission;
import azkaban.user.Role;
import azkaban.user.User;
import azkaban.user.UserManager;
import azkaban.user.UserManagerException;
import azkaban.utils.Props;
import cn.com.hfbank.utils.ActiveDirectory;

public class AdLdapUserManager implements UserManager {
	protected static final Logger LOG = Logger.getLogger(AdLdapUserManager.class.getName());
	public static final String AD_HOST = "user.manager.host";
	public static final String AD_PORT = "user.manager.port";
	public static final String AD_DOMAIN = "user.manager.domain";
	public static final String AD_BIND_ACCOUNT = "user.manager.bindAccount";
	public static final String AD_BIND_PASSWORD = "user.manager.bindPassword";
	public static final String AD_ALLOWEDGROUPS = "user.manager.allowedGroups";
	private String adHost;
	private int adPort;
	private String domain;
	private String bindUser;
	private String bindPwd;
	private List<String> adAllowedGroups;

	public AdLdapUserManager(Props props) {
		adHost = props.getString(AD_HOST);
		adPort = props.getInt(AD_PORT);
		domain = props.getString(AD_DOMAIN);
		bindUser = props.getString(AD_BIND_ACCOUNT);
		bindPwd = props.getString(AD_BIND_PASSWORD);
		adAllowedGroups = getAllowedGroups(AD_ALLOWEDGROUPS);
	}

	/**
	 * Parse group name from a string
	 * 
	 * @param adAllowedGroups
	 *            A String like "admin,system",which were separated by a comma
	 * @return groups A List contains many group names
	 */
	public List<String> getAllowedGroups(String adAllowedGroups) {
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
		groups.add(sb.toString());
		return groups;
	}

	/**
	 * Check where the user is part of allowed groups
	 * 
	 * @param ad
	 *            Instance of ActiveDirectory
	 * @param userName
	 *            The username that to be checked
	 * @param allowedGroups
	 *            The allowed groups that we set
	 * @return Attributes Null if not belong to, otherwise will return the
	 *         Attributes of searched user
	 * @throws NamingException
	 *             When the authentication failed
	 * @throws UserManagerException
	 *             when more than one user were found
	 */
	public Attributes memberOfAllowedGroups(ActiveDirectory ad, String userName, List<String> allowedGroups)
			throws NamingException, UserManagerException {
		String searchBy = "username";
		NamingEnumeration<SearchResult> results = ad.searchUser(searchBy, userName, null);
		SearchResult userResult = null;
		if (results.hasMoreElements()) {
			userResult = results.nextElement();
		}

		// the size of resutls must be one ,0 and more than one are not correct!
		if (userResult == null || results.hasMoreElements()) {
			throw new UserManagerException("More than one user were found.");
		}

		// then check: bug exists
		Attributes userProfile = userResult.getAttributes();
		String userGroups = userProfile.get("memberof").toString();
		for (String group : allowedGroups) {
			if (userGroups.contains(group)) {
				return userProfile;
			}
		}
		return null;
	}

	@Override
	public User getUser(String username, String password) throws UserManagerException {
		// The username and password should not be empty
		if (username == null || username.trim().isEmpty()) {
			throw new UserManagerException("Username is empty!");
		} else if (password == null || password.trim().isEmpty()) {
			throw new UserManagerException("Password is empty!");
		}

		// Attempt to connect AD server with username and password
		try {
			// if authentication failed, an Exception will be throwed
			ActiveDirectory ad = new ActiveDirectory(adHost, adPort, bindUser, bindPwd, domain);

			// otherwise succeed, next step is to check whether the user is part
			// of allowed groups
			Attributes userProfile = this.memberOfAllowedGroups(ad, bindUser, adAllowedGroups);

			// if not in allowed groups
			if (userProfile == null) {
				throw new UserManagerException("User is not member of allowed groups.");
			}

			// other wise return Azkaban User infomation
			User user = new User(bindUser);
			String email = userProfile.get("mail").toString();
			email = email.substring(email.indexOf(":") + 1);
			user.setEmail(email);
			user.addRole("Admin");
			return user;
		} catch (NamingException e) {
			LOG.severe(e.getMessage());
			throw new UserManagerException("username or password is incorrect!", e);
		}
	}

	@Override
	public boolean validateUser(String userName) {
		// The username should not be null or empty
		if (userName == null || userName.trim().isEmpty()) {
			return false;
		}
		try {
			// Conneting to AD server with the binding account
			ActiveDirectory ad = new ActiveDirectory(adHost, adPort, bindUser, bindPwd, domain);

			// Check whether belong allowed groups
			Attributes userProfile = memberOfAllowedGroups(ad, userName, adAllowedGroups);

			// If not, return false
			if (userProfile == null) {
				return false;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean validateGroup(String group) {
		return this.adAllowedGroups.contains(group);
	}

	@Override
	public Role getRole(String roleName) {
		Permission permission = new Permission();
		permission.addPermissionsByName(roleName.toUpperCase());
		return new Role(roleName, permission);
	}

	@Override
	public boolean validateProxyUser(String proxyUser, User realUser) {
		return false;
	}
}
