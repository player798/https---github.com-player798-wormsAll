// stores/invitationStore.js
import { defineStore } from 'pinia';

export const useInvitationStore = defineStore('invitationStore', {
    state: () => ({
        invitationReceived: false,
        gameId: null,
        senderData: null, // Hier das Sender-Objekt speichern
    }),
    actions: {
        setGameId(id) {
            this.gameId = id;
        },
        setInvitationReceived(status) {
            this.invitationReceived = status;
        },
        setSenderData(senderData) {
            this.senderData = senderData;
        }
    }
});
