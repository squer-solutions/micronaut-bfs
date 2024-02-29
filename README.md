# To the Cloud

In this session we will test the natively cloud-native promise of Micronaut and experiment with features like service discovery,
client-side load balancing and serverless functions.

## Setup
To make it easy to experiment with these features locally, we will use a local Minikube cluster. If you have not yet installed this on your machine,
please follow the instruction [here](https://minikube.sigs.k8s.io/docs/start/).
We will also need to have [kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl) installed to interact with our local cluster.
After installation, just start Minikube using the following command:
```shell
minikube start
```
If you want, you can use the minikube dashboard to see what is going on within your local cluster:
```shell
minikube dashboard
```
You can also point your local Docker CLI to the Minikube docker daemon, so docker builds images directly inside Minikube and you don't have to push images to a remote registry.
You can get instructions on how to do that by executing the following command:
```shell
minikube docker-env
```

## Service Discovery
To make things easy, we will use Kubernetes built-in service discovery mechanisms. But we could as well use tools like Eureka, integration with Micronaut should be very similar.
There are two simple Micronaut applications:
- hello-world: A simple API exposing a single endpoint.
- simple-client: A simple HTTP client calling the hello-world service every 5 seconds.

There are also deployment and service manifests for each application available. So you just need to build the images for both applications (`./gradlew dockerBuild`)
and apply all the manifests (`kubectl apply -f manifests`). This will create the resources in your local cluster and spin up two instances each.
The client application can now simply discover the API service and call it using the service name:
```yaml
apiVersion: v1
kind: Service
metadata:
  name: hello-world-micronaut-app
```
```java
@Client("http://hello-world-micronaut-app")
public interface SimpleClient {

    @Get("/api/hello")
    String callHelloWorldService();
}
```
