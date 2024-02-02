// shrimpStore.js

import { defineStore } from 'pinia';

export const useShrimpStore = defineStore('shrimpStore', {
    state: () => ({
        shrimps: [],
    }),
    actions: {
        addShrimp(shrimp) {
            this.shrimps.push(shrimp);
        },
        updateShrimpAngle({ shrimpId, newAngle }) {
            const shrimp = this.shrimps.find((s) => s.id === shrimpId);
            if (shrimp) {
                shrimp.angle = newAngle;
            }
        },
    },
    getters: {
        getShrimps() {
            return this.shrimps;
        },
    },
});
