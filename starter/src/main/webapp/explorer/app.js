Ext.form.action.Submit.override({
    onSuccess: function (response) {
        var form = this.form,
            formActive = form && !form.destroying && !form.destroyed,
            success = true,
            result = this.processResponse(response);

        if (result !== true && !result._created) {
            if (result.errors && formActive) {
                form.markInvalid(result.errors);
            }
            this.failureType = Ext.form.action.Action.SERVER_INVALID;
            success = false;
        }

        if (formActive) {
            form.afterAction(this, success);
        }
    }
});

Ext.application({
    name: 'explorer',

    extend: 'explorer.Application',

    views: [
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
