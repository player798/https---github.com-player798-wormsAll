import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { ref } from 'vue';
import {getMyPlayer} from "@/services/Api.js";
import LobbyView from "@/views/LobbyView.vue";
import {useInvitationStore, setSenderData} from "@/stores/invitationStore.js";
import { useGameStore } from '@/stores/gameStore';
import gameService from "@/services/GameService.js";

export const latestMessage = ref(null);
export const latestGameObject = ref(null);

export const latestWeaponFired = ref(null);
let stompClient;
let senderData = null;
export async function connectToWebsocket() {
    const playerId = await getPlayerId();
        await registerWebsocket(playerId);

}
async function getPlayerId() {
    try {
        const myPlayer = await getMyPlayer();
        const playerId = myPlayer.id;
        console.log('Spieler-ID:', playerId);
        return playerId;
    } catch (error) {
    console.error('Fehler beim Abrufen der Spielerinformationen:', error);
    return null;
    }
}
function registerWebsocket(playerId) {
    let socket = new SockJS('/register_socket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, () => onConnected(playerId), onError);
    }
function onConnected(playerId){
    // Abonnieren der Lobby-Nachrichten
    stompClient.subscribe('/chat/messages', (message) => {
        handleWebSocketMessage(JSON.parse(message.body));
        console.log("Nachrichten aboniert als Client: ", playerId);
    });

    // Beim Server als Online anmelden
    stompClient.send("/shrimps/socket.addUser", {}, {});

    // Private Nachrichten empfangen
    stompClient.subscribe('/chat/messages/' + playerId, (message) => {
        handlePersonalInvite(JSON.parse(message.body));
        console.log("Privater Messagekanal offen.", playerId, message);
    });
}

function handleWebSocketMessage(message) {
    console.log("WebSocket Message received:", message);
    latestMessage.value = message;  // Setzen der neuesten Nachricht
    if (message.destination && message.destination.startsWith('/chat/messages/')) {
        // Es handelt sich um eine private Nachricht, behandeln Sie sie als Einladung
        const invitationStore = useInvitationStore();
        invitationStore.setInvitationReceived(true);
    }

}
function onError() {
    console.log('Could not connect to WebSocket server. Please refresh this page to try again!');
}

export function sendMessage(message) {
    if (stompClient && stompClient.connected) {
        stompClient.send("/shrimps/socket.sendMessage", {}, JSON.stringify(message));
    } else {
        console.error("WebSocket is not connected.");
    }
}
function handlePersonalInvite(inviteMessage) {
    console.log("Einladung erhalten", inviteMessage);
    const invitationStore = useInvitationStore();
    const gameId = inviteMessage.gameInvite.id;
    const senderData = inviteMessage; // Holen des Sender-Objekts

    if (gameId) {
        invitationStore.setGameId(gameId);
        invitationStore.setInvitationReceived(true);
        invitationStore.setSenderData(senderData); // Setzen des Sender-Objekts
        console.log("SenderData bei setSenderData: ", senderData);
        const gameInvite = inviteMessage.gameInvite;
        const gameStore = useGameStore();
        gameStore.updateGameData(gameInvite);

    } else {
        console.error("Empfangene Einladung hat keine gültige Spiel-ID");
    }
}


export function registerGameWebSocket(gameId) {
    const gameStore = useGameStore();

    stompClient.subscribe('/chat/game/' + gameId + '/gameChanged', (message) => {
        const gameData = JSON.parse(message.body);
        console.log("gameChanged Event empfangen:", gameData);
        gameStore.updateGameData(gameData);
        latestGameObject.value = gameData;
    });
    stompClient.subscribe('/chat/game/' + gameId + "/weaponFired", (message) => {
        const weaponFiredData = JSON.parse(message.body);
        console.log("Weapon Fired!: ", weaponFiredData);
        latestWeaponFired.value = weaponFiredData;
    });
}

function getSenderData() {
    return senderData
}
function refreshGame(gameId) {
    stompClient.send("/shrimps/" + gameId + "/refreshGame", (gameDto) => {
        const gameData = JSON.parse(gameDto.body);
        const gameStore = useGameStore().updateGameData(gameData);
        console.log("Game refreshed:", gameData);
        latestGameObject.value = gameData;
    });

}
export { stompClient, getSenderData, refreshGame };