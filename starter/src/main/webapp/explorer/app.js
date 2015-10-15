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
    name: 'explorer',

    extend: 'explorer.Application',

    requires: [
        'explorer.view.main.Main'
    ],

    // The name of the initial view to create. With the classic toolkit this class
    // will gain a "viewport" plugin if it does not extend Ext.Viewport. With the
    // modern toolkit, the main view will be added to the Viewport.
    //
    mainView: 'explorer.view.main.Main'
	
    //-------------------------------------------------------------------------
    // Most customizations should be made to explorer.Application. If you need to
    // customize this file, doing so below this section reduces the likelihood
    // of merge conflicts when upgrading to new versions of Sencha Cmd.
    //-------------------------------------------------------------------------

});
