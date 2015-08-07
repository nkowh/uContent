Ext.define('dm.Application', {
    extend: 'Ext.app.Application',

    name: 'dm',
    models: [],
    stores: [],

    requires: [
        'Ext.data.proxy.OData'
    ],
    launch: function () {
        // TODO - Launch the application
    },

    onAppUpdate: function () {
        Ext.Msg.confirm('Application Update', 'This application has an update, reload?',
            function (choice) {
                if (choice === 'yes') {
                    window.location.reload();
                }
            }
        );
    }
});
