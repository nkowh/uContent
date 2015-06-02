Ext.define('dm.view.system.NewType', {
    extend: 'Ext.form.Panel',
    frame: true,
    bodyPadding: 10,
    scrollable: true,
    width: 355,

    fieldDefaults: {
        labelAlign: 'right',
        labelWidth: 115,
        msgTarget: 'side'
    },

    items: [{
        xtype: 'fieldset',
        title: 'Type Info',
        defaultType: 'textfield',
        defaults: {
            anchor: '100%'
        },

        items: [
            {allowBlank: false, fieldLabel: 'Name', name: 'typeName', emptyText: 'type name'},
        ]
    }],

    buttons: [{
        text: '注册',
        disabled: true,
        formBind: true,
        handler: function () {
            var me = this;
            var typeName = me.up('form').getForm().getValues().typeName;
            var mapping = {};
            mapping[typeName] = {
                properties: {
                    _acl: {
                        type: "nested"
                    },
                    _contents: {
                        type: "nested"
                    }
                }
            };

            Ext.Ajax.request({
                method: 'POST',
                url: Ext.util.Cookies.get('service') + '/documents/_mapping/' + typeName,
                jsonData: mapping,
                callback: function (options, success, response) {
                    if (!success) {
                        form.toast(response.responseText);
                        return;
                    }
                    me.up('window').close();

                }
            });


        }
    }, {
        text: '关闭',
        handler: function () {
            var me = this;
            me.up('window').close();
        }
    }]


});