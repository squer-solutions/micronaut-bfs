# Explore Essential Features

In this session we will start from scratch and build a Micronaut application that touches some of the common features. This includes project setup, exposing http endpoints, accessing a relational database, introducing caching and integrating with kafka.

The scenario for which we build this application looks as follows:
The EcoGreen Initiative, a non-profit organization dedicated to promoting urban gardening, is launching a new project to manage community gardens accross the city. The project aims to connect local residents with available garden plots. EcoGreen seeks to develop a simple yet effectinve application to manage garden plots.

## Project Setup
To start with, we want to setup the basic structure for a Micronaut application. Micronaut offers two easy ways to do that:

### Micronaut CLI
There a multiple [options to install](https://micronaut-projects.github.io/micronaut-starter/latest/guide/#installation) the Micronaut CLI. I would recommend SDKMAN as this is useful for handling different versions:
If you don't have SDKMAN installed on your machine, go ahead and execute the following command:
```shell
curl -s "https://get.sdkman.io" | bash
```
```shell
sdk update
sdk install micronaut
```

After that you should have access to the Micronaut CLI and you can create a new application and open it in your preferred IDE:
```shell
mn create-app micronaut-bfs-essential-features --features data-jpa,postgres,redis-lettuce
```

### Micronaut Launch
Micronaut offers another tool to create applications called [Micronaut Launch](https://micronaut.io/launch/). You can go to this page and create an application based on your needs. Then you can download the project and open it in your preferred IDE.
Some IDEs have an integration with Micronaut Launch and you can create a new application right in the IDE. In IntelliJ, go to File->New->Project and select Micronaut in the list. 

## Running the Application
After you have created a new Micronaut application, you can simply run it from the IDE or using gradle:
```shell
./gradlew run
```

## Exposing HTTP endpoints
We will start by exposing some HTTP endpoints to manage garden plots. In this first iteration, all information will be stored in memory.
The following endpoints should be available:
- GET `/api/plots`
- GET `/api/plots/{id}`
- POST `/api/plots`

The model should be as simple as possible and consist only of a generated unique ID and some address where the plot is located.

You can find the relevant documentation to building APIs with Micronaut [here](https://docs.micronaut.io/latest/guide/#binding).
Keep in mind that you need to explicitely tell Micronaut which classes should be serialized and deserialized by adding the `@Serdeable` annotation. This is because Micronaut is not using reflection to get access to class metadata, so it needs to collect this information at compile time.

## Connecting to a relational database
Since we don't want to lose our data on each restart, we want to store the data in a relational database. There is a postgres instance defined in the docker-compose.yml file, which the application can use. The necessary dependencies are already available, you just need to add the following to your application.properties file:
```properties
datasources.default.url=jdbc:postgresql://localhost:5432/micronaut-bfs-db
datasources.default.driver-class-name=org.postgresql.Driver
datasources.default.username=postgres
datasources.default.password=password
jpa.default.properties.hibernate.hbm2ddl.auto=update
```
To get started, we need to define the Entity class of our plot type using the correct annotations. Additionally, we want to implement a Repository for data access. 
We will be using Micronaut Data JPA and you can find the relevant documentation [here](https://micronaut-projects.github.io/micronaut-data/latest/guide/#hibernateJpaAnnotations).

## Adding a cacheing layer
To optimize read operations against our database, we want to introduce a cacheing layer with Redis. There is a Redis instance defined in the docker-compose.yml file, which the application can use. The necessary dependencies are already available, you just need to add the following to your application.properties file:
```properties
```
For the cache to properly work, we want to update the cache on every write operation, so the read operation is always up-to-date. You can find all relevant documentation [here](https://micronaut-projects.github.io/micronaut-cache/latest/guide/#annotations).
Keep in mind that Micronaut cache relies on serialization of the objects that should be cached. This can be achieved by either implementing the `java.io.Serializable` interface or implementing some custom serializer.

## Integrating with Kafka
Since we really want to also use Kafka in this simple application, we will expose an additional HTTP endpoint which will trigger a producer to publish a message. We will also implement a simple consumer that will consume from the same topic and log to stdout. Redpanda is included in the docker-compose setup and the topic `micronaut-bfs-plot-notifications` is created automatically. The necessary dependencies are already available, you just need to add the following to your application.properties file:
```properties
https://micronaut-projects.github.io/micronaut-kafka/latest/guide/#kafkaQuickStart
```
You can find all relevant documentation [here](https://micronaut-projects.github.io/micronaut-kafka/latest/guide/#kafkaQuickStart)
