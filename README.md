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

There are also some kubernetes manifests available in the dedicated folder. 
To get started, just build the images for both applications (`./gradlew dockerBuild`) and apply all the manifests in the correct namespace (`kubectl apply -n micronaut-bfs -f manifests`). 
This will create the resources in your local cluster and spin up some pods.

## Service Discovery
To make things easier, the client application already uses Kubernetes built-in service discovery mechanisms. The application already defines one additional dependency and is configured in a way,
that it will scan all kubernetes Service resources, so it can communicate with them. 
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

**Before going ahead and building on top of that, it might make sense to also check the rest of the README.**

### Consul
Let's go a step further and introduce Consul in our cluster, where our applications can register and discover services.
We will start with installing Consul using Helm in the cluster. If not already available, [install helm](https://helm.sh/docs/intro/install/) on your machine.
Then we can use the Consul helm chart to install all necessary resources in our cluster. There is a custom [custom values file](consul/values.yml) available that should be used.
```shell
helm repo add hashicorp https://helm.releases.hashicorp.com
helm install --values consul/values.yml consul hashicorp/consul --namespace micronaut-bfs
```

Now you can start integrating the application with Consul. There is some documentation available [here](https://micronaut-projects.github.io/micronaut-discovery-client/latest/guide/#serviceDiscoveryConsul).
You will need to update some dependencies in both applications and add the necessary configuration. Then you should be able to re-build the images,
restart your deployments and verify if the service is properly registered.

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
