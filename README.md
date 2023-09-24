# Installation

## Med docker

Konsultera installationsförfarande för ditt operativsystem

### Bygg dockerimage

docker build -t 1337 .

### Kör dockercontainer

docker run --name 1337 1337

## Utan docker

Installera java 17 jdk
    Varierar beroende på ditt operativsystem
    https://adoptium.net/en-GB/temurin/releases/
Installera Maven 3
    Varierar beroende på ditt operativsystem
    https://maven.apache.org/install.html

bygg projektet:
mvn clean install

kör projektet:
mvn spring-boot:run

exekveringen skrivs ut i standard out och resultatet ligger i katalogen results


Kommentarer på uppgiften och min lösning:

Pga tidsbrist valde jag att fokusera på själva implementationen av
algoritmen. Den bygger på breadth first traversal med rekursion. Varje nivå i trädets länkar hämtas parallellt och alla trådar inväntas innan nästa nivå påbörjas. Detta görs med Javas CompletableFutures. Traverseringen minns vilka länkar som är besökta och lagras i ett trådsäkert Hashset. Eftersom listan är ett globalt state och nås ifrån varje rekursion avviker detta ifrån äkta rekursion, så där har jag "fuskat" lite.


Det som inte fungerar idag är 
* mock av sajtstruktur för enhetstester
* fullständig traversering. Algoritmen lyckas inte nå page2 i paginering. Oklart varför, jag har inte bemödat mig att kika närmre på vad länktraverseringen gör. Det hade jag kunant verifiera med mockad mindre sajt eller med mer tid åt riktiga bok-sajten.


