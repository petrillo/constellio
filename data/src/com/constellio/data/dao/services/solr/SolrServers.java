package com.constellio.data.dao.services.solr;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.constellio.data.dao.services.bigVault.solr.BigVaultLogger;
import com.constellio.data.dao.services.bigVault.solr.SolrBigVaultServer;
import com.constellio.data.extensions.DataLayerExtensions;

public class SolrServers {
	private final SolrServerFactory solrServerFactory;
	private final Map<String, SolrBigVaultServer> servers = new HashMap<>();
	private final BigVaultLogger bigVaultLogger;
	private final DataLayerExtensions extensions;

	public SolrServers(SolrServerFactory solrServerFactory, BigVaultLogger bigVaultLogger, DataLayerExtensions extensions) {
		this.solrServerFactory = solrServerFactory;
		this.bigVaultLogger = bigVaultLogger;
		this.extensions = extensions;
	}

	public synchronized SolrBigVaultServer getSolrServer(String core) {
		SolrBigVaultServer server = servers.get(core);
		if (server == null) {
			server = new SolrBigVaultServer(core, bigVaultLogger, solrServerFactory, extensions.getSystemWideExtensions());
			servers.put(core, server);
		}
		return server;
	}

	public synchronized void close() {
		solrServerFactory.clear();
		servers.clear();
	}

	public Collection<SolrBigVaultServer> getServers() {
		return servers.values();
	}

}
