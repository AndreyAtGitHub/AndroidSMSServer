# SMS Gateway
This app turns Android device into SMS gateway which allows sending SMS through your Android device using `HTTP` requests.

![sms server](https://user-images.githubusercontent.com/35717992/144476637-ff9c0ed0-934c-4a0f-8004-c9b0dfc2e783.gif)


# Usage
To send SMS, HTTP client must provide `phone` and `message` parameters to path `/sendSMS` via `POST` method

# Example
POST `https://192.168.0.102:8081/sendSMS`

with `POST` body containing following paramaters

 * `phone` = "0347123456"
 * `message` = "Hello your verification code is 2341"       

# Note
As per Android offical docs https://developer.android.com/about/versions/kitkat/android-4.4#SMS 
>Beginning with Android 4.4, the system settings allow users to select a "default SMS app." Once selected, only the default SMS app is able to write to the SMS Provider and only the default SMS app receives the SMS_DELIVER_ACTION broadcast when the user receives an SMS

[SMS_DELIVER_ACTION](https://developer.android.com/reference/android/provider/Telephony.Sms.Intents#SMS_DELIVER_ACTION) is intent which is broadcast by Android OS to apps when delivery report arrives from SMSC (sms center)

So, Android SMS server app (non default sms app) has no way to notify http clients about whether delivery was successfull or not. It can only tell whether sms was successfully sent or not 😢


# Download APK
Download APK from [release section](https://github.com/umer0586/AndroidSMSServer/releases) *(require Android 5.0)*
