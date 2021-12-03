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


# Download APK
Download APK from [release section](https://github.com/umer0586/AndroidSMSServer/releases) *(require Android 5.0)*
