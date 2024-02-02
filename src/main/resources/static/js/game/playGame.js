/* Diese Datei soll die eingesetzten Verfahren verdeutlichen, ist aber selbst
 * hart-codiert auf 1366x768 Auflösung. Dies müsste für den Produktivbetrieb
 * in der Art angepasst werden, dass auch andere Formate unterstützt werden.
 */

let stompClient;
const canvasElement = document.getElementById("myCanvas");
const canvasContext = canvasElement.getContext("2d");
const levelForeground = document.getElementById("imgForeground");
const levelBackground = document.getElementById("imgBackground");
const levelMask = document.getElementById("imgMask");
const activeIndicator = document.getElementById("imgActiveIndicator");
const targetIndicator = document.getElementById("imgTargetIndicator");
const shrimpLookingRight = document.getElementById("imgShrimpNormal");
const shrimpLookingLeft = document.getElementById("imgShrimpReverse");

// Zum Warten auf das Laden von Bildern
let imagesToLoad = 0;

let controlsEnabled = false;
let weaponAimAngle = 90;           // Standard => gerade vor

let finishedAnimating = true;
let animationStep = 0;
let animationTimer;

function buttonHandler() {
    console.log("Button pressed");
}

function getConnected() {
    console.log("Etabliere WebSocket-Verbindung ... ");

    let socket = new SockJS('/register_socket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, onConnected, onError);
}

function onConnected(event) {
    let gameId = game.id;
    stompClient.subscribe('/chat/game/' + gameId + "/gameChanged", onGameChanged);
    stompClient.subscribe('/chat/game/' + gameId + "/weaponFired", onWeaponFired);
    initializeScreen();
}

function onWeaponFired(event) {
    if (event !== undefined) {
        console.log("Fired Weapon Data received ...");
        let data = JSON.parse(event.body);
        console.log('Data: ' + data);

        // Todo: Waffenschuss und Schaden animieren
        finishedAnimating = false;
        animationTimer = window.setInterval(animate, 100);
    }
}

function animate() {
    console.log("Animate has been called ... ");

    // Waffenergebnis animieren
    // Todo: machen
    // ...

    // Alle Schritte fertig
    finishedAnimating = true;
    animationStep = 0;
    window.clearInterval(animationTimer);

    // Ready an Server melden
    const gameId = game.id;
    stompClient.send("/shrimps/" + gameId + "/setReady", {}, {});
}

function onGameChanged(event) {
    if (event !== undefined) {
        console.log("Event Daten gefunden: ");
        console.log(event.toString());
        game = JSON.parse(event.body);

        imagesToLoad += 1;
        levelMask.src = 'data:image/png;base64,' + game.foregroundMask;
    }
}

function initializeScreen() {
    let levelForegroundFileName = "level_" + game.levelForeground + "_foreground_fill.png";
    let levelBackgroundFileName = "level_" + game.levelBackground + "_background.png";

    // Das Setzen einer neuen Quelle für Bilddateien ist async
    // daher müssen wir warten bis die Bilder geladen sind
    // Gesetzte Bilder: 3

    levelForeground.onload = function() { imageLoaded(); }
    levelBackground.onload = function() { imageLoaded(); }
    levelMask.onload = function() { imageLoaded(); }

    imagesToLoad = 3;

    levelForeground.src = "http://localhost:8080/levels/" + levelForegroundFileName;
    levelBackground.src = "http://localhost:8080/levels/" + levelBackgroundFileName;
    levelMask.src = 'data:image/png;base64,' + game.foregroundMask;
}

function imageLoaded() {
    if (imagesToLoad > 1) {

        imagesToLoad--;
    } else {

        imagesToLoad--; // Letzte Bild wurde geladen, es kann losgehen
        this.draw();
    }
}

function draw() {
    /* ToDo: Neuzeichnen der gesamten Oberfläche, dies könnte auf viele Wege angepasst werden,
  * beziehungsweise optimiert werden in dem z.B. der durch Änderungen invalidierte Teil
  * dazu müsste dann zum Beispiel nur im onGameChanged-Event der alte Zustand von game
  * abgespeichert werden dann könnte hier eine Ermittlung der Änderungen stattfinden.
  */
    // invalidateAreas(areaArray);
    // Alles invalidieren:
    canvasContext.clearRect(0, 0, 1366, 768);
    let clippedImage = clipImageWithMask(levelForeground, levelMask, levelBackground);

    canvasContext.putImageData(clippedImage, 0, 0);
    placeShrimps();
    if (player.id === game.currentPlayerId) {
        decorateActiveShrimp();
        controlsEnabled = true;
    } else {
        controlsEnabled = false;
    }
}

function decorateActiveShrimp() {
    const activePlayerIdx = game.currentPlayerIdx;
    const activeShrimpIdx = game.currentShrimpIdx;

    const activeShrimp = game.playerStatisticsList[activePlayerIdx].shrimpDTOList[activeShrimpIdx];
    const xOffset = -10;
    const yOffset = -90;

    let x = activeShrimp.x_position + xOffset;
    let y = activeShrimp.y_position + yOffset;

    canvasContext.drawImage(activeIndicator, x, y, 20, 20);

    const calcAngle = activeShrimp.lookingRight ? weaponAimAngle : 360 - weaponAimAngle;
    const newAngle = Math.floor((360 - (calcAngle - 90)) % 360);
    const newAngleRad = newAngle * (Math.PI / 180);
    const circleX = 70;
    const circleY = 0;

    // Volle Formel zum Verständnis, da circleY = 0 ist fallen entsprechende Teile weg
    const xOffsetCalc = Math.floor((circleX * Math.cos(newAngleRad)) - (circleY * Math.sin(newAngleRad)));
    const yOffsetCalc = Math.floor((circleY * Math.cos(newAngleRad)) + (circleX * Math.sin(newAngleRad)));
    console.log("Angle: " + calcAngle);
    console.log("xOffset: " + xOffsetCalc);
    console.log("yOffset: " + yOffsetCalc);

    x = (activeShrimp.x_position + 20) + xOffsetCalc;
    y = (activeShrimp.y_position - 20) - yOffsetCalc;   // - Weil oben und unten vertauscht

    canvasContext.drawImage(targetIndicator, x, y, 20, 20);
}

function clipImageWithMask(image, mask, background) {
    let offscreenCanvas = new OffscreenCanvas(1366, 768);
    let offscreenContext = offscreenCanvas.getContext("2d");

    // Auslesen der Bilddaten
    offscreenContext.drawImage(image, 0 , 0);
    let imageData = offscreenContext.getImageData(0, 0, 1366, 768);

    // Löschen der Canvaoberfläche
    offscreenContext.clearRect(0, 0, 1366, 768);

    // Auslesen der Maskendaten
    offscreenContext.drawImage(mask, 0, 0);
    let maskData = offscreenContext.getImageData(0, 0, 1366, 768);

    // Erneutes Löschen
    offscreenContext.clearRect(0,0,1366, 768);

    // Auslesen der Hintergrunddaten
    offscreenContext.drawImage(background, 0, 0);
    let backgroundData = offscreenContext.getImageData(0, 0,1366, 768);

    // Erneutes Löschen
    offscreenContext.clearRect(0,0,1366, 768);

    for (let i = 0; i < imageData.data.length; i += 4) {
        let maskAlpha = maskData.data[i+3];

        if (maskAlpha === 0) {
            // Maske ist an diesem Pixel transparent also Werte des Vordergrunds überschreiben
            imageData.data[i]   = backgroundData.data[i];     // Rot
            imageData.data[i+1] = backgroundData.data[i+1];   // Grün
            imageData.data[i+2] = backgroundData.data[i+2];   // Blau
            imageData.data[i+3] = backgroundData.data[i+3];   // Alpha
        }
    }

    offscreenContext.putImageData(imageData, 0 , 0);

    return offscreenContext.getImageData(0,0, 1366, 768);
}

function placeShrimps() {
    const playerStatisticCount = game.playerStatisticsList.length;

    // Todo: Für Iterator umbauen (ForEach)
    for (let i = 0; i < playerStatisticCount; i++) {
        const currentPlayerStatistic = game.playerStatisticsList[i]
        const shrimpCount = currentPlayerStatistic.shrimpDTOList.length;
        const teamColor = getColorForTeam(i);
        for (let j = 0; j < shrimpCount; j++) {
            const currentShrimp = currentPlayerStatistic.shrimpDTOList[j];
            const x = currentShrimp.x_position - 20; //currentShrimp.lookingRight ? currentShrimp.x_position : currentShrimp.x_position - 40;
            const y = currentShrimp.y_position - 40;

            let shrimpImage = currentShrimp.lookingRight ? shrimpLookingRight : shrimpLookingLeft;
            canvasContext.drawImage(shrimpImage, x, y, 40, 40);
            drawLifeBar(x , (y - 20), currentShrimp.name, currentShrimp.hitpoints, teamColor);
        }
    }
}

function getColorForTeam(teamNumber) {
    let result = "dimgrey";
    // 4 Teams max
    switch (teamNumber) {
        case 0:
            result = "green";
            break;
        case 1:
            result = "firebrick";
            break;
        case 2:
            result = "steelblue";
            break;
        case 3:
            result = "darkgoldenrod"
    }

    return result;
}

function drawLifeBar(x, y, name, hitpoints, teamColor) {
    // Balkenanzeige 40x20 pixel, x, y = links unten
    canvasContext.fillStyle = "black";
    canvasContext.fillRect(x, y-5, 40, 15);
    canvasContext.fillStyle = teamColor;

    // Lebensanzeige soll prozentual Darstellen wie viel Leben der jeweilige
    // Shrimp noch übrig hat
    const targetWidth = Math.floor((hitpoints / 100) * 38);
    canvasContext.fillRect(x+1, y-4, targetWidth, 13);

    canvasContext.fillStyle = "white";
    canvasContext.fillText(name, x+3, y+5, 36);
}

function onError(event) {
    // ToDo: Fehlerbehandlung
}

function handleKeyboardInput(event) {
    // WASD - für Bewegung / Ziel-Winkel (A Links, D Rechts, A Winkel nach oben, S unten)
    // QE - für Waffenwinkel
    // Space - für Sprung [sobald verfügbar]
    // RF - Waffenkraft, falls notwendig

    // Während Transitionen / warten auf Server-Antworten sollen keine Eingaben verarbeitet werden
    if (controlsEnabled) {
        const pressedKey = event.key;
        let direction = "";

        switch(pressedKey) {
            case "a":       // Gehe links
                direction = "LEFT";
                break;
            case "d":       // Gehe Rechts
                direction = "RIGHT";
                break;
            case "w":       // Ziele höher
                weaponAimAngle--;
                weaponAimAngle = Math.max(0, Math.min(weaponAimAngle, 180));
                this.draw();
                break;
            case "s":       // Ziele niedriger
                weaponAimAngle++;
                weaponAimAngle = Math.max(0, Math.min(weaponAimAngle, 180));
                this.draw();
                break;
            case " ":
                fireWeapon();
                break;
        }

        if (direction !== "") {

            sendMove(direction);
        }

    }
}

function fireWeapon() {
    // console.log("Firing ... ")
    const activePlayerIdx = game.currentPlayerIdx;
    const activeShrimpIdx = game.currentShrimpIdx;
    const activeShrimp = game.playerStatisticsList[activePlayerIdx].shrimpDTOList[activeShrimpIdx];
    const weaponAngle = activeShrimp.lookingRight ? weaponAimAngle : 360 - weaponAimAngle;

    let fireWeaponDTO = {
        id: 1,
        angle: weaponAngle,
        force: 100,
        shrimpDTO: activeShrimp
    }

    const gameId = game.id;
    stompClient.send("/shrimps/" + gameId + "/fireWeapon", {}, JSON.stringify(fireWeaponDTO));
}

function sendMove(direction) {
    // Deaktivieren der Steuerung, solange auf Serverantwort gewartet wird
    controlsEnabled = false;
    let activePlayerIdx = game.currentPlayerIdx;
    let activeShrimpIdx = game.currentShrimpIdx;

    let currentShrimp = game.playerStatisticsList[activePlayerIdx].shrimpDTOList[activeShrimpIdx];

    let playerMoveDTO = {
        shrimpDTO: currentShrimp,
        moveDirection: direction
    }

    let gameId = game.id;

    stompClient.send("/shrimps/" + gameId + "/makeMove", {}, JSON.stringify(playerMoveDTO));
}

window.addEventListener('keydown', (event) => { handleKeyboardInput(event); }, true);
getConnected();