Ext.define('dm.view.Login', {
    extend: 'Ext.form.Panel',
    frame: true,
    width: 420,
    bodyPadding: 10,
    title: 'uContent DM',
    shadowOffset: 10,
    defaultType: 'textfield',

    items: [{
        xtype: 'component',
        hidden: true,
        id: 'qcode'
    },
        {
            allowBlank: false,
            fieldLabel: 'Service',
            name: 'service',
            value: window.location.protocol + '//' + window.location.hostname + ':' + window.location.port,
            emptyText: 'http://192.168.1.90:9200'
        },
        {
            allowBlank: false,
            fieldLabel: 'User ID',
            name: 'username',
            value: 'admin',
            emptyText: 'user id'
        }, {
            allowBlank: false,
            fieldLabel: 'Password',
            name: 'password',
            value: '123',
            emptyText: 'password',
            inputType: 'password'
        }
    ],

    tools: [
        {
            xtype: 'button',
            glyph: 0xf029,
            handler: function (button) {
                var form = button.up('form');
                var qrcode = form.down('component[id=qcode]');
                qrcode.isHidden() ? qrcode.show() : qrcode.hide();
            }
        }
    ],

    buttons: [
        {
            text: '进入',
            handler: function () {
                var me = this;
                var form = me.up('form');
                var values = me.up('form').getForm().getValues();
                Ext.Ajax.request({
                    method: 'GET',
                    params: {username: values.username, password: values.password},
                    url: values.service,
                    callback: function (options, success, response) {
                        if (!success) {
                            form.toast(response.responseText);
                            return;
                        }
                        var mask = Ext.create('Ext.LoadMask', {
                            msg: '进入...',
                            target: me.up('viewport')
                        });
                        mask.show();
                        Ext.util.Cookies.set("service", values.service);
                        Ext.util.Cookies.set("username", values.username);
                        Ext.util.Cookies.set("session", Ext.util.Base64.encode(values.username + ':' + values.password));
                        window.location.reload();
                    }
                });

            }
        }
    ],

    listeners: {
        afterrender: function () {
            new QRCode(document.getElementById("qcode"), window.location.href);
        }
    },

    initComponent: function () {


        this.defaults = {
            anchor: '100%',
            labelWidth: 120
        };

        this.callParent();


    },

    toast: function (html) {
        Ext.toast({
            html: html,
            closable: false,
            align: 't',
            slideInDuration: 400,
            minWidth: 400
        });
    }

});