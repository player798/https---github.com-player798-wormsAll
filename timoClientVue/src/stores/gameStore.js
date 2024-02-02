// stores/gameStore.js
import { defineStore } from 'pinia';

export const useGameStore = defineStore('gameStore', {
    state: () => ({
        gameData: null
    }),
    actions: {
        updateGameData(data) {
            console.log("Updating game data in store");
            this.gameData = data;
        }
    },
    mutations: {
        updateGameData(newGameData) {
            this.gameData = newGameData;
        }
    }
});
