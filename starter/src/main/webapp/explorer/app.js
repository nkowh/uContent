Ext.application({
    name: 'explorer',

    extend: 'explorer.Application',

    requires: [
        'explorer.view.main.Main'
    ],

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
            this.setMainView('explorer.view.main.Main');
        } else {
            window.location.href = '/entry/index.html';
        }
    }


});
