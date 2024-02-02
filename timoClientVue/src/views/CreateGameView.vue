<template>
  <div class="container mt-4">

    <h1 class="mb-4">Spiel Einstellungen: GameID {{ gameData.id }} PlayerNumber {{ playerNumber }}</h1>

    <div class="card">
      <div class="card-body">
        <h5 class="card-title">Playername: {{ getUserName }} mit ID: {{getUserID}}</h5>
        <p class="card-text">Shrimp 1: {{ shrimpNames[0] || 'N/A' }}</p>
        <p class="card-text">Shrimp 2: {{ shrimpNames[1] || 'N/A' }}</p>
        <p class="card-text">Shrimp 3: {{ shrimpNames[2] || 'N/A' }}</p>
      </div>
      <div v-if="invitationAccepted">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Playername Guest: {{ getGuestUserName }} mit ID: {{getGuestUserID}}</h5>
            <p class="card-text">Shrimp 1: {{ getGuestShrimpNames[0] || 'N/A' }}</p>
            <p class="card-text">Shrimp 2: {{ getGuestShrimpNames[1] || 'N/A' }}</p>
            <p class="card-text">Shrimp 3: {{ getGuestShrimpNames[2] || 'N/A' }}</p>
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
        <div v-if="canStartGame">
            <button @click="startGame">Spiel starten</button>
        </div>
      </div>
    </div>

    <!-- Display connected clients -->
    <div class="card mt-4">
      <div class="card-body">
        <OnlineUsers @userClicked="handleUserClick" :enableInvitations="true"/>
      </div>
    </div>
  </div>
</template>

<script>
import OnlineUsers from "@/components/OnlineUsers.vue";
import GameService from "@/services/GameService.js";
import {getMyPlayer, getOnlineUsers, startNewGame} from "@/services/Api.js";
import {useGameStore} from "@/stores/gameStore.js";
import {latestGameObject, registerGameWebSocket, stompClient} from "@/services/WebSocketService.js";
import {watch} from "vue";
import {usePlayerStore} from "@/stores/playerStore.js";

export default {
  components: { OnlineUsers },
  data() {
    return {
      gameId: null,
      onlineUsers: [],
      senderId: null,
      invitationAccepted: false,
      stateReady: false,
      gameReadyStatus: false,
      hostReady: '',
      guestReady: ''
    };
  },

  async created() {
    await this.fetchGameData();
    await this.initializeUsers();
    const myPlayer = await getMyPlayer();
    if (myPlayer && myPlayer.id) {
      this.senderId = myPlayer.id;
    }

    watch(() => latestGameObject.value, (value) => {
      if (value) {
        this.invitationAccepted = true;
        const gameState = value.gameState;
        if (gameState === "READY") {

        }
        if (gameState === "PLAYER_TURN") {
          const playerStore = usePlayerStore();
          const gameData = latestGameObject.value;
          playerStore.assignPlayerNumbers(gameData);
          playerStore.setPlayerColors(gameData.playerStatisticsList, ['red', 'green', 'blue'])

          this.$router.push("/playGame");
        }
        }
    })
  },
  methods: {
    async fetchGameData() {
      const gameStore = useGameStore();
      try {
        const gameData = await startNewGame(); // API-Anfrage
        console.log("API Response Data: ", gameData); // Überprüfen der API-Antwort
        gameStore.updateGameData(gameData);
        this.gameId = gameData.id; // Setzen der gameId
        console.log("Stored Game Data: ", gameStore.gameData); // Überprüfen der gespeicherten Daten
        registerGameWebSocket(gameStore.gameData.id)
        } catch (error) {
        console.error("Error fetching user data:", error);
      }
    },
    setReady(){
      console.log("Folgendes vor dem Senden: ",this.gameId, this.stateReady)
      stompClient.send("/shrimps/" + this.gameId + "/setReady", {}, {});
      this.stateReady = true;
      console.log("Folgendes nach dem Senden: ",this.gameId, this.stateReady)
    },
    getReadyStatus() {
      // Hier überprüfe, ob sowohl Host als auch Gast bereit sind
      const hostReady = this.gameData.playerStatisticsList[0]?.status === 'READY';
      const guestReady = this.gameData.playerStatisticsList[1]?.status === 'READY';

      // Setze den gameReadyStatus entsprechend
      this.gameReadyStatus = hostReady && guestReady;
    },

    // Weitere Methoden...
    async handleUserClick(user) {
      if (this.gameId){
        const receiver = await this.findPlayerInOnlineUsers(user.id);
        if (receiver) {
          console.log("Mein Receiver-Objekt", receiver);
          const sender = await getMyPlayer();
          if (sender) {
            console.log("Mein Sender-Objekt", sender);
            console.log("Mein Receiver-Objekt", receiver);
            console.log("Meine Game-ID", this.gameId);
            GameService.sendInviteToPlayer(receiver, this.gameId, sender);
          }
        }
      }
      else {
        console.error("gameId ist null, kann keine Einladung senden");
      }
    },

    async findPlayerInOnlineUsers(userId) {
      console.log("findPlayerInOnlineUsers")
      this.onlineUsers = await getOnlineUsers();
      return this.onlineUsers.find(player => player.id === userId);
    },
    async initializeUsers() {
      this.onlineUsers = await getOnlineUsers();
    },
    acceptInvitation() {
      this.invitationAccepted = true;
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
    canStartGame() {
      // Hier überprüfen, ob Host und Gast bereit sind
      return this.readyStatus.hostReady && this.readyStatus.guestReady;
    },
    startGame(){
      fetch("http://localhost:8080/game/" + this.gameId + "/playSpring")
      console.log("Fetched Data from ", this.gameId)
    },
    gameData() {
      const gameStore = useGameStore();
      return gameStore.gameData;
    },
    gameId(){
      const gameStore = useGameStore();
      return gameStore.gameData.id;
    },
    formattedGameData(){
      return JSON.stringify(this.gameData, null, 2);
    },

    playerNumber() {
      console.log("Getting playerNumber")
      const userPlayerStat = this.gameData.playerStatisticsList.find(
          (playerStat) => playerStat.playerDTO.username === this.gameData.username
      );

      if (userPlayerStat) {
        return this.gameData.playerStatisticsList.indexOf(userPlayerStat) + 1;
      }

      return 1;
    },

    shrimpNames() {
      const shrimpList = this.gameData.playerStatisticsList[0]?.shrimpDTOList;
      return shrimpList ? shrimpList.map(shrimp => shrimp.name) : [];
    },

    getUserName() {

      return this.gameData.playerStatisticsList[0]?.playerDTO.username;
    },
    getUserID() {
      return this.gameData.playerStatisticsList[0]?.playerDTO.id;
    },
    getGuestShrimpNames() {
      const shrimpList = this.gameData.playerStatisticsList[1]?.shrimpDTOList;
      return shrimpList ? shrimpList.map(shrimp => shrimp.name) : [];
    },

    getGuestUserName() {

      return this.gameData.playerStatisticsList[1]?.playerDTO.username;
    },
    getGuestUserID() {
      return this.gameData.playerStatisticsList[1]?.playerDTO.id;
    },
  },

}


</script>

<style scoped>

</style>