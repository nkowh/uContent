Ext.application({
    extend: 'starter.Application',

    views: [
        'starter.view.login.LoginFrame',
        'starter.view.main.Main',
        'starter.view.init.InitFrame'
    ],

    launch: function () {
        var me = this;
        Ext.Ajax.clearListeners();
        if (Ext.util.Cookies.get('userId') && Ext.util.Cookies.get('digest')) {
            Ext.Ajax.setDefaultHeaders({Authorization: Ext.util.Cookies.get('digest')})
            Ext.Ajax.addListener('requestexception', function (conn, response, options, eOpts) {
                if (response.status === 401) {
                    Ext.util.Cookies.clear('userId');
                    Ext.util.Cookies.clear('digest');
                    window.location.href = '/entry/index.html';
                }
            });
            me.setMainView('starter.view.main.Main');
        } else {
            window.location.href = '/entry/index.html';
        }

    }
});
