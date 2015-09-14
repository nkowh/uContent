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
    extend: 'starter.Application',

    views: [
        'starter.view.login.LoginFrame',
        'starter.view.main.Main',
        'starter.view.init.InitFrame'
    ],

    launch: function () {
        var me = this;
        Ext.Ajax.clearListeners();
        Ext.Ajax.request({
            url: '/initialization/status',
            success: function (response, opts) {
                me.onSuccess(response.responseText);
            }, failure: function (response, opts) {
                Ext.Msg.show({
                    title: '错误',
                    message: '网络通信失败!',
                    buttons: Ext.Msg.OK,
                    icon: Ext.Msg.ERROR
                });
            }
        });

    },

    onSuccess: function (status) {
        var me = this;
        if ('false' === status) {
            me.setMainView('starter.view.init.InitFrame');
        } else if (Ext.util.Cookies.get('userId') && Ext.util.Cookies.get('digest')) {
            Ext.Ajax.addListener('requestexception', function (conn, response, options, eOpts) {
                if (response.status === 401) {
                    Ext.util.Cookies.clear(userId);
                    Ext.util.Cookies.clear('digest')
                    window.location.reload();
                }
            });
            me.setMainView('starter.view.main.Main');
        } else {
            me.setMainView('starter.view.login.LoginFrame');
        }
    }
});
