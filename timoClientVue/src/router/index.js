import { createRouter, createWebHistory } from 'vue-router'
import LobbyView from "@/views/LobbyView.vue";
import CreateGameView from "@/views/CreateGameView.vue";
import JoinGameView from "@/views/JoinGameView.vue";
import PlayGameView from "@/views/PlayGameView.vue";
import LogOutView from "@/views/LogOutView.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'lobby',
      component: LobbyView
    },
    {
      path: '/setupGame',
      name: 'setupGame',
      component: CreateGameView
    },
    {
      path: '/playGame',
      name: 'playGame',
      component: PlayGameView
    },
    {
      path: '/joinGame',
      name: 'joinGame',
      component: JoinGameView
    },
    {
      path: '/logout',
      name: 'logout',
      component: LogOutView
    }
  ]
})

export default router
