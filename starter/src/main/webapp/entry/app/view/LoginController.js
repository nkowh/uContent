Ext.define('entry.view.LoginController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.login',

    login: function () {
        var me = this;
        var form = me.getView().down('form').getForm();
        if (!form.isValid())return;
        var combo = me.getView().down('form').down('combo');
        var userId = me.getViewModel().get("userId");
        var password = me.getViewModel().get("password");
        var digest = 'Basic ' + Ext.util.Base64.encode(userId + ':' + password)
        Ext.Ajax.request({
            method: 'GET',
            headers: {Authorization: digest},
            url: '/svc/users/' + userId + '/groups',
            success: function (response, options) {
                var result = Ext.JSON.decode(response.responseText);
                var groups = Ext.Array.map(result.groups, function (g) {
                    return g._id;
                }).join(',');
                Ext.util.Cookies.set('userId', userId);
                Ext.util.Cookies.set('groups', groups);
                Ext.util.Cookies.set('digest', digest);
                window.location.href = combo.getValue();
            }, failure: function (response, options) {
                Ext.toast({
                    html: '<span style="color: red;">登录失败</span>',
                    width: 200,
                    align: 't'
                });
            }
        })
    }

});
