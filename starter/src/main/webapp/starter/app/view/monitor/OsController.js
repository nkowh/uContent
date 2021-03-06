Ext.define('starter.view.monitor.OsController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.os',

    onAfterrender: function () {
        var me = this;
        var store = Ext.create('starter.store.Nodes');
        store.load(function (store, records, successful, eOpts) {
            Ext.each(records.getRecords(), function (rec) {
                var tab = me.getView().add({xtype: 'monitorNode'});
                tab.getViewModel().set("name", rec.get('name'));
                tab.getViewModel().set("transport_address", rec.get('transport_address'));
                tab.getViewModel().set("host", rec.get('host'));
                tab.getViewModel().set("version", rec.get('version'));
                tab.getViewModel().set("build", rec.get('build'));
            });
            me.getView().setActiveTab(0);
        });
    },

    onDeactivate: function () {
        this.getView().items.each(function (item) {
            item.getController().stopTimeChart();
        });
    },

    onActivate: function () {
        this.getView().items.each(function (item) {
            item.getController().restartTimeChart();
        });
    },

    startTimeChart: function () {
        this.timeChartTask = Ext.TaskManager.start({
            run: this.refresh,
            interval: 5000,
            scope: this
        });
    },

    restartTimeChart: function () {
        if (this.timeChartTask)
            Ext.TaskManager.start(this.timeChartTask);
    },

    stopTimeChart: function () {
        if (this.timeChartTask) {
            Ext.TaskManager.stop(this.timeChartTask);
        }
    },

    refresh: function () {
        var me = this,
            chart = me.lookupReference('time-chart'),
            store = chart.getStore();
        var vm = me.getViewModel();
        store.load({params: {node: vm.get('name')}});
    }

});
