package com.constellio.app.modules.es.services.crawler;

import java.util.List;
import java.util.concurrent.*;

import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorEventObserver;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import org.apache.commons.collections.Bag;
import org.apache.commons.collections.BagUtils;
import org.apache.commons.collections.bag.HashBag;

public class MultithreadConnectorJobCrawler implements ConnectorJobCrawler {

	private final BlockingQueue<Runnable> queue;
	private final ThreadPoolExecutor executor;
	private final Bag runningJobs;

	public MultithreadConnectorJobCrawler() {
		queue = new LinkedBlockingQueue<>();
		executor = new ThreadPoolExecutor(30, 50, 10, TimeUnit.SECONDS, queue, new ThreadPoolExecutor.CallerRunsPolicy());
		runningJobs = BagUtils.synchronizedBag(new HashBag());
	}

	public boolean hasActiveJobsFor(Connector connector) {
		return runningJobs.contains(connector);
	}

	/* (non-Javadoc)
	 * @see connector.manager.ConnectorManager#crawl(java.util.List)
	 */
	public <V> void crawl(final Connector connector, final List<ConnectorJob> jobs, final ConnectorEventObserver observer)
			throws InterruptedException {
		runningJobs.add(connector);
		executor.submit(new Callable<Object>() {
			public Object call() {
				try {
					for (final ConnectorJob job : jobs) {
						job.run();
					}
				} finally {
					try {
						observer.flush();
					} finally {
						runningJobs.remove(connector, 1);
					}
				}
				return null;
			}
		});
	}

	public void shutdown() {
		executor.shutdown();
	}
}
