# Fast-Track Deployment Document #

This document will outline a fast-track version of setting up the application environment for the database, Back-End services, and the web-application. At the end of this process you should have accomplished the following:

1. Create the necessary database tables with super-admin access.
3. Deploy (i.e. Setup, configure, and test connectivity) The Back-End services.
4. Deploy (i.e. Setup, configure and test connectivity) The Web-Based desktop application.

**NOTE:** This manual does not require you to setup up any coding/development environment. To look more at the coding environment please refer to the **Advanced Deployment Manual**.

## Set Up The Environment: ##

Please make sure first that the proper environment is setup before you proceed to stand-up the services and the desktop application. Refer to the **Environment Setup Manual**

## Set Up The Database Tables ##
Here what we will do is setup the table structure for the database which will create a single super admin user for the application.

**Setup the tables**

1. Locate the Database creation script: `\STF-TruTrace\deployment\backend_db_script_v2.sql`
2. Run this script in the MySQL database.
	1. You can do this through MySQL Workbench as shown here: [Generate DB from SQL in Workbench](https://dev.mysql.com/doc/workbench/en/wb-reverse-engineer-create-script.html)
	2. Alternatively, you can do this through the command line as shown here: [Generate DB from SQL using command line](https://stackoverflow.com/questions/8940230/how-to-run-sql-script-in-mysql)

**Ensure Admin Access**
The script should create a Super Admin which will have a `user-name: "admin" and password: "admin"` which you will use later to log into the Admin Portal. 


## Deploy The Back-end Services ##
Once the database has been setup we will setup the back-end services. This will be done in a few steps:

**NOTE:** Make sure that you have the MySQL root name and password ready for this step.

1. Stop the TomCat server.
2. Locate the WAR file with the Back-End code: `\STF-TruTrace\deployment\WWFShrimpProject_v2.war`
3. Copy this file to a temporary directory.
4. Open up the .WAR file with a zip program (i.e. unzip it)
5. You will see a set of files and directories.
	1. Locate the following file: 'WWFShrimpProject_v2\META-INF\context.xml'
	2. Change the following line in this file with your root name and password data:
		1. `username="root" password="your password"` 
		2. Save the file
	3. Zip the whole `WWFShrimpProject_v2` directory into a new file and overwrite the existing .WAR file.
4. Take the modified .WAR file and drop it into the Drop it into tomcat's `\webapps directory`
5. Restart Tomcat.

Once the application is deployed you can test if the server is live with a simple GET HTTP call from your browser:

`http://localhost:8080/WWFShrimpProject_v2/api_v2/server/summary`

which should produce something like this:

`/server/ com.wwf.shrimp.application is online...[2020-03-02 05:26:02]`

Once this setup is done and the server is running you can then run the web-Based Desktop Application.

**NOTE** if you have setup the back-end services remotely then you should be able to hit the services through a public IPv4 address (for example):

`http://www.somedomain.com:8080/WWFShrimpProject_v2/api_v2/server/summary`


## Deploy The Desktop Application ##
Once the database and the back-end services have been setup we will setup and deploy the admin portal application.

1. Stop the TomCat server.
2. Locate the .ZIP file with the Back-End code: `\STF-TruTrace\deployment\desktop-application-distro.zip`
3. Copy this file and drop it into tomcat's `\webapps directory`
4. Open up the .ZIP file with a zip program (i.e. unzip it)
	1. On windows right-click on the file and choose "Extract Here..."
	2. This should explode the file into a `\TruTraceDesktopApplication` directory
5. Go into the `\TruTraceDesktopApplication\assets` directory.
	1. Locate the following file: 'server.json'
	2. This configuration file points to the back-end services.
	2. Change the following line in this file with the url to your back-end services:
		1. ` "accessUrl": "http://your url:8080/WWFShrimpProject_v2/api_v2"` 
		2. Save the file
4. Restart Tomcat.

Once the server has started you should be able to run the desktop as follows:

`http://localhost:8080/TruTraceDesktopApplication//#/login`

You should see a login screen:

![alt text](https://github.com/STF-TruTrace/blob/master/deployment/artifacts/desktop.login.page.png)

Login with Admin and admin as your user-name and password:

![alt text](https://github.com/STF-TruTrace/blob/master/deployment/artifacts/desktop.login.page.filled.png)

##Summary##
Once you have been able to set this up you will be able to add users and documents and see traces. Please refer to the specific manuals for learning how to use the application.

