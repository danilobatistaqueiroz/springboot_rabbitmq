package br.com.labs.rabbit.acl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ACLConfigurationTest {

    void authentication_authorisation() {
        /********************************************************************************************
        ## Select an authentication/authorisation backend to use.
        ##
        ## Alternative backends are provided by plugins, such as rabbitmq-auth-backend-ldap.
        ##
        ## NB: These settings require certain plugins to be enabled.
        ##
        ## Related doc guides:
        ##
        ##  * https://rabbitmq.com/plugins.html
        ##  * https://rabbitmq.com/access-control.html
        ##

        # auth_backends.1   = rabbit_auth_backend_internal

        ## uses separate backends for authentication and authorisation,
        ## see below.
        # auth_backends.1.authn = rabbit_auth_backend_ldap
        # auth_backends.1.authz = rabbit_auth_backend_internal

        ## The rabbitmq_auth_backend_ldap plugin allows the broker to
        ## perform authentication and authorisation by deferring to an
        ## external LDAP server.
        ##
        ## Relevant doc guides:
        ##
        ## * https://rabbitmq.com/ldap.html
        ## * https://rabbitmq.com/access-control.html
        ##
        ## uses LDAP for both authentication and authorisation
        # auth_backends.1 = rabbit_auth_backend_ldap

        ## uses HTTP service for both authentication and
        ## authorisation
        # auth_backends.1 = rabbit_auth_backend_http

        ## uses two backends in a chain: HTTP first, then internal
        # auth_backends.1   = rabbit_auth_backend_http
        # auth_backends.2   = rabbit_auth_backend_internal

        ## Authentication
        ## The built-in mechanisms are 'PLAIN',
        ## 'AMQPLAIN', and 'EXTERNAL' Additional mechanisms can be added via
        ## plugins.
        ##
        ## Related doc guide: https://rabbitmq.com/authentication.html.
        ##
        # auth_mechanisms.1 = PLAIN
        # auth_mechanisms.2 = AMQPLAIN

        ## The rabbitmq-auth-mechanism-ssl plugin makes it possible to
        ## authenticate a user based on the client's x509 (TLS) certificate.
        ## Related doc guide: https://rabbitmq.com/authentication.html.
        ##
        ## To use auth-mechanism-ssl, the EXTERNAL mechanism should
        ## be enabled:
        ##
        # auth_mechanisms.1 = PLAIN
        # auth_mechanisms.2 = AMQPLAIN
        # auth_mechanisms.3 = EXTERNAL

        ## To force x509 certificate-based authentication on all clients,
        ## exclude all other mechanisms (note: this will disable password-based
        ## authentication even for the management UI!):
        ##
        # auth_mechanisms.1 = EXTERNAL
        # para usar certificados, é necessário usar mecanismo externo
        ********************************************************************************************/
    }
    
    void default_user() {
        /********************************************************************************************
        ##
        ## Default User / VHost
        ## ====================
        ##

        ## On first start RabbitMQ will create a vhost and a user. These
        ## config items control what gets created.
        ## Relevant doc guide: https://rabbitmq.com/access-control.html
        ##
        # default_vhost = /
        # default_user = guest
        # default_pass = guest

        # default_permissions.configure = .*
        # default_permissions.read = .*
        # default_permissions.write = .*

        ## Tags for default user
        ##
        ## For more details about tags, see the documentation for the
        ## Management Plugin at https://rabbitmq.com/management.html.
        ##
        # default_user_tags.administrator = true

        ## Define other tags like this:
        # default_user_tags.management = true
        # default_user_tags.custom_tag = true
        ********************************************************************************************/
    }

}
