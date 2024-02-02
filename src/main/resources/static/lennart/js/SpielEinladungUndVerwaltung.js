

document.getElementById('onlineSpielerAusgabe').addEventListener('click', function(event) {
    // Überprüfe, ob das geklickte Element eine Chatbenutzerklasse hat
    if (event.target.classList.contains('einzelnerBenutzerDiv')) {
        // Extrahiere die Benutzer-ID aus dem geklickten Element
        var empfaengerID = event.target.dataset.id; // Annahme: Die Benutzer-ID ist als 'userId' in den Datensätzen des Elements gespeichert
        
        // Einladung senden
        sendeSpieleEinladung(empfaengerID);
    }
});

function sendeSpieleEinladung(empfaengerID) {
    // Annahme: Du hast bereits Zugriff auf die erforderlichen Daten wie Spielerobjekt, Spielobjekt und Websocket-Client (nachrichtenClientStomp)

    let spielePartyID = game.id; // Annahme: Du hast bereits die Spiel-ID verfügbar

    // Annahme: Du musst den Spieler mit der Spieler-ID aus deiner Spielerliste finden
    let empfaenger = findPlayer(empfaengerID);

    if (empfaenger != null) {
        // Annahme: Du hast bereits Zugriff auf das aktuelle Spielerobjekt des sendenden Spielers (player)

        let spielEinladung = {
            id: null,
            sender: spielerObjekt.data, // Annahme: Der sendende Spieler
            receiver: empfaenger,
            gameInvite: game, // Annahme: Das Spielobjekt, zu dem du einlädst
            timestamp: Date.now(),
            text: null // Du könntest hier eine optionale Nachricht hinzufügen, falls gewünscht
        };

        // Annahme: Du sendest die Einladung über deinen WebSocket-Client (nachrichtenClientStomp)
        // Du könntest hier die Logik ergänzen, um die Nachricht an den Server zu senden
        // Hier sendest du die Nachricht über einen WebSocket-Kanal, der zum Senden von Einladungen dient.
        // Die genaue Implementierung hängt von deiner Backend-Logik und deinem WebSocket-Setup ab.
        nachrichtenClientStomp.send("/shrimps/" + gameId + "/sendInvite", {}, JSON.stringify(spielEinladung));
    }
}
