<template>
  <div>
    <div id="gameContainer">
      <div>
        <Board :gameStore="gameStore"
               :gameStoreData="latestGameObject.value"
               :level-data="levelData"
               :player-colors="playerColors"
               @update-level-data="handleUpdateLevelData"
               @updateGamestoreData="updateBoardData"
               :current-shrimp-prop="currentShrimp"
               :lastWeaponFiredProp="lastWeaponFired" @updateLastWeaponFired="updateLastWeaponFired"

        />
      </div>
    </div>
  </div>
  <div>
    <p> PlayGameVue: currentShrimp : {{currentShrimp}}</p>
    <p> PlayGameVue: Gamestore : {{this.gameStoreData}}</p>
  </div>
  <div class="modal-overlay" v-if="showWinnerModal">
    <div class="modal">
      <p>{{ winnerMessage }}</p>
      <button @click="backToLobby">Zurück zur Lobby</button>
    </div>
  </div>
</template>

<script>
import {useGameStore} from "@/stores/gameStore.js";
import {
  latestGameObject,
  latestMessage,
  latestWeaponFired,
  refreshGame,
  registerGameWebSocket
} from "@/services/WebSocketService.js";
import {watch} from "vue";
import Board from "@/components/Board.vue";
import Shrimp from "@/components/Shrimp.vue";
import {getMyPlayer} from "@/services/Api.js";
import GameService from "@/services/GameService.js";
import gameService from "@/services/GameService.js";


