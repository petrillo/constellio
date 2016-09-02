package com.constellio.app.modules.es.services.crawler;

import java.util.List;

import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorEventObserver;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;

public interface ConnectorJobCrawler {

	public abstract <V> void crawl(String connectorInstanceId, List<ConnectorJob> jobs, ConnectorEventObserver connectorEventObserver)
			throws Exception;

	public boolean hasActiveJobsFor(String connectorInstanceId);
}
