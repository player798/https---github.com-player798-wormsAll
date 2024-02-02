<template>
  <div ref="boardContainer" class="board-container">
    <canvas ref="canvasProcessed" :width="canvasWidth" :height="canvasHeight"></canvas>
    <button @click="showMyStore">ShowStoreInBoard</button>
    <button @click="showMyShrimp">ShowMyShrimp</button>
    <button @click="showMyWeaponAngle">ShowMyWeaponAngle</button>
    <button @click="toggleLookingDirection">Richtung ändern</button>
    <button @click="showMyShrimp">ShowMyShrimpInBoard</button>
    <button @click="updateCurrentShrimp">UpdateCurrentShrimpInBoard</button>
    <button @click="updateCurrentShrimpProp">UpdateCurrentShrimpPropInBoard</button>

    <!-- Hier werden die Shrimp-Informationen für jeden Spieler gerendert -->
    <div>
      <div v-for="(playerStats, index) in this.gameStoreData.playerStatisticsList" :key="playerStats.playerId">
        <Shrimp v-for="shrimp in playerStats.shrimpDTOList"
                :key="shrimp.id"
                :shrimp="shrimp"
                :playerColor="playerColor(playerStats.playerDTO.id)"
                :currentShrimpId="this.gameStoreData.currentShrimpId"
                :canvasOffset="canvasOffset"
                :weaponAngle="weaponAngle"
                :lookingRight="lookingRight"
                @updateWeaponAngle="updateWeaponAngle"
                weapon="weapon"
                />
      </div>
    </div>
    <div>
      <TurnControls :currentShrimpId="this.gameStoreData.currentShrimpId"
                  :gameStoreData="this.gameStoreData"
                  :weaponAngle="weaponAngle"
                  @updateWeaponAngle="updateWeaponAngle"
                  :current-shrimp-prop="currentShrimpProp"
      />
      <projectile v-if="lastWeaponFiredProp"
                  :projectilePathProp="lastWeaponFiredProp.projectilePath"
                  :gameStoreData="this.gameStoreData" />
    <div>
      <p>Board: Data Shrimp: {{ shrimp }}</p>
      <p>Board: Prop Current Shrimp: {{ currentShrimpProp }}</p>
      <p>Board: Gamestore currentShrimp {{ gameStoreData.currentShrimpId }}</p>
      <div>Board: {{ lastWeaponFiredProp }}</div>

    </div>
  </div>
  </div>
</template>

<script>

import {watch, ref, reactive} from "vue";
import Shrimp from "@/components/Shrimp.vue";
import {useGameStore} from "@/stores/gameStore.js";
import player from "@/components/Player.vue";
import {usePlayerStore} from "@/stores/playerStore.js";
import TurnControls from "@/components/TurnControls.vue";
import Projectile from "@/components/projectile.vue";


export default {
  components: {
    Projectile,
    TurnControls,
    Shrimp
  },
  props: {
    levelData: {
      type: Object,
      required: true
    },

    playerColors: {
      type: Object,
      required: true
    },
    gameStoreData: Object,
    currentShrimpProp: Object,
    shrimp: {
      type: Object,
      required: true
    },
    lastWeaponFiredProp:{
      type: Object,
      required: true},
    projectilePath: Object

  },
  data() {
    return {
      canvasWidth: 1366,
      canvasHeight: 768,
      shrimpImage: null,
      canvasOffset: { x: 0, y: 0 },
      weaponAngle: 90,
      weapon: null,
      lookingRight: true
    };
  },
  created() {
    const playerStore = usePlayerStore();

    this.gameStoreData.playerStatisticsList.forEach(playerStats => {
      const playerColor = playerStore.playerColors[playerStats.playerDTO.id];
      console.log(`B: Farbe für Spieler ${playerStats.playerDTO.id}:`, playerColor);
    })

  },

  mounted() {
    this.$nextTick(() => {
      try {
        const foregroundMask = this.levelData.foregroundMask;
        console.log("B: Level Foreground erfolgreich abgerufen", foregroundMask);
        this.$emit("update-level-data", foregroundMask);
      } catch {
        console.log("B: Level Data konnte nicht abgerufen werden");
      }
      this.loadAndProcessImages();
      this.shrimp = this.currentShrimpProp;
    });


  },
  computed: {
    // Verwenden Sie eine berechnete Eigenschaft, um auf gameStoreData zu reagieren

    },

  methods: {
    updateWeaponAngle(newAngle) {
      // Hier können Sie die Logik zur Aktualisierung der weaponAngle-Prop durchführen
      this.weaponAngle = newAngle;
    },

    updateCurrentShrimp() {
      const currentPlayerIdx = this.gameStoreData.currentPlayerIdx;
      const currentShrimpIdx = this.gameStoreData.currentShrimpIdx;
      console.log('ToBeUpdatedCurrentShrimp: ', this.shrimp);
      this.shrimp = this.gameStoreData.playerStatisticsList[currentPlayerIdx].shrimpDTOList[currentShrimpIdx];
      console.log('UpdatedCurrentShrimp: ', this.shrimp);
    },

    showMyStore() {
      console.log("Mein Store unter Board: ", this.gameStoreData);
    },
    showMyShrimp() {
      console.log("Mein Shrimp unter Board: ", this.shrimp);
    },
    showMyWeaponAngle() {
      console.log("Mein WeaponAngle unter Board: ", this.weaponAngle);
    },
    onProcessedImageReceived(processedImage) {
      const canvasProcessed = this.$refs.canvasProcessed;
      const contextProcessed = canvasProcessed.getContext('2d');
      contextProcessed.clearRect(0, 0, this.canvasWidth, this.canvasHeight);
      const imgProcessed = new Image();
      imgProcessed.src = processedImage;
      imgProcessed.onload = () => {
        //Hintergrund laden und auf den verarbeiteten Canvas zeichnen
        const background = new Image();
        background.src = 'http://localhost:8080/levels/level_1_background.png';
        background.onload = () => {
          const canvasBackground = document.createElement('canvas');
          canvasBackground.height = this.canvasHeight;
          canvasBackground.width = this.canvasWidth;
          const contextBackground = canvasBackground.getContext('2d');
          contextBackground.drawImage(background, 0, 0);
          contextProcessed.drawImage(imgProcessed, 0, 0, this.canvasWidth, this.canvasHeight);
        };
        this.calculateCanvasOffset();
        //this.drawShrimps();
      };
    },
    loadAndProcessImages() {
      const originalImage = new Image();
      originalImage.src = 'http://localhost:8080/levels/level_1_foreground_fill.png';

      originalImage.onload = () => {
        // Create a canvas for the original image
        const canvasOriginal = document.createElement('canvas');
        canvasOriginal.width = this.canvasWidth;
        canvasOriginal.height = this.canvasHeight;
        const contextOriginal = canvasOriginal.getContext('2d');
        contextOriginal.drawImage(originalImage, 0, 0);

        // Extract new alpha mask information from this.userData.foregroundMask
        const alphaMaskImage = new Image();
        alphaMaskImage.src = 'data:image/png;base64,' + this.levelData.foregroundMask;
        alphaMaskImage.onload = () => {
          const canvasForegroundMask = document.createElement('canvas');
          canvasForegroundMask.width = this.canvasWidth;
          canvasForegroundMask.height = this.canvasHeight;
          const contextForegroundMask = canvasForegroundMask.getContext('2d');
          contextForegroundMask.drawImage(alphaMaskImage, 0, 0);

          // Create a canvas for the processed image
          const canvasProcessed = this.$refs.canvasProcessed;
          const contextProcessed = canvasProcessed.getContext('2d');

          // Draw the original image with updated alpha onto the processed canvas
          const imageDataOriginal = contextOriginal.getImageData(0, 0, this.canvasWidth, this.canvasHeight);
          const imageDataAlpha = contextForegroundMask.getImageData(0, 0, this.canvasWidth, this.canvasHeight);

          for (let i = 0; i < imageDataOriginal.data.length; i += 4) {
            // Replace the alpha value with the new alpha mask value
            imageDataOriginal.data[i + 3] = imageDataAlpha.data[i + 3];
          }

          // Draw the modified image back to the processed canvas
          contextProcessed.putImageData(imageDataOriginal, 0, 0);
          const background = new Image();
          background.src = 'http://localhost:8080/levels/level_1_background.png';
          background.onload = () => {
            contextProcessed.drawImage(background, 0, 0);
          };
          // Convert the processed canvas content to base64 for display
          const processedImageUrl = canvasProcessed.toDataURL();

          // Set the processed image URL for display
          this.onProcessedImageReceived(processedImageUrl);

        }
      }

    },

    calculateCanvasOffset() {
      const rect = this.$refs.boardContainer.getBoundingClientRect();
      this.canvasOffset.x = rect.left;
      this.canvasOffset.y = rect.top;
    },
    playerColor(playerId) {
      const playerStore = usePlayerStore();
      return playerStore.playerColors[playerId];
    },
    toggleLookingDirection() {
      // Ändern Sie die Blickrichtung in der übergeordneten Komponente
      this.lookingRight = !this.lookingRight;
    },



  },

}
</script>

<style scoped>

</style>