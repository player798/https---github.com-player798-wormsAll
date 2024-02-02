<template>
  <div class="container mt-4">
    <!--
    <h1>Join Game View</h1>
    <h2>Aktueller Spielstand:</h2>
    <pre>{{ formattedGameStoreData }}</pre>
    <h2>Aktueller Inhalt des InvitationStore:</h2>
    <pre>{{ formattedInvitationStoreData }}</pre>
    <button @click="refreshMyGame">Game Refresh</button>
    <button @click="refreshPSL">PSL</button>
-->
    <h1 class="mb-4">Spiel Einstellungen: GameID {{ gameData.id }} PlayerNumber {{ playerNumber }}</h1>

    <div class="card">
      <div class="card-body">
        <h5 class="card-title">Playername Host: {{ getHostUserName }} mit ID: {{getHostUserId}}</h5>
        <p class="card-text">Shrimp 1: {{ hostShrimpNames[0] || 'N/A' }}</p>
        <p class="card-text">Shrimp 2: {{ hostShrimpNames[1] || 'N/A' }}</p>
        <p class="card-text">Shrimp 3: {{ hostShrimpNames[2] || 'N/A' }}</p>
      </div>
    </div>
    <div class="card">
      <div class="card-body">
        <h5 class="card-title">Playername Guest: {{ getUserName }} mit ID: {{getUserID}}</h5>
        <p class="card-text">Shrimp 1: {{ shrimpNames[0] || 'N/A' }}</p>
        <p class="card-text">Shrimp 2: {{ shrimpNames[1] || 'N/A' }}</p>
        <p class="card-text">Shrimp 3: {{ shrimpNames[2] || 'N/A' }}</p>
      </div>
    </div>
    <div>
      <p>Hoststatus: {{ readyStatus.hostReady ? 'READY' : 'NOT READY' }}</p>
      <p>Gueststatus: {{ readyStatus.guestReady ? 'READY' : 'NOT READY' }}</p>
    </div>
    <div v-if="stateReady">
      <button class="v-if" @click="">Bereit!</button>
    </div>
    <div v-if="!stateReady">
      <button class="v-if" @click="setReady">Bereit?</button>
    </div>
    </div>
</template>

<script>
import {useGameStore} from "@/stores/gameStore.js";
import {getGameData} from "@/services/Api.js";
import {useInvitationStore} from "@/stores/invitationStore.js";
import {latestGameObject, refreshGame, registerGameWebSocket, stompClient} from "@/services/WebSocketService.js";
import {watch} from "vue";
import {usePlayerStore} from "@/stores/playerStore.js";

export default {
  data() {
    return {
      stateReady: false,
    }
  },
  async created() {
    console.log('Join Game betreten')
    const invitationStore = useInvitationStore();
    const gameId = invitationStore.gameId;
    if (gameId) {
       //Einrichten des WebSocket-Abonnements
      registerGameWebSocket(gameId);
      refreshGame(gameId);
      try {
        const gameStore = useGameStore();
        const gameData= gameStore.gameData;
        {console.log("Game Data erfolgreich abgerufen", gameData);}
      } catch {console.log("Game Data konnte nicht abgerufen werden");}
    }
    watch(() => latestGameObject.value, (value) => {
      if (value) {
        this.invitationAccepted = true;
        const playerStore = usePlayerStore();
        const gameData = latestGameObject.value;
        playerStore.assignPlayerNumbers(gameData);
        playerStore.setPlayerColors(gameData.playerStatisticsList, ['red', 'green', 'blue'])
        const gameState = value.gameState;
        if (gameState === "PLAYER_TURN") {
          this.$router.push("/playGame");
        }
      }
    })
  },

  methods: {
    refreshMyGame() {
      const invitationStore = useInvitationStore();
      const gameId = invitationStore.gameId;
      console.log('Ausgabe vor refresh: ',gameId);
      refreshGame(gameId)
    },

    refreshPSL(){
      console.log("refreshPSL!");
      console.log("Array 0 = ", this.gameData.playerStatisticsList[0]);
      console.log("Array 1 = ", this.gameData.playerStatisticsList[1]);
      console.log("Array 2 = ", this.gameData.playerStatisticsList[0].shrimpDTOList[0]);
      console.log("Array 3 = ", this.gameData.playerStatisticsList[1].shrimpDTOList[0]);
    },
    setReady(){
      const invitationStore = useInvitationStore();
      const gameId = invitationStore.gameId;
      console.log("Folgendes vor dem Senden: ",gameId, this.stateReady)
      stompClient.send("/shrimps/" + gameId + "/setReady", {}, {});
      this.stateReady = true;
      console.log("Folgendes nach dem Senden: ",gameId, this.stateReady)
    }
  },

  computed: {
    readyStatus() {
      const gameData = latestGameObject.value;

      if (gameData) {
        const hostReady = gameData.playerStatisticsList[0]?.status === "READY";
        const guestReady = gameData.playerStatisticsList[1]?.status === "READY";
        return {
          hostReady,
          guestReady
        };
      }
      return null;
    },
    getReadyStatus() {
      // Hier überprüfe, ob sowohl Host als auch Gast bereit sind
      const hostReady = this.gameData.playerStatisticsList[0]?.status === 'READY';
      const guestReady = this.gameData.playerStatisticsList[1]?.status === 'READY';

      // Setze den gameReadyStatus entsprechend
      this.gameReadyStatus = hostReady && guestReady;
    },

    // Weitere Methoden...
    canStartGame() {
      // Hier überprüfen, ob Host und Gast bereit sind
      return this.readyStatus.hostReady && this.readyStatus.guestReady;
    },
    startGame() {
      fetch("http://localhost:8080/game/" + this.gameId + "/playSpring")
      console.log("Fetched Data from ", this.gameId);
    },
    gameStoreData() {
      const gameStore = useGameStore();
      return gameStore.gameData;
    },
    formattedGameStoreData() {
      return JSON.stringify(this.gameStoreData, null, 2);
    },

    formattedInvitationStoreData() {
      const invitationStore = useInvitationStore();

      // Extrahieren Sie nur die benötigten Daten
      const relevantData = {
        gameId: invitationStore.gameId,
        invitationReceived: invitationStore.invitationReceived,
        // Fügen Sie hier weitere relevante Daten hinzu
      };

      return JSON.stringify(relevantData, null, 2);
    },
    gameData() {
      const gameStore = useGameStore();
      return gameStore.gameData;
    },
    gameId() {
      const gameStore = useGameStore();
      return gameStore.gameData.id;
    },
    getUserID() {
      return this.gameData.playerStatisticsList[1]?.playerDTO.id;
    },
    getUserName() {
      return this.gameData.playerStatisticsList[1]?.playerDTO.username;
    },
    shrimpNames() {
      const shrimpList = this.gameData.playerStatisticsList[1]?.shrimpDTOList;
      return shrimpList ? shrimpList.map(shrimp => shrimp.name) : [];
    },
    getHostUserId() {
      return this.gameData.playerStatisticsList[0]?.playerDTO.id;
    },
    getHostUserName() {
      return this.gameData.playerStatisticsList[0]?.playerDTO.username;
    },
    hostShrimpNames() {
      const shrimpList = this.gameData.playerStatisticsList[0]?.shrimpDTOList;
      return shrimpList ? shrimpList.map(shrimp => shrimp.name) : [];
    }

  }
}
</script>
<style scoped>

</style>