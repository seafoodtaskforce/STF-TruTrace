# Android Deployment Document #

This document will outline the deployment of an Android based smart-phone client against the TruTrace back-end application setup (which is outlined in the deployment documentation for database and back-end,) ad it will consist of the following steps:

1. Get the Android source code from *github*
2. Load it into your favourite IDE
3. Change the server URL in the configuration file to point the mobile device correctly to the back-end.
4. Generate the APK and load it onto your device.


**NOTE:** This manual does assume that you have development level skills and can work with a development environment. It does **NOT** require any coding though, just a recompilation of the code base and a creation of an APK.

## Set Up The Environment: ##

Please make sure first that the proper server environment is setup before you proceed to recompile the client. Refer to the **Environment Setup Manual**

## Set Up IDE Environment ##
The code for Android was developed using Android Studio which you can use, but any other environment that you are familiar with, will suffice.

**Getting Android Studio**

1. You can obtain Android Studio here: [Android Studio Download](https://developer.android.com/studio "Android Studio Download")
2. The project in *github* is an Android Studio project so you can run it directly from within Android Studio.

Please refer to specific Android Studio Documentation.

## Make the change to connect the Android client to server services ##
Once the database and the back-end services have been setup we will setup the connectivity of the Android client.

1. In the source code go to the `\src\main\res\raw` directory.
2. In there you will find the `config.properties` text file
	1. This configuration file has an entry that points to the back-end services.
	2. Change the following line in this file with the url to your back-end services:
		1. ` server.backend.api.url="your server url"`
		2. **NOTE:** this is only the IP address and port number portion of the URL.
	3. Next (if necessary) change the URL post-fix in this line:
		1.  `server.backend.api.application.url=WWFShrimpProject_v2/api_v2`
		2.  **NOTE:** you will typically leave this unchanged if you followed the backend setup instructions.
		3. Save the file
3. Compile the project

**Create the APK to be deployed**

1. Create the APK file which can then be deployed and run against the server.

Once you start the application on your device you will be able to log in with the admin credentials you obtained when you created the back-end environment. The back-end script should have created a `Super Admin` which will have a `user-name: "trutrace.admin" and password: "admin"` which you can use to log in using the Android application.

Once you do that you will be able to see the see and any existing documents in the system. 

## Summary ##
Once you have been able to set this up you will be able to add users and documents and see traces. Please refer to the specific manuals for learning how to use the application.

