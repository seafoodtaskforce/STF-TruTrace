# # TruTrace - Backend Services

The goal of this software application (SA) is to provide a simple, easy-to-use, free SA that can be used on a mobile device or desktop to house and provide ease of flow of documentation throughout the supply chain that is required under the ***Seafood Task Force's*** (STF) electronic traceability (i.e. track and trace) program.

This is the backend services portion of this endeavour.
This is a collection of RESTful services which wrap around a number of DAO services to process user, document, and trace data API.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

Please read the general architecture and deployment documents in the \STF-TruTrace\docs\architecture and \STF-TruTrace\docs\deployment directories respectively on how to build and deploy this project as well as what technology is utilized.

This project is the back-end set of RESTful services which other clients such as android, iOS, or Html could utilize as long as they follow the provided RESTful contracts. 

The project is provided as an eclipse source code project with Maven dependencies configuration. You will find it in the \STF-TruTrace\platform-backend directory and you should be able to import it into eclipse as a project.


### Installing and Running the code

You will need to setup tomcat (or other J2EE) server to host the RESTful services which can be deployed as WAR file.

```
Do that directly from eclipse by right-clicking on the project, choosing "Export" option and then "WAR file" as the output.  
```

Once you have the WAR file you can simply:

```
Drop it into tomcat's \webapps directory
```

Once the application is deployed you can test if the server is live with a simple GET HTTP call from either your browser or Postman:

```
http://localhost:8080/WWFShrimpProject_v2/api_v2/server/summary
```

which should produce something like this:

```
/server/ com.wwf.shrimp.application is online...[2020-03-02 05:26:02]
``` 
## Deployment

Please read the deployment documents in the following directories:

```
\STF-TruTrace\docs\deployment\database
```

```
\STF-TruTrace\docs\deployment\server
```

```
\STF-TruTrace\docs\deployment\services
```

Additionally, please consult the UML document provided for a high-level overview of the services and classes.

## Built With

* [eclipse](https://www.eclipse.org/ide/) - The IDE used
* [Maven](https://maven.apache.org/) - Dependency Management
* [Tomcat](https://tomcat.apache.org/download-70.cgi) - Used as the J2EE application server
* [MySQL](https://dev.mysql.com/downloads/mysql/) - Used as the database engine and repository.
* [MySQL Workbench](https://dev.mysql.com/downloads/workbench/) - Integrated MySQL DB Management tool.
* [Postman](https://www.getpostman.com/apps) - Used as a simple tester of RESTful services
* [Jersey](https://jersey.github.io/) - Used as a the JAX-RS implementation of Web Services in java.
* [TCUML Tool](https://www.topcoder.com/community/tools/) - A UML tool used to document the high level class information about the architecture for the back-end.

## Contributing

This is a private repository.

## Versioning

We use semantic versioning and the code is simply versioned as a whole. Current version of the services is **1.91.0** 

You can query the server version through this RESTful URL:

```
http://localhost:8080/WWFShrimpProject_v2/api_v2/server/version
```
NOTE:The path specified above (**WWFShrimpProject_v2/api_v2/**) can be configured toyour specfic needs.
## Authors

* **Republic Systems** - *Phase 1* - [RepublicSystems](http://republicsystems.com/)

## License

This project is licensed under the **Apache License 2.0**.