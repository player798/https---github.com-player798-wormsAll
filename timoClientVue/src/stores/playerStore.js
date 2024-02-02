// stores/playerStore.js
import { defineStore } from 'pinia';
import {getMyPlayer} from "@/services/Api.js";

export const usePlayerStore = defineStore('playerStore', {
    state: () => ({
        playerNumbers: {},
        playerColors: {},
    }),
    actions: {
        async assignPlayerNumbers(gameData) {
            try {
                const myPlayer = await getMyPlayer();
                //console.log('Mein Player Objekt: ', myPlayer);
                const myPlayerId = myPlayer.id;

                // Abrufen der Spieler-IDs aus playerStatisticsList
                const allPlayerId = gameData.playerStatisticsList.map(playerStats => playerStats.playerDTO.id).sort();
                //console.log('Extrahierte IDs:', allPlayerId);

                // Zuweisen der Spieler-Nummer basierend auf der sortierten Position
                allPlayerId.forEach((playerId, index) => {
                    this.playerNumbers[playerId] = index + 1; // 1-basierte Spieleranzahl
                });

                console.log('Spielernummer:', this.playerNumbers);
            } catch (error) {
                console.error('Fehler beim Zuweisen der Spieler-Nummern:', error);
            }
        },
        setPlayerColors(playerStatisticsList, availableColors) {
            this.playerColors = playerStatisticsList.reduce((colors, playerStats, index) => {
                const color = availableColors[index % availableColors.length];
                colors[playerStats.playerDTO.id] = color;
                console.log(`Spieler ${playerStats.playerDTO.id} bekommt die Farbe: ${color}`);
                return colors;
            }, {});
        }
    }
});
