openmrs-module-mergeconcepts
============================

**make sure you have installed vagrant, virtualbox, and ansible**

  - [vagrant](http://www.vagrantup.com) >=1.6.5
  - [virtualbox](http://www.virtualbox.org) >=4.3.18
  - [ansible](http://docs.ansible.com/intro_installation.html#installation) >=1.7.2


***How to get up and running the openmrs-standalone (you will need an internet connection for the provision step):***

0. run `$ vagrant up --provision` on the host machine; if you see a report that "VirtualBox VM is already running.", use `$ vagrant provision` on the host machine
0. wait a couple of minutes and point browser to http://localhost:9595/openmrs-standalone/login.htm
0. login as super user administrator with username: admin and password: test
0. to take the OpenMRS standalone server down use control+c from the terminal where ansible is paused in provisioning
0. to ssh into vagrant box use `$ vagrant ssh` on the host machine in a different terminal
0. when you are done with the OpenMRS standalone server, use `$ vagrant halt` on the host machine

***How to setup the merge concepts module in your OpenMRS standalone server:***

0. build MCM file via `[vagrant@localhost mcm]$ mvn clean package` on the remote vagrant box
0. upload omod file (/omod/target/mergeconcepts-1.0-SNAPSHOT.omod) to OpenMRS standalone as module through your host browser
0. you do not need to start the OpenMRS standalone server to consume the newly installed or update module


***How to start development on the merge concepts module:***

0. the entire project folder is synced through virtualbox so that any changes made in the remote vagrant box or in your host machine are immediately reflected in the other
0. this allows you to use which ever IDE that you would like to develop in on you host machine
0. after you have made changes, run the module unit tests via `[vagrant@localhost mcm]$ mvn test` on the remote vagrant box
0. and follow the above instructions to package the new module and add it to your OpenMRS standalone server.


***How to use mysqlworkbench to inspect OpenMRS database:***

  - SSH hostname: 10.0.0.6
  - SSH Username: vagrant
  - SSH Pasword: vagrant
  - MYSQL Hostname: 127.0.0.1
  - MYSQL Server Port: 3316
  - Username: openmrs
  - Password: (ssh into vagrant box and look at openmrs-standalone-runtime.properties in openmrs-standalone-1.9.7/)

 
