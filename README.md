openmrs-module-mergeconcepts
============================

How to get up and running:

- make sure you have installed vagrant
- `vagrant up --provision`
- wait a couple of minutes and point browser to `http://localhost:9595/openmrs-standalone/login.htm` 
  - user: `admin`
  - password: `test`
- ssh into vagrant box via `vagrant ssh`
- build MCM file via `mvn clean package`
- upload omod file (`/omod/target/mergeconcepts-1.0-SNAPSHOT.omod`) to OpenMRS standalone as module
- run tests in the vagrant box via `mvn test`
- use mysqlworkbench to inspect OpenMRS database
  - SSH hostname: `10.0.0.6`
  - SSH Username: `vagrant`
  - SSH Pasword: `vagrant`
  - MYSQL Hostname: `127.0.0.1`
  - MYSQL Server Port: `3316`
  - Username: `openmrs`
  - Password: (ssh into vagrant box and look at `openmrs-standalone-runtime.properties` in openmrs-standalone-1.9.7/)

 
