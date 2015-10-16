/*
 * This file is generated and updated by Sencha Cmd. You can edit this file as
 * needed for your application, but these edits will have to be merged by
 * Sencha Cmd when upgrading.
 */
Ext.application({
    name: 'admin',

    extend: 'admin.Application',

    requires: [
        'admin.view.main.Main'
    ],

    // The name of the initial view to create. With the classic toolkit this class
    // will gain a "viewport" plugin if it does not extend Ext.Viewport. With the
    // modern toolkit, the main view will be added to the Viewport.
    //
    mainView: 'admin.view.main.Main',
    launch: function () {
        if (Ext.util.Cookies.get('userId') && Ext.util.Cookies.get('digest')) {
            Ext.Ajax.setDefaultHeaders({Authorization: Ext.util.Cookies.get('digest')})
            Ext.Ajax.addListener('requestexception', function (conn, response, options, eOpts) {
                if (response.status === 401) {
                    Ext.util.Cookies.clear('userId');
                    Ext.util.Cookies.clear('digest');
                    window.location.href = '/entry/index.html';
                }
            });
            this.setMainView('admin.view.main.Main');
        } else {
            window.location.href = '/entry/index.html';
        }
    }
});
