package com.constellio.model.conf.ldap.config;

import java.util.List;

public class NonAzurADUserSyncConfig {
	String user, password;
	List<String> groupBaseContextList;
	List<String> usersWithoutGroupsBaseContextList;
}
