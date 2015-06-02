Ext.define('dm.view.monitor.cat.Allocation', {
    extend: 'Ext.Component',
    xtype: 'allocation',
    initComponent: function () {
        var me = this;
        Ext.apply(me, {
            listeners: {
                afterrender: function () {
                    me.reload();
                }
            }
        });
        me.callParent();

    },

    reload: function () {
        var me = this;
        Ext.Ajax.request({
            url: Ext.util.Cookies.get('service') + '/_cat/allocation?v&bytes=b',
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

                me.getEl().setHtml(me.buildTable(headers, body));
            }
        });

    },

    buildTable: function (headers, body) {
        var table = [];
        table.push('<table><th>')
        Ext.each(headers, function (item) {
            table.push('<td>' + item + '</td>');
        });
        table.push('</th>');
        Ext.each(body, function (row) {
            table.push('<tr>')
            Ext.each(row, function (col) {
                table.push('<td>' + col + '</td>');
            });
            table.push('</tr>')
        });
        table.push('</table>');
        return table.join('');
    }


});