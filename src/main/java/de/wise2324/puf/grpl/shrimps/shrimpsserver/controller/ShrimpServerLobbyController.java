package de.wise2324.puf.grpl.shrimps.shrimpsserver.controller;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.dto.HistoryDTO;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.dto.LeaderboardDTO;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Message;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Player;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.service.LobbyService;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.service.PlayerDetails;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.service.PlayerDetailsService;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

/***
 * Der LobbyController stellt die Dienste des LobbyService
 * per RestAPI zur Verfügung unter dem Präfix "/lobby". Die
 * Dienste umfassen hierbei das Senden und Empfangen von
 * Nachrichten, direkte sowie allgemeine. Neu eintreffende
 * Nachrichten werden hierbei ebenso über einen WebSocket
 * bekannt gegeben.
 */
@Controller
public class ShrimpServerLobbyController {

    /***
     * Service für die angebotenen Dienste
     */
    @Autowired
    public LobbyService lobbyService;

    /***
     * Service für spielerbezogene Dienste
     */
    @Autowired
    public PlayerDetailsService playerService;

    @Autowired
    public GameService gameService;

    /***
     * Initialisiert mit einer neuen Service-Instanz
     */
    ShrimpServerLobbyController() {
        this.lobbyService = new LobbyService();
    }

    /***
     * Initialisiert mit übergebener Service-Instanz
     * @param service   Die übergebene Service-Instanz
     */
    ShrimpServerLobbyController(LobbyService service) {
        this.lobbyService = service;
    }

    /***
     * Zeigt die Lobby Seite an
     * @param model  Daten für das Binding an die Seite
     * @return  Die Lobby Seite
     */
    @GetMapping("/lobby")
    public String viewLobby(Model model, Principal principal) {
        model.addAttribute("lobby_messages", new Message());
        model.addAttribute("player",
                ((PlayerDetails) playerService.loadUserByUsername(principal.getName())).getPlayer());
        return "lobby";
    }

    /***
     * REST API Schnittstelle die einem die eigenen Daten
     * zurückgibt.
     * @param principal Die Authentifizierungsdaten des Users
     * @return  Player-Objekt des authentifizierten Users
     */
    @GetMapping("/myPlayer")
    @ResponseBody
    public Player getMyPlayer(Principal principal) {
        Player result;
        result = ((PlayerDetails) playerService.loadUserByUsername(principal.getName())).getPlayer();
        return result;
    }

    /***
     * Gibt die persönlichen Nachrichten des eingeloggten Users aus
     * @param principal     Der eingeloggte Nutzer
     * @return  Liste von Nachrichten
     */
    @GetMapping("/lobby/get_private_messages/")
    public List<Message> getPrivateMessages(Principal principal) {

        return lobbyService.getMessagesFor(principal.getName());
    }

    /***
     * Ermöglicht das Versenden von Nachrichten. Sender muss dabei
     * zwingend gefüllt sein. Durch die gesetzten Attribute wird die
     * Natur der Nachricht bestimmt.
     * Fall 1, Sender und Empfänger != null, GameInvite == null:
     * Es handelt sich um eine direkte Nachricht
     * Fall 2, Sender, Empfänger und GameInvite != null:
     * Eine Einladung in eine Partie
     * Fall 3, Sender != null, Empfänger und GameInvite == null:
     * Eine Nachricht für die Lobby
     * Fall 4, Sender != null, Empfänger = null und GameInvite != null:
     * Nachricht für Partie-Chat
     * @param message   Die Nachricht die übermittelt wird
     * @return  String  Ok falls valide, sonst Err
     */
    @PostMapping("/lobby/send_message")
    public String sendMessage(Message message) {
        // ToDo: Handling sendMessage(...) returns with switch case
        //  returning Err or Ok instead of internal result maybe
        //  even notify Clients via WebSocket
        return lobbyService.sendMessage(message);
    }

    /***
     * Gibt die 20 neusten Einträge im Lobby-Chat zurück
     * @return  List of Message, Die Nachrichtenliste
     */
    @GetMapping("/lobby/top20LobbyChat")
    @ResponseBody
    public List<Message> getTop20LobbyChat() {
        return lobbyService.getLobbyMessagesLast20();
    }

    /***
     * Gibt alle Nachrichten der Lobby zurück, sortiert von neuster
     * bis zur ältesten Nachricht.
     * @return  List of Message, Liste der Nachrichten
     */
    @GetMapping("/lobby/getAllLobbyChat")
    @ResponseBody
    public List<Message> getLobbyChat() {
        return lobbyService.getLobbyMessages();
    }

    /***
     * Gibt eine Liste aller, zurzeit in der Lobby eingeloggten Spieler zurück
     * @return  Liste der Spieler
     */
    @GetMapping("/lobby/onlineUsers")
    @ResponseBody
    public List<Player> getOnlineUsers() {
        return playerService.getOnlineUsers();
    }

    @GetMapping("/lobby/getPlayerHistory/{playerId}/")
    @ResponseBody
    public HistoryDTO getPlayerHistory(@PathVariable Long playerId) {
        HistoryDTO result = this.gameService.getHistoryFromId(playerId);

        return result;
    }


    /***
     * WebSocket Schnittstelle
     * Nachrichten die an /lobby/socket.sendMessage gesandt werden,
     * werden direkt auf /lobby/chat/Messages ausgegeben.
     *
     * @return List of Message, Liste aktueller Nachrichten
     */
    @MessageMapping("/socket.sendMessage")
    @SendTo("/chat/messages")
    public Message sendSocketMessage(@Payload Message message) {
        sendMessage(message);
        return message;
    }

    /***
     * Registrierungseinstieg für WebSocket Verbindungen für die Lobby.
     * Der Spieler wird der Lobby hinzugefügt, der Username wird der Session
     * hinzugefügt, eine Meldung zum Einloggen in der Lobby wird gesendet, sowie
     * der Online-Status in der Datenbank erfasst.
     * @return                  Nachricht für die Lobby (outgoing)
     */
    @MessageMapping("/socket.addUser")
    @SendTo("/chat/messages")
    public Message addUser(Principal principal) {
        Player player = ((PlayerDetails) this.playerService.loadUserByUsername(principal.getName())).getPlayer();
        Message message = new Message();
        message.setSender(player);
        String text = player.getUsername() + " hat den Chat betreten.";
        message.setText(text);
        message.setTimestamp(new Timestamp(System.currentTimeMillis()));
        // message soll nicht gespeichert werden

        this.playerService.loginUser(player);

        return message;
    }
    /***
     * API-Schnittstelle zum Abrufen der Bestenliste, soritert nach MOST WINS, LESS LOSSES,
     * MOST GAMESFINISHED
     * @return  Die generierte Abfrage als JSON
     */
    @GetMapping("/lobby/leaderboard")
    @ResponseBody
    public List<LeaderboardDTO> getLeaderboard() {
        return this.gameService.getPlayerLeaderboard();
    }
}
