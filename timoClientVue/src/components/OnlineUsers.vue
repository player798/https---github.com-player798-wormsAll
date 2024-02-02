<template>
  <div class="online-users">
    <ul class="list-group">
      <li
          class="list-group-item"
          :class="{ highlighted: enableInvitations && highlightedUser === user }"
          v-for="user in onlineUsers"
          :key="user.id"
          @mouseover="enableInvitations ? highlightUser(user) : null"
          @mouseleave="enableInvitations ? unhighlightUser(user) : null"
          @mousemove="updateInfoBoxPosition"
          @click="$emit('userClicked', user)"
      >
        {{ user.username }} ({{ user.status }})
      </li>
      <div class="info-box" v-if="hover && showInfoBox" :style="{ left: infoBoxX + 'px', top: infoBoxY + 'px' }">
        <ul>
          <li><strong>Username:</strong> {{ highlightedUser.username }}</li>
          <li><strong>Status:</strong> {{ highlightedUser.status }}</li>
          <li><strong>Email:</strong> {{ highlightedUser.email }}</li>
          <li v-if="playerHistory"><strong>Player History:</strong></li>
          <li v-for="(item, key) in playerHistory" :key="key" v-if="playerHistory">
            <strong>{{ key }}:</strong> {{ item }}
          </li>
        </ul>
      </div>
    </ul>
  </div>
</template>

<script>
import { watch } from "vue";
import { getOnlineUsers } from "@/services/Api.js"; // Annahme: Sie haben eine Funktion getOnlineUsers zum Abrufen von Benutzerdaten

export default {
  props: {
    enableInvitations: {
      type: Boolean,
      default: false,
    },
  },
  components: {},
  data() {
    return {
      onlineUsers: [],
      highlightedUser: null,
      hover: false,
      showInfoBox: false,
      infoBoxX: 0,
      infoBoxY: 0,
      playerHistory: null, // Geändert, um null initial zu setzen
    };
  },
  created() {
    this.initializeUsers();
    watch(() => latestMessage.value, async (newValue, oldValue) => {
      if (newValue) {
        setTimeout(async () => {
          this.onlineUsers = await getOnlineUsers();
          console.log("Neue Nachricht empfangen:", newValue);
        }, 500);
      }
    });
  },
  methods: {
    async initializeUsers() {
      this.onlineUsers = await getOnlineUsers();
    },
    async fetchPlayerHistory(playerId) {
      try {
        const response = await fetch(`http://localhost:8080/lobby/getPlayerHistory/${playerId}/`);
        if (response.ok) {
          const data = await response.json();
          this.playerHistory = data;
        } else {
          console.error("Fehler beim Abrufen der Spielerhistorie:", response.status);
        }
      } catch (error) {
        console.error("Fehler beim Abrufen der Spielerhistorie:", error);
      }
    },
    highlightUser(user) {
      this.highlightedUser = user;
      this.hover = true;
      if (user.id) {
        this.fetchPlayerHistory(user.id);
      }
    },
    unhighlightUser(user) {
      if (this.highlightedUser === user) {
        this.highlightedUser = null;
        this.hover = false;
        this.hideInfoBox();
      }
    },
    updateInfoBoxPosition(event) {
      if (this.hover) {
        this.infoBoxX = event.clientX - 360;
        this.infoBoxY = event.clientY - 365;
        this.showInfoBox = true;
      }
    },
    hideInfoBox() {
      this.showInfoBox = false;
    },
  },
};
</script>

<style scoped>
.h2 {
  font-size: 1.5rem;
}
.highlighted:hover {
  background-color: green;
  color: white;
  cursor: pointer;
}
.info-box {
  position: absolute;
  background-color: white;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  pointer-events: none;
}
</style>
