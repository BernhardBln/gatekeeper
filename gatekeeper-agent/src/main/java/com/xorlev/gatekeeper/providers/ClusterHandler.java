package com.xorlev.gatekeeper.providers;

/**
 * 2013-09-18
 *
 * @author Michael Rose <elementation@gmail.com>
 */
public interface ClusterHandler {
    void processClusters(ClustersUpdatedEvent event);
}
