### Personality Test

This project has both a server and a client projects. Client side gets data from the server which is integrated 
with elastic search and also client sends answers of the questions sent by the server. All client components like 
forms were formed according to dynamic web principles. 

Two pages are defined in the ui which is HOME and ANSWERS. In the home page, user selects a category and 
the questions of the related category show up in the page. When the user selects another category and 
submit by button the new questions come. User answers all questions of this category and sends the answers to the server. 
Server saves the data to elasticsearch.

In the answer page, user types the nickname which he/she gave in the question section and the answers she/he gaves show up in the page.

NOTE 1: nickname and the category together form an id, and the if the given nickname is found in the database, an error is shown 
in the page that indicates that the user must send the answers with different nickname.

NOTE 2: questions of the categories are answered and submitted separatedly.

NOTE 3: the scripts for the elasticsearch can be found in the script folder. 

# the env variables to set in open-api and agents

- npm-version: 6.14.8
- @angular-devkit/architect    0.901.10
- @angular-devkit/core         9.1.10
- @angular-devkit/schematics   9.1.10
- @schematics/angular          9.1.10
- @schematics/update           0.901.10
- rxjs                         6.5.4
- spring-boot-starter-actuator 2.3.5.RELEASE
- spring-boot-starter-web      2.3.5.RELEASE
- gson 2.8.6
- spring-data-elasticsearch 4.0.0.RELEASE
- junit 4.13
- mockito-all  1.10.19
- spring-boot-test 2.3.5.RELEASE
- spring-boot-starter-test 2.3.5.RELEASE
- system-rules 1.19.0

# elastic docker
```
chmod +x index.sh
./index.sh
```
docker run --name personaltestelastic -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:7.7.0
```
