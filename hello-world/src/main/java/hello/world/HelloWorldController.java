package hello.world;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.runtime.context.scope.Refreshable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Refreshable
@Controller("/api/hello")
public class HelloWorldController {

    private static final Logger LOG = LoggerFactory.getLogger(HelloWorldController.class);

    @Value("${greeting.message:Hello World}")
    protected String message;

    @Get
    public String getHelloWorld() {
        LOG.info("Handling request and sending {}", this.message);
        return this.message;
    }

}
