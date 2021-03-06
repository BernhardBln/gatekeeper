package com.xorlev.gatekeeper.handler;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.xorlev.gatekeeper.AppConfig;
import com.xorlev.gatekeeper.data.Cluster;
import com.xorlev.gatekeeper.events.ConfigChangedEvent;
import com.xorlev.gatekeeper.data.Location;
import com.xorlev.gatekeeper.events.ClustersUpdatedEvent;
import com.xorlev.gatekeeper.events.ConfigWrittenEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

public class ConfigurationChangedEventHandler {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationChangedEventHandler.class);
    private final ConfigWriter configWriter;
    private final EventBus eventBus;

    @Inject
    public ConfigurationChangedEventHandler(EventBus eventBus, ConfigWriter configWriter) {
        this.configWriter = configWriter;
        this.eventBus = eventBus;
    }

    @Subscribe
    public void regenerateConfiguration(ClustersUpdatedEvent event) {
        ConfigChangedEvent configChangedEvent = buildConfigContext(event.getClusters());

        try {
            configWriter.writeConfig(configChangedEvent);
        } catch (IOException e) {
            log.error("Failed to write config, {}", e.getMessage(), e);
        }

        eventBus.post(new ConfigWrittenEvent(event));
    }

    private ConfigChangedEvent buildConfigContext(List<Cluster> clusters) {
        ConfigChangedEvent configChangedEvent = new ConfigChangedEvent(clusters);

        for (Cluster cluster : clusters) {
            log.info("({}) Locating contexts for {}://{}:{}, {} servers in group",
                    cluster.getClusterName(), cluster.getProtocol(), cluster.getClusterName(), cluster.getPort(),
                    cluster.getServers().size());

            for (String context : AppConfig.getStringList("cluster." + cluster.getClusterName() + ".context")) {
                log.info("({}) Adding context=[{}]", cluster.getClusterName(), context);
                configChangedEvent.getLocations().add(new Location(context, cluster));
            }
        }
        return configChangedEvent;
    }
}
