FROM ubuntu:16.04
MAINTAINER jake@apache.org

RUN apt-get -y -q update
RUN apt-get install -qqy sudo net-tools wget curl vim man faketime unzip iptables iputils-ping logrotate # sysvinit-core sysvinit sysvinit-utils

RUN apt-get -y -q install software-properties-common
RUN add-apt-repository ppa:openjdk-r/ppa
RUN apt-get -y -q update
RUN apt-get install -qqy openjdk-8-jdk





