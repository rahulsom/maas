MAAS
====

*Med-codes as a Service* is an attempt to turn a variety of medical coding systems
into a microservice that is easy to deploy, and lets developers focus on what is
important to them - build great applications around these codes.

Hacking
----

1. Chekout the code.
2. Run `./grailsw`
3. Inside the grails prompt, run `run-app`
4. Download the data files from the sources linked here.
5. Using the api console, load the data. This takes a long time.
6. Code - Save - Refresh
7. (Optional but a good practice) Send a pull request.

Deployment
----

1. Docker - Run these 2 commands:
```bash
docker pull rahulsom/maas
docker run -d -p 8080 -v /opt/maas:/opt/maas-master/data rahulsom/maas
```
2. Manual Deployment
  1. Download the war
  2. Deploy to a tomcat instance.
  3. Download the code files.
  4. Using the api console, load the data.

Data Files
----

| DataSet | Location | Status | Comments |
|---------|--------|-----|-----|
| LOINC   | [ZIP/CSV](https://loinc.org/downloads/loinc) | Working | Need to sign up for account to download codes |
| NDC   | [ZIP/TSV](http://www.fda.gov/drugs/informationondrugs/ucm142438.htm) | Working | Open Licensed by FDA |
| ICD-9 | [GZIP/TAR/FLATFILE](https://github.com/rahulsom/maas/archive/master.tar.gz) | Working | Licensed by WHO to US |
