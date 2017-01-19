package com.constellio.data.dao.services.bigVault.solr;

import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.NULL_STRING;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient.RouteException;
import org.apache.solr.client.solrj.impl.CloudSolrClient.RouteResponse;
import org.apache.solr.client.solrj.impl.HttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.UpdateParams;
import org.apache.solr.common.util.NamedList;
import org.joda.time.LocalDateTime;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException.CouldNotExecuteQuery;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException.OptimisticLocking;
import com.constellio.data.dao.services.bigVault.solr.BigVaultRuntimeException.BadRequest;
import com.constellio.data.dao.services.bigVault.solr.BigVaultRuntimeException.SolrInternalError;
import com.constellio.data.dao.services.bigVault.solr.listeners.BigVaultServerAddEditListener;
import com.constellio.data.dao.services.bigVault.solr.listeners.BigVaultServerListener;
import com.constellio.data.dao.services.bigVault.solr.listeners.BigVaultServerQueryListener;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.solr.ConstellioSolrInputDocument;
import com.constellio.data.dao.services.solr.DateUtils;
import com.constellio.data.dao.services.solr.SolrServerFactory;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.data.utils.TimeProvider;
import com.google.common.annotations.VisibleForTesting;

public interface BigVaultServer {

	public SolrServerFactory getSolrServerFactory();

	//chargement
	public QueryResponse query(SolrParams params)
			throws CouldNotExecuteQuery;

	public void softCommit()
			throws IOException, SolrServerException;

	public TransactionResponseDTO addAll(BigVaultServerTransaction transaction)
			throws BigVaultException;

	public SolrClient getNestedSolrServer();

	public AtomicFileSystem getSolrFileSystem();

	public void removeLockWithAgeGreaterThan(int ageInSeconds);

	public void reload();

	public BigVaultServer cloneServer();

	public void disableLogger();

	public void expungeDeletes();
}
