Ext.define('dm.view.system.Initialization', {
    extend: 'Ext.panel.Panel',

    initComponent: function () {
        var me = this;
        Ext.apply(me, {
            items: [
                //{
                //    xtype: 'button',
                //    scale: 'large',
                //    iconAlign: 'top',
                //    text: '清空文档',
                //    glyph: 0xf1c0,
                //    handler: me.clearFiles
                //},
                //{
                //    xtype: 'button',
                //    scale: 'large',
                //    iconAlign: 'top',
                //    text: '文档结构初始化',
                //    glyph: 0xf1c0,
                //    handler: me.initFiles
                //},

                {
                    xtype: 'button',
                    scale: 'large',
                    iconAlign: 'top',
                    text: '重建索引',
                    glyph: 0xf1c0,
                    handler: me.reindexing
                }
            ]

        });

        me.callParent();
    },

    clearFiles: function () {
        Ext.Ajax.request({
            url: Ext.util.Cookies.get('service') + '/documents/person', method: 'DELETE',
            callback: function (opts, success, response) {
                Ext.toast({
                    html: response.responseText,
                    closable: false,
                    align: 't',
                    slideInDuration: 1000,
                    minWidth: 400
                });
            }
        });
    },

    initFiles: function () {

        var q = {
            person: {
                properties: {
                    _acl: {
                        type: "nested"
                    },
                    _contents: {
                        type: "nested"
                    }
                }
            }
        }
        Ext.Ajax.request({
            url: Ext.util.Cookies.get('service') + '/documents/_mapping/person', method: 'POST', jsonData: q,
            callback: function (opts, success, response) {
                Ext.toast({
                    html: response.responseText,
                    closable: false,
                    align: 't',
                    slideInDuration: 400,
                    minWidth: 400
                });
            }
        });
    },

    reindexing: function () {
        Ext.ux.Ajax
            .put(Ext.util.Cookies.get('service').replace('/dm','/my_index_v1'))
            .then(Ext.ux.Ajax.put(Ext.util.Cookies.get('service').replace('/dm','/my_index_v1')), errorHandler)
            .then(Ext.ux.Ajax.request('data3.json'), errorHandler)
            .then(function (data3) {
            alert("Ah! That's much better ;)");
        }, errorHandler);
    }


});