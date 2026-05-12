package by.aleksandr.music.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Music Service API",
                version = "v1",
                description = "REST API for managing artists, albums, tracks, genres and users.",
                contact = @Contact(name = "Alexander Briket"),
                license = @License(name = "Internal use")
        ),
        servers = @Server(url = "/", description = "Default server")
)
public class OpenApiConfig {
}
