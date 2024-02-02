async function getLobbyChat() {
    const response = await fetch('http://localhost:8080/lobby/getAllLobbyChat');
    if (!response.ok) {
        throw new Error('Network response was not ok');
    }
    return response.json();
}

async function getOnlineUsers() {
    console.log("getOnlineUser ausgelöst");
    const response = await fetch('http://localhost:8080/lobby/onlineUsers');
    if (!response.ok) {
        throw new Error('Network response was not ok');
    }
    return response.json();
}

async function getMyPlayer() {
    const response = await fetch('http://localhost:8080/myPlayer');
    if (!response.ok) {
        throw new Error('Network response was not ok');
    }
    return response.json();
}

async function startNewGame() {
    const response = await fetch('http://localhost:8080/game/startNewGame');
    if (!response.ok) {
        throw new Error('Network response was not ok');
    }
    return response.json();
}
// Api.js

async function getGameData(gameId) {
    try {
        const response = await fetch(`http://localhost:8080/game/${gameId}`);
        if (!response.ok) {
            throw new Error('Netzwerkantwort war nicht ok');
        }
        return response.json();
    } catch (error) {
        console.error("Fehler beim Abrufen der Spieldaten:", error);
        throw error; // Fehler weitergeben, damit die aufrufende Komponente darauf reagieren kann
    }
}
async function getPlayerHistory(playerId) {
    try {
        const response = await fetch(`http://localhost:8080/lobby/getPlayerHistory/${playerId}/`);
        return response.json();
        }
        catch (error) {
        console.error("Fehler beim Abrufen der Spieldaten:", error);
        throw error; // Fehler weitergeben, damit die aufrufende Komponente darauf reagieren kann
    }
}

async function logout() {
    const response = await fetch(`http://localhost:8080/logout`);
    //return response.text();
    }

export { getLobbyChat, getOnlineUsers, getMyPlayer, startNewGame, getGameData, getPlayerHistory, logout };