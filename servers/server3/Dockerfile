FROM rabbitmq:3.11-rc-management
RUN apt update
RUN apt upgrade
RUN apt install curl -y
RUN apt install iptables -y
RUN modprobe iptable-nat
EXPOSE 4369 5552 5672 25672 15672

