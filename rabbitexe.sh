#!/usr/bin/env bash
docker run -it --rm --name rabbitmq3 --hostname rabbitmq3 -p 5673:5672 -p 15673:15672 -p 25673:25672 -p 5553:5552 rabbitmq:3.11-rc-management
