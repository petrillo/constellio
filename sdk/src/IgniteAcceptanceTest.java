import java.util.Arrays;

import javax.cache.configuration.Factory;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction;
import org.apache.ignite.cache.store.CacheStore;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.transactions.Transaction;
import org.apache.solr.common.SolrDocument;
import org.junit.Before;
import org.junit.Test;

public class IgniteAcceptanceTest {

	private static class MyRecord {

		long version;
		String id;
		String info;

		public MyRecord(String id, long version, String info) {
			this.version = version;
			this.id = id;
			this.info = info;
		}
	}

	Ignite ignite;
	IgniteCache<String, MyRecord> zeCache;

	@Before
	public void setUp()
			throws Exception {
		IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
		TcpDiscoverySpi spi = new TcpDiscoverySpi();
		// Creates a VmIPFinder. Note this is different from multi-cast, to limit nodes to our local machines.
		TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
		// Sets initial IP addresses.
		ipFinder.setAddresses(Arrays.asList("127.0.0.1", "127.0.0.1:47500..47509"));
		// Sets the ip finder for the spi object
		spi.setIpFinder(ipFinder);

		igniteConfiguration.setDiscoverySpi(spi);
		igniteConfiguration.setPeerClassLoadingEnabled(true);
		igniteConfiguration.setClientMode(true);
		ignite = Ignition.start(igniteConfiguration);

		CacheConfiguration<String, MyRecord> configuration = new CacheConfiguration<>();
		configuration.setName("records");
		configuration.setAtomicityMode(CacheAtomicityMode.ATOMIC);
		configuration.setCacheMode(CacheMode.PARTITIONED);
		configuration.setBackups(1);
		configuration.setReadFromBackup(false);
		configuration.setAffinity(new RendezvousAffinityFunction());
		configuration.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);
		//		configuration.setCacheStoreFactory(new Factory<CacheStore<? super String, ? super MyRecord>>() {
		//			@Override
		//			public CacheStore<? super String, ? super MyRecord> create() {
		//				return null;
		//			}
		//		})
		zeCache = ignite.getOrCreateCache(configuration);
		zeCache.removeAll();
	}

	@Test
	public void testComputeGrid()
			throws Exception {

	}

	//
	//	@Test
	//	public void testTransactions()
	//			throws Exception {
	//
	//		zeCache.put("rec1", new MyRecord("rec1", 14L, "Record in first version"));
	//
	//		Thread t1 = new Thread() {
	//
	//			Transaction tx1;
	//
	//			@Override
	//			public void run() {
	//				tx1 = ignite.transactions().txStart();
	//				zeCache.put("rec1", new);
	//			}
	//		};
	//		t1.start();
	//
	//		Thread t2 = new Thread() {
	//
	//			Transaction tx2;
	//
	//			@Override
	//			public void run() {
	//				tx2 = ignite.transactions().txStart();
	//			}
	//		};
	//		t2.start();
	//
	//		Transaction tx2 = ignite.transactions().txStart();
	//
	//		tx1.commit();
	//		tx2.commit();
	//
	//	}
}
