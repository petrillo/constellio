package com.constellio.data.dao.dto.solr;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.cache.Cache.Entry;
import javax.cache.configuration.Factory;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriterException;

import org.apache.ignite.cache.store.CacheStore;
import org.apache.ignite.cache.store.CacheStoreAdapter;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.UpdateParams;

import com.constellio.data.dao.services.bigVault.solr.BigVaultUpdateRequest;
import com.constellio.data.utils.ImpossibleRuntimeException;

public class IgnitedSolrStore extends CacheStoreAdapter<String, SolrDocument> {

	SolrClient nestedSolrClient;

	public IgnitedSolrStore(SolrClient nestedSolrClient) {
		this.nestedSolrClient = nestedSolrClient;
	}

	@Override
	public SolrDocument load(String key)
			throws CacheLoaderException {

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.add("q", "id:" + key);

		try {
			SolrDocumentList solrDocumentList = nestedSolrClient.query(params).getResults();
			return solrDocumentList.isEmpty() ? null : solrDocumentList.get(0);

		} catch (SolrServerException e) {
			throw new CacheLoaderException(e);
		}

	}

	@Override
	public void writeAll(Collection<Entry<? extends String, ? extends SolrDocument>> entries) {
		List<SolrInputDocument> solrInputDocuments = new ArrayList<>();

		for (Entry<? extends String, ? extends SolrDocument> entry : entries) {
			SolrInputDocument solrInputDocument = new SolrInputDocument();
			for (String field : entry.getValue().getFieldNames()) {
				Object value = entry.getValue().getFieldValue(field);
				solrInputDocument.addField(field, value);
			}
			solrInputDocument.removeField("_version_");
			solrInputDocuments.add(solrInputDocument);
		}

		BigVaultUpdateRequest req = new BigVaultUpdateRequest();
		req.setParam(UpdateParams.VERSIONS, "true");
		req.add(solrInputDocuments);
		try {
			UpdateResponse updateResponse = req.process(nestedSolrClient);
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
		softCommit();
	}

	@Override
	public void deleteAll(Collection<?> keys) {
		List<SolrInputDocument> solrInputDocuments = new ArrayList<>();

		BigVaultUpdateRequest req = new BigVaultUpdateRequest();
		req.setParam(UpdateParams.VERSIONS, "true");
		req.deleteById(new ArrayList<String>((Collection) keys));
		try {
			UpdateResponse updateResponse = req.process(nestedSolrClient);
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
		softCommit();
	}

	@Override
	public void write(Entry<? extends String, ? extends SolrDocument> entry)
			throws CacheWriterException {
		List<SolrInputDocument> solrInputDocuments = new ArrayList<>();

		SolrInputDocument solrInputDocument = new SolrInputDocument();
		for (String field : entry.getValue().getFieldNames()) {
			Object value = entry.getValue().getFieldValue(field);
			solrInputDocument.addField(field, value);
		}
		solrInputDocument.removeField("_version_");
		solrInputDocuments.add(solrInputDocument);

		BigVaultUpdateRequest req = new BigVaultUpdateRequest();
		req.setParam(UpdateParams.VERSIONS, "true");
		req.add(solrInputDocuments);
		try {
			UpdateResponse updateResponse = req.process(nestedSolrClient);
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
		softCommit();

	}

	@Override
	public void delete(Object key)
			throws CacheWriterException {
		List<SolrInputDocument> solrInputDocuments = new ArrayList<>();

		BigVaultUpdateRequest req = new BigVaultUpdateRequest();
		req.setParam(UpdateParams.VERSIONS, "true");
		req.deleteById((String) key);
		try {
			UpdateResponse updateResponse = req.process(nestedSolrClient);
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
		softCommit();
	}

	public static class IgnitedSolrStoreFactory
			implements Serializable, Factory<CacheStore<? super String, ? super SolrDocument>> {

		String url;

		public IgnitedSolrStoreFactory(SolrClient solrClient) {

			if (solrClient instanceof HttpSolrClient) {
				this.url = ((HttpSolrClient) solrClient).getBaseURL();
			} else {
				throw new ImpossibleRuntimeException("Unsupported solr client : " + solrClient.getClass().getName());
			}
		}

		@Override
		public CacheStore<? super String, ? super SolrDocument> create() {
			return new IgnitedSolrStore(new HttpSolrClient(url));
		}
	}

	private void softCommit() {
		try {
			nestedSolrClient.commit(true, true, true);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
