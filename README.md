# Getting started

Clone this repo, install needed tools and run it (see below)

## Prerequisites

* Java 1.8
* SBT 0.13.5 or higher
* Scala 2.12
* Postgres 13 or higher

## Installing and running

After cloning project, navigate to root directory and pack FAT jar (with all dependencies) with

```
sbt assembly
```

This task will also execute spec tests. Then following options are available:

### Starting CLI from command line

```
java -cp target/scala-2.12/Tickets4Sale-assembly-0.1.jar tickets4sale.Cli
```

### Starting server from command line

```
java -cp target/scala-2.12/Tickets4Sale-assembly-0.1.jar tickets4sale.api.WebServer
```

### Running from IDE

Another way of running locally is to import project in IntelliJ (or IDE or your choice).
To do this follow the next procedure:
 * import project as SBT project
 * refresh project in SBT tool window to make all sbt dependencies available
 * create run configuration
 * run project with created configuration

### Running with Docker

For running with Docker, we first need to create Dockerfile on project root. Then build image:

 ``` docker build -t <image_name> . ```

 Then run container with:

 ```docker run -p 8080:8080 -t -i <image_name>```

 Server shoould be accessible on port 8080.

# Configuration

There are no specific configurations needed to run command line program or server. If you want to specify other locations or database, check application.conf to see what env variables are needed to be set

# Tests

Run `sbt test` task from SBT console or create test configuration inside favourite IDE and run it. Project is containing both unit tests and API routes tests. Not all functions and scenarios are convered with tests currently.

# Deployment

To deploy the program or server, one should create Dockerfile and Jenkinsefile with appropriate build and run steps (check [Installing and running_section](https://github.com/amerpersonal/PopularityRankings/blob/master/README.md#installing-and-running))


# Built With

* [Akka HTTP](http://doc.akka.io/docs/akka-http/current/scala/http/) - library for creating APIs in Scala using Akka stack
* [Akka Typed](https://doc.akka.io/docs/akka/2.5/typed/index.html) - feamework for building reactive applications in Scala
* [SBT](http://www.scala-sbt.org/) - Dependency Management

# API specification

API contains only 2 routes: get performance inventory and reserve ticket.

## Get performance inventory

```
curl --location --request GET 'http://localhost:8080/api/v1/performance_inventory?query_date=2021-11-25&performance_date=2021-12-05'
```

Response body

```
{
    "inventory": {
        "COMEDY": [
            {
                "show": {
                    "genre": "COMEDY",
                    "opening_day": "2021-11-09",
                    "title": "\"39 STEPS,THE \""
                },
                "status": "Open for sale",
                "ticketsAvailable": 10,
                "ticketsLeft": 60
            },
            {
                "show": {
                    "genre": "COMEDY",
                    "opening_day": "2021-12-02",
                    "title": "\"BEAUX' STRATAGEM,THE\""
                },
                "status": "Open for sale",
                "ticketsAvailable": 0,
                "ticketsLeft": 50
            },
            ...
        ]
    }
}
```

## Reserve a ticket

```
curl --location --request POST 'http://localhost:8080/api/v1/reserve_ticket' \
--header 'Content-Type: application/json' \
--data-raw '{
    "title" : "\"BEAUX'\'' STRATAGEM,THE\"",
    "query_date" : "2021-11-25",
    "performance_date" : "2021-12-05"
}'
```

If show is not valid, or show is not currently running or there are not tickets available to reserve on a specific date for a specific performance, the appropriate error message is shown and 400 bad request response is returned to the client.

Otherwise, request is successfull with a following response body:

```
{
    "performance_date": "2021-12-05",
    "reservation_date": "2021-11-25",
    "tickets_left": 4,
    "title": "\"BEAUX' STRATAGEM,THE\""
}
```

# Implementation and design  status and further work

This project contains only a POC, with a huge room for improvement. If we want to Scale the process so it works in reasonable amound of time for big files and streams, these are some of the options we can use:

- use FS2 library that is meant for working with streams
- use Apache Spark
- experiment with other data structures, such as BinaryTree or similar which may be more suitable for sorting hugh data sets
- use more optimised algorithms for sorting/searching in the process, based on the data types and structures

To write a code more in a functional way, we may use Typelevel stack (cats.io and http4s) instead of Lightbend(Akka).
