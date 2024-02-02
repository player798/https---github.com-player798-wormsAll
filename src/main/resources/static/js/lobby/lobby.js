let stompClient = null;
let messageForm = document.querySelector('#messageForm');
let messageInput = document.querySelector('#message');
let messageArea = document.querySelector('#message_container');
let onlineUserArea = document.querySelector('#chat_user_container')
let connectingElement = document.querySelector('#client_status');
let message_datasource = [];
let gameInviteCache = [];       // Cache für die Nachricht mit der Einladung

function connect() {
        let socket = new SockJS('/register_socket');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, onConnected, onError);
}

function onError() {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}
function onConnected() {
    // Abonnieren der Lobby-Nachrichten
    stompClient.subscribe('/chat/messages', onMessageReceived);

    // Abonnieren persönlicher Spieleinladungen
    stompClient.subscribe('/chat/messages/' + player.id, onGameInvite);

    // Beim Server als Online anmelden
    stompClient.send("/shrimps/socket.addUser", {}, {});

    // Fetching Messages
    getChatHistory();

    // Fetching OnlineUsers
    getCurrentUsers();

    connectingElement.innerText = 'Connected!';
}

function onGameInvite(event) {
    const myModalElement = document.getElementById('inviteModalDialog');
    const nameElement = document.getElementById('senderName');
    gameInviteCache = JSON.parse(event.body);

    nameElement.innerText = gameInviteCache.sender.username;

    myModalElement.style.display = 'block';
}

function closeModal() {
    const myModalElement = document.getElementById('inviteModalDialog');
    myModalElement.style.display = 'none';
}

function denyInvite() {
    const myModalElement = document.getElementById('inviteModalDialog');
    myModalElement.style.display = 'none';
    const gameId = gameInviteCache.gameInvite.id;

    stompClient.send("/shrimps/" + gameId + "/denyInvite", gameInviteCache);
    gameInviteCache = undefined;
}

function acceptInvite() {
    const gameId = gameInviteCache.gameInvite.id;

    stompClient.send("/shrimps/" + gameId + "/acceptInvite", {}, JSON.stringify(gameInviteCache));

    const url = "http://localhost:8080/game/" + gameId + "/joinSpring";
    window.location.replace(url);
}

function getChatHistory() {
    fetch('http://localhost:8080/lobby/getAllLobbyChat')
        .then(response => response.json())
        .then (data => addPreviousMessagesToDatasource(data))
        .catch(error => console.log(error));
}

function getCurrentUsers() {
    fetch('http://localhost:8080/lobby/onlineUsers')
        .then(response => response.json())
        .then (data => updateOnlineUserElement(data))
        .catch(error => console.log(error));
}

function updateOnlineUserElement(users) {

    // Alle Einträge entfernen
    onlineUserArea.innerHTML = "";

    console.log("User found online: ");
    console.log(users);

    // Einen Eintrag je User hinzufügen
    for (let i=0; i < users.length; i++) {
        let user = users[i];

        let username = user.username;
        console.log("Adding " + username);

        const userElement = document.createElement("div");
        userElement.innerText = username;
        userElement.style.height = '1.5 em';
        userElement.style.border = ' 1px solid black';
        userElement.style.margin = '0.1 em';
        userElement.style.width = '10 em';

        onlineUserArea.appendChild(userElement);
    }
}

function addPreviousMessagesToDatasource(data) {
    message_datasource = message_datasource.concat(data);
    message_datasource.sort(function(a,b) {
       return a.timestamp - b.timestamp;
    });

    refreshChat();
}

function refreshChat() {
    clearChat();
    for (let i = 0; i < message_datasource.length; i++) {
        let message = message_datasource[i];
        console.log("Message: " + message);
        addMessageToChat(message);
    }
}

function clearChat() {
    messageArea.innerHTML = "";
}

function addMessageToChat(message) {
    let newMessageElement = document.createElement('li');

    const timePart = document.createElement('span');
    let time = new Date(message.timestamp);
    timePart.innerText =
        time.getDate().toString().padStart(2, '0') + "." +
        (time.getMonth() + 1).toString().padStart(2, '0') + "." +
        time.getFullYear().toString() + " " +
        time.getHours().toString().padStart(2, '0') + ":" +
        time.getMinutes().toString().padStart(2, '0');
    timePart.style.fontStyle = 'italic';
    timePart.style.fontSize = 'smaller';

    let userPart = document.createElement('span');
    userPart.innerText = " " + message.sender.username + ": ";

    let messagePart = document.createElement('span');
    messagePart.innerText = message.text;

    newMessageElement.appendChild(timePart);
    newMessageElement.appendChild(userPart);
    newMessageElement.appendChild(messagePart);
    messageArea.appendChild(newMessageElement);
}

function onMessageReceived(payload) {
    let message = JSON.parse(payload.body);
    addMessageToChat(message);

    let currentDate = new Date();
    let timeString = currentDate.getHours().toString().padStart(2, '0') + ":" +
        currentDate.getMinutes().toString().padStart(2, '0');
    connectingElement.innerText = "Server-Nachricht empfangen ... " + timeString;
}
function sendMessage(event) {
    let messageContent = messageInput.value.trim();
    if(messageContent && stompClient) {

        let message = {
            id: null,
            sender: player,
            receiver: null,
            gameInvite: null,
            timestamp: Date.now(),
            text: messageInput.value,
        };
        stompClient.send("/shrimps/socket.sendMessage", {}, JSON.stringify(message));
        messageInput.value = '';
    }
    event.preventDefault();
}

connect();
messageForm.addEventListener('submit', sendMessage, true);
setInterval(getCurrentUsers, 15000);