/* global cordova */

var exec = require('cordova/exec');

var SSDP = {

    /**
     * Discover the specified network services.
     */
    getNetworkServices: function(service, successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'SSDP', 'getNetworkServices', [service]);
    }

};

module.exports = SSDP;
