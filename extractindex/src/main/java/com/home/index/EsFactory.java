package com.home.index;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.InetAddress;

/**
 * Factory that returns ES transport client object configured using passed settings.
 */
public class EsFactory {

    private static final Log logger = LogFactory.getLog(EsFactory.class);

    /**
     * Returns a newly created ES client object.
     * @param esSettings Settings to configure the client.
     * @return Configured ES transport client.
     */
    public static Client getClient(EsSettings esSettings) {
        Settings settings = Settings.settingsBuilder().put("cluster.name", esSettings.getClusterName()).build();
        TransportClient client = TransportClient.builder().settings(settings).build();
        for(EsSettings.Host host : esSettings.getHosts()) {
            logger.debug("Adding host to Elasticsearch client, host: " + host.getHost() + ", port:" + host.getPort());
            try {
                client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host.getHost()), host.getPort()));
            }
            catch(Exception e) {
                System.out.println("Error adding address to ES: " + e);
            }
        }
        return client;
    }
}
