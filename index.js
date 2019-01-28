// @flow
import { NativeModules, Platform, DeviceEventEmitter } from 'react-native';

const { RNSmsRetrieverAndroid } = NativeModules;
const SMS_RECEIVED_EVENT = 'RNSmsRetrieverAndroid_SMS_RETRIEVE_EVENT';

const handleResponse = (callback?: (error: ?Error, data?: any) => void, err: ?Error, data?: any) => {
  if (err) {
    if (callback) return callback(err);
    return Promise.reject(err);
  }
  if (callback) return callback(err, data);
  return Promise.resolve(data);
};

let SMSListeners = [];

const retriever = {
  async getAppSignature(callback?: (error: ?Error, signatures?: Array<string>) => void) {
    try {
      if (Platform.OS === 'android') {
        const signatures = await RNSmsRetrieverAndroid.getAppSignature();
        return handleResponse(callback, null, signatures);
      }
      return handleResponse(callback, null, []);
    } catch (e) {
      // Handle Error
      return handleResponse(callback, e);
    }
  },
  async retrieveSMS(listener: (response: { message: ?string, error: ?string, code: number }) => void, callback?: (error: ?Error, subscription?: { remove: () => void }) => void) {
    try {
      if (Platform.OS === 'android') {
        if (!listener || typeof listener !== 'function') {
          throw new Error('Invalid arguments received');
        }
        await RNSmsRetrieverAndroid.retrieveSMS();
        const subscription = DeviceEventEmitter.addListener(SMS_RECEIVED_EVENT, listener);
        return handleResponse(callback, null, subscription);
      }
      return handleResponse(callback, null, { remove: () => {} });
    } catch (e) {
      // Handle Error
      return handleResponse(callback, e);
    }
  }
};

export default retriever;
