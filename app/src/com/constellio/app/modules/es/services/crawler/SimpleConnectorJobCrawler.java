package com.constellio.app.modules.es.services.crawler;

import java.util.List;

import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorEventObserver;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import org.apache.commons.lang.NotImplementedException;

public class SimpleConnectorJobCrawler implements ConnectorJobCrawler {

	public SimpleConnectorJobCrawler() {
	}

	/* (non-Javadoc)
	 * @see connector.manager.ConnectorManager#crawl(java.util.List)
	 */
	@Override
	public <V> void crawl(String connectorInstanceId, List<ConnectorJob> jobs, ConnectorEventObserver observer)
			throws Exception {
		try {
			for (ConnectorJob job : jobs) {
				job.run();
			}
		} finally {
			observer.flush();
		}
	}

	public boolean hasActiveJobsFor(String connectorInstanceId) {
		return false;
	}
}
