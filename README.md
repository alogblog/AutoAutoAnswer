Auto AutoAnswer
==============

##Introduction

**Auto AutoAnswer** is an open sourced Android app forked from **AutoAnswer**.


First, I introduce the **AutoAnswer** Android app, which is open source project hosted in [Google Code](https://code.google.com/p/auto-answer/).
That AutoAnswer app was created by [EverySoft](http://www.everysoft.com).

I wanted that AutoAnswer can communicate with Android's automation apps such as Tasker or Locale by way of plugin, so I added additional automizing feature to original app. That's the reason why I prefixed duplicated **Auto** to app's name. And then I contacted the project owner by email, but in no vain. Email was returned with broken message, and that Google Code project seems to be deserted...

So I've uploaded the modified sources into github and you can get this app for free in Google's Playstore.
My modification is just only one Receiver class, so my knowledge about original app's routines about call and bluetooth interruption is a very small bit.

If this app is not working on your device, then that' the end. :( You shoud find another good apps.


If you use an automation app like Tasker or Locale, you can get my related free plugin in PlayStore.

##Features

- Can **enable/disable** the auto answering feature. When enabled, its icon is always on notification bar. If you want to disble or change its configuaration, you can tap the notification menu. Below options are valid only when enabled.
- Can select **which calls** it has to auto answer.
  - Calls from all callers
  - Only my contacts
  - Only starred contacts
- Can specify the **delay seconds** before auto answering, ex.) 2, 4, 6, 8, 10, 12 secs.
- Can turn on the **speakphone** when auto answering.
- Can use auto answering only when connected to **Bluetooth headset**.
- Can select not to auto answer **if in call**.

##Authors

- Original AutoAnswer app, [EverySoft](http://www.everysoft.com)
- Additional features for interacting with some automizing apps, [Lee Kyung-joon](http://alogblog.com)

##Copyright and License

[GNU GPL v3](http://www.gnu.org/licenses/gpl.html)







