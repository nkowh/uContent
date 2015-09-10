Ext.define('starter.view.login.LoginController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.login',

    login: function () {
        var me = this;
        var viewport = me.getView();
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
                window.location.reload();
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
