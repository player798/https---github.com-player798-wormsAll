<template>
<h1> Sie wurden erfolgreich ausgeloggt!</h1>
</template>

<script>
import {logout} from "@/services/Api.js";

export default {
  name: "LogOutView",
  mounted() {
    this.waitForRedirect();
  },
  methods: {
    waitForRedirect() {
      logout();
      setTimeout(() => {
        console.clear();
            location.replace("http://localhost:8080/login");
          }
      ,5000);
    }
  }
}
</script>



<style scoped>

</style>