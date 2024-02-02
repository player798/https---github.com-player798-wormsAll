let stompClient                 = null;
let playerStatBlock     = document.getElementById("playerStatsContainer");
const btnReadyElement   = document.getElementById("btnReady");
const btnInviteElement  = document.getElementById("btnInvite");
const btnStartGameElement = document.getElementById("btnStartGame");

let playersOnline              = [];

function getPlayerNo() {
    let result = -1;
    let length = game.playerStatisticsList.length;

    for (let i = 0; i < length; i++) {
        let playerStatistic = game.playerStatisticsList[i];
        if (playerStatistic.playerDTO.id === player.id) {
            result = i + 1;
        }
    }

    // Bestimmen, ob Spiel gestartet werden kann
    let allReady = true;
    // Prüfen ab 2tem Element, erstes ist Host
    for (let i = 1; i < length; i++) {
        let currentStatistic = game.playerStatisticsList[i];
        if (currentStatistic.status === 'NOT_READY') {
            allReady = false;
        }
    }

    if (allReady && (length > 1) && (game.playerStatisticsList[0].status === "READY")) {
        // Alle Spieler sind "Ready", auch der Host => Host hat auf
        // "Spiel starten geklickt" => weiterleiten auf playGame

        window.location.replace("http://localhost:8080/game/" + game.id + "/playSpring");
    }

    if (result === 1) {
        // Setze Buttons für Host
        btnInviteElement.classList.remove("invisible");
        btnStartGameElement.classList.remove("invisible")
        btnStartGameElement.disabled = true;

        if (length > 1) {
            // Mindestens 2 Spieler
            btnStartGameElement.disabled = !allReady;
        }
    } else {
        // Setze Buttons für andere
        btnReadyElement.classList.remove("invisible")
    }

    return result;
}

function initialize() {
    let playerNo = getPlayerNo();

    let playerNoElement = document.getElementById("playerNo")
    playerNoElement.innerText = playerNo.toString();
    buildPlayerStats();
}

function buildPlayerStats() {
    const length = game.playerStatisticsList.length;

    // Bisherige Einträge entfernen
    playerStatBlock.innerHTML = "";

    // Neue Blöcke erzeugen
    for (let i = 0; i < length; i++) {
        let currentPlayerStatistic = game.playerStatisticsList[i];
        let currentParticipant = currentPlayerStatistic.playerDTO;

        let mainBlock = document.createElement("div");
        mainBlock.classList.add("player-slot");

        let nameSpan = document.createElement('span');
        nameSpan.innerText = "Name: " + currentParticipant.username;

        let spacer = document.createElement('span');
        spacer.innerText = "Team";
        spacer.classList.add('vbox');

        for (let i = 0; i < currentPlayerStatistic.shrimpDTOList.length; i++) {
            let currentShrimp = currentPlayerStatistic.shrimpDTOList[i];

            let shrimpSpan = document.createElement('span');
            shrimpSpan.innerText = "Shrimp " + (i + 1).toString() + ": " + currentShrimp.name;
            spacer.appendChild(shrimpSpan);
        }

        mainBlock.appendChild(nameSpan);
        mainBlock.appendChild(spacer);
        playerStatBlock.appendChild(mainBlock);

    }
}

function getPlayerStats(participant) {

    let playerStatCount = game.playerStatisticsList.length;
    let foundStats = undefined;

    for (let i = 0; i < playerStatCount; i++) {
        if (game.playerStatisticsList[i].player.id === participant.id) {
            foundStats = game.playerStatisticsList[i];
        }
    }

    return foundStats;
}

function connect() {
    let socket = new SockJS('/register_socket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, onConnected, onError);
}

function onConnected() {
    let gameId = game.id;
    stompClient.subscribe('/chat/game/' + gameId + "/gameChanged", onGameChanged);
}

function onGameChanged(event) {

    console.log("OnGameChange wurde aufgerufen");

    if (event !== undefined) {
        console.log("Event Daten gefunden: ");
        console.log(event.toString());
        game = JSON.parse(event.body);
        initialize();
    }
}

function onError(err) {
    console.log('Fehler: ' + err.toString());
}

function invitePlayerDialog() {
    fetch('http://localhost:8080/lobby/onlineUsers')
        .then(response => response.json())
        .then (data => showPlayersInDialog(data))
        .catch(error => console.log(error));
}

function closeModal() {
    const modal = document.getElementById('inviteModalDialog');
    modal.style.display = 'none';
}

function showPlayersInDialog(data) {
    const length = data.length;
    const mainListElement = document.getElementById('playerList');
    this.playersOnline = data;

    // Leeren der ggf. bereits vorhandenen Liste
    mainListElement.innerHTML = "";

    for (let i = 0; i < length; i++) {
        // Alle vorhandenen Spieler auflisten
        let liElement = document.createElement('li');
        let spanElement = document.createElement('span');
        let currentUser = data[i];
        spanElement.innerText = currentUser.username;
        liElement.onclick = function() { invitePlayerWithId(currentUser.id); };
        liElement.appendChild(spanElement);

        mainListElement.appendChild(liElement);
    }

    const myModalElement = document.getElementById('inviteModalDialog');
    myModalElement.style.display = 'block';
}

function invitePlayerWithId(playerId) {
    console.log('Send Invite to player Id: ' + playerId);

    sendInviteToPlayer(playerId);

    const myModalElement = document.getElementById('inviteModalDialog');
    myModalElement.style.display = 'none';
}

function findPlayer(id) {
    const length = this.playersOnline.length;
    let result = null;

    for (let i = 0; i < length; i++) {
        let currentEntry = this.playersOnline[i];
        if (currentEntry.id === id) {
            result = currentEntry;
        }
    }

    return result;
}

function sendInviteToPlayer(playerId) {
    let gameId = game.id;

    let receivingPlayer = findPlayer(playerId);
    if (receivingPlayer != null) {
        let message = {
            id          : null,
            sender      : player,
            receiver    : receivingPlayer,
            gameInvite  : game,
            timestamp   : Date.now(),
            text        : null
        };
        //console.log('ID: ', id);
        console.log('Sending Player: ', player);
        console.log('Receiving Player: ', receivingPlayer);
        console.log('gameInvite: ', game);
        console.log('timestamp: ', Date.now());
        //console.log('text: ', text);

        stompClient.send("/shrimps/" + gameId + "/sendInvite", {}, JSON.stringify(receivingPlayer));
    }
}

function requestRefresh() {

    const gameId = game.id;
    stompClient.send("/shrimps/" + gameId + "/refreshGame", {}, {});
}

function setReadyState() {

    const gameId = game.id;
    stompClient.send("/shrimps/" + gameId + "/setReady", {}, {});
}

initialize();
connect();