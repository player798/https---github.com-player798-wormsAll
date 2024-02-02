// GameService.js
import { stompClient } from './WebSocketService';


const GameService = {
    gameInvite: null,

    sendInviteToPlayer(receiver, gameId, sender) {
        this.fetchData(gameId).then(r => {
            let message = {
                //id          : null,
                player      : sender,
                receiver    : receiver,
                //gameInvite  : game,
                timestamp   : Date.now(),
                text        : null
            };
           stompClient.send(`/shrimps/${gameId}/sendInvite`, {}, JSON.stringify(receiver));

        })},
    async fetchData(gameId) {
        try {
            const response = await fetch(`http://localhost:8080/game/startNewGame/${gameId}`);
            const data = await response.json();
            this.gameInvite = data; // Aktualisieren Sie das gameInvite-Feld
            console.log("Fetched Data: ", this.gameInvite);
            return this.gameInvite;
        } catch (error) {
            console.error("Error fetching gameInvite data:", error);
        }

    },
    async setAllIngame(gameId) {
        try {
            const response = await fetch(`http://localhost:8080/game/${gameId}/allInGame`);
            const data = await  response.text();
            console.log("Alle Spieler auf InGame gesetzt");
        }catch (error) {
            console.error("Error saying InGame", error);
        }
    }

};

export default GameService;
