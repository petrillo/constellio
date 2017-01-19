package com.constellio.data.dao.services.bigVault.solr;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;

import com.constellio.data.dao.dto.records.QueryResponseDTO;
import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException.CouldNotExecuteQuery;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException.OptimisticLocking;
import com.constellio.data.dao.services.bigVault.solr.BigVaultRuntimeException.TryingToRegisterListenerWithExistingId;
import com.constellio.data.dao.services.bigVault.solr.listeners.BigVaultServerAddEditListener;
import com.constellio.data.dao.services.bigVault.solr.listeners.BigVaultServerListener;
import com.constellio.data.extensions.DataLayerSystemExtensions;

public abstract class BaseBigVaultServer implements BigVaultServer {

	protected BigVaultLogger bigVaultLogger;
	protected DataLayerSystemExtensions extensions;
	protected final String name;
	protected final List<BigVaultServerListener> listeners;

	public BaseBigVaultServer(String name, DataLayerSystemExtensions extensions) {
		this.name = name;
		this.extensions = extensions;
		this.listeners = new ArrayList<>();
	}

	public BaseBigVaultServer(String name, DataLayerSystemExtensions extensions,
			List<BigVaultServerListener> listeners) {
		this.name = name;
		this.extensions = extensions;
		this.listeners = listeners;
	}

	public void registerListener(BigVaultServerListener listener) {
		for (BigVaultServerListener existingListener : this.listeners) {
			if (existingListener.getListenerUniqueId().equals(listener.getListenerUniqueId())) {
				throw new TryingToRegisterListenerWithExistingId(listener.getListenerUniqueId());
			}
		}
		this.listeners.add(listener);
	}

	public void unregisterListener(BigVaultServerListener listener) {
		Iterator<BigVaultServerListener> iterator = this.listeners.iterator();
		while (iterator.hasNext()) {
			BigVaultServerListener existingListener = iterator.next();
			if (existingListener.getListenerUniqueId().equals(listener.getListenerUniqueId())) {
				iterator.remove();
				//only one
				return;
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setExtensions(DataLayerSystemExtensions extensions) {
		this.extensions = extensions;
	}

	public SolrDocumentList queryResults(SolrParams params)
			throws BigVaultException.CouldNotExecuteQuery {

		return query(params).getResults();
	}

	public void disableLogger() {
		bigVaultLogger = BigVaultLogger.disabled();
	}

	public abstract QueryResponse query(SolrParams params)
			throws CouldNotExecuteQuery;

	public SolrDocument querySingleResult(SolrParams params)
			throws BigVaultException {
		SolrDocumentList results = queryResults(params);
		if (results.isEmpty()) {
			throw new BigVaultException.NoResult(params);
		} else if (results.size() > 1) {
			throw new BigVaultException.NonUniqueResult(params, results);
		} else {
			SolrDocument document = results.get(0);
			if (document == null) {
				throw new BigVaultException.NoResult(params);
			}
			return document;
		}
	}

	public long countDocuments() {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "*:*");
		try {
			return query(params).getResults().getNumFound();
		} catch (CouldNotExecuteQuery couldNotExecuteQuery) {
			throw new RuntimeException(couldNotExecuteQuery);
		}
	}

	public void unregisterAllListeners() {
		this.listeners.clear();
	}
}
