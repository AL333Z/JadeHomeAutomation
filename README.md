JadeHomeAutomation
==================

Abstract
==================

Questo progetto intende proporre un architettura che risolva il problema dell’automazione domestica (domotica), utilizzando approcci multiagente tramite l'uso del framework [JADE](http://jade.tilab.com/ "JADE").
Il nostro obiettivo è poter interagire con dei dispositivi hardware fisici, come sensori ed attuatori, astraendo il più possibile da ogni aspetto di interfacciamento con l'hardware, considerando ognuno di questi dispositivi come un agente.

Le tecnologie utilizzate saranno:
- piattaforma JADE, per sviluppare software con un approccio multiagente;
- La piattaforma Arduino, per realizzare delle schede con sensori ed attuatori, che simulino dei dispositivi realmente presenti dentro una abitazione.
- applicazione Android (con agenti JADE), per permettere all'utente di controllare con il sistema.

Lo scopo di questa sperimentazione è quello di valutare quanto la piattaforma JADE sia adatta a modellare realtà come quella presa in esame e valutarne i limiti.


Visione
==================
![Alt text](/Images/vision.png "Visione")

Ogni __dispositivo__ fisico (lampadina, pulsante, finestra, climatizzatore...) verrà trattato come un'entità semplice ed autonoma, che conterrà delle proprietà e delle azioni che potranno essere attivate.
Ogni dispositivo potrà essere verosimilmente dislocato fisicamente (e logicamente) in una __stanza__.

Infine ogni stanza potrà fare riferimento ad uno specifico __edificio__.
Un dispositivo potrebbe anche non essere dislocato in nessuna stanza, ma genericamente all’interno di un edificio.

__Ogni dispositivo è visto come un ente autonomo che potrà interagire e comunicare con gli altri dispositivi.__

Dispositivi, stanze ed edifici saranno quindi __agenti__. Il comportamento che ogni agente avrà e l’interazione tra i vari agenti determinerà quindi lo stato del sistema.
Oltre ai precendenti, ci sarà anche un ulteriore tipologia di agenti che potrà ispezionare lo stato del sistema, leggere lo stato dei sensori, attivare le azioni dei servi ed impostare regole di interazione fra i vari dispositivi.
Ogni dispositivo avrà una __conoscenza locale__ dell’ __ambiente__ in cui è immerso,  e potrà modificarne lo stato.

I dispositivi vengono generalmente divisi in due categorie: __sensori__ ed __attuatori__. 
I sensori leggono delle informazioni dall’ambiente fisico, e possono essere per esempio dei pulsanti, ricevitori di telecomandi ad infrarossi, sensori di temperatura o di luminosità.
Gli attuatori invece sono dispositivi che intervengono sull’ambiente fisico, per esempio regolando la luminosità di una lampadina, azionando l’impianto di climatizzazione, accendendo o spegnendo un elettrodomestico, aprendo o chiudendo una finestra.

