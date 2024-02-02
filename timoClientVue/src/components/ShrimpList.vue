<template>
  <div>
    <Shrimp v-for="shrimp in shrimpList" :key="shrimp.id" :shrimp="shrimp" />
  </div>
</template>

<script>
import Shrimp from './Shrimp.vue';

export default {
  components: {
    Shrimp
  },
  props: {
    shrimpList: {
      type: Array,
      required: true
    }
  }
};
</script>

<style scoped>
/* Ihr CSS hier */
</style>
