package com.constellio.model.conf.ldap;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.model.conf.ldap.config.AzureADServerConfig;
import com.constellio.model.conf.ldap.config.AzureADUserSyncConfig;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.sdk.tests.ConstellioTest;

public class LDAPConfigurationManagerAcceptanceTest extends ConstellioTest {

	private LDAPConfigurationManager ldapConfigManager;
	private RegexFilter azureADUsersRegex = new RegexFilter("zAcceptUser", "zRejectUser"), azurGroupsRegex = new RegexFilter("zAccG",
			"zRejectGroups");
	private Duration azureADDuration = new Duration(120000 * 60);
	private List<String> azureADCollections = Arrays.asList("zAzurColl1", "zAzurColl2");

	@Before
	public void setup()
			throws Exception {
		//pour avoir le fichier d encryptage
		prepareSystem(
				withZeCollection()
		);

		sdkProperties = new HashMap<>();
		ldapConfigManager = getModelLayerFactory().getLdapConfigurationManager();
	}

	private void saveValidLDAPConfig() {
		LDAPServerConfiguration ldapServerConfiguration = LDAPTestConfig.getLDAPServerConfiguration();
		LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTestConfig.getLDAPUserSyncConfiguration();
		ldapConfigManager.saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);
	}

	private void saveValidAzureADConfig() {
		AzureADServerConfig serverConfig = new AzureADServerConfig().setClientId("zclientId").setAuthorityUrl("zUrl")
				.setTenantName("zTanentId");
		LDAPServerConfiguration ldapServerConfiguration = new LDAPServerConfiguration(serverConfig, false);
		AzureADServerConfig azureADServerConfig = new AzureADServerConfig().setClientSecret("zClientSecret");
		LDAPUserSyncConfiguration ldapUserSyncConfiguration = new LDAPUserSyncConfiguration(new AzureADUserSyncConfig(), azureADUsersRegex,
				azurGroupsRegex, azureADDuration, azureADCollections);
		ldapConfigManager.saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);
	}

	@Test
	public void givenConfigWithDurationWhenSaveConfigurationThenDurationIsSavedAsNull()
			throws Exception {
		LDAPServerConfiguration ldapServerConfiguration = LDAPTestConfig.getLDAPServerConfiguration();
		LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTestConfig.getLDAPUserSyncConfiguration();
		ldapUserSyncConfiguration.setDurationBetweenExecution(new Duration(0));
		ldapConfigManager.saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);

		assertThat(ldapConfigManager.isLDAPAuthentication()).isTrue();
		assertThat(ldapConfigManager.idUsersSyncActivated()).isFalse();
		ldapUserSyncConfiguration = ldapConfigManager.getLDAPUserSyncConfiguration();

		assertThat(ldapUserSyncConfiguration.getDurationBetweenExecution()).isNull();
	}

	@Test(expected = TooShortDurationRuntimeException.class)
	public void givenConfigWithAShortDurationWhenSaveConfigurationThenException()
			throws Exception {
		LDAPServerConfiguration ldapServerConfiguration = LDAPTestConfig.getLDAPServerConfiguration();
		LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTestConfig.getLDAPUserSyncConfiguration();
		ldapUserSyncConfiguration.setDurationBetweenExecution(new Duration(LDAPConfigurationManager.MIN_DURATION - 1));
		ldapConfigManager.saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);
	}

	@Test
	public void givenConfigWithANonShortDurationWhenSaveConfigurationSavedCorrectly()
			throws Exception {
		LDAPServerConfiguration ldapServerConfiguration = LDAPTestConfig.getLDAPServerConfiguration();
		LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTestConfig.getLDAPUserSyncConfiguration();

		Duration nonShortDuration = new Duration(LDAPConfigurationManager.MIN_DURATION + 1);
		ldapUserSyncConfiguration.setDurationBetweenExecution(nonShortDuration);
		ldapConfigManager.saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);
		ldapUserSyncConfiguration = ldapConfigManager.getLDAPUserSyncConfiguration();

		assertThat(ldapUserSyncConfiguration.getDurationBetweenExecution().toStandardMinutes())
				.isEqualTo(nonShortDuration.toStandardMinutes());
	}

	@Test
	public void givenLDAPSavedAfterAzurWhenGetLDAPServerConfigurationThenItIsCreatedWithConfigInformation()
			throws Exception {
		saveValidAzureADConfig();
		saveValidLDAPConfig();
		assertThat(ldapConfigManager.isLDAPAuthentication()).isTrue();
		LDAPServerConfiguration ldapServerConfiguration = ldapConfigManager.getLDAPServerConfiguration();

		assertThat(ldapServerConfiguration.getDirectoryType()).isEqualTo(LDAPDirectoryType.ACTIVE_DIRECTORY);
		assertThat(ldapServerConfiguration.getUrls()).containsAll(LDAPTestConfig.getUrls());
		assertThat(ldapServerConfiguration.getDomains()).containsAll(LDAPTestConfig.getDomains());

		assertThat(ldapServerConfiguration.getTenantName()).isNull();
		assertThat(ldapServerConfiguration.getAuthorityUrl()).isEqualTo("https://login.microsoftonline.com/");
		assertThat(ldapServerConfiguration.getClientId()).isNull();
	}

	@Test
	public void givenLDAPSavedAfterAzurWhenGetLDAPSyncConfigurationThenItIsCreatedWithConfigInformation()
			throws Exception {
		saveValidAzureADConfig();
		saveValidLDAPConfig();
		LDAPUserSyncConfiguration ldapUserSyncConfiguration = ldapConfigManager.getLDAPUserSyncConfiguration(true);

		//assertThat(ldapUserSyncConfiguration.getDurationBetweenExecution().getStandardDays()).isEqualTo(1l);
		assertThat(ldapUserSyncConfiguration.getGroupBaseContextList())
				.containsAll(Arrays.asList("OU=Groupes,DC=test,DC=doculibre,DC=ca"));
		assertThat(ldapUserSyncConfiguration.getUsersWithoutGroupsBaseContextList())
				.containsAll(Arrays.asList("CN=Users,DC=test,DC=doculibre,DC=ca"));
		assertThat(ldapUserSyncConfiguration.getUser()).isEqualTo(LDAPTestConfig.getUser());
		assertThat(ldapUserSyncConfiguration.getPassword()).isEqualTo(LDAPTestConfig.getPassword());
		assertThat(ldapUserSyncConfiguration.getGroupFilter().getAcceptedRegex())
				.isEqualTo(LDAPTestConfig.getGroupFiler().getAcceptedRegex());
		assertThat(ldapUserSyncConfiguration.getGroupFilter().getRejectedRegex())
				.isEqualTo(LDAPTestConfig.getGroupFiler().getRejectedRegex());
		assertThat(ldapUserSyncConfiguration.getUserFilter().getAcceptedRegex())
				.isEqualTo(LDAPTestConfig.getUserFiler().getAcceptedRegex());
		assertThat(ldapUserSyncConfiguration.getUserFilter().getRejectedRegex())
				.isEqualTo(LDAPTestConfig.getUserFiler().getRejectedRegex());
		assertThat(ldapUserSyncConfiguration.isGroupAccepted("GGS-SEC-ALF_SCEC_ext")).isFalse();
		assertThat(ldapUserSyncConfiguration.isGroupAccepted("GGS-SEC-ALF_SCEC")).isTrue();
		assertThat(ldapUserSyncConfiguration.isGroupAccepted("GGS-SEC-ALF_SCEC_ext")).isFalse();
		assertThat(ldapUserSyncConfiguration.isGroupAccepted("GGS-SEC-ALF_tous_centres_SCEC")).isFalse();
		assertThat(ldapUserSyncConfiguration.isUserAccepted("testuser")).isTrue();
		assertThat(ldapUserSyncConfiguration.isUserAccepted("testAuj")).isFalse();
		assertThat(ldapUserSyncConfiguration.isUserAccepted("admin")).isFalse();
	}

	@Test
	public void givenAzurSavedAfterLDAPWhenGetLDAPServerConfigurationThenItIsCreatedWithConfigInformation()
			throws Exception {
		saveValidLDAPConfig();
		saveValidAzureADConfig();

		assertThat(ldapConfigManager.isLDAPAuthentication()).isFalse();
		LDAPServerConfiguration ldapServerConfiguration = ldapConfigManager.getLDAPServerConfiguration();

		assertThat(ldapServerConfiguration.getClientId()).isEqualTo("zclientId");
		assertThat(ldapServerConfiguration.getAuthorityUrl()).isEqualTo("zUrl");
		assertThat(ldapServerConfiguration.getTenantName()).isEqualTo("zTanentId");

		assertThat(ldapServerConfiguration.getDirectoryType()).isEqualTo(LDAPDirectoryType.AZURE_AD);
		assertThat(ldapServerConfiguration.getUrls()).isNull();
		assertThat(ldapServerConfiguration.getDomains()).isNull();

	}

	@Test
	public void givenAzurSavedAfterLDAPWhenGetLDAPSyncConfigurationThenItIsCreatedWithConfigInformation()
			throws Exception {
		saveValidLDAPConfig();
		saveValidAzureADConfig();

		LDAPUserSyncConfiguration ldapUserSyncConfiguration = ldapConfigManager.getLDAPUserSyncConfiguration(true);

		assertThat(ldapUserSyncConfiguration.getGroupFilter().getAcceptedRegex()).isEqualTo(azurGroupsRegex.getAcceptedRegex());
		assertThat(ldapUserSyncConfiguration.getGroupFilter().getRejectedRegex()).isEqualTo(azurGroupsRegex.getRejectedRegex());
		assertThat(ldapUserSyncConfiguration.getUserFilter().getAcceptedRegex()).isEqualTo(azureADUsersRegex.getAcceptedRegex());
		assertThat(ldapUserSyncConfiguration.getUserFilter().getRejectedRegex()).isEqualTo(azureADUsersRegex.getRejectedRegex());
		assertThat(ldapUserSyncConfiguration.getSelectedCollectionsCodes()).containsExactlyElementsOf(azureADCollections);

		assertThat(ldapUserSyncConfiguration.getGroupBaseContextList()).isNull();
		assertThat(ldapUserSyncConfiguration.getUsersWithoutGroupsBaseContextList()).isNull();
		assertThat(ldapUserSyncConfiguration.getUser()).isNull();
		assertThat(ldapUserSyncConfiguration.getPassword()).isNull();
	}
}
