package simple.client;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;

@Client(id = "hello-world-micronaut-app")
public interface SimpleClient {

    @Get("/api/hello")
    String callHelloWorldService();
}
