package com.constellio.model.conf.ldap;

import java.util.List;

import org.joda.time.Duration;

import com.constellio.model.conf.ldap.config.AzureADServerConfig;
import com.constellio.model.conf.ldap.config.AzureADUserSyncConfig;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.sdk.SDKPasswords;

public class AzureADTestConfig {
	public static LDAPServerConfiguration getLDAPServerConfiguration() {
		AzureADServerConfig azurServerConf = new AzureADServerConfig().setClientId(SDKPasswords.testAzureClientId())
				.setTenantName(SDKPasswords.testAzureTenantName())
				.setClientSecret(SDKPasswords.testAzureApplicationKey());
		return new LDAPServerConfiguration(azurServerConf, false);
	}

	public static LDAPUserSyncConfiguration getLDAPUserSyncConfiguration(List<String> collections) {
		AzureADUserSyncConfig azurUserSynch = new AzureADUserSyncConfig();
		return new LDAPUserSyncConfiguration(azurUserSynch, null, null, new Duration(10000000), collections);

	}
}
