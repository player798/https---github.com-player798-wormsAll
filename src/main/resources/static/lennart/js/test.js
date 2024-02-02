



axios.get('http://localhost:8080/authenticate/test')
    .then(function (response) {
        // Verarbeiten Sie hier die Antwort
        console.log('Antwort vom Server:', response.data);
    })
    .catch(function (error) {
        // Fehlerbehandlung
        console.error('Fehler bei der Anfrage:', error);
    });