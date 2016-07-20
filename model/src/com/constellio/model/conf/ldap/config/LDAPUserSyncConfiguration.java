package com.constellio.model.conf.ldap.config;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Duration;

import com.constellio.model.conf.ldap.RegexFilter;
import com.constellio.model.services.security.authentification.LDAPAuthenticationService;

public class LDAPUserSyncConfiguration {
	transient RegexFilter userFilter;

	transient RegexFilter groupFilter;

	Duration durationBetweenExecution;

	private List<String> selectedCollectionsCodes;

	AzureADUserSyncConfig azurUserSynchConfig = new AzureADUserSyncConfig();
	NonAzurADUserSyncConfig nonAzurADUserSyncConfig = new NonAzurADUserSyncConfig();

	public LDAPUserSyncConfiguration(String user, String password,
			RegexFilter userFilter, RegexFilter groupFilter, Duration durationBetweenExecution,
			List<String> groupBaseContextList, List<String> usersWithoutGroupsBaseContextList) {
		this(user, password, userFilter, groupFilter, durationBetweenExecution, groupBaseContextList,
				usersWithoutGroupsBaseContextList, new ArrayList<String>());
	}

	public LDAPUserSyncConfiguration(String user, String password,
			RegexFilter userFilter, RegexFilter groupFilter, Duration durationBetweenExecution,
			List<String> groupBaseContextList, List<String> usersWithoutGroupsBaseContextList,
			List<String> selectedCollectionsCodes) {
		this.nonAzurADUserSyncConfig.user = user;
		this.nonAzurADUserSyncConfig.password = password;
		this.userFilter = userFilter;
		this.groupFilter = groupFilter;
		this.durationBetweenExecution = durationBetweenExecution;
		this.nonAzurADUserSyncConfig.groupBaseContextList = groupBaseContextList;
		this.nonAzurADUserSyncConfig.usersWithoutGroupsBaseContextList = usersWithoutGroupsBaseContextList;
		this.selectedCollectionsCodes = selectedCollectionsCodes;
	}

	public LDAPUserSyncConfiguration(AzureADUserSyncConfig azurUserSynchConfig,
			RegexFilter userFilter, RegexFilter groupFilter, Duration durationBetweenExecution,
			List<String> selectedCollectionsCodes) {
		this.azurUserSynchConfig = azurUserSynchConfig;
		this.userFilter = userFilter;
		this.groupFilter = groupFilter;
		this.durationBetweenExecution = durationBetweenExecution;
		this.selectedCollectionsCodes = selectedCollectionsCodes;
	}

	public String getUser() {
		return nonAzurADUserSyncConfig.user;
	}

	public String getPassword() {
		return nonAzurADUserSyncConfig.password;
	}

	public boolean isUserAccepted(String userName) {
		if (userName == null) {
			return false;
		}
		if (userName.equals(LDAPAuthenticationService.ADMIN_USERNAME)) {
			return false;
		}
		if (this.userFilter == null) {
			return true;
		}
		return this.userFilter.isAccepted(userName);
	}

	public boolean isGroupAccepted(String groupName) {
		if (groupName == null) {
			return false;
		}
		if (this.groupFilter == null) {
			return true;
		}
		return this.groupFilter.isAccepted(groupName);
	}

	public Duration getDurationBetweenExecution() {
		return durationBetweenExecution;
	}

	public void setDurationBetweenExecution(Duration durationBetweenExecution) {
		this.durationBetweenExecution = durationBetweenExecution;
	}

	public List<String> getGroupBaseContextList() {
		return nonAzurADUserSyncConfig.groupBaseContextList;
	}

	public List<String> getUsersWithoutGroupsBaseContextList() {
		return nonAzurADUserSyncConfig.usersWithoutGroupsBaseContextList;
	}

	public String getUsersFilterAcceptanceRegex() {
		if (this.userFilter == null) {
			return "";
		}
		return this.userFilter.getAcceptedRegex();
	}

	public String getUsersFilterRejectionRegex() {
		if (this.userFilter == null) {
			return "";
		}
		return this.userFilter.getRejectedRegex();
	}

	public String getGroupsFilterAcceptanceRegex() {
		if (this.groupFilter == null) {
			return "";
		}
		return this.groupFilter.getAcceptedRegex();
	}

	public String getGroupsFilterRejectionRegex() {
		if (this.groupFilter == null) {
			return "";
		}
		return this.groupFilter.getRejectedRegex();
	}

	public RegexFilter getUserFilter() {
		return userFilter;
	}

	public RegexFilter getGroupFilter() {
		return groupFilter;
	}

	public List<String> getSelectedCollectionsCodes() {
		return selectedCollectionsCodes;
	}

}
