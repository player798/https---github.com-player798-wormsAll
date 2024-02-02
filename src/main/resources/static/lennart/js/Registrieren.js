
// Registrierung
/*
* Registriert einen neuen Spieler in der Datenbank. Zuvor wird
     * das übergebene Passwort verschlüsselt. Dies sollte bei einem
     * Produktivsystem bereits im jeweiligen Client passieren.
     * 
     * @param player    Das Spieler-Objekt, das hinzufügt werden soll.
     * @return  Verweis auf die Seite für erfolgreiche Registrierung
*/

//Eventlistener wartet auf Einreichung des Formulars
document.getElementById('registrierungsFormular').addEventListener('submit', function (e) {
    /* Verhindert Standartverhalten. 
       Benutzername und Passwort werden asynchron an den Server gesendet, ohne Seite neu zu laden. */
    e.preventDefault();

    /* JS Variablen erhalten Werte aus dem Formular */
    var benutzernameJS = document.getElementById('usernameForm').value;
    var emailJS = document.getElementById('emailForm').value;
    var passwortJS = document.getElementById('passwordForm').value;
 
        /* POST Anfrage an Endpunkt process_register
           Also Senden der Daten des Benutzers */
    axios.post(this.action, {
        username: benutzernameJS,
        email: emailJS,
        password: passwortJS
    })
        .then(response => {
            if (response.status === 200) {
                // Erfolgreiche Registrierung
                // Hier können Sie die oben erwähnten Aktionen ausführen, z.B. weiterleiten zur Anmeldeseite
                //window.location.href = 'http://localhost:8080/login';
                alert('Die Registrierung war erfolgreich');
                console.log('Server-Antwort:', response.data); // Fügt einen Log für die Serverantwort hinzu
                console.log('benutzername:', benutzernameJS);
                console.log('Passwort:', passwortJS);
                window.location.href = 'http://localhost:8080/login'; // Ändern Sie die URL bei Bedarf
            } else {
                // Nicht erfolgreich, hier können Sie Fehlermeldungen anzeigen
                console.error('Fehler bei der Registrierung. Statuscode:', response.status);
                // Sie können auch auf weitere Informationen im response.data zugreifen, wenn der Server zusätzliche Details bereitstellt
                console.error('Server-Antwort:', response.data);
            }
        })
        .catch(error => {
            // Fehler bei der Anfrage an den Server
            console.error('Fehler bei der Anfrage an den Server:', error);
            // Hier können Sie Fehlermeldungen anzeigen oder spezifisch auf Netzwerk- oder Anfragefehler reagieren
        })
});
