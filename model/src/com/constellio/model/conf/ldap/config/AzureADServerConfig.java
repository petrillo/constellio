package com.constellio.model.conf.ldap.config;

public class AzureADServerConfig {

    public static final String GRAPH_API_URL = "https://graph.windows.net/";

    public static final String GRAPH_API_VERSION = "1.6";

    String authorityUrl = "https://login.microsoftonline.com/";
    String tenantName;
    String clientId;
    String clientSecret;
	String resource = GRAPH_API_URL;

	public String getAuthorityUrl() {
		return authorityUrl;
	}

	public AzureADServerConfig setAuthorityUrl(String authorityUrl) {
		this.authorityUrl = authorityUrl;
		return this;
	}

	public String getTenantName() {
		return tenantName;
	}

	public AzureADServerConfig setTenantName(String tenantName) {
		this.tenantName = tenantName;
		return this;
	}

    public String getClientId() {
        return clientId;
    }

    public AzureADServerConfig setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public AzureADServerConfig setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public String getResource() {
        return resource;
    }

    public AzureADServerConfig setResource(String resource) {
        this.resource = resource;
        return this;
    }
}
