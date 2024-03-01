# To the Cloud

In this session we will test the natively cloud-native promise of Micronaut and experiment with features like service discovery,
client-side load balancing and serverless functions. Sadly I did not spend enough time here to prepare an actual cloud environment in AWS for example.
So we will take a step back and experiment with those features in a local Minikube cluster.

## Setup
If you have not yet installed this on your machine, please follow the instruction [here](https://minikube.sigs.k8s.io/docs/start/).
We will also need to have [kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl) installed to interact with our local cluster.
After installation, just start Minikube using the following command:
```shell
minikube start
```
If you want, you can use the minikube dashboard to see what is going on within your local cluster:
```shell
minikube dashboard
```
You should also point your local Docker CLI to the Minikube docker daemon, so docker builds images directly inside Minikube and you don't have to push images to a remote registry or manually add them.
By executing the following command you can do this for the current terminal:
```shell
eval $(minikube docker-env)
```
Let's also create a dedicated namespace for our experiments:
```shell
kubectl create namespace micronaut-bfs
```

There are two simple Micronaut applications available on this branch:
- hello-world: A simple API exposing a single endpoint.
- simple-client: A simple HTTP client calling the hello-world service every 5 seconds.

There are also some kubernetes manifests available in a [dedicated folder](manifests). 
To get started, just build the images for both applications (`./gradlew dockerBuild`, this should publish the images directly into minikube) 
and apply all the manifests in the correct namespace (`kubectl apply -n micronaut-bfs -f manifests`). This will create the resources in your local cluster and spin up some pods.

## Service Discovery
To make things easier, the client application already uses Kubernetes built-in service discovery mechanisms. 
The application already defines one additional dependency and is configured in a way, that it will scan all kubernetes Service resources, so it can communicate with them. 
With this setup, the client application can simply reference the API it wants to call by the Service name.
```yaml
apiVersion: v1
kind: Service
metadata:
  name: hello-world-micronaut-app
```
```java
@Client(id = "hello-world-micronaut-app")
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
the requests are handled by a different instance each time. You can read a bit more in the [official docs](https://docs.micronaut.io/latest/guide/#clientSideLoadBalancing).

## Cloud Configuration
When you check the controller in the API application, you will notice that the actual greeting message is injected. This value
actually is defined in a Configmap in the local cluster. For that to work, just a little bit of configuration is necessary.
You can try to update the value in the Configmap and see if the log statements in the client pod change. This works because of
the Refreshable scope of the controller class. Micronaut will update the environment and trigger a refresh of all refreshable beans.
After that you can try to create a Secret in the cluster and inject some additional property in the API application from there.
You should find the relevant documentation [here](https://micronaut-projects.github.io/micronaut-kubernetes/latest/guide/#config-client).

## Distributed Tracing
To experiment with the distributed tracing support of Micronaut, we will deploy [Zipkin](https://zipkin.io/) to our local cluster:
```shell
kubectl apply -f zipkin/zipkin.yml -n micronaut-bfs
```
Then we can do some port-forwarding to access the zipkin UI on localhost:9411 in our local browser:
```shell
kubectl port-forward svc/zipkin 9411:9411 -n micronaut-bfs
```

Now let's integrate our API application with Zipkin to send some traces. You can find the documentation [here](https://micronaut-projects.github.io/micronaut-tracing/latest/guide/#zipkin).
You will need to add a dependency and some configuration to the application. By default, all HTTP requests should already be sent to Zipkin and visible on the UI.
But you can still experiment with some custom span definitions using Micronauts [tracing annotations](https://micronaut-projects.github.io/micronaut-tracing/latest/guide/#introduction).

## Serverless Functions
If you still have time, you can learn a bit more about how Micronaut can be used for serverless functions in the [official docs](https://docs.micronaut.io/latest/guide/#serverlessFunctions) 
and especially with [AWS Lambda](https://micronaut-projects.github.io/micronaut-aws/latest/guide/#lambda). You can also check out this [guide](https://guides.micronaut.io/latest/micronaut-aws-lambda-eventbridge-event-gradle-java.html)
to create a simple Lambda using Micronaut, you might be able to use one of the AWS sandbox environments, if you have someone that is familiar with those.
