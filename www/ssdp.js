/* global cordova */

var exec = require('cordova/exec');

var SSDP = {

    /**
     * Discover the specified network services.
     *
     * @param {String} service
     * @param {function(array): void} successCallback callback A function that run when the discover is successful.
     * @param {function(any): void} errorCallback callback A function that run when the discover is successful.
     */
    getNetworkServices: function(service, successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'SSDP', 'getNetworkServices', [service]);
    }

};

module.exports = SSDP;
