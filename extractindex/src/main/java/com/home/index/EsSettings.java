package com.home.index;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used for representing ElasticSearch settings.
 */
public class EsSettings {

    private String clusterName;

    private List<Host> hosts;

    public String getClusterName() {
        return clusterName;
    }

    public List<Host> getHosts() {
        return new ArrayList<>(hosts);
    }

    public static class Builder {
        private final String clusterName;
        private final List<Host> hosts;

        public Builder(String clusterName) {
            this.clusterName = clusterName;
            this.hosts = new ArrayList<>();
        }

        public void addHost(Host host) {
            hosts.add(host);
        }

        public EsSettings build() {
            return new EsSettings(this);
        }
    }

    private EsSettings(Builder builder) {
        this.hosts       = builder.hosts;
        this.clusterName = builder.clusterName;
    }

    private EsSettings() {
    }

    /**
     * Setting for localhost.
     *
     * @return Settings for using local ES cluster.
     */
    public static EsSettings getLocalSettings() {
        EsSettings localSettings = new EsSettings();
        localSettings.clusterName = "elasticsearch";
        localSettings.hosts = new ArrayList<>();
        localSettings.hosts.add(new Host("127.0.0.1", 9300));
        return localSettings;
    }

    public static class Host {
        private String host;
        private int port;

        public Host(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }
}
