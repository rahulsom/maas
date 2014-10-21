FROM rahulsom/docker-java:7u55

MAINTAINER Rahul Somasunderam

RUN yum install -y tar unzip

WORKDIR /opt

RUN wget http://deploy.certifydatasystems.com/cdsng/rahul/LOINC_248_Text.zip && \
    mkdir loinc && \
    cd loinc && \
    unzip ../LOINC_248_Text.zip

RUN wget http://www.fda.gov/downloads/Drugs/DevelopmentApprovalProcess/UCM070838.zip && \
    mkdir ndc && \
    cd ndc && \
    unzip ../UCM070838.zip

RUN source ~/.bashrc && \
    wget --no-check-certificate https://github.com/rahulsom/maas/archive/master.tar.gz && \
    tar xzvf master.tar.gz && \
    cd maas-master && \
    ./grailsw compile

EXPOSE 8080

VOLUME ["/opt/maas-master/data"]

WORKDIR /opt/maas-master

ENTRYPOINT source ~/.bashrc && \
    ./grailsw run-script userScripts/Dataload.groovy && \
    ./grailsw run-app
