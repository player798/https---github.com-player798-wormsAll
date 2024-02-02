




/* <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< ALLES ZUM THEMA LOBBY >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> */
function OnlineBenutzerLobby() {
    axios.get('http://localhost:8080/lobby/onlineUsers')
        .then(response => {
            var onlineBenutzerListe = response.data;
            var spielerausgabeElement = document.getElementById('onlineSpielerAusgabe');
            var benutzernamenHTML = "Online:<br>";

            onlineBenutzerListe.forEach(function (benutzer) {
                benutzernamenHTML += benutzer.username + '<br>';
            });

            spielerausgabeElement.innerHTML = benutzernamenHTML;

            console.log('Erfolgreich online Benutzer abgerufen');
            console.log(onlineBenutzerListe);
        })
        .catch(error => {
            console.error('Fehler beim Abrufen von onlineUsers:', error);
        });
}

function holeLetzteZwanzigChatNachrichten() {
    axios.get('/lobby/top20LobbyChat')
        .then(response => {
            var letzteZwanzigNachrichten = response.data;
            var ausgabefeldTop20 = document.getElementById('chatAusgabe');

            //gibt die neuste Nachricht unten im Chat aus
            letzteZwanzigNachrichten.reverse();

            letzteZwanzigNachrichten.forEach(nachricht => {
                var neuerTimeStamp = new Date(nachricht.timestamp);
                var formatierterTimestamp = neuerTimeStamp.toLocaleDateString() + ' ' + neuerTimeStamp.toLocaleTimeString();
                var textJS = nachricht.text;
                var senderJS = nachricht.sender.username;

                ausgabefeldTop20.innerHTML += formatierterTimestamp + ' ' + senderJS + ': ' + textJS + '\n';
            });
        })
        .catch(error => {
            console.error('Fehler beim Abrufen der Chat-Nachrichten:', error);
        });
}

function logout() {
    axios.post('http://localhost:8080/logout')
        .then(function (response) {
            // Logout erfolgreich
            console.log('Erfolgreich ausgeloggt:', response);
            alert('Sie wurden erfolgreich ausgeloggt.'); // Alert hinzugefügt
        })
        .catch(function (error) {
            // Fehler beim Logout
            console.error('Fehler beim Logout:', error);
        });
}

document.getElementById('accountDatenAusgebenId').addEventListener('click', function (e) {
    e.stopPropagation(); // Verhindert, dass das Klick-Event weitergeleitet wird
    axios.get('http://localhost:8080/myPlayer')
        .then(response => {
            if (response.status === 200) {
                var idJS = response.data.id;
                var emailJS = response.data.email;
                var benutzernameSpielerJS = response.data.username;

                //Das Passwort in der Ausgabe halbieren
                var passwortJS = response.data.password; // Zugriff auf das Passwort-Feld in der Antwort
                var halbeLaenge = Math.floor(passwortJS.length / 2);
                var ersteHaelfte = passwortJS.substring(0, halbeLaenge);
                var zweiteHaelfte = passwortJS.substring(halbeLaenge);
                var passwortAusgabe = ersteHaelfte + '\n' + zweiteHaelfte;

                var statusJS = response.data.status;

                var gesamtausgabe =
                    " ID: " + idJS + '\n'
                    + "Email:" + emailJS + '\n'
                    + "Benutzername:" + benutzernameSpielerJS + '\n'
                    + "Passwort:" + passwortAusgabe + '\n'
                    + "Spieler Status:" + statusJS;

                /* Dem Ausgabe span in HTML die gesamtausgabe zuweisen. */
                document.getElementById('accountDatenInhalt').innerText = gesamtausgabe;

                /* Den DatencontainerJS erstellen und diesen dem HTML Container zuweisen.
                Dem DatencontainerJS das style.display geben um diesen immer wieder ein und ausklappen zu können */
                var accountDatenContainerJS = document.getElementById('accountDatenContainer');
                accountDatenContainerJS.style.display = accountDatenContainerJS.style.display === 'none' ? 'block' : 'none';
            }
        })
        .catch(error => {
            console.error('Es gab einen Fehler bei der Anfrage:', error);
        });
});

// Stellen Sie sicher, dass das DOM vollständig geladen ist
document.addEventListener('DOMContentLoaded', function () {
    OnlineBenutzerLobby();
    holeLetzteZwanzigChatNachrichten()
    setInterval(OnlineBenutzerLobby, 10000);

    // Setzen Sie ein Intervall für die wiederholte Ausführung
    //setInterval(OnlineBenutzerLobby, 5000); // 5000 Millisekunden = 5 Sekunden
    console.log('erfolgreich abgerufen:', OnlineBenutzerLobby);
});

