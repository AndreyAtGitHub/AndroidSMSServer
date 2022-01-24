# SMS Gateway
This app turns Android device into SMS gateway which allows sending SMS through your Android device using `HTTP` requests.

![demo](https://user-images.githubusercontent.com/35717992/146224046-6c0d296b-dd10-4b0d-8897-56c7d460b418.gif)



# Usage
To send SMS, HTTP client must provide `phone` and `message` parameters to path `/sendSMS` via `POST` method

# Example

Using Node Js a simple http `POST` request would be :-
```javascript
const request = require('request');


request.post({
  url: 'http://192.168.0.101:8081/sendSMS',
  form: {
    phone: '03475144819',
    message: 'Your verification code is 1234'
  }
}, function (err, httpResponse, body) { 

    console.log(body);

 })
```
Response body will always be in `JSON` formate.

# Note
As per Android offical docs https://developer.android.com/about/versions/kitkat/android-4.4#SMS 
>Beginning with Android 4.4, the system settings allow users to select a "default SMS app." Once selected, only the default SMS app is able to write to the SMS Provider and only the default SMS app receives the SMS_DELIVER_ACTION broadcast when the user receives an SMS

[SMS_DELIVER_ACTION](https://developer.android.com/reference/android/provider/Telephony.Sms.Intents#SMS_DELIVER_ACTION) is intent which is broadcast by Android OS to apps when delivery report arrives from SMSC (sms center)

So, Android SMS server app (non default sms app) has no way to notify http clients about whether delivery was successfull or not. It can only tell whether sms was successfully sent or not. 


# Download APK
Download latest APK from [release section](https://github.com/umer0586/AndroidSMSServer/releases) *(requires Android 5.0)*. Don't forget to ⭐ this repo if you found this helpful
