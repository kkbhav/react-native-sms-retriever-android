
# react-native-sms-retriever-android

## Getting started

`$ npm install react-native-sms-retriever-android --save`

### Mostly automatic installation

`$ react-native link react-native-sms-retriever-android`

### Manual installation


#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNSmsRetrieverAndroidPackage;` to the imports at the top of the file
  - Add `new RNSmsRetrieverAndroidPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-sms-retriever-android'
  	project(':react-native-sms-retriever-android').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-sms-retriever-android/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-sms-retriever-android')
  	```


## Usage
```javascript
import RNSmsRetrieverAndroid from 'react-native-sms-retriever-android';

/*
 * Returns app signature, which is used to identify the SMS sent from your server
 * SMS body Example: "<#> Your ExampleApp code is: 123ABC78 FA+9qCX9VSu"
 * The last part of above example is your app signature
 * callback: (optional) to receive signature array
 */
const signatureArray = await RNSmsRetrieverAndroid.getAppSignature(callback);

/*
 * Subscribe to listen for incoming SMS
 * listener: ({ message: string, error: string, code: number})
 * callback: (optional) to check if subscription was successful or not
 * Note: This will timeout in 5 minutes, so call it when you are expecting a SMS
 */
const subscription = await RNSmsRetrieverAndroid.retrieveSMS(listener, callback);

// To remove subscription
subscription.remove();
```

## Note

1. App signature will vary for debug and release builds
