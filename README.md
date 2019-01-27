
# react-native-android-sms-retriever

## Getting started

`$ npm install react-native-android-sms-retriever --save`

### Mostly automatic installation

`$ react-native link react-native-android-sms-retriever`

### Manual installation


#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNAndroidSmsRetrieverPackage;` to the imports at the top of the file
  - Add `new RNAndroidSmsRetrieverPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-android-sms-retriever'
  	project(':react-native-android-sms-retriever').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-android-sms-retriever/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-android-sms-retriever')
  	```


## Usage
```javascript
import RNAndroidSmsRetriever from 'react-native-android-sms-retriever';

/*
 * Returns app signature, which is used to identify the SMS sent from your server
 * SMS body Example: "<#> Your ExampleApp code is: 123ABC78 FA+9qCX9VSu"
 * The last part of above example is your app signature
 * callback: (optional) to receive signature array
 */
const signatureArray = await RNAndroidSmsRetriever.getAppSignature(callback);

/*
 * Subscribe to listen for incoming SMS
 * listener: ({ message: string, error: string, code: number})
 * callback: (optional) to check if subscription was successful or not
 * Note: This will timeout in 5 minutes, so call it when you are expecting a SMS
 */
const subscription = await RNAndroidSmsRetriever.retrieveSMS(listener, callback);

// To remove subscription
subscription.remove();
```

## Note

1. App signature will vary for debug and release builds
