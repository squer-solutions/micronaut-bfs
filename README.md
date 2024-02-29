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
By executing the following command you can do this for the current shell:
```shell
eval $(minikube docker-env)
```
It is advisable to create a dedicated namespace for our experiments:
```shell
kubectl create namespace micronaut-bfs
```

## Service Discovery
To make things easy, we will use Kubernetes built-in service discovery mechanisms. But we could as well use systems like Consul or Eureka, integration with Micronaut should be very similar.
There are two simple Micronaut applications available in the repo:
- hello-world: A simple API exposing a single endpoint.
- simple-client: A simple HTTP client calling the hello-world service every 5 seconds.

There are also some kubernetes manifests available. So you just need to build the images for both applications (`./gradlew dockerBuild`)
and apply all the manifests (`kubectl apply -n micronaut-bfs -f manifests`). This will create the resources in your local cluster and spin up some pods.
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
If everything went as planned, you should be able to see log messages in the client pods, logging the response from the API.

## Client-side Load Balancing
If you successfully deployed the applications in the previous step, you should already be able to see the client-side load balancing in action.
You should currently have a single instance of our client running in the cluster and three instances of the API application.
When you check the logs of your API pods, you should be able to see log statements in each one of them, which means that
the requests are handled by a different instance each time.

## Cloud Configuration
When you check the controller in the API application, you will notice that the actual greeting message is injected. This value
actually is defined in a Configmap in the local cluster. For that to work, just a little bit of configuration is necessary.
You can try to update the value in the Configmap and see if the log statements in the client pod change. This works because of
the Refreshable scope of the controller class. Micronaut will update the environment and trigger a refresh of all refreshable beans.
After that you can try to create a Secret in the cluster and inject some additional property in the API application from there.
You should find the relevant documentation [here](https://micronaut-projects.github.io/micronaut-kubernetes/latest/guide/#config-client).

## Distributed Tracing

## Serverless Functions
