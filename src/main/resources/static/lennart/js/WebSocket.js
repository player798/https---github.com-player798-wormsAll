
var nachrichtenClientStomp;

function verbindungAufbauen() {
    // Web Socket Verbindung zum Spring Boot Server an Endpunkt register_socket
    //Wird verwendet für STOMP-Kommunikation
    const webSocketVerbindung = new WebSocket('ws://localhost:8080/register_socket'); // Native WebSocket-Verbindung

    /* Brücke zwischen höherer STOMP Protokollschicht und Websocket.
    Ermöglicht es WebSocket Verbindungen  mit 
    STOMP(Rahmenwerk für Messaging Interaktion zb Senden und Empfangen, Abonnieren von Themen) 
    Funktionalitäten zu nutzen */

    // nimmt vorhandene Web Socket Verbindung 
    //Erstellt dazu neue STOMP CLient Instanz -> Wird dann genutzt für STOMP Kommunikation 
    //(Ändert Art und Weise wie Nachrichten über diese Verbindung gesendet und empfangen werden)
    nachrichtenClientStomp = Stomp.over(webSocketVerbindung);

   
    // Verbindung mit dem STOMP-Server herstellen
    nachrichtenClientStomp.connect({}, function (verbindungDetailsStomp) {
        console.log("STOMP-Verbindung erfolgreich hergestellt", verbindungDetailsStomp);

        // Benutzer dem Chat hinzufügen
        nachrichtenClientStomp.send("/shrimps/socket.addUser", {}, {});

        nachrichtenClientStomp.subscribe("/chat/messages", function (serverNachrichtInhalt) {
            console.log("Neue Nachricht empfangen:", serverNachrichtInhalt);
            var chatAusgabeJS = document.getElementById('chatAusgabe');
            var nachrichtenInhalt = JSON.parse(serverNachrichtInhalt.body); // Parsing des JSON-Inhalts
            console.log("Mein eingegangenes Objekt:", nachrichtenInhalt);

            var datum = new Date().toLocaleDateString();
            var zeit = new Date().toLocaleTimeString(); 

            var sender = nachrichtenInhalt.sender.username;
            var timestamp = datum + " " + zeit;
            var text = nachrichtenInhalt.text;

            var ausgegebenerTextFormatiert = timestamp + " " + sender + ": " + text + "\n";

            chatAusgabeJS.value += ausgegebenerTextFormatiert;
            //auf die scroll Eigenschaft des chat Ausgabe Textfields zugreifen.
            //Somit wird automatisch bei öffnen der seite auf das Ende des chats gescrollt.
            chatAusgabeJS.scrollTop = chatAusgabeJS.scrollHeight;
            console.log(chatAusgabeJS.value);
        }, function (error) {
            console.error("Fehler beim Abonnieren des Topics:", error);
        });
    });
}

function holeBenutzerobjekt() {
    axios.get('http://localhost:8080/myPlayer')
        .then(myPlayerAntwort => {
            if (myPlayerAntwort.status === 200) {
                spielerObjekt = myPlayerAntwort; // Setze den Benutzernamen

            }
            console.log("hier ist mein player Objekt:", spielerObjekt);
        })
        .catch(error => {
            console.error('Es gab einen Fehler bei der Anfrage:', error);
        });
}

function nachrichtSenden() {
    var eingabeNachricht = document.getElementById('chatEingabe').value;
 
    if (eingabeNachricht.trim() !== '') { // Prüft, ob das Eingabefeld nicht leer ist
        var nachricht = {
            id: null,
            sender: spielerObjekt.data,
            receiver: null,
            gameInvite: null,
            timestamp: Date.now(),
            text: eingabeNachricht,
        };

        console.log(nachricht);
        // Nachricht an den Server senden
        nachrichtenClientStomp.send("/shrimps/socket.sendMessage", {}, JSON.stringify(nachricht));
        console.log(nachricht);
        document.getElementById('chatEingabe').value = '';
    } else {
        console.log("Eingabefeld ist leer, keine Nachricht gesendet");
    }
}

document.addEventListener('DOMContentLoaded', function () {
    holeBenutzerobjekt();
    verbindungAufbauen();
});

function verbindungTrennen() {
    if (nachrichtenClientStomp !== null) {
        nachrichtenClientStomp.disconnect();
    }
    console.log("Verbindung getrennt");
}


