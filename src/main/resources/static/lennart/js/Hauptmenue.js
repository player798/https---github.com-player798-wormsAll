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

