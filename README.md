Azkaban Ldap UserManager
========================

This plugin enables ldap authentication for the Azkaban workflow manager (https://azkaban.github.io/)

This plugin supports two productions: Microsoft Active Directory and OpenLDAP, and it is work in progress, configuration options may change.

Installation
------------

Build the plugin

```
gradle build
```

and place the created jar from ./build/libs into the ./extlib folder of Azkaban (see also http://azkaban.github.io/azkaban/docs/2.5/#custom-usermanager) for details.

In your azkaban.properties file set the UserManager to the new Ldap one:

```
user.manager.class=com.hfbank.azkaban.user.LdapUserManager
```

Configuration
-------------

The following configuration options are currently available:

```
user.manager.ldap.host=192.168.10.137
user.manager.ldap.port=398
user.manager.ldap.domain=test.com
user.manager.ldap.bind.name=cn=Manager,dc=test,dc=com
user.manager.ldap.bind.password=123456
user.manager.ldap.name.tag=uid(sAMAccountName in microsoft active directory)
user.manager.ldap.email.tag=mail
user.manager.ldap.group.tag=member(memberof in microsoft active directory)
user.manager.ldap.allowed.groups=ou=finandata,ou=marketing(In Microsoft Active Directory, the value may start with "cn=")
```
