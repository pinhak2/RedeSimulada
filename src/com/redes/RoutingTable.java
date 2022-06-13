package com.redes;

import java.io.Serializable;

public class RoutingTable implements Serializable {

    private String destinationPort;
    private int metric;
    private String exitPort;
    private String localPort;

    public RoutingTable(String destinationPort, int metric, String exitPort) {
        this.destinationPort = destinationPort;
        this.metric = metric;
        this.exitPort = exitPort;
    }

    public RoutingTable(String destinationPort, int metric, String exitPort, String localPort) {
        this.destinationPort = destinationPort;
        this.metric = metric;
        this.exitPort = exitPort;
        this.localPort = localPort;
    }

    public String getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(String destinationPort) {
        this.destinationPort = destinationPort;
    }

    public int getMetric() {
        return metric;
    }

    public void setMetric(int metric) {
        this.metric = metric;
    }

    public String getExitPort() {
        return exitPort;
    }

    public void setExitPort(String exitPort) {
        this.exitPort = exitPort;
    }

    public String getLocalPort() {
        return localPort;
    }

    public void setLocalPort(String localPort) {
        this.localPort = localPort;
    }

}
