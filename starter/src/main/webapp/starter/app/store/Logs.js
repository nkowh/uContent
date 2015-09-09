Ext.define('starter.store.Logs', {
    extend: 'Ext.data.Store',
    alias : 'store.logs',
    model: 'starter.model.Log',
    pageSize: 10,
    //sorters  : [{
    //    property : "logDate",
    //    direction: "DESC"
    //}],
    proxy: {
        type: 'rest',
        url: '/svc/logs',
        reader: {
            type: 'json',
            rootProperty: 'logInfos',
            totalProperty: 'total'
        }
    },
    autoLoad: true
});