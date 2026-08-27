package dev.ashish.outbox;

import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// AutoConfigurationPackage adds this package to the ones boot already scans for
// entities and repositories, so importing this is enough. spelling it out with
// EntityScan instead would replace the service's own package rather than add to it.
// that one does not cover plain beans though, hence the component scan as well
@Configuration
@AutoConfigurationPackage
@ComponentScan
@EnableScheduling
public class OutboxConfig {

}
