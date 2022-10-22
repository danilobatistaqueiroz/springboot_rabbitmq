package br.com.labs.rabbit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TLSTest {

    void test() {
        /*******************************************************************************************
        ## HTTPS listener settings.
        ## See https://rabbitmq.com/management.html and https://rabbitmq.com/ssl.html for details.
        ##
        management.ssl.port       = 15671
        management.ssl.cacertfile = /path/to/ca_certificate.pem
        management.ssl.certfile   = /path/to/server_certificate.pem
        management.ssl.keyfile    = /path/to/server_key.pem
        
        ## TLS listeners
        ## See https://rabbitmq.com/stomp.html and https://rabbitmq.com/ssl.html for details.
        # stomp.listeners.ssl.default = 61614
        #
        ssl_options.cacertfile = path/to/ca_certificate_bundle.pem //cacert.pem
        ssl_options.certfile   = path/to/server_certificate.pem //cert.pem
        ssl_options.keyfile    = path/to/server_key.pem //key.pem
        ssl_options.verify     =  verify_peer
        ssl_options.fail_if_no_peer_cert = true
        
        ## TLS listeners are configured in the same fashion as TCP listeners,
        ## including the option to control the choice of interface.
        ##
        listeners.ssl.default = 5671
        
        auth_mechanisms.1 = EXTERNAL
        # para usar certificados, é necessário usar mecanismo externo
        
        ## It is possible to disable regular TCP (non-TLS) listeners. Clients
        ## not configured to use TLS and the correct TLS-enabled port won't be able
        ## to connect to this node.
        listeners.tcp = none
        
        stream.listeners.ssl.1 = 5551
        *******************************************************************************************/
    }

}
