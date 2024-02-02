<template>
  <div class="container l">
    <Header/>
    <div class="row">
      <div class="col-md-9">
        <h3>Chat-Fenster</h3>
        <Chat/>
      </div>
      <div class="col-md-3">

          <h3>Online Users</h3>
          <OnlineUsers :enable-invitations="false"/>
        </div>
          <div v-if="invitationReceived">
            <h3>Game Invites</h3>
            <p>Einladung eingegangen. Wollen Sie diese Annehmen?</p>
            <button @click="acceptInvite">JA</button>
            <button @click="declineInvite">NEIN</button>
          </div>
        </div>
      </div>


</template>

<script>
import {connectToWebsocket, getSenderData, latestMessage, stompClient} from "@/services/WebSocketService.js";
import Header from "@/components/Header.vue";
import Chat from "@/components/Chat.vue";
import OnlineUsers from "@/components/OnlineUsers.vue";
import {watch} from "vue";
import {getMyPlayer, getOnlineUsers} from "@/services/Api.js";
import {useInvitationStore} from "@/stores/invitationStore.js";
import {useGameStore} from "@/stores/gameStore.js";
import GameService from "@/services/GameService.js";

export default {
  components: {
    OnlineUsers,
    Chat, Header
  },
  data() {
    return {
      senderData: null, // Hier initialisieren
    };
  },
  computed: {
    invitationReceived() {
      const invitationStore = useInvitationStore();
      return invitationStore.invitationReceived;
    }
  },
  created() {
    connectToWebsocket();
    watch(() => latestMessage.value, async (newValue, oldValue) => {
      if (newValue) {
        setTimeout(async () => {
          this.onlineUsers = await getOnlineUsers();
          console.log("Neue Nachricht empfangen:", newValue);
        }, 500);
      }
    });
    const invitationStore = useInvitationStore();
    this.$watch(() => invitationStore.invitationReceived, (newVal) => {
      console.log("Der Einladungsstatus hat sich geändert:", newVal);
      });
  },
  methods: {
  displayGameData() {
  const gameStore = useGameStore();
    return gameStore.gameData;
  },
    acceptInvite() {
      console.log("Accept geklickt");
      const invitationStore = useInvitationStore();
      const gameId = invitationStore.gameId;
      console.log("Spiel-ID:", gameId);
      console.log('displayGameData: ',this.displayGameData());
      const senderData = invitationStore.senderData;
      console.log('Senderdata', senderData)
      if (senderData) {
        stompClient.send("/shrimps/" + gameId + "/acceptInvite", {}, JSON.stringify(senderData));
        this.$router.push({ name: 'joinGame' }); // Weiterleitung zur JoinGameView
      } else {
        console.error("Keine gültigen Senderdaten vorhanden");
      }
      if (gameId) {
        this.$router.push({ name: 'joinGame' });
      } else {
        console.error("Keine gültige Spiel-ID vorhanden");
      }
    },

    declineInvite() {
      console.log("Deny geklickt");
    }
  }
}
</script>


<style scoped>

</style>