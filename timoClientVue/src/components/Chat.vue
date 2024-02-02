<template>
  <div class="chat-messages" ref="chatMessages">
    <!-- Your existing chat messages rendering logic here -->
    <div v-for="message in messages.slice(-20)" :key="message.id">
      <p>
        <span>{{ formatTimestamp(message.timestamp) }}</span>
        <span>{{ message.sender.username }}:</span>
        <span>{{ message.text }}</span>
      </p>
    </div>
  </div>
  <!-- Input field for sending messages -->
  <div class="chat-input">
    <input v-model="newMessage" @keyup.enter="sendMessage" class="form-control m6" />
    <button class="btn btn-primary m2" @click="sendMessage">Send</button>
  </div>

</template>

<script>
import {sendMessage, latestMessage} from "@/services/WebSocketService.js";
import {watch} from "vue";
import {getMyPlayer} from "@/services/Api.js";
export default {
  data() {
    return {
      messages: [],
      newMessage: "",
      user: {
        id: null,
        username: ""
      }
    }
  },
  async created() {
    watch(() => latestMessage.value, (newMessage) => {
      if (newMessage) {
        this.messages.push(newMessage);
      }
    });
    try {
      this.user = await getMyPlayer(); // Setzen Sie hier die erhaltenen Benutzerdaten
    } catch (error) {
      console.error("Fehler beim Abrufen der Benutzerdaten:", error);
    }
    this.getChatHistory();
  },
  methods: {
    getChatHistory() {
      fetch("http://localhost:8080/lobby/getAllLobbyChat")
          .then((response) => response.json())
          .then((data) => {
            this.messages = data.slice(-20);
          })
          .catch((error) => console.log(error));
    },
    sendMessage() {
      try {
        const message = {
          text: this.newMessage,
          sender: {
            id: this.user.id,
            username: this.user.username,
          },
          timestamp: Date.now(),
        };
        sendMessage(message); // Verwenden der sendMessage Methode aus WebSocketService
      } catch (error) {
        console.error("Error in sendMessage:", error);
      }
      this.newMessage = "";
    },
    formatTimestamp(timestamp) {
      const time = new Date(timestamp);
      return `${time.getDate()}.${time.getMonth() + 1}.${time.getFullYear()} ${time
          .getHours()
          .toString()
          .padStart(2, "0")}:${time.getMinutes().toString().padStart(2, "0")}`;
    }
  }
}
</script>

<style scoped>
#chatContainer {
  margin-top: 20px;
}

.chat-messages {
  max-height: 600px;
  overflow-y: auto;
  border: 1px solid #ccc;
  padding: 10px;
  border-radius: 5px;
}

.chat-messages p {
  margin: 5px 0;
}

.col-md-8 {
  border: 1px solid #ccc;
  border-radius: 5px;
  padding: 10px;
  margin-bottom: 20px;
}

.chat-input {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-primary {
  width: 20%;
}

.col-md-4 {
  border: 1px solid #ccc;
  border-radius: 5px;
  padding: 10px;
  margin-bottom: 20px;
}

</style>