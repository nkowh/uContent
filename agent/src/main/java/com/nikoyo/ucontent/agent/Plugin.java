package com.nikoyo.ucontent.agent;


import com.nikoyo.ucontent.agent.monitor.MonitorService;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.plugins.AbstractPlugin;

import java.util.Collection;

public class Plugin extends AbstractPlugin {


    @Inject
    public Plugin() {
    }

    public String name() {
        return "Agent";
    }

    public String description() {
        return "Agent plugin";
    }


    @Override
    public Collection<Class<? extends LifecycleComponent>> services() {
        return ImmutableList.<Class<? extends LifecycleComponent>>of(MonitorService.class);
    }
}
