Ext.define('chemistry.store.Logs', {
    extend: 'Ext.data.Store',
    alias : 'store.logs',
    model: 'chemistry.model.Log',
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