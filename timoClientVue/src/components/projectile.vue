<template>
  <div class="projectile" :style="projectileStyle">
    <img :src="projectileImageSource" alt="Projektil"/>
  </div>
</template>


<script>
import {refreshGame, stompClient} from "@/services/WebSocketService.js";

export default {
  props: {
    projectilePathProp: {
      type: Array,
      required: true,
    },
    gameStoreData: null
  },
  data() {
    return {
      currentIndex: 0,
    };
  },
  computed: {
    projectileImageSource() {
      return '/elements/projektil_small.png'
    },
    projectileStyle() {
      if (this.currentIndex < this.projectilePathProp.length) {
        const position = this.projectilePathProp[this.currentIndex];
        return {
          position: 'absolute',
          left: `${position.x}px`,
          top: `${position.y}px`,
          width: "20px", /* Passen Sie die Größe nach Bedarf an */
          height: "20px" /* Passen Sie die Größe nach Bedarf an */
          // Weitere Stilangaben hier
        };
      }
      return {
        display: this.isProjectileVisible ? 'block' : 'none',
      };
    },
  },
  watch: {
    projectilePathProp(newPath) {
      // Starte die Animation, wenn ein neuer Pfad empfangen wird
      this.currentIndex = 0;
      this.animateProjectile();
    },
  },
  methods: {
    animateProjectile() {
      if (this.currentIndex < this.projectilePathProp.length - 1) {
        // Verzögere die Aktualisierung der Position für die Animation (z.B. 50ms für schnellere Animation)
        setTimeout(() => {
          this.currentIndex++;
          this.animateProjectile();
        }, 5)}
        else {
          const gameId = this.gameStoreData.id;
          console.log("Set Ready gedrückt für:", gameId)
          stompClient.send("/shrimps/" + gameId + "/setReady", {}, {})
          refreshGame(gameId)
        }
        if (this.currentIndex === this.projectilePathProp.length - 1) {
          this.isProjectileVisible = false;

        }
      }

  },
};
</script>

<style scoped>

</style>
