package com.hfbank.azkaban.user;

import java.util.List;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import com.hfbank.azkaban.user.utils.LdapDriver;
import com.hfbank.azkaban.user.utils.LdapUtils;

import azkaban.user.Permission;
import azkaban.user.Role;
import azkaban.user.User;
import azkaban.user.UserManager;
import azkaban.user.UserManagerException;
import azkaban.utils.Props;

public class LdapUserManager implements UserManager {
	private LdapDriver driver;
	private String host;
	private int port;
	private String domain;
	private String bindName;
	private String bindPwd;
	private String nameTag;
	private String emailTag;
	private String groupTag;
	public List<String> allowedGroups;
	public static final String LDAP_HOST = "user.manager.ldap.host";
	public static final String LDAP_PORT = "user.manager.ldap.port";
	public static final String LDAP_DOMAIN = "user.manager.ldap.domain";
	public static final String LDAP_BIND_NAME = "user.manager.ldap.bind.name";
	public static final String LDAP_BIND_PWD = "user.manager.ldap.bind.password";
	public static final String LDAP_NAME_TAG = "user.manager.ldap.name.tag";
	public static final String LDAP_EMAIL_TAG = "user.manager.ldap.email.tag";
	public static final String LDAP_GROUP_TAG = "user.manager.ldap.group.tag";
	public static final String LDAP_ALLOWED_GROUPS = "user.manager.ldap.allowed.groups";
	
	public LdapUserManager(Props props) {
		host = props.getString(LDAP_HOST);
		port = props.getInt(LDAP_PORT);
		domain = LdapUtils.buildDomainBase(props.getString(LDAP_DOMAIN));
		bindName = props.getString(LDAP_BIND_NAME);
		bindPwd = props.getString(LDAP_BIND_PWD);
		nameTag = props.getString(LDAP_NAME_TAG);
		emailTag = props.getString(LDAP_EMAIL_TAG);
		groupTag = props.getString(LDAP_GROUP_TAG);
		allowedGroups = props.getStringList(LDAP_ALLOWED_GROUPS, ",");
		driver = new LdapDriver(host,port);
	}
	
	@Override
	public User getUser(String username, String password) throws UserManagerException {
		try {
			driver.bind(bindName, bindPwd);
			
			EntryCursor entries = driver.searchUserByName(domain, nameTag, username);
			Entry entry = null;	
			//didn't use entries.next(),for the reason of supporting badly to microsoft AD
			try {
				//first time to move the cursor
				entries.next();
				entry = entries.get();
			}
			catch(CursorException e) {
				throw new UserManagerException("No user found!", e);
			}
			
			try {
				//second to move the cursor
				entries.next();
				entry = entries.get();
				throw new UserManagerException("More than one user found!");
			}
			catch(CursorException e) {
				//do nothing
			}
			
			String userDn = entry.getDn().toString();
			driver.bind(userDn, password);
			String memberof = entry.get(groupTag).toString();
			boolean isMemberOfAllowedGroups = LdapUtils.memberOfAllowedGroups(memberof, allowedGroups);
			if(!isMemberOfAllowedGroups) {
				throw new UserManagerException("User is not member of allowed groups!");
			}
			
			String email = entry.get(emailTag).toString();
			User user = new User(username);
			user.setEmail(email);
			user.addRole("Admin");
			
			driver.unBind();
			return user;
			
		} catch (LdapException e) {
			throw new UserManagerException("Bind error: username or password was wrong!",e);
		}
	}

	@Override
	public boolean validateUser(String username) {
		try {
			driver.bind(bindName, bindPwd);
			EntryCursor entries = driver.searchUserByName(domain, nameTag, username);
			Entry entry = null;
			//didn't use entries.next(),for the reason of supporting badly to microsoft AD
			try {
				//first time to move the cursor
				entries.next();
				entry = entries.get();
			}
			catch(CursorException e) {
				return false;
			}
					
			try {
				//second to move the cursor
				entries.next();
				entry = entries.get();
				return false;
			}
			catch(CursorException e) {
				//do nothing
			}
			
			String memberof = entry.get(groupTag).toString();
			boolean isMemberOfAllowedGroups = LdapUtils.memberOfAllowedGroups(memberof, allowedGroups);
			if(!isMemberOfAllowedGroups) {
				return false;
			}
			return true;
			
		} catch (LdapException e) {
			return false;
		}
	}

	@Override
	public boolean validateGroup(String group) {
		if(allowedGroups.isEmpty()) {
			return true;
		}
		for(String allowedGroup : allowedGroups) {
			if(allowedGroup.contains(group)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Role getRole(String roleName) {
		Permission permission = new Permission();
		permission.addPermissionsByName(roleName.toUpperCase());
		Role role = new Role(roleName, permission);
		return role;
	}

	@Override
	public boolean validateProxyUser(String proxyUser, User realUser) {
		return false;
	}

}
