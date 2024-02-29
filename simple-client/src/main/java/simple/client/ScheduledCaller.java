package simple.client;

import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ScheduledCaller {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduledCaller.class);

    private final SimpleClient client;

    public ScheduledCaller(SimpleClient client) {
        this.client = client;
    }

    @Scheduled(fixedDelay = "5s")
    void callHelloWorldService() {
        var response = client.callHelloWorldService();
        LOG.info("Got a response: {}", response);
    }

}
