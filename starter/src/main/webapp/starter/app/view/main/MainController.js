Ext.define('starter.view.main.MainController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.main',

    onTitleClick: function (cmp, e, eOpts) {
        Ext.Msg.prompt('确认', '是否退出系统?', function (btn, text) {
            if (btn != 'ok')return;
            Ext.util.Cookies.clear('userId');
            Ext.util.Cookies.clear('digest');
        });
        Ext.Msg.show({
            title:'确认',
            message: '是否退出系统?',
            buttons: Ext.Msg.OKCANCEL,
            icon: Ext.Msg.QUESTION,
            fn: function(btn) {
                if (btn != 'ok')return;
                Ext.util.Cookies.clear('userId');
                Ext.util.Cookies.clear('digest');
                window.location.reload();
            }
        });
    }

});
