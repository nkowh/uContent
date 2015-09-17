Ext.define('starter.view.monitor.Os', {
    extend: 'Ext.tab.Panel',
    xtype: 'monitorOs',
    controller: 'os',
    viewModel: 'os',

    listeners: {
        afterrender: 'onAfterrender',
        activate: 'onActivate',
        deactivate: 'onDeactivate'

    }

});