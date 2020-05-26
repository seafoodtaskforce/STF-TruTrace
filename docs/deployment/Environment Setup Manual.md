# Application Environment Setup Document #

This document will outline the setting up of the environment for the Java Virtual Machine, Database, and Application Server and the Web-Based Desktop application. At the end of this process you should have accomplished the following:

1. Set up the environment for Java, MySQL Database, and Tomcat Server.
2. Test your environment

We will need to make sure first that the proper environment is setup before we can stand-up the services and the desktop application.

**NOTE:** This manual does not require you to setup up any coding/development environment. To look more at the coding environment please refer to the Full-Scope Deployment Document.

# Java Environment # !need to specify compatible versions?
You will need Java installed on your system first. If you do not then follow the appropriate link: 


1. Windows - [Java Installation for Windows](https://docs.oracle.com/en/java/javase/11/install/installation-jdk-microsoft-windows-platforms.html#GUID-96EB3876-8C7A-4A25-9F3A-A2983FEC016A "Java Installation for Windows")
2. Mac - [Java installation for a Mac](https://docs.oracle.com/en/java/javase/11/install/installation-jdk-macos.html#GUID-2FE451B0-9572-4E38-A1A5-568B77B146DE "Java installation for a Mac")
3. Linux - [Java installation for Linux](https://docs.oracle.com/en/java/javase/11/install/installation-jdk-linux-platforms.html#GUID-737A84E4-2EFF-4D38-8E60-3E29D1B884B8 "Java installation for Linux")

Once you have installed the java environment (or you already have it) proceed to the next step.

**To check your Java installation** please follow the instructions in this link:

1. Windows - [Check Java On Windows](https://www.ibm.com/support/knowledgecenter/en/SS88XH_1.6.0/iva/install_mils_windows_java.html "Check Java On Windows")
2. Mac - [Check Java On Mac](https://stackoverflow.com/questions/14292698/how-do-i-check-if-the-java-jdk-is-installed-on-mac "Check Java On Mac")
3. Linux - [Check Java On Linux](https://superuser.com/questions/356519/how-to-know-that-java-is-installed-in-a-linux-system/356520 "Check Java On Linux")

# Database Environment #
We will need to setup the database environment for our data layer. We will be installing the MySQL database server and client.

1. Windows - [Install MySQL on Windows](https://dev.mysql.com/doc/refman/8.0/en/windows-installation.html "Install MySQL on Windows")
2. Mac - [Install MySQL on Mac](https://dev.mysql.com/doc/refman/8.0/en/osx-installation.html "Install MySQL on Mac")
3. Linux - [Install MySQL on Linux](https://dev.mysql.com/doc/refman/8.0/en/linux-installation.html "Install MySQL on Linux")

**To check and verify your installation** please follow these instructions:

1. [General Tutorial](https://dev.mysql.com/doc/refman/8.0/en/tutorial.html)
2. [Check your server installation](https://dev.mysql.com/doc/refman/8.0/en/connecting-disconnecting.html)
3. [Run Queries](https://dev.mysql.com/doc/refman/8.0/en/entering-queries.html)

**Note Down Root access**

1. Make sure to get the Database Engine Root user name and the password.
2. We will need it during deployment.

**Highly Recommended:** A useful tool to download and use would be the MySQL Workbench which gives you a GUI environment to run and work with the database.

You can install it from here: [MySQL Workbench Installer](https://dev.mysql.com/downloads/workbench/ "MySQL Workbench Installer")

# Application Server Environment #
We will need to setup the server environment which will run the back-end services as well as the desktop web-application. We will be using Tomcat Application server version 7.x (we tested it on this version)

**NOTE:** This step depends on having properly setup the Java Environment outlined above.

1. [Download Tomcat 7.x Server](https://tomcat.apache.org/download-70.cgi "Download and Install Tomcat 7.x Server")
2. [Setup The Server](https://tomcat.apache.org/tomcat-7.0-doc/setup.html "Setup The Server")
2. [Verify the installation](https://tomcat.apache.org/tomcat-7.0-doc/appdev/deployment.html "Verify the installation")
3. [Secure Your Server](http://tomcat.apache.org/tomcat-7.0-doc/security-howto.html "Secure Your Server")

**Secure your server:** Make sure that the server is secure and is not open to the world with default Admin settings. Please make sure to properly change the admin passwords in the *tomcat-users.xml* configuration file.

1. You will find this file in the `\Tomcat 7.0\conf` directory
2. Make sure to remove the standard user permissions and create a specific admin user.

    ``` 
	<tomcat-users>
		<user username="Your Admin Name" password="Your Password" roles="admin-gui,manager-gui" />
	 </tomcat-users>
	```

## Summary ##
Once you have the environment setup you can move to the deployment stage.

1. If you want to stand up the application without needing to setup the development environment please refer to the **Fast-Track Deployment Manual**
2. If you need to go through the development environment then please follow the **Advanced Deployment Manual** (currently being created.)

You will find both documents in the same directory as this manual.
