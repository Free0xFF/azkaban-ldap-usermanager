package cn.com.hfbank.azkaban;

import java.util.List;
import java.util.logging.Logger;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;

import azkaban.user.Permission;
import azkaban.user.Role;
import azkaban.user.User;
import azkaban.user.UserManager;
import azkaban.user.UserManagerException;
import azkaban.utils.Props;
import cn.com.hfbank.utils.LdapDriver;
import cn.com.hfbank.utils.LdapUtil;

public class OpenldapUserManager implements UserManager {
	//connection params
	protected static final Logger LOG = Logger.getLogger(AdLdapUserManager.class.getName());
	public static final String LDAP_HOST = "user.manager.ldap.host";
	public static final String LDAP_PORT = "user.manager.ldap.port";
	public static final String LDAP_DOMAIN = "user.manager.ldap.domain";
	public static final String LDAP_BIND_ACCOUNT = "user.manager.ldap.bindAccount";
	public static final String LDAP_BIND_PASSWORD = "user.manager.ldap.bindPassword";
	private String ldapHost;
	private int ldapPort;
	private String domain;
	private String bindUser;
	private String bindPwd;
	private String nameAlias;
	private String emailAlias;
	private String ldapType;
	
	//property name
	public static final String LDAP_NAME_ALIAS = "user.manager.ldap.name.alias";
	public static final String LDAP_EMAIL_ALIAS = "user.manager.ldap.email.alias";
	
	//groups
	public static final String LDAP_ALLOWEDGROUPS = "user.manager.ldap.allowedGroups";
	private List<String> allowedGroups;
	
	//ldap product name
	public static final String LDAP_TYPE = "user.manager.ldap.type";
	
	//drivers
	LdapDriver driver = null;
	
	
	public OpenldapUserManager(Props props) {
		//get params from config
		ldapHost = props.getString(LDAP_HOST);
		ldapPort = props.getInt(LDAP_PORT);
		domain = LdapUtil.buildDomainBase(props.getString(LDAP_DOMAIN));
		bindUser = props.getString(LDAP_BIND_ACCOUNT);
		bindPwd = props.getString(LDAP_BIND_PASSWORD);
		allowedGroups = LdapUtil.getAllowedGroups(props.getString(LDAP_ALLOWEDGROUPS));
		nameAlias = props.getString(LDAP_NAME_ALIAS);
		emailAlias = props.getString(LDAP_EMAIL_ALIAS);
		ldapType = props.getString(LDAP_TYPE);
		driver = new LdapDriver(ldapHost, ldapPort);
	}
	
	@Override
	public User getUser(String username, String password) throws UserManagerException {
		if(username == null || username.trim().isEmpty()) {
			throw new UserManagerException("Username is empty!");
		} else if (password == null || password.trim().isEmpty()) {
			throw new UserManagerException("Password is empty!");
		}
		
		try {
			driver.bind(bindUser, bindPwd);
			
			EntryCursor cursor = driver.searchUserByName(domain, nameAlias, username);
			if(!cursor.next()) {
				throw new UserManagerException("No user found!");
			}
			
			final Entry entry = cursor.get();
			if(cursor.next()) {
				throw new UserManagerException("More than one user found!");
			}
			
			String userDn = entry.getDn().toString();
			if(!LdapUtil.memberOfAllowedGroups(userDn, allowedGroups, ldapType)) {
				throw new UserManagerException("User is not member of allowed groups.");
			}
			
			driver.bind(userDn, password);
			String email = entry.get(emailAlias).getString();
			User user = new User(username);
			user.setEmail(email);
			user.addRole("Admin");
			driver.unBind();
			return user;
			
		} catch (LdapException e) {
			throw new UserManagerException("LDAP error!",e);
		} catch (CursorException e) {
			throw new UserManagerException("Cursor error!",e);
		}
	}

	@Override
	public boolean validateUser(String username) {
		try {
			driver.bind(bindUser, bindPwd);
			EntryCursor cursor = driver.searchUserByName(domain, nameAlias, username);
			if(!cursor.next()) {
				return false;
			}
			
			final Entry entry = cursor.get();
			if(cursor.next()) {
				return false;
			}
			
			String userDn = entry.getDn().toString();
			if(!LdapUtil.memberOfAllowedGroups(userDn, allowedGroups, ldapType)) {
				return false;
			}
			
			return true;
			
		} catch (LdapException e) {
			return false;
		} catch (CursorException e) {
			return false;
		}
	}

	@Override
	public boolean validateGroup(String group) {
		return allowedGroups.contains(group);
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
