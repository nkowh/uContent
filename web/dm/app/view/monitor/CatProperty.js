Ext.define('dm.view.monitor.CatProperty', {
    extend: 'Ext.grid.Panel',
    xtype: 'cat_property',
    initComponent: function () {
        var me = this;
        Ext.apply(me, {
            listeners: {
                afterrender: function () {
                    me.reload();
                }
            }
        })
        me.callParent();
    },

    reload: function () {
        var me = this;
        Ext.Ajax.request({
            url: Ext.util.Cookies.get('service') + '/_cat/' + me.tag + '?v',
            callback: function (options, success, response) {
                if (!success) {
                    form.toast(response.responseText);
                    return;
                }
                var table = [];
                Ext.each(Ext.util.CSV.decode(response.responseText, ' '), function (line) {
                    table.push(Ext.Array.filter(line, function (item) {
                        return item.length > 0
                    }));
                });
                var headers = table[0];
                var body = Ext.Array.toArray(table, 1, table.length - 1);
                var columns = [];
                Ext.each(headers, function (header) {
                    columns.push({dataIndex: header, text: header, flex: 1});
                });

                me.reconfigure(Ext.create('Ext.data.ArrayStore', {
                    fields: headers, data: body
                }), columns)
            }
        });

    }


});
