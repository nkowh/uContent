Ext.define('starter.view.monitor.OsModel', {
    extend: 'Ext.app.ViewModel',
    alias: 'viewmodel.os',

    stores: {
        os: {type: 'os'}
    },

    data: {
        'name': '',
        'transport_address': '',
        'host': '',
        'ip': '',
        'version': '',
        'build': ''
    }

});