export default {
  computed: {
    latestGameObject() {
      return latestGameObject
    },
      },

  data() {
    return {
      gameStore: null,
      gameStoreData: null,
      levelData: {
        foregroundMask: null,
        levelForeground: null,
        levelBackground: null
      },
      availableColors: ['red', 'green', 'blue'],
      playerColors: {},
      currentShrimp: null,
      lastWeaponFired: null,
      showWinnerModal: false, // Steuert die Sichtbarkeit des modalen Overlays
      winnerMessage: '', // Hier wird die Gewinnernachricht gespeichert
    }
  },
  components: {
    Shrimp,
    Board
  },
  async created() {
    try {

      const gameStore = useGameStore();
      this.gameData = gameStore.gameData;
      console.log("Game Data erfolgreich abgerufen", this.gameData);
      const gameId = this.gameData.id;
      console.log("Game ID", gameId);
      this.levelData.levelForeground = this.gameData.levelForeground;
      console.log("Foreground", this.levelData.levelForeground);
      this.levelData.foregroundMask = this.gameData.foregroundMask;
      console.log("ForegroundMask", this.levelData.foregroundMask);
      this.levelData.levelBackground = this.gameData.levelBackground;
      console.log("Background", this.levelData.levelBackground);
      console.log('PlayGame this.currentShrimp soll  zugewiesen werden: ', this.gameData.playerStatisticsList[0].shrimpDTOList[0])
      this.currentShrimp = this.gameData.playerStatisticsList[0].shrimpDTOList[0]
      console.log('PlayGame this.currentShrimp wurde zugewiesen: ', this.currentShrimp)

      if (gameId) {
        registerGameWebSocket(gameId);
        this.$emit("update:levelData.foregroundMask", this.gameData.foregroundMask)
        await gameService.setAllIngame(gameId);
      }
    } catch {
      console.log("PG: Game Data konnte nicht abgerufen werden");
    }
    watch(() => latestGameObject.value, (value) => {
      if (value) {

        this.$emit("update:levelData.foregroundMask", latestGameObject.value.foregroundMask);
        this.$emit("updateGamestoreData:", latestGameObject.value);
        console.log('PlayGame-Aktualiisert. ');
        const currentPlayerIdx = latestGameObject.value.currentPlayerIdx;
        const currentShrimpIdx = latestGameObject.value.currentShrimpIdx;
        this.currentShrimp = latestGameObject.value.playerStatisticsList[currentPlayerIdx].shrimpDTOList[currentShrimpIdx];
        const winner = this.checkForWinner(value);
        if (winner) {
          // Setze die Gewinnernachricht basierend auf dem Gewinner
          this.winnerMessage = `Spieler ${winner.playerName} hat gewonnen!`;

          // Zeige das modale Overlay an
          this.showWinnerModal = true;
        }
      }
    })
    watch(() => latestWeaponFired.value, (value) => {
      if (value) {
        console.log("Fired Weapon Event auch in PlayGame bemerkt: ", latestWeaponFired.value);
        this.lastWeaponFired = latestWeaponFired.value;
        this.$emit("updateLastWeaponFired", this.lastWeaponFired);
      }
    })
  },
  mounted() {

  },
  methods: {

    handleUpdateLevelData(newForegroundMask) {
      this.levelData.foregroundMask = newForegroundMask;
    },
    updateLastWeaponFired(newWeaponFired) {
      this.lastWeaponFired = newWeaponFired;
    },

    async assignPlayerNumbers() {
      try {

        const myPlayer = await getMyPlayer();
        console.log('Mein Player Objekt: ', myPlayer);
        const myPlayerId = myPlayer.id;
        console.log('Meine Player ID:', myPlayerId);
        console.log('PlayerstatisticList: ', this.gameData.playerStatisticsList);
        // Abrufen der Spieler-IDs aus playerStatisticsList
        this.allPlayerId = this.gameData.playerStatisticsList.map(playerStats => playerStats.playerDTO.id).sort();
        console.log('Extrahierte IDs:', this.allPlayerId);
        // Zuweisen der Spieler-Nummer basierend auf der sortierten Position
        this.playerNumber = this.allPlayerId.indexOf(myPlayerId);
        console.log('Meine PlayerNumber:', this.playerNumber);

        // Überprüfen, ob die Spieler-Nummer innerhalb des Farbenarrays liegt
        if (this.playerNumber === -1) {
          console.error('Spieler-ID wurde in playerStatisticsList nicht gefunden.');
        } else if (this.playerNumber >= this.availableColors.length) {
          console.error('Mehr Spieler als verfügbare Farben.');
          this.playerNumber = null;
        } else {
          this.gameData.playerStatisticsList.forEach((playerStats, index) => {
            console.log('playerStats in foreach:', playerStats);
            const playerId = playerStats.playerDTO.id; // Zugriff auf die Spieler-ID
            // Zuweisung einer Farbe aus der verfügbaren Liste
            this.playerColors[playerId] = this.availableColors[index % this.availableColors.length];
          });

          console.log("Zugewiesene Spielerfarben:", this.playerColors);
        }
      } catch (error) {
        console.error('Fehler beim Zuweisen der Spieler-Nummern:', error);
      }
    },
    checkForWinner(gameObject) {
      // Hier implementierst du deine Gewinnlogik.
      // Zum Beispiel könntest du überprüfen, ob ein Spieler die erforderlichen Bedingungen für den Sieg erfüllt hat.

      // Wenn ein Spieler gewonnen hat, gib ihn zurück, ansonsten null
      // Hier ist ein fiktives Beispiel:
      const currentPlayerIdx = gameObject.currentPlayerIdx;
      const currentShrimp = gameObject.players[currentPlayerIdx].shrimps[0]; // Beispielhaft nur erster Shrimp
      if (currentShrimp.hitpoints > 0) {
        return {
          playerName: `Spieler ${currentPlayerIdx + 1}`,
          shrimpName: currentShrimp.name,
        };
      }

      // Wenn kein Spieler gewonnen hat, gib null zurück
      return null;
    },
    backToLobby() {
      // Hier implementierst du die Aktion, um zur Lobby zurückzukehren
      // Zum Beispiel eine Navigation oder andere Aktionen
      // Du kannst die Logik hier anpassen, um zur Lobby zurückzukehren.
      console.log('Zurück zur Lobby geklickt');
      this.$router.push("/");
      // Verberge das modale Overlay
      this.showWinnerModal = false;
    },
  }
}

</script>

<style scoped>
/* CSS-Stile für das modale Overlay */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5); /* Hintergrund mit Transparenz */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000; /* Z-Index über dem Rest der Seite */
}

.modal {
  background-color: #fff;
  padding: 20px;
  border-radius: 5px;
  text-align: center;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
}

/* Weitere Stile für das modale Overlay hier anpassen */
</style>