var exec = require('cordova/exec');

var HeadsetDetection = {
  detect: function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "HeadsetDetection", "detect", []);
  },
  registerRemoteEvents: function(actionCallback) {
    // Need to call a native function to start recieve events on android
    exec(null, null, "HeadsetDetection", "registerRemoteEvents", []);
    this.actionCallback = actionCallback;
  },
  remoteHeadsetRemoved: function() {
    this.actionCallback && this.actionCallback('headsetRemoved');
  },
  remoteHeadsetAdded: function() {
    this.actionCallback && this.actionCallback('headsetAdded');
  },
  remoteBluetoothHeadsetRemoved: function() {
    this.actionCallback && this.actionCallback('bluetoothHeadsetRemoved');
  },
  remoteBluetoothHeadsetAdded: function() {
    this.actionCallback && this.actionCallback('bluetoothHeadsetAdded');
  }

};

module.exports = HeadsetDetection;
