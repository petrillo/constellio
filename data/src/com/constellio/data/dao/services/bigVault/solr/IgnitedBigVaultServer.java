package com.constellio.data.dao.services.bigVault.solr;

import static java.util.Arrays.asList;
import static org.apache.ignite.transactions.TransactionConcurrency.OPTIMISTIC;
import static org.apache.ignite.transactions.TransactionIsolation.READ_COMMITTED;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.transactions.Transaction;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.jetbrains.annotations.NotNull;

import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.dto.solr.IgnitedSolrStore.IgnitedSolrStoreFactory;
import com.constellio.data.dao.services.bigVault.BigVaultRecordDao;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException.CouldNotExecuteQuery;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException.OptimisticLocking;
import com.constellio.data.dao.services.bigVault.solr.listeners.BigVaultServerListener;
import com.constellio.data.dao.services.solr.SolrServerFactory;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;

public class IgnitedBigVaultServer extends BaseBigVaultServer {

	BigVaultServer bigVaultServer;
	static IgniteCache<String, SolrDocument> cache;
	static Ignite igniteClient;
	static Ignite igniteServer;
	static AtomicInteger hitCount = new AtomicInteger();
	final static SolrDocument inModification = new SolrDocument();

	public IgnitedBigVaultServer(SolrBigVaultServer bigVaultServer) {
		super(bigVaultServer.name, bigVaultServer.extensions);
		this.bigVaultServer = bigVaultServer;
		init();
	}

	public IgnitedBigVaultServer(List<BigVaultServerListener> listeners,
			SolrBigVaultServer bigVaultServer) {
		super(bigVaultServer.name, bigVaultServer.extensions, listeners);
		this.bigVaultServer = bigVaultServer;
		init();
	}

	private void init() {

		IgniteConfiguration igniteServerConfiguration = igniteClientConfig();
		igniteServerConfiguration.setClientMode(false);
		igniteServer = Ignition.getOrStart(igniteServerConfiguration);
		System.out.println(igniteServer.name() + " started");
		IgniteConfiguration igniteClientConfiguration = igniteClientConfig();
		igniteClientConfiguration.setClientMode(true);
		igniteClient = Ignition.getOrStart(igniteClientConfiguration);
		System.out.println(igniteClient.name() + " started");

		CacheConfiguration<String, SolrDocument> recordsCacheConfig = new CacheConfiguration<>();
		recordsCacheConfig.setName("records");
		recordsCacheConfig.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
		recordsCacheConfig.setCacheMode(CacheMode.PARTITIONED);
		recordsCacheConfig.setBackups(1);
		recordsCacheConfig.setReadFromBackup(false);
		recordsCacheConfig.setCacheStoreFactory(new IgnitedSolrStoreFactory(bigVaultServer.getNestedSolrServer()));
		//recordsCacheConfig.setWriteBehindEnabled(true);
		recordsCacheConfig.setWriteThrough(true);
		recordsCacheConfig.setReadThrough(true);
		recordsCacheConfig.setAffinity(new RendezvousAffinityFunction());
		recordsCacheConfig.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);

