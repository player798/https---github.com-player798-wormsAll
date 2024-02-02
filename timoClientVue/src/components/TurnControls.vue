<template>
  <div>
    <button @click="reload">Reload</button>

    <p>TurnControl: Prop Current Shrimp: {{ currentShrimpProp }}</p>
  </div>
  <div>
    <button @click="setReady">Set Ready</button>
  </div>
</template>

<script>
import {useGameStore} from "@/stores/gameStore.js";
import {getMyPlayer} from "@/services/Api.js";
import {latestGameObject, refreshGame, stompClient} from "@/services/WebSocketService.js";
import {watch} from "vue";



export default {

  data() {
    return {
      controls: false,
      gameId: null,
      currentPlayerIdx: null,
      currentShrimpIdx: null,
      targetIndicatorRotation: 0,
    }
  },
  props: {
    currentShrimpId: null,
    weaponAngle: 0,
    gameStoreData: Object,
    currentShrimpProp: Object,
    currentWeaponId: 1,
  },
  created() {
    this.gameId = this.gameStoreData.id;
    console.log('TurnControls: Im created ist this.gameStoreData: ', this.gameStoreData)
    this.currentPlayerIdx = this.gameStoreData.currentPlayerIdx;
    this.currentShrimpIdx = this.gameStoreData.currentShrimpIdx;
  },
  async mounted() {
    window.addEventListener('keydown', this.handleGlobalKeyPress);
    const myPlayer = await getMyPlayer();

    console.log('Turn: getMyPlayer ', myPlayer);
    const myId = myPlayer.id;
    console.log('Turn: getMyPlayer ', myId);
    watch(() => latestGameObject.value, (value) => {
      if (value) {
        const currentPlayerId = value.currentPlayerId;
        if (currentPlayerId === myId) {
          this.controls = true;
          console.log("Steuerung für Player ", myId, " aktiviert")
        } else {
          this.controls = false
          console.log("Steuerung für Player ", myId, " deaktiviert")
        }
      }
    })
    refreshGame(this.gameId);

  },
  beforeDestroy() {
    window.removeEventListener('keydown', this.handleGlobalKeyPress);
  },
  methods: {

    handleGlobalKeyPress(event) {

      if (!this.controls) return;

      let direction = '';
      let fireWeapon = '';
      if (event.key === 'w' || event.key === 'ArrowUp') {
        console.log("Keydown Event erhalten: ", event.key);// Bewegung nach oben
        console.log("TC vor Event: ", this.weaponAngle);
        const newWeaponAngle = this.weaponAngle -5;
        this.$emit('updateWeaponAngle', newWeaponAngle, this.shrimp);
        console.log("TC nach Event: ", this.weaponAngle);
      } else if (event.key === 'a' || event.key === 'ArrowLeft') {
        console.log("Bewegungsevent empfangen: ",direction);
        console.log("Keydown Event erhalten: ", event.key);
        direction = 'LEFT';
      } else if (event.key === 's' || event.key === 'ArrowDown') {
        console.log("Keydown Event erhalten: ", event.key);
        console.log("TC vor Event: ", this.weaponAngle);
        const newWeaponAngle = this.weaponAngle +5;
        this.$emit('updateWeaponAngle', newWeaponAngle, this.shrimp);
        console.log("TC nach Event: ", this.weaponAngle);
      } else if (event.key === 'd' || event.key === 'ArrowRight') {
        console.log("Keydown Event erhalten: ", event.key, "für ShrimpProp: " , this.currentShrimpProp);
        direction = 'RIGHT';
        console.log("Bewegungsevent empfangen: ",direction);
      } else if (event.key === ' ') {
        event.preventDefault();
        console.log("Keydown Event erhalten: ", event.key);
        fireWeapon = true;
        console.log("Bewegungsevent empfangen: ",direction);
      }
      else if (event.key === ' ') {
        event.preventDefault();
        console.log("Keydown Event erhalten: ", event.key);
        fireWeapon = true;
        console.log("Bewegungsevent empfangen: ",direction);
      } else if (event.key === 'q') {
        console.log("Keydown Event erhalten: ", event.key);
        this.currentWeaponId = 1;
      } else if (event.key === 'r') {
        console.log("Keydown Event erhalten: ", event.key);
        this.currentWeaponId = 1; //Hier wird noch die ID gegen die nächste ausgetauscht
        this.$emit('updateWeaponProp', this.currentWeaponId, this.shrimp);
      }
      // Begrenzen Sie die Rotation auf den gültigen Bereich von 0-360 Grad
      this.targetIndicatorRotation = (this.targetIndicatorRotation + 360) % 360;


      if (direction) {
        const shrimpDTO = this.currentShrimpProp;
        const playerMoveDTO = { shrimpDTO, moveDirection: direction }
        console.log('PlayerMoveDTO', playerMoveDTO);
        stompClient.send("/shrimps/" + this.gameId + "/makeMove", {}, JSON.stringify(playerMoveDTO));
      }

      if (fireWeapon) {
        const shrimpDTO = this.currentShrimpProp
        const weaponAngle = this.weaponAngle;
        const weaponId = this.currentWeaponId
        const fireWeaponDTO = { id: weaponId, angle: weaponAngle, force: 100, shrimpDTO}
        console.log("Weapon fired in TurnControls!: ", fireWeaponDTO);
        stompClient.send("/shrimps/" + this.gameId + "/fireWeapon", {}, JSON.stringify(fireWeaponDTO));

        fireWeapon = false;
      }
      },
      reload() {
      console.log('Reload gedrückt');
      const gameId = this.gameStoreData.id;
      refreshGame(gameId)
    },
    setReady() {
      const gameId = this.gameId;
      console.log("Set Ready gedrückt für:", gameId)
      stompClient.send("/shrimps/" + gameId + "/setReady", {}, {})
}
  }

}



</script>
<style scoped>

</style>