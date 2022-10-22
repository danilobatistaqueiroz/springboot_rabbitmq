package br.com.labs.rabbit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LogTest {

    void test() {
        /***************************************************************************************************
        ## Logging settings.
        ##
        ## See https://rabbitmq.com/logging.html and https://github.com/erlang-lager/lager for details.
        ##

        ## Log directory, taken from the RABBITMQ_LOG_BASE env variable by default.
        ##
        # log.dir = /var/log/rabbitmq

        ## Logging to file. Can be false or a filename.
        ## Default:
        # log.file = rabbit.log

        ## To disable logging to a file
        # log.file = false

        ## Log level for file logging
        ##
        # log.file.level = info

        ## File rotation config. No rotation by default.
        ## DO NOT SET rotation date to ''. Leave the value unset if "" is the desired value
        # log.file.rotation.date = $D0
        # log.file.rotation.size = 0

        ## Logging to console (can be true or false)
        ##
        # log.console = false

        ## Log level for console logging
        ##
        # log.console.level = info

        ## Logging to the amq.rabbitmq.log exchange (can be true or false)
        ##
        # log.exchange = false

        ## Log level to use when logging to the amq.rabbitmq.log exchange
        ##
        # log.exchange.level = info
        ***************************************************************************************************/
    }

}
