package org.example.precrime.infrastructure.facade.security

import org.apache.coyote.http11.AbstractHttp11Protocol
import org.springframework.boot.tomcat.TomcatConnectorCustomizer
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.stereotype.Component

/**
 * Suppresses the `Server` response header that Tomcat emits by default, which
 * discloses the server implementation and version. Security scanners such as
 * OWASP ZAP flag this as an information-disclosure finding.
 */
@Component
class ServerInfoHeaderCustomizer : WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    override fun customize(factory: TomcatServletWebServerFactory) {
        factory.addConnectorCustomizers(
            TomcatConnectorCustomizer { connector ->
                val protocolHandler = connector.protocolHandler
                if (protocolHandler is AbstractHttp11Protocol<*>) {
                    // A null server string makes Tomcat omit the Server header entirely,
                    // rather than emitting it with an empty value.
                    protocolHandler.setServer(null)
                    protocolHandler.setServerRemoveAppProvidedValues(true)
                }
            },
        )
    }
}
