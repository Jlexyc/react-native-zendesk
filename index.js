import {
	NativeModules,
	NativeEventEmitter,
	DeviceEventEmitter,
	NativeAppEventEmitter,
	Platform,
} from 'react-native';

const Emitter = new NativeEventEmitter(NativeModules.RNZendeskBridge);

const zendeskEvents = {
	submitRequestCompletedSet: (callback) => {
		this.successListener = Emitter.addListener('submitRequestCompleted', (notification) => {
			if(callback) {
				callback(notification)
			}
		});
	},
	submitRequestCompletedClean: () => {
		this.successListener.remove();
	},
	getUpdatesCompletedSet: (callback) => {
		this.updateListener = Emitter.addListener('zendeskUpdatesReceived', (notification) => {
			if(callback) {
				callback(notification)
			}
		});
	},
	getUpdatesCompletedClean: () => {
		this.updateListener.remove();
	},
}


module.exports = {
	ZendeskSupport: NativeModules.RNZendeskBridge || {},
	zendeskEvents: zendeskEvents,
}
