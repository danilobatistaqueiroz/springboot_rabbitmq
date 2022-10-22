#!/usr/bin/env bash
docker run -it --rm --name rabbitmq4 --hostname rabbitmq4 --ip 192.168.1.34 \
-p 5674:5672 -p 15674:15672 -p 25674:25672 -p 5554:5552 \
-v $PWD/conf/rabbitmq4/rabbitmq.conf:/etc/rabbitmq/rabbitmq.conf \
-v $PWD/history/rabbitmq4/.bash_history:/var/lib/rabbitmq/.bash_history \
-v $PWD/cookie/rabbitmq4/.erlang.cookie:/var/lib/rabbitmq/.erlang.cookie \
-e RABBITMQ_DEFAULT_USER=rabbit_admin \
-e RABBITMQ_DEFAULT_PASS=.123-321. \
-e RABBITMQ_CONFIG_FILES=/etc/rabbitmq/rabbitmq.conf \
-e RABBITMQ_DIST_PORT=25674 \
-e RABBITMQ_NODE_PORT=5674 \
-e RABBITMQ_NODENAME=rabbit@localhost \
rabbitmq:3.11-rc-management
