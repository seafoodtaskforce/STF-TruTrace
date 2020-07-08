# iOS Deployment Document #

This document will outline the deployment of an iOS based smart-phone client (i.e.iPhone) against the TruTrace back-end application setup (which is outlined in the deployment documentation for database and back-end,) ad it will consist of the following steps:

1. Get the iOS XCode source code from *github*
2. Load it into XCode IDE
3. Change the server URL in the configuration file to point the mobile device correctly to the back-end.
4. Generate the .IPA and load it onto your device.


**NOTE:** This manual does assume that you have development level skills and can work with a development environment. It does **NOT** require any coding though, just a recompilation of the code base and a creation of an .IPA file.

## Set Up The Environment: ##

Please make sure first that the proper server environment is setup before you proceed to recompile the client. Refer to the **Environment Setup Manual**

## Set Up IDE Environment ##
The code for iOS was developed using XCode and the protect is provided in *github*.

**Getting XCode**

1. You can obtain XCode here: [XCode download](https://developer.apple.com/support/xcode/ "XCode Download")

Please refer to specific XCode Documentation

## Make the change to connect the iOS client to server services ##
Once the database and the back-end services have been setup we will setup the connectivity of the iOS client.

1. In the project you will find a `server.json` file.
	1. This configuration file has an entry that points to the back-end services.
	2. Change the following line in this file with the full url to your back-end services:
		1. `"accessUrl": "http://your server url:8080/WWFShrimpProject_v2/api_v2"`
		2. Save the file
3. Compile the project

**Create the .IPA to be deployed**

1. Create the .IPA file which can then be deployed and run against the server.

Once you start the application on your device you will be able to log in with the admin credentials you obtained when you created the back-end environment. The back-end script should have created a `Super Admin` which will have a `user-name: "trutrace.admin" and password: "admin"` which you can use to log in using the Android application.

Once you do that you will be able to see the see and any existing documents in the system. 

## Summary ##
Once you have been able to set this up you will be able to add users and documents and see traces. Please refer to the specific manuals for learning how to use the application.

