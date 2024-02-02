import 'bootstrap/dist/css/bootstrap.min.css';  // Import Bootstrap CSS
import 'materialize-css/dist/css/materialize.min.css';  // Import Materialize CSS
import 'materialize-css';  // Initialize Materialize JS

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