I vari sensori ed attuatori fisicamente consisteranno in dei componenti elettronici collegati ad un microcontrollore. In un edificio ci saranno molti microcontrollori (sono poco costosi) dislocati nei luoghi in cui devono essere presenti i sensori o attuatori.
I vari microcontrollori possono comunicare tra di loro con vari tipi di interfacce hardware sia via cavo che wireless, tramite le quali formano una [rete di topologia mesh](http://en.wikipedia.org/wiki/Mesh_networking), nella quale ogni nodo è un "router" che permette ad altri dispositivi di collegarsi al resto della rete tramite ad esso. Grazie a questo si può avere resistenza al guasto di un nodo della rete.
Tramite questa rete mesh, i microcontrollori possono comunicare con una o più "basi", dei calcolatori con hardware più potente in grado di eseguire il software basato su Jade da noi realizzato.

Gli agenti Jade che rappresentano i dispositivi fungono quindi da _wrapper_ dei dispositivi fisici, nascondendo al resto degli agenti il modo in cui si comunica con i dispositivi fisici veri e propri ed incapsulando comportamenti precisi.

Il modo in cui comunicano tra di loro i microcontrollori è trasparente al resto del sistema di agenti, quindi può essere definito in fase di implementazione, a basso livello.


Architettura Logica
==================
In questa sezione vengono descritte le entità principali individuate dall'analisi dei requisiti.

Struttura
---------

###EDIFICIO (BUILDING)###
Entità che astrae il concetto di edificio. Un edificio viene visto come un "contenitore" di stanze.

_Principali funzionalità_:
- Restituire la struttura dell'edificio, instesa come insieme delle entità stanza e dispositivi.
- Interagire con le entità stanza e con le entità dispositivo presenti in tutte le stanze.

_Principali proprietà_:
- buildingId
- name
- description
- entityList


###STANZA (ROOM)###
Entità che contiene informazioni sullo stato dei dispositivi all’interno di una stanza.

_Principali funzionalità_:
- Restituire le entità (sensori/attuatori) presenti all’interno della stanza.
- Interagire con tutti i sensori/attuatori interni alla stanza.

_Principali proprietà_:

- roomId
- name
- description
- deviceList
- buildingId


###DISPOSITIVO (DEVICE)###
Entità che permettono di astrarre dalle caratteristiche di basso livello di ogni tipo di dispositivo fisico, e ne incapsulano le proprietà e operazioni.

_Principali funzionalità_:
- Rispondere alle richieste degli altri agenti, che possono essere:
  1. lettura dei dati di un sensore (temperatura, luminosità, ...)
  2. compimento di una azione (accensione/spegnimento di un elettrodomestico, ...)
- Interagire con gli altri dispositivi:
richiedere il compimento di azioni in risposta a determinati eventi (ad es, spegnere le luci dopo una certa ora, ...)

Principali proprietà:

- deviceId
- name
- desciption
- hostId

####FUNZIONALITA’ SPECIFICHE DI SENSORI ED ATTUATORI####
Di seguono vengono elencate le funzionalità di alcuni sensori ed attuatori che il sistema intende offrire alle entità utilizzatrici.

__Lampadina (Bulb)__
- void on();
- void off();
- boolean getStatus();

__Sensore di luminosità (LightSensor)__
- double getLightValue()

__Servo (Motor)__
- void forward(int millisec);
- void backward(int millisec);
- void stop();
- void setSpeed(float speed);
- float getSpeed();
- void rotate(int angle, boolean immediateReturn);
- void rotateTo(int angle, boolean immediateReturn);
- boolean isMoving();

__Suono (Speaker)__
- void beeps();

__Sensore Sonoro (SoundSensor)__
- float getSoundValue();

__Bottone on/off (Switch)__
- void switch();
- boolean getStatus();

__Pulsante (Button)__
- void click();

__Sensore di Temperatura (ThermometerSensor)__
- float getTemp();

Interazione e comportamento
------------------------------------------

Il sistema dovrà essere in grado di configurare le varie entità in manierà automatica. Si pensi ad esempio il modo in cui si associa una entità stanza ad una entità edificio. Questa procedura deve sempre preservare il vincolo di autonomia di ogni entità.
Dovrà quindi essere possibile effettuare la "registrazione" di una entità stanza presso un edificio, e di un entità dispositivo presso una stanza o presso un edificio.
Una volta avvenuta tale "configurazione", le varie entità dovranno poter registrare i propri servizi ed iniziare a servire le richieste delle entità esterne al sistema.

Per la parte di implementazione si farà largo utilizzo degli strumenti  che mette a disposizione Jade (AMS, DF, ACL, ...)

I diagrammi che seguono descrivono:

1. l'interazione tra le entità di alto livello (Building, Room, Device ...), e tra le stesse entità ed il "resto del mondo".
2. l'interazione tra le entità a basso livello (microcontrollori con sensori ed attuatori).

###Interazione tra entità interne al sistema###
Tale interazione riguarderà sostanzialmente la fase di inizializzazione del sistema, ovvero la fase in cui avviene il mapping tra la rappresentazione del sistema fisico con la sua astrazione logica.

Ogni agente Building, dovrà rendere pubblica l'interfaccia dei servizi che offre registrandosi al DF di Jade. Una volta avvenuta la registrazione, l'agente diviene "contattabile" dagli altri agenti del sistema.

![Alt text](/Images/IntBuilding.png "Building Interaction")

Stessa cosa devono fare anche gli agenti Room, oltre che registrare la loro presenza presso un edificio. Tale azione permette di mappare la configurazione del sistema fisico con quello logico, ma non è comunque necessaria ai fini del sistema.

![Alt text](/Images/IntRoom.png "Room Interaction")

Analogamente agli agenti precedenti, anche i singoli sensori/attuatori devono registrare la loro presenza all'interno della piattaforma Jade. Un device può inoltre registrare la sua presenza all'interno di un agente Room.
Ovviamente ogni sensore/attuatore offrirà servizi specifici, che saranno publicizzati al momento della registrazione dell'agente presso la piattaforma JADE.

![Alt text](/Images/IntDevice.png "Device Interaction")

###Interazione tra il sistema e le entità esterne###

Gli agenti esterni che vorranno interagire col sistema non dovranno fare altro che richiederne i servizi attraverso il DF della piattaforma JADE.
Un semplice esempio di interazione è descritto dal diagramma seguente, in cui l'agente "controllore" richiede alla piattaforma la lista degli agenti Building. Per ogni building, viene poi richiesta la lista degli agenti Room associati, e per ogni Room, la lista degli agenti Device.
A questo punto il controller conosce la configurazione del sistema, e può decidere di interagire con i singoli device fisici, richiedendo l'esecuzione dei servizi che loro hanno pubblicizzato attraverso il DF di JADE.

![Alt text](/Images/IntSampleController.png "Sample Controller Interaction")

Tale interazione può poi essere leggermente modificata e adattata per interagire con la piattaforma Android (JADE agents e Android Activity). 

###Interazione a basso livello tra i dispositivi fisici e i loro agenti "wrapper"##

Gli agenti della piattaforma Jade che rappresentano i vari dispositivi (per esempio LightBulb, PushButton, TempSensor, ...) per funzionare veramente devono poter comunicare con i dispositivi hardware veri e propri a cui essi corrispondono.

Dato che è economicamente troppo costoso utilizzare per ogni singolo dispositivo fisico un computer in grado di eseguire una JVM con Jade, abbiamo deciso di separare gli elaboratori in cui viene eseguita la piattaforma Jade da quelli che sono direttamente collegati con i sensori ed attuatori fisici. In questo modo con un solo computer (o più di uno per avere fault tolerance) si è in grado di controllare un numero altissimo di microcontrollori (computer miniaturizzati a bassissimo costo, 1€ circa) ai quali sono collegati i sensori veri e propri.

Per far si che ciò funzioni, è necessario che i computer ed i microcontrollori possano comunicare tra di loro con una sorta di rete. Dato che alcuni dispositivi fisici possono essere dislocati in luoghi in cui non si può arrivare con dei cavi, occorre poter utilizzare anche dei sistemi di comunicazione wireless. Dato però che vanno coperti spazi molto grandi, è opportuno poter utilizzare una rete con topologia mesh, in modo che ogni dispositivo estenda il raggio di copertura della rete.

Le tecnologie di rete comunemente utilizzate nelle reti casalinghe ed aziendali basate sullo stack TCP/IP, non sono adatte per il nostro sistema, in quanto i microcontrollori presenti in ogni dispositivo devono essere a basso costo, e quindi non sono in grado di gestire il protocolli usati per Internet. Essi hanno per esempio delle CPU ad 8 bit ed una memoria RAM nell'ordine dei 4 KiloByte, troppo piccola per poter gestire efficacemente dei pacchetti IP con una MTU di 1500 byte.

Per risolvere questo problema sono state sviluppate delle tecnologie di rete apposite come ZigBee, tuttavia i dispositivi basati su esse sono ancora troppo costosi e non alla nostra portata, e quindi abbiamo deciso di realizzare __MeshNet__uno stack di protocolli adatti per realizzare una rete mesh che permetta ad ogni microcontrollore di scambiare dei messaggi con almeno una __"base"__, cioè un computer dove è in esecuzione la piattaforma ad agenti Jade ed una libreria Java che si occupa di coordinare la rete MeshNet e di comunicare con essa.

Nella rete MeshNet permette quindi ad una o più "basi" di comunicare con tutti i dispositivi, anche quelli non connessi direttamente ad essa, ma tramite un altro dispositivo che funge quindi da "router". Quando viene attivata una base MeshNet, essa invia in broadcast un messaggio di "beacon". Quando un dispositivo riceve un beacon, lo ritrasmette in broadcast a tutti gli altri dispositivi direttamente raggiungibili da esso, e poi invia verso la base un messaggio "beaconResponse" dove specifica qual'è il dispositivo da cui ha ricevuto il beacon. In questo modo la base potrà formare un albero delle connessioni tra i vari dispositivi, e calcolare in base ad esso due numeri interi da assegnare ad ogni dispositivo: l'"address" ed il "maxRoute". Questi numeri sono assegnati tale che gli address dei figli di un certo nodo, siano tutti gli interi compresi tra l'"address" ed il "maxRoute" del nodo (padre). In questo modo ogni nodo non ha bisogno di sapere la topologia di tutta l'intera rete mesh, che sarebbe troppo grande da tenere in una memoria RAM nell'ordine di qualche KiloByte, ma solo una piccola tabella di routing con una riga per ogni suo figlio diretto (non i figli dei figli).