		cache = igniteClient.getOrCreateCache(recordsCacheConfig);
		cache.removeAll();
	}

	@NotNull
	private IgniteConfiguration igniteClientConfig() {
		IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
		TcpDiscoverySpi spi = new TcpDiscoverySpi();
		// Creates a VmIPFinder. Note this is different from multi-cast, to limit nodes to our local machines.
		TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
		// Sets initial IP addresses.
		ipFinder.setAddresses(asList("127.0.0.1", "127.0.0.1:47500..47509"));
		// Sets the ip finder for the spi object
		spi.setIpFinder(ipFinder);

		igniteConfiguration.setDiscoverySpi(spi);
		igniteConfiguration.setPeerClassLoadingEnabled(true);
		igniteConfiguration.setClientMode(true);
		return igniteConfiguration;
	}

	@Override
	public SolrServerFactory getSolrServerFactory() {
		return bigVaultServer.getSolrServerFactory();
	}

	@Override
	public void softCommit()
			throws IOException, SolrServerException {
		bigVaultServer.softCommit();

	}

	private long cacheVersionNumber() {
		return new Date().getTime();
	}

	private List<String> getIdsOfDeleteByQuery(final String query) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.add("q", query);
		params.add("fl", "id");
		params.add("rows", "999999999");
		SolrDocumentList documents;
		try {
			documents = bigVaultServer.getNestedSolrServer().query(params).getResults();
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}

		List<String> ids = new ArrayList<>();
		for (SolrDocument solrDocument : documents) {
			ids.add((String) solrDocument.getFieldValue("id"));
		}
		return ids;
	}

	@Override
	public TransactionResponseDTO addAll(BigVaultServerTransaction transaction)
			throws BigVaultException {

		long start = cacheVersionNumber();
		//TODO : Utiliser cache.putIfAbsent() pour vérifier que la cache ne contient pas un record

		List<String> deleteByQueryIds = new ArrayList<>();
		for (String deleteByQuery : transaction.getDeletedQueries()) {
			if (deleteByQuery.equals("*:*")) {
				try {
					bigVaultServer.getNestedSolrServer().deleteByQuery(deleteByQuery);
					bigVaultServer.getNestedSolrServer().commit(true, true, true);
				} catch (SolrServerException | IOException e) {
					throw new RuntimeException(e);
				}
				cache.removeAll();
			} else {
				deleteByQueryIds.addAll(getIdsOfDeleteByQuery(deleteByQuery));
			}
		}

		List<String> idsInTransactions = new ArrayList<>();
		for (SolrInputDocument inputDocument : transaction.getNewDocuments()) {
			idsInTransactions.add((String) inputDocument.getFieldValue("id"));
		}
		for (SolrInputDocument inputDocument : transaction.getUpdatedDocuments()) {
			idsInTransactions.add((String) inputDocument.getFieldValue("id"));
		}
		idsInTransactions.removeAll(transaction.getDeletedRecords());
		idsInTransactions.removeAll(deleteByQueryIds);

		Map<String, Long> versions = new HashMap<>();

		Transaction tx = igniteClient.transactions().txStart(OPTIMISTIC, READ_COMMITTED);
		try {

			List<SolrInputDocument> atomicUpdates = new ArrayList<>();
			atomicUpdates.addAll(transaction.getUpdatedDocuments());
			for (SolrInputDocument inputDocument : transaction.getNewDocuments()) {

				List<String> fieldsWithMapValue = fieldsWithMapValue(inputDocument);
				if (!fieldsWithMapValue.isEmpty()) {
					atomicUpdates.add(inputDocument);
				} else {

					String id = (String) inputDocument.getFieldValue("id");
					long newVersion = cacheVersionNumber();
					inputDocument.setField("_version_", newVersion);
					cache.put(id, ClientUtils.toSolrDocument(inputDocument));

					for (String field : inputDocument.getFieldNames()) {
						Object fieldValue = inputDocument.getFieldValue(field);
						if (fieldValue instanceof Map) {
							fieldValue = ((Map) fieldValue).get("set");
						}

						validateNewField(idsInTransactions, transaction.isValidateNewReferences(), field, fieldValue);
					}

					versions.put(id, newVersion);
				}
			}

			for (SolrInputDocument inputDocument : atomicUpdates) {
				String id = (String) inputDocument.getFieldValue("id");

				Long expectedVersion = (Long) inputDocument.getFieldValue("_version_");
				//				if (id.startsWith("idx_act")) {
				//					if (expectedVersion == 1) {
				//
				//					}
				//
				//				} else {

				SolrDocument currentDocument = cache.get(id);

				long newVersion = cacheVersionNumber();
				if (expectedVersion != null) {
					if (currentDocument == null && expectedVersion > 1) {
						throw new BigVaultException.OptimisticLocking(id, expectedVersion, null);
					} else if (currentDocument == null && expectedVersion <= 0) {
						//OK
					} else if (currentDocument != null && expectedVersion < 0) {
						throw new BigVaultException.OptimisticLocking(id, expectedVersion, null);

					} else if (currentDocument != null && expectedVersion == 1) {
						//OK
					} else if (currentDocument != null && expectedVersion > 0) {
						long currentVersion = (Long) currentDocument.getFieldValue("_version_");

						if (expectedVersion != currentVersion) {
							throw new BigVaultException.OptimisticLocking(id, expectedVersion, null);
						}
					}
				}

				SolrInputDocument merge = currentDocument == null ? new SolrInputDocument() :
						ClientUtils.toSolrInputDocument(currentDocument);
				for (String field : inputDocument.getFieldNames()) {
					if (!field.equals("id") && !field.equals("_version_")) {
						Object fieldValue = inputDocument.getFieldValue(field);
						validateNewField(idsInTransactions, transaction.isValidateNewReferences(), field, fieldValue);
						if (fieldValue != null && fieldValue instanceof Map) {
							Map<String, Object> mapValue = (Map<String, Object>) fieldValue;
							if (mapValue.containsKey("inc")) {
								Double increment = ((Number) mapValue.get("inc")).doubleValue();
								Double currentValue = ((Number) merge.getFieldValue(field)).doubleValue();
								merge.setField(field, currentValue == null ? increment : increment + currentValue);
							} else if (mapValue.containsKey("set")) {
								Object newValue = mapValue.get("set");
								merge.setField(field, newValue);
							}
						} else {
							merge.setField(field, fieldValue);
						}
					}

				}
				merge.setField("_version_", newVersion);
				versions.put(id, newVersion);
				cache.put(id, ClientUtils.toSolrDocument(merge));
			}
			//			}

			for (String deleteId : transaction.getDeletedRecords()) {
				cache.remove(deleteId);
			}

			for (String deleteId : deleteByQueryIds) {
				cache.remove(deleteId);
			}

			tx.commit();

		} catch (BigVaultException.OptimisticLocking e) {
			tx.rollback();
			tx.close();
			throw e;

		} catch (Exception e) {
			tx.rollback();
			tx.close();
			throw new BigVaultRuntimeException("Exception while executing transaction", e);
		}

		long end = cacheVersionNumber();
		int duration = (int) (end - start);
		return new TransactionResponseDTO(duration, versions);

		//		for (SolrInputDocument inputDocument : transaction.getNewDocuments()) {
		//			cache.put((String) inputDocument.getFieldValue("id"), inModification);
		//		}
		//
		//		for (SolrInputDocument inputDocument : transaction.getUpdatedDocuments()) {
		//			cache.put((String) inputDocument.getFieldValue("id"), inModification);
		//		}
		//
		//		for (String removedDocument : transaction.getDeletedRecords()) {
		//			cache.put(removedDocument, inModification);
		//		}
		//
		//		TransactionResponseDTO responseDTO = bigVaultServer.addAll(transaction);
		//
		//		for (SolrInputDocument inputDocument : transaction.getNewDocuments()) {
		//			cache.remove((String) inputDocument.getFieldValue("id"));
		//		}
		//
		//		for (SolrInputDocument inputDocument : transaction.getUpdatedDocuments()) {
		//			cache.remove((String) inputDocument.getFieldValue("id"));
		//		}
		//
		//		for (String removedDocument : transaction.getDeletedRecords()) {
		//			cache.remove(removedDocument);
		//		}

	}

	private List<String> fieldsWithMapValue(SolrInputDocument document) {
		List<String> fields = new ArrayList<>();

		for (String field : document.getFieldNames()) {
			if (document.getFieldValue(field) instanceof Map) {
				fields.add(field);
			}
		}

		return fields;
	}

	private void validateNewField(List<String> idsInTransaction, boolean checkReferences, String field, Object fieldValue)
			throws OptimisticLocking {

		if (field.endsWith("_d") && fieldValue != null && !(fieldValue instanceof Number || fieldValue instanceof Map)) {
			Double.parseDouble(fieldValue.toString());

		} else if (field.endsWith("Id_s") && checkReferences) {
			String id;
			if (fieldValue instanceof String) {
				id = (String) fieldValue;

			} else if (fieldValue instanceof Map) {
				Object newValue = ((Map) fieldValue).get("set");
				if (newValue instanceof String) {
					id = (String) newValue;
				} else {
					throw new RuntimeException("Unsupported type : " + newValue.getClass().getName());
				}
			} else {
				throw new RuntimeException("Unsupported type : " + fieldValue.getClass().getName());
			}
			if (!idsInTransaction.contains(id) && cache.get(id) == null) {
				throw new BigVaultException.OptimisticLocking(BigVaultRecordDao.ACTIVE_IDX_PREFIX + id, 1L, null);
			}
		} else if (field.endsWith("Id_ss") && checkReferences) {

			List<String> ids;
			if (fieldValue instanceof String) {
				ids = asList((String) fieldValue);
			} else if (fieldValue instanceof List) {
				ids = (List) fieldValue;

			} else if (fieldValue instanceof Map) {
				Object newValue = ((Map) fieldValue).get("set");
				if (newValue instanceof String) {
					ids = asList((String) newValue);
				} else if (newValue instanceof List) {
					ids = (List) newValue;
				} else {
					throw new RuntimeException("Unsupported type : " + newValue.getClass().getName());
				}
			} else {
				throw new RuntimeException("Unsupported type : " + fieldValue.getClass().getName());
			}

			for (String id : ids) {
				if (!idsInTransaction.contains(id) && cache.get(id) == null) {
					throw new BigVaultException.OptimisticLocking(BigVaultRecordDao.ACTIVE_IDX_PREFIX + id, 1L, null);
				}
			}
		}
	}

	@Override
	public QueryResponse query(SolrParams params)
			throws CouldNotExecuteQuery {

		QueryResponse response = bigVaultServer.query(params);

		if (params.get("fl") == null) {
			for (SolrDocument document : response.getResults()) {
				String id = (String) document.getFieldValue("id");
				SolrDocument currentInCache = cache.get(id);

				if (currentInCache != null && currentInCache.getFieldValue("id") != null) {
					cache.put(id, document);
				}
			}
		}
		return response;
	}

	@Override
	public SolrClient getNestedSolrServer() {
		return bigVaultServer.getNestedSolrServer();
	}

	@Override
	public AtomicFileSystem getSolrFileSystem() {
		return bigVaultServer.getSolrFileSystem();
	}

	@Override
	public void removeLockWithAgeGreaterThan(int ageInSeconds) {
		bigVaultServer.removeLockWithAgeGreaterThan(ageInSeconds);
	}

	@Override
	public void reload() {
		bigVaultServer.reload();
	}

	@Override
	public BigVaultServer cloneServer() {
		return new IgnitedBigVaultServer(new ArrayList<>(listeners), (SolrBigVaultServer) bigVaultServer.cloneServer());
	}

	@Override
	public void expungeDeletes() {
		bigVaultServer.expungeDeletes();
	}

	public SolrDocument getById(String id) {
		SolrDocument document = cache.get(id);

		if (document != null && document.getFieldValue("id") == null) {
			return null;
		}

		if (document != null) {
			System.out.println("Hit count : " + hitCount.incrementAndGet());
		}

		return document;
	}
}
