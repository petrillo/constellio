package com.constellio.data.dao.services.bigVault.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;

import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException.CouldNotExecuteQuery;
import com.constellio.data.dao.services.bigVault.solr.listeners.BigVaultServerListener;
import com.constellio.data.dao.services.solr.SolrServerFactory;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;

public class IgnitedBigVaultServer extends BaseBigVaultServer {

	BigVaultServer BigVaultServer;

	public IgnitedBigVaultServer(SolrBigVaultServer BigVaultServer) {
		super(BigVaultServer.name, BigVaultServer.extensions);
		this.BigVaultServer = BigVaultServer;
	}

	public IgnitedBigVaultServer(List<BigVaultServerListener> listeners,
			SolrBigVaultServer BigVaultServer) {
		super(BigVaultServer.name, BigVaultServer.extensions, listeners);
		this.BigVaultServer = BigVaultServer;
	}

	@Override
	public SolrServerFactory getSolrServerFactory() {
		return null;
	}

	@Override
	public void softCommit()
			throws IOException, SolrServerException {
		//TODO

	}

	@Override
	public TransactionResponseDTO addAll(BigVaultServerTransaction transaction)
			throws BigVaultException {

		//TODO

		return null;
	}

	@Override
	public QueryResponse query(SolrParams params)
			throws CouldNotExecuteQuery {

		//TODO
		return null;
	}

	@Override
	public SolrClient getNestedSolrServer() {
		return BigVaultServer.getNestedSolrServer();
	}

	@Override
	public AtomicFileSystem getSolrFileSystem() {
		return BigVaultServer.getSolrFileSystem();
	}

	@Override
	public void removeLockWithAgeGreaterThan(int ageInSeconds) {
		BigVaultServer.removeLockWithAgeGreaterThan(ageInSeconds);
	}

	@Override
	public void reload() {
		BigVaultServer.reload();
	}

	@Override
	public BigVaultServer cloneServer() {
		return new IgnitedBigVaultServer(new ArrayList<>(listeners), (SolrBigVaultServer) BigVaultServer.cloneServer());
	}

	@Override
	public void expungeDeletes() {
		BigVaultServer.expungeDeletes();
	}

}
