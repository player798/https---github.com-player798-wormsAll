<template>
  <div id="headerMenu" class="navbar navbar-light bg-light">
    <span class="navbar-brand mb-0 h1">Lobby - Chat</span>
    <div class="d-flex">
      <router-link to="/setupGame" class="nav-link">Start New Game</router-link>
      <router-link to="/logout" class="nav-link">Logout</router-link>
    </div>
  </div>
</template>
<script>
export default {
  // Your component logic here
};
</script>

<style scoped>
#headerMenu {
  background-color: #f8f9fa; /* Light gray background color */
  padding: 10px; /* Add padding for better appearance */
  margin-bottom: 20px; /* Add margin to separate from the rest of the content */
  display: flex;
  justify-content: space-between;
  align-items: center;
}

#headerMenu h1 {
  margin: 0; /* Remove margin to prevent spacing issues */
}

#headerMenu .d-flex a {
  margin-right: 20px; /* Add margin between menu items */
}
</style>