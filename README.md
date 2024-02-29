# Performance Benchmarking

We want to gain some insights on how well Micronaut performs in certain areas compared to other JVM frameworks (e.g. Spring, Quarkus). The promise of Micronaut is fast startup times and a low memory footprint, let's see for ourselves if this promise is kept and how big the difference to other frameworks actually is. We might also be interested in other metrics like image sizes, build times and execution performance. We could also extend these comparisons to include native images as well.

## Scenario Definition
Before actually collecting metrics, we will have to build a (simple) Micronaut application and the equivalent of this application in another framework (e.g. Spring Boot). Let's think about an application scope that is suitable for such a benchmark, it could be a simple API without any other dependencies, but could also be a bit more complex and include database access, cacheing, messaging, etc. Keep in mind that the time here is very limited, so let's pick a scenario that can be implementend in less than 30 minutes.
After this scenario is defined, let's implement the two applications.

A simple Micronaut application is already available that exposes a single endpoint `/api/hello`. You can use this application as is, extend it or start from scratch.

## Micronaut Project Setup
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

After that you should have access to the Micronaut CLI and you can create a new application and open it in your preferred IDE. Make sure to include the necessary features, depending on the chosen scenario by providing the `--features data-jpa,postgres,redis-lettuce,kafka` flag in the following command:
```shell
mn create-app micronaut-bfs-essential-features
```

### Micronaut Launch
Micronaut offers another tool to create applications called [Micronaut Launch](https://micronaut.io/launch/). You can go to this page and create an application based on your needs. Make sure to select all necessary features, depending on the chosen scenario: data-jpa, postgres, redis-lettuce, kafka, etc. Then you can download the project and open it in your preferred IDE.
Some IDEs have an integration with Micronaut Launch and you can create a new application right in the IDE. In IntelliJ, go to File->New->Project and select Micronaut in the list. 

## Running the Application
After you have created a new Micronaut application, you can simply run it from the IDE or using gradle:
```shell
./gradlew run
```

## Implementing the Scenario
There is a docker-compose.yml file available in this repo, that defines the following services, that can be used in your application: postgres, redis, redpanda (kafka).

### Micronaut
Here are some hints on the key features that you might need to implement a simple feature in your Micronaut application.

#### HTTP Endpoints
[Official Docs](https://docs.micronaut.io/latest/guide/#binding).
Keep in mind that you need to explicitely tell Micronaut which classes should be serialized and deserialized by adding the `@Serdeable` annotation on your DTOs.

#### Connecting to a relational database
Add the following to your application.properties file:
```properties
datasources.default.url=jdbc:postgresql://localhost:5432/micronaut-bfs-db
datasources.default.driver-class-name=org.postgresql.Driver
datasources.default.username=postgres
datasources.default.password=password
jpa.default.properties.hibernate.hbm2ddl.auto=update
```
[Official Docs](https://micronaut-projects.github.io/micronaut-data/latest/guide/#hibernateJpaAnnotations).

#### Adding a cacheing layer
Add the following to your application.properties file:
```properties
redis.uri=redis://localhost
redis.caches.plots.expire-after-write=1h
```
[Official Docs](https://micronaut-projects.github.io/micronaut-cache/latest/guide/#annotations).
Keep in mind that the classes used as cache values must implement the `java.io.Serializable` interface.

#### Integrating with Kafka
The `micronaut-bfs-plot-notifications` topic is created automatically with the docker compose setup. Add the following to your application.properties file:
```properties
kafka.bootstrap.servers=localhost:9092
```
[Official Docs](https://micronaut-projects.github.io/micronaut-kafka/latest/guide/#kafkaQuickStart)

### Other Framework
I hope you have chosen a framework that you are familiar with and can easily port the Micronaut application. The two applications should be functionally equivalent.

## Benchmarking
There might not be a single tool to measure all relevant metrics. Here are some suggestions on how a very simple benchmarking could look like. For some metrics you can simply run the applications using the gradle commands, for some others it makes sense to build a docker image and run the containerized application.
For Micronaut there is a simple gradle command to build a docker image or even the native docker image:
```shell
./gradlew clean dockerBuild
./gradlew clean dockerBuildNative
```

### Compilation times
Measuring compilation times should be quite simple, as build tools like Maven and Gradle report execution times of build steps by default.
So you could simply run the following command and check the output to compare the compilation times:
```shell
./gradlew clean compileJava --profile
```

### Startup times
Measuring startup times should be quite simple, as Micronaut, Spring Boot and Quarkus report startup times in the application log by default.
So you could simply run your application and check the output for the log statement:
```shell
./gradlew clean run
./gradlew clean bootRun
./gradlew clean quarkusDev
```

### Requests per second
To measure a metric like requests per second tools like [hey](https://github.com/rakyll/hey) can be useful. Follow the installation instruction (or just use homebrew).
The you can simply run your application and execute a hey command like this:
```shell
hey -n 100000 -c 50 http://localhost:8080/api/hello
```
This will issue 100000 request against `http://localhost:8080/api/hello` with a concurrency of 50 requests and print a report.

### Memory Footprint
The easiest way to measure the memory footprint of a Micronaut application is to build a docker image, run it and inspect metrics there.
To build and run a docker image and inspect stats you can use the following commands:
```shell
./gradlew clean dockerBuild
docker run -p 8080:8080 --name micronaut-mem {{image}}
docker stats micronau-mem
```
You can also use `hey` to put some load on the application. Here we want to issue requests for a specified amount of time, so we can monitor the memory usage. 
```shell
hey -z 30s -c 50 http://localhost:8080/api/hello
```

Keep in mind that for Spring you might want to use the Jib plugin to build your docker image or [create the Dockerfile](https://spring.io/guides/topicals/spring-boot-docker) yourself.
