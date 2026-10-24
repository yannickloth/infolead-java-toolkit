package eu.infolead.jtk.spring.autoconfigure;

import eu.infolead.jtk.jackson3.JtkJacksonModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.json.JsonMapper;

/**
 * Registers the Infolead Jackson 3 module with Spring Boot.
 *
 * <p>Spring Boot 4 collects {@link JsonMapperBuilderCustomizer} beans but not {@code JacksonModule}
 * beans, so the module is contributed through a customizer. The module bean is published as well,
 * so applications and other integrations can inject it without re-creating it.
 *
 * <p>This class is auto-configured only when Jackson 3 and the Infolead module are on the
 * classpath. Nothing here is a requirement for the core library: without Spring Boot, {@code
 * jtk-jackson3} is used directly.
 */
@AutoConfiguration
@ConditionalOnClass({JsonMapper.class, JtkJacksonModule.class})
public class JtkJacksonAutoConfiguration {

    /**
     * Publishes the Infolead module as a bean.
     *
     * @return the module
     */
    @Bean
    @ConditionalOnMissingBean
    public JtkJacksonModule jtkJacksonModule() {
        return new JtkJacksonModule();
    }

    /**
     * Adds the Infolead module to the auto-configured {@link JsonMapper}.
     *
     * <p>The customizer is static so that it can be instantiated without triggering the enclosing
     * configuration class early.
     *
     * @return the customizer
     */
    @Bean
    public static JsonMapperBuilderCustomizer jtkJsonMapperBuilderCustomizer() {
        return builder -> builder.addModule(new JtkJacksonModule());
    }
}
