# Introduction #
Effective traceability tools—that can be widely adopted at scale—must be low cost and developed to maximize ease of use by companies and less sophisticated producers and processors along their supply chain. 

TruTrace is a cloud-based smartphone app and web portal that can connect entire supply chains from beginning to end. 
To ensure the app is equally accessible to each stage of the supply chain, it is open source and publicly available here on GitHub for any supply chain actor to utilize. This way farmers, consumers, and everyone in between can utilize the app without paying licensing fees that have hindered traceability efforts in the past.
  

# How This Application's Code Is Organized #

This application comprises 4 main elements:

1. **Backend Server** - where all the main functionality, access, and data security resides.
2. **Desktop Application** - which is a web based portal to the application including administrative capabilities. Here you will be able to administer data traceability and manage infrastructure as in:
	1. Document management such as accepting or rejecting documents that were submitted from the field.
	2. Running traceability charts to see the full trace of a given commodity as well export such data into PDF formatted documents.
	3. User management to manage user access and authorization (i.e. access to functionality)
	4. Resource Management such as document data, organization chains, and stage distributions.
5. **Android Application** - which is a mobile version of the application that will run on most android smart phones and which allows for creation and management of traceability data in the field such as the capture of documents to be added to the traceability chain.
6. **iOS Application** - which is a mobile version of the application that will run on iPhones and which allows for creation and management of traceability data in the field such as the capture of documents to be added to the traceability chain.

Together all these pieces comprise the whole "system". Typical usage scenario would be as follows:

1. Field Users can either use the Android or iPhone client application to capture document data and add it to the trace chain.
2. Office users can then view the different document submissions in a trace format on the Desktop to see the full data "movement" for the specific commodity traced for the given organization.
3. Office users will also be able to add new organizations and stages to the traced chain as well as manage user access for the application as a whole.

## Setting up the system ##

The application is provided in two different formats:

1. Precompiled partial system elements for easier deployment.
2. Complete source code which comprises the four elements mentioned above. This is for the advanced user.

# Precompiled System Setup #
This is a simpler way of setting up the application *without the need* to compile server code.
Please consult the following documentation in the provided order:

1. **Set up the server environment:** [Environment Setup Manual](https://github.com/seafoodtaskforce/STF-TruTrace/blob/master/docs/deployment/Environment%20Setup%20Manual.md)
	1. Here you will setup the java environment, database environment, and the application server.
2. **Setup the Back-End and Admin Portal:** [Fast-Track Deployment Document](https://github.com/seafoodtaskforce/STF-TruTrace/blob/master/docs/deployment/Fast-Track%20Deployment%20Manual.md)
	1. You will setup the database structure
	2. You will setup the back-end services to go live
	3. You will setup the Desktop Application Admin Portal
4. **Setup the Android Client:** Please refer to the Advanced System Setup below.
5. **setup iOS client:** Please refer to the Advanced System Setup below.

# Advanced System Setup #
In general any documentation that deals with deployment/setup, as well as any architectural documentation necessary to understand the source code, would be provided in those sub-folders.

![git directory structure](https://github.com/seafoodtaskforce/STF-TruTrace/blob/master/deployment/artifacts/github.dir.structure.png)

1. **Setup the Android Client:** [Setting Up Android Client Application](https://github.com/seafoodtaskforce/STF-TruTrace/blob/master/docs/deployment/Android%20(Advanced)%20Deployment%20Manual.md "Setting Up Android Client Application")
	1. **NOTE:** Android versions supported: 5+
2. **Setup the iOS Client:** [Setting Up iOS Client Application](https://github.com/seafoodtaskforce/STF-TruTrace/blob/master/docs/deployment/iOS%20(Advanced)%20Deployment%20Manual.md "Setting Up iOS Client Application")
	1. **NOTE:** iPhones 6s and up are supported.