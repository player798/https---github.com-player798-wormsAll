<template>
  <div :style="{ backgroundColor: playerColor }">
    <img
        class="shrimp-image"
        :src="shrimpImageSource"
        :style="shrimpImageStyle"

     alt=""/>

    <div class="shrimp-info" :style="infoStyle">
      <div class="info-box" :style="{ color: playerColor }">
        <div class="shrimp-name">{{ shrimp.name }}</div>
        <div class="shrimp-hitpoints">HP: {{ shrimp.hitpoints }}</div>
        <img v-if="isActiveShrimp" :src="activeIndicatorUrl" class="active-indicator">
        <img v-if="isActiveShrimp" :src="targetIndicatorUrl" class="target-indicator" :style="targetStyle">
        </div>

    </div>
  </div>
  <button v-if="isActiveShrimp" @click="weaponAnglePlus">+5°</button>
  <button v-if="isActiveShrimp" @click="weaponAngleMinus">-5°</button>
  <button v-if="isActiveShrimp" @click="showMyShrimpShrimp">ShowMyShrimp</button>
  <div v-if="isActiveShrimp">
    <p>Shrimps: Name: {{ shrimp.name }} Shrimp Hitpoints: {{ shrimp.hitpoints }} Looking Right: {{ shrimp.lookingRight ? 'Right' : 'Left' }}
    Prop WeaponAngle: {{weaponAngle}} </p>
  </div>
</template>

<script>

import {latestGameObject} from "@/services/WebSocketService.js";
import {watch} from "vue";

export default {
  mounted() {
    console.log('lookingRight Prop in Shrimps:', this.shrimp.lookingRight);
    this.$nextTick(() => {
      // ...
      // this.weaponAngle = this.initialWeaponAngle;

    });
    watch(() => latestGameObject.value, (value) => {
          if (value && this.isActiveShrimp) {
            const newWeaponAngle = 0;
            if (this.shrimp.lookingRight) {
              console.log("Watch: Meine ShrimpID: ", this.shrimp.id, "AktiverShrimp: ",
                  "Mein lookingRightWert: ", this.shrimp.lookingRight)
              this.$emit('updateWeaponAngle', 90);
            } else {
              console.log("Watch: Meine ShrimpID: ", this.shrimp.id, "AktiverShrimp: ",
                  "Mein lookingRightWert: ", this.shrimp.lookingRight);
              this.$emit('updateWeaponAngle', 270);
            }
          }
        }
        )


        //this.$emit('updateWeaponAngle', newWeaponAngle);
        //console.log('Watch in Shrimps emittet:', newWeaponAngle);


  },

  data() {
    return {

    }

  },


  props: {
    shrimp: {
      type: Object,
      required: true
    },
    currentShrimpId: {
      type: Number,
      required: true
    },
    canvasOffset: {
      type: Object,
      required: true
    },
    playerColor: {
      type: String,
      default: '',
      required: true
    },
    weaponAngle: {
      type: Number,
      required: true
    },
    weapon: {
      type: Object,
      required: true
    }
  },

  computed: {
    shrimpImageSource() {
      if (this.shrimp.hitpoints === 0) {
        return "elements/grabstein_small.png"
      }
      return this.shrimp.lookingRight
          ? "elements/shrimp_fill.png"
          : "elements/shrimp_reverse.png";
    },
    shrimpImageStyle() {
      return {
        width: "40px", // Passen Sie die Größe nach Bedarf an
        height: "40px", // Passen Sie die Größe nach Bedarf an
        position: "absolute",
        left: `${this.shrimp.x_position + this.canvasOffset.x - 20}px`,
        top: `${this.shrimp.y_position + this.canvasOffset.y - 40}px`,
      };
    },
    projectileImageSource() {
      return "elements/Projektil.png"
    },
    projectileImageStyle() {
      return {
        width: "10px", // Passen Sie die Größe nach Bedarf an
        height: "10px", // Passen Sie die Größe nach Bedarf an
        position: "absolute",

      };
    },
    targetStyle() {
      // Radius des Zielkreises (Abstand vom Ursprung)
      const circleRadius = 80; // Ändern Sie dies auf den gewünschten Radius

      // Winkel des Zielkreises basierend auf weaponAngle
      const angleInRadians = (90 - this.weaponAngle) * (Math.PI / 180);

      // Ursprung des Kreises um 20 Pixel nach links und 60 Pixel nach oben verschoben
      const originX = 20; // 40 Pixel nach links
      const originY = -60; // 90 Pixel nach oben

      // Berechnen Sie die X- und Y-Offsets basierend auf dem Winkel und dem Radius
      const xOffset = Math.cos(angleInRadians) * circleRadius;
      const yOffset = Math.sin(angleInRadians) * circleRadius;

      // Berechnen Sie die finale Position des Zielkreuzes relativ zum verschobenen Ursprung
      const xPosition = originX + xOffset;
      const yPosition = originY + yOffset;

      return {
        position: "absolute",
        width: "20px",
        height: "auto",
        left: `calc(50% + ${xPosition}px)`,
        bottom: `${yPosition}px`,
        transformOrigin: "center center",
        transform: `translateX(-50%) translateY(-100%) rotate(${this.weaponAngle}deg)`,
      };
    },



    infoStyle() {
      //console.log('Im infoStlye:LEFT ', this.shrimp.x_position, 'Offset ', this.canvasOffset.x)
      //console.log('Im infoStlye:TOP ', this.shrimp.y_position, 'Offset ', this.canvasOffset.y)
      return {
        position: 'absolute',
        left: `${this.shrimp.x_position + this.canvasOffset.x -50}px`,
        top: `${this.shrimp.y_position + this.canvasOffset.y -100}px`,
        // Weitere Stilangaben hier
      };
    },
    isActiveShrimp() {
      return this.shrimp.id === this.currentShrimpId;
    },
    activeIndicatorUrl() {
      return 'http://localhost:8080/assets/active_indicator.png';
    },
    targetIndicatorUrl() {
      return 'http://localhost:8080/assets/target_indicator.png';
    },

  },
  methods: {
    onShrimpImageLoad() {
    //  this.updateWeaponAngle(this.weaponAngle);
    },
    updateWeaponAngle(newAngle) {
      // Hier den Winkel aktualisieren
      this.weaponAngle = this.clampWeaponAngle(newAngle);


      // Den geänderten Winkel an Board.vue senden
     // this.$emit('updateWeaponAngle', newAngle);
    },
    weaponAnglePlus() {
      console.log("Winkel vorher: ", this.weaponAngle);
      const newAngle = (this.weaponAngle + 5) % 360;
      this.$emit('updateWeaponAngle', newAngle);
      console.log("Winkel nachher: ", this.weaponAngle);

    },
    weaponAngleMinus() {
      console.log("Winkel vorher: ", this.weaponAngle);
      if (this.weaponAngle > 5) {
        const newAngle = (this.weaponAngle - 5 + 360) % 360;
        this.$emit('updateWeaponAngle', newAngle);
        console.log("Winkel nachher: ", this.weaponAngle);
      }
   },
    clampWeaponAngle(angle) {
      if (this.shrimp.lookingRight) {
        // Blickrichtung nach rechts: Winkel zwischen 0 und 180 Grad
        return Math.min(Math.max(angle, 0), 180);
      } else {
        // Blickrichtung nach links: Winkel zwischen 180 und 360 Grad
        return Math.min(Math.max(angle, 180), 360);
      }
    },
    showMyShrimpShrimp() {
      console.log("Mein Shrimp unter Shrimp: ", this.shrimp);
    },
  },
};
</script>

<style scoped>
.info-box {
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  text-align: center;
}
.shrimp-name {
  font-weight: bold;
}
.shrimp-hitpoints {
  color: #666;
}
.active-indicator {
  position: absolute;
  width: 20px;
  height: auto;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
}

</style>