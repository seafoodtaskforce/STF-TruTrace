# TruTrace - Android Client

The goal of this software application (SA) is to provide a simple, easy-to-use, free SA that can be used on a mobile device or desktop to house and provide ease of flow of documentation throughout the supply chain that is required under the ***Seafood Task Force's*** (STF) electronic traceability (i.e. track and trace) program. 

This is the android client that will work with RESTful backend services.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

Please read the architecture and deployment documents in the \docs\deployment directories respectively on how to build and deploy this project as well as what technology is utilized.

This project is the android client which utilizes the backend RESTful services for the document management and traceability functionality

The project is provided as an Android Studio source code project with Gradle dependencies configuration. You will find it in the \platform-android directory and you should be able to import it into Android Studio as a project.


### Installing

The client will only work if the server side has been installed and is running. 

Please read [Server-Side-README.md](https://github.com/republic-systems/deloitte-wwf-shrimp-services/blob/master/README.md) for details on backend services installation. 

```
Make sure to configure the client with proper server URL which would be best set as a secure, https based, url.  
```
### Securing the client
When setting the client with RESTful access, if the backend services are provided through an *https* URL prefix then the Android client needs to simply point to that *https* based URL.

## Deployment

Standard deployment of an APK to Google Play applies here.



## Built With

* [Android](https://en.wikipedia.org/wiki/Android_Nougat) - Used java android SDK. Compiled with using 9.x (Pie) with back-compatibility down to android 5.0 (Lollipop)
* [Android Studio](https://developer.android.com/studio/index.html?gclid=CjwKCAjw-NXPBRB4EiwAVNRLKrZdO7UwdlU6xHhHmCufm-aNnKn6W4g5_-y2VZu0rB6PU49_mnCgbBoCIAkQAvD_BwE) - The IDE used, latest version (3.5.2)
* [Gradle](https://gradle.org/) - Dependency Management, latest version (5.4.1)
* [Material Design](https://developer.android.com/design/material/index.html) - UI paradigm used.

**Note:** All the libraries used in this project are open source and the list is provided in the Gradle file (build.gradle Module:app) in the application.

## Contributing

This is a private repository.

## Versioning

We use semantic versioning and the code is simply versioned as a whole. Current version is **1.99.0**

You can get the version of the client through the settings option in the UI. 


## Authors

* **Republic Systems** - *Phase 1* - [RepublicSystems](http://Republicsystems.com/)

## License

This project is licensed under the **Apache License 2.0**.
