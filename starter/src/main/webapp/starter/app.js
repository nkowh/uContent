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
    name: 'starter',
    extend: 'starter.Application',

    requires: [
        'starter.view.main.Main',
        'starter.view.login.LoginFrame'
    ],

    launch: function () {
        var me = this;
        if (Ext.util.Cookies.get('userId') && Ext.util.Cookies.get('digest')) {
            me.setMainView('starter.view.main.Main');
        } else {
            me.setMainView('starter.view.login.LoginFrame');
        }

    }
});
