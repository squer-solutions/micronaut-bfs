package hello.world;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/api/hello")
public class HelloWorldController {

    @Get
    public String getHelloWorld() {
        return "Hello World";
    }

}
