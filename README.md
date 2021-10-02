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

This task will also execute all tests.

### Migrating database

Ticket reservations are stored in Postgres database. Therefore, to start the app locally, Postgres server needs to be installed and running on localhost. Create database manually using psql, via following command

```
create database tickets4sale;
```

Then we need to start migrations. Flyway schema migration is used. To execute database migrations, simply run DatabaseMigrationsRunner file. All migrations are located in resources/db/migrations directory. One can also execute migrations by running commands from SQL file using psql:

```
\i <migration file>.sql
```

When all migrations are executed, we're ready to run the server.

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

API contains 2 routes: get performance inventory and reserve ticket.

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

# Implementation and design concerns, status and further work

This project contains only a POC, with a huge room for improvement. Below is a detailed description of design and implementation concerns.

## Extensibility via config

Parameters like show duration, number of days running in big and small hall, when the ticker sale starts etc are configurable through main application.conf file. Almost each property can also be defined in an environment variable. That way, we can achieve change in configuration without pushing code and building artefacts. Simply changing environment variables (stored on server runtime environment) and restart service would be enough. This way, we could satisfy principles from the [Twelve factor app](https://12factor.net/)

## Storing tickets reservations

Postgres is used for storing ticket reservations. The system is made using a simplified, manually implemented dependency injection. [TicketOrderRepository](https://github.com/amerpersonal/Tickets4Sale/blob/master/src/main/scala/tickets4sale/repository/TicketOrderRepository.scala) is the interface for working with ticket reservations.

It has 3 implementations:

- [TicketOrderEmptyRepository](https://github.com/amerpersonal/Tickets4Sale/blob/master/src/main/scala/tickets4sale/repository/TicketOrderEmptyRepository.scala) - used for CLI, reservations doesn't do anything here and it always returns 0 for number of reserved tickets for a performance
- [TicketOrderMapRepository](https://github.com/amerpersonal/Tickets4Sale/blob/master/src/main/scala/tickets4sale/repository/TicketOrderMapRepository.scala) - used for tests. Reservation data is stored in memory, in a form on map, which is updated on each reservation.
- [TicketOrderDatabaseRepository](https://github.com/amerpersonal/Tickets4Sale/blob/master/src/main/scala/tickets4sale/repository/TicketOrderDatabaseRepository.scala) - maintains data about ticket reservations in Postgres database.

TicketOrderEmptyRepository is then set as dependency for creating [TicketOrderService](https://github.com/amerpersonal/Tickets4Sale/blob/master/src/main/scala/tickets4sale/services/TicketOrderServiceFactory.scala). We must inject repository to create a service factory, which will create a service for us. This pattern can be used in Scala. Of course, except of this, we could use Guava or other library for dependency injection. I just wanted to show a simple, but yet powerful option.

Example of how repository is injected in service:

- [Server](https://github.com/amerpersonal/Tickets4Sale/blob/master/src/main/scala/tickets4sale/api/WebServer.scala#L18)
- [Tests](https://github.com/amerpersonal/Tickets4Sale/blob/master/src/test/scala/api/ApiSpec.scala#L37)

CLI uses empty repository. Changing just one line of code would make it work with database. We could also store ticket reservation data in a CSV file. There are various options. For example, we could add it to existing CSV file. In that case we would need to have a special mark the end of reservation records for one show. This way of storing one part of data in CSV (information about performances) and the other part in database (ticket reservations) is not the best practice, but having an implementation like this, it would be easy to store them in CSV.

## Working with database

### Privileges

In a real-world project, we would use a different roles and users with different privilleges for working on database. In this simple POC, I'm doing all the operations with default postgres user.

### Performances

Slick library is used as a database client. It's DSL has a known issue of creating heavily unoptimised queries, especially when using joins. In general, ORMs often have performance issues when mapping DSL query to SQL query. That's why I use plain SQL code (calling a database function) for some queries in [DatabaseOps](https://github.com/amerpersonal/Tickets4Sale/blob/master/src/main/scala/tickets4sale/database/dsl/DatabaseOps.scala)

### Connections

For working with database, only one connection is created. Again, in a real-world project, connection pool should be created, using Hikari or other pooling library. In that case we need to take care or pool configuration, to prevent opening to much connections or killing connection after short period of inactivity, which leads to opening new ones constantly.

## Reactive architecture

Akka stack is used for building the app.

[Akka-Http](https://doc.akka.io/docs/akka-http/current/index.html) is used for building API and [Akka Typed] (https://doc.akka.io/docs/akka/2.5/typed/index.html) as a middleware. I decided for Akka typed (instead of Akka classic), because it can prevent a lot of hidden bugs caused by not checking the types of messages passed to Actors in a compile time. Each HTTP request is served in non-blocking manner. Architecture should be scalable both horizontally and vertically.

### Execution context

In this POC, I use a global execution context. In a real-world project, we may carefullt choose between thread poll and fork join executor, based on the requirements.

## Calculating inventory

For calculating inventory, a [bulk version of getting reservations data is used](/blob/master/src/main/scala/tickets4sale/services/TicketOrderServiceFactory.scala#L38). This way, we do not execute expensive query on database for each show. This solution also has it's own constraints and limits. If there are millions of records in database, this query would be a quite expensive in terms of execution time. We may pass additional parameter - array of show titles for which to return reserved tickets counts. Since not all shows are running at all on a certain performance date.

Regarding implementation of these function, we could also use a recursion to build a final inventory. That way, we would avoid using groupBy which is known as not so well performant. But given a restricted amount of time I had to do this task, I did this in the most straightforward way. Using recursion would be a nice add on.

## Serialisation

[spray-json](https://github.com/spray/spray-json) is used for serialisation and deserialisation od data. There are certainly better options, like [Circe.io](https://circe.github.io/circe/), which has a much neater syntax, but as I am more familiar with Spray, I decided to use it.

## Pricing

There is a requirements section which talks about different prices for tickets, based on show genre. Prices are not used or calculated anywhere inside a solution, since no user story is explicitly asking for a pricing mechanism. Of course, it could be a nice add on. We would add price property to each ticket order. That would enable us to run some nice calculations of Ticket4Sale profit and  to create a financial reports.

## Tests

I added a few unit and API tests. In my opinion, tests are of utmost importance. They will certainly help to find issues at initial implementation. The are especially usefull to check if new changes break any existing functionalities.

## Logging

Logging is not implemented as a part of this POC. Of course, for production system, we need to have a clear logs. I usually do it using Logback.

## Scalability and further options

If we want to Scale the process so it has better performance for big files and streams, these are some of the options we can use:

- use FS2 library that is meant for working with streams
- use Apache Spark
- experiment with other data structures, such as BinaryTree or similar which may be more suitable for sorting hugh data sets
- use more optimised algorithms for sorting/searching in the process, based on the data types and structures

To write a code more in a functional way, we may use Typelevel stack (cats.io and http4s) or ZIO instead of Lightbend(Akka). In [CLI](https://github.com/amerpersonal/Tickets4Sale/blob/master/src/main/scala/tickets4sale/Cli.scala) I made a few tweaks on writing/reading parameters from Stdin/Stdout. Most IO operations are lifted into Future and then for comprehension is used. This is inspired by cats.io and it's IO abstraction. For example, cats has IO.printline and IO.pure abstractions which would enable us to write a similar code for CLI client out of the box.

However, Future has some known limitations - it cannot be cancelled, it is executed as soon as it is created etc. I simply use it because I'm the most familiar with it. We started introducing cats.io for new services on the project.




