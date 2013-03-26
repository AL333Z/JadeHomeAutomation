JadeHomeAutomation
==================

Abstract
==================

Questo progetto intende proporre un architettura che risolva il problema dell’automazione domestica, utilizzando approcci multiagente e il framework [JADE](http://jade.tilab.com/ "JADE").
Tale architettura permetterà di definire il comportamento di ogni agente in maniera semplice e precisa, astraendo la parte di interfacciamento con i dispositivi hardware.

Le tecnologie utilizzate saranno:
- piattaforma JADE, per la parte di sviluppo;
- scheda Arduino, per la parte di simulazione di un ambiente domotico;
- applicazione Android (con agenti JADE), per la parte di controllo del sistema.

Lo scopo di questa sperimentazione è quello di valutare quanto la piattaforma JADE sia adatta a modellare realtà come quella presa in esame e valutarne i limiti.


Visione
==================
![Alt text](/Images/vision.png "Visione")

Ogni __dispositivo__ verrà trattato come un entità semplice ed autonoma, che conterrà delle proprietà e delle azioni che potranno essere attivate.
Ogni dispositivo potrà essere verosimilmente dislocato fisicamente (e logicamente) in una __stanza__.

Infine ogni stanza potrà fare riferimento ad uno specifico __edificio__.
Un dispositivo potrebbe anche non essere dislocato in nessuna stanza, ma genericamente all’interno di un edificio.

__Ogni dispositivo è visto come un ente autonomo che potrà interagire e comunicare con gli altri dispositivi.__

Dispositivi, stanze ed edifici saranno quindi __agenti__. Il comportamento che ogni agente avrà e l’interazione tra i vari agenti determinerà quindi lo stato del sistema.
Oltre ai precendenti, ci sarà anche un ulteriore tipologia di agenti che potrà ispezionare lo stato del sistema, leggere lo stato dei sensori, attivare le azioni dei servi ed impostare regole di interazione fra i vari dispositivi.
Ogni dispositivo avrà una __conoscenza locale__ dell’ __ambiente__ in cui è immerso,  e potrà modificarne lo stato.

Tutti i dispositivi vengono divisi principalmente in due categorie: __sensori__ ed __attuatori__. 
I sensori leggono delle informazioni dall’ambiente fisico, e possono essere per esempio dei pulsanti, ricevitori di telecomandi ad infrarossi, sensori di temperatura o di luminosità.
Gli attuatori invece sono dispositivi che intervengono sull’ambiente fisico, per esempio regolando la luminosità di una lampadina, azionando l’impianto di climatizzazione, accendendo o spegnendo un elettrodomestico, aprendo o chiudendo una finestra.

I vari sensori ed attuatori fisicamente consisteranno in dei componenti elettronici collegati a microcontrollori. In un edificio ci saranno molti microcontrollori dislocati nei luoghi in cui devono essere presenti i sensori o attuatori.

Gli agenti che rappresentano i dispositivi fungono quindi da _wrapper_, nascondendo al resto degli agenti il modo in cui si comunica con i dispositivi fisici veri e propri ed incapsulando comportamenti precisi.

Il modo in cui comunicano tra di loro i microcontrollori è trasparente al resto del sistema di agenti, quindi può essere definito in fase di implementazione, a basso livello.


Architettura Logica
==================
In questa sezione vengono descritte le entità principali individuate dall'analisi dei requisiti.

Struttura
---------

###EDIFICIO (BUILDING)###
Entità che astraggono il concetto di edificio. Un edificio viene visto come un "contenitore" di stanze.

_Principali funzionalità_:
- Restituire la struttura dell'edificio, instesa come insieme delle entità stanza e dispositivi.
- Interagire con le entità stanza e con le entità dispositivo presenti in tutte le stanze.

_Principali proprietà_:
- buildingId
- name
- description
- entityList


###STANZA (ROOM)###
Entità che contengono informazioni sullo stato dei dispositivi all’interno di una stanza.

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
Entità che permettono di astrarre dai singoli controllori fisici, e ne incapsulano le proprietà e operazioni.

_Principali funzionalità_:
- Rispondere alle richieste degli altri agenti, che possono essere:
  1. lettura dei dati di un sensore (temperatura, luminosità, ...)
  2. compimento di una azione (apertura/chiusura di porte/finestre, ...)
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

__Suono (Speacker)__
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
Una volta avvenuta tale "configurazione", le varie entità dovranno poter registrare i proprio servizi ed iniziare a servire le richieste delle entità esterne al sistema.



