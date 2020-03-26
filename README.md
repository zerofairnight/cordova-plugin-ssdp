# Cordova SSDP

A cordova plugin to get SSDP services on a local network

## Usage
```js
    var success = function (devices) {
        devices.forEach(device => {
            console.log(device.usn)
            console.log(device.location)
            console.log(device.st)
            console.log(device.server)
        });
    }

    var failure = function(e) {
        console.error(e);
    }

    // ssdp:all
    // upnp:rootdevice
    // uuid:<device unique identifier>
    // urn:<fully qualified device type>

    // the fully qualified device type format
    // urn:<device namespace>:device:<device type>:<device version>

    // Examples:
    // uuid:00000000-0000-0000-0000-000000000000
    // urn:schemas-upnp-org:device:LocalService:1
    var service = 'ssdp:all';

    cordova.plugins.ssdp.getNetworkServices(service, success, failure);
```
