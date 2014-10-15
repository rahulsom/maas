FROM rahulsom/docker-java:7u55

MAINTAINER Rahul Somasunderam

RUN yum install -y tar unzip

RUN cd /opt && \
    wget http://deploy.certifydatasystems.com/cdsng/rahul/LOINC_248_Text.zip && \
    mkdir loinc && \
    cd loinc && \
    unzip ../LOINC_248_Text.zip
    
RUN cd /opt && \
    wget http://www.fda.gov/downloads/Drugs/DevelopmentApprovalProcess/UCM070838.zip && \
    mkdir ndc && \
    cd ndc && \
    unzip ../UCM070838.zip
    
RUN cd /opt && \
    source ~/.bashrc && \
    wget --no-check-certificate https://github.com/rahulsom/maas/archive/master.tar.gz && \
    tar xzvf master.tar.gz && \
    cd maas-master && \
    ./grailsw compile && \
    ./grailsw run-script userScripts/Dataload.groovy

EXPOSE 8080
