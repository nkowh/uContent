Ext.define('starter.monitor.NodeModel', {
    extend: 'Ext.app.ViewModel',
    alias: 'viewmodel.node',

    stores: {
        os: {type: 'os'}
    },

    data: {
        name: 'CP120'
    }

});
