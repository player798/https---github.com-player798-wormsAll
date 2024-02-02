package de.wise2324.puf.grpl.shrimps.shrimpsserver.controller;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.dto.*;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.entity.OnlineStatus;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Game;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Player;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.repository.PlayerRepository;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.service.GameService;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.service.MessageService;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Objects;

/***
 * Controller für die Bereitstellung der auf das Spiel bezogenen
 * Dienste. Während die Initialisierung des Spiels, wie das Finden
 * von Spielern sowie die initiale Konfiguration werden hierbei per
 * REST API durchgeführt, während des Spiels findet die Client - Server
 * Kommunikation per WebSocket statt.
 */
@Controller
public class ShrimpServerGameController {

    private SimpMessagingTemplate template;

    /***
     * Service für bereitgestellte Dienste
     */
    @Autowired
    GameService gameService;

    @Autowired
    MessageService messageService;

    @Autowired
    PlayerService playerService;

    /***
     * Service für Spieler-basierte Dienste
     */
    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    public ShrimpServerGameController(SimpMessagingTemplate template) {
        this.template = template;
    }

    /***
     * Diese REST Schnittstelle bietet für den integrierten Spring Prototyp-Client
     * die Konfigurationsseite, um eine Spielpartie zu konfigurieren. Hierbei kann nur
     * der "Hostende"-Spieler Spielgrundeinstellungen ändern [game.hostingPlayer]. Die
     * anderen Spieler können die Namen Ihrer TeamShrimps ändern.
     *
     * @param model         Model um Attribute für Thymeleaf zu "injecten"
     * @param principal     Der Aufrufer dieser Methode
     * @return              Die Konfigurationsseite des Spiels
     */
    @RequestMapping(value = "/game/configureSpring", method = RequestMethod.GET)
    public String configureGame(Model model, Principal principal) {
        String username = principal.getName();
        Player player = playerRepository.findByEmail(username);

        Game game = gameService.createNewGame(player);
        model.addAttribute("player", this.playerService.getDTOFromObject(player));
        model.addAttribute("game", this.gameService.getDTOFromObject(game));

        return "configureGame";
    }

    @RequestMapping(value="/game/{gameId}/joinSpring", method = RequestMethod.GET)
    public String joinGame(@PathVariable Long gameId, Model model, Principal principal) {

        Player player = this.playerService.getPlayerByEmail(principal.getName());
        Game game = this.gameService.getGameFromId(gameId);

        model.addAttribute("player", this.playerService.getDTOFromObject(player));
        model.addAttribute("game", this.gameService.getDTOFromObject(game));

        return "configureGame";
    }

    /***
     * REST Schnittstelle für die Spielseite für den integrierten Spring-Thymeleaf-Client.
     * Auf dieser Seite findet das Spiel statt bis die Partie beendet wurde.
     *
     * @param gameId            Die Id des Spiels, welche als Pfadvariable für den Socket dient
     * @param model             Das Modell der Seite um Objekte für Thymeleaf zu injecten
     * @param principal         Der Aufrufer der Seite
     * @return                  Die HTML-Thymeleaf-Seite
     */
    @RequestMapping(value = "/game/{gameId}/playSpring", method = RequestMethod.GET)
    public String playGame(@PathVariable Long gameId, Model model, Principal principal) {

        Player player   = this.playerService.getPlayerByEmail(principal.getName());
        player.setStatus(OnlineStatus.Ingame);
        this.playerService.save(player);

        Game game       = this.gameService.getGameFromId(gameId);

        // Verhindern, dass das Spiel mehrfach gestartet wird
        GameDTO gameDTO;
        if (game.getPlayerStatisticsList().getFirst().getPlayer().getId().equals(player.getId())) {
            game            = this.gameService.startGame(game);
            gameDTO = this.gameService.getDTOFromObject(game);
            this.template.convertAndSend("/chat/game/" + gameId + "/gameChanged", gameDTO);
        } else {
            gameDTO = this.gameService.getDTOFromObject(game);
        }

        PlayerDTO playerDTO = this.playerService.getDTOFromObject(player);

        model.addAttribute("game", gameDTO);
        model.addAttribute("player", playerDTO);
        return "playGame";
    }

    /***
     * Diese REST-API Methode wird vom hostenden Spieler aufgerufen, um das Spiel zu beginnen.
     * Dies ist nur erforderlich, sofern es sich nicht um den integrierten Spring-Client handelt.
     *
     * @param gameId        Die Id des Spiels, dient als Identifikation der Partie
     * @param principal     Der Aufrufer des Services zur validierung der Berechtigung
     */
    @ResponseBody
    @RequestMapping(value = "/game/{gameId}/play", method = RequestMethod.GET)
    public GameDTO startGame(@PathVariable Long gameId, Principal principal) {

        Player player   = this.playerService.getPlayerByEmail(principal.getName());
        player.setStatus(OnlineStatus.Ingame);
        this.playerService.save(player);

        Game game       = this.gameService.getGameFromId(gameId);

        // Verhindern, dass das Spiel mehrfach gestartet wird
        GameDTO gameDTO;
        if (game.getPlayerStatisticsList().getFirst().getPlayer().getId().equals(player.getId())) {
            game        = this.gameService.startGame(game);
            gameDTO     = this.gameService.getDTOFromObject(game);
            this.template.convertAndSend("/chat/game/" + gameId + "/gameChanged", gameDTO);
        } else {
            gameDTO     = this.gameService.getDTOFromObject(game);
        }

        PlayerDTO playerDTO = this.playerService.getDTOFromObject(player);

        return gameDTO;
    }

    /***
     * REST Schnittstelle um ein Spiel zu erstellen, die neu erstellte Partie wird dann
     * zurückgegeben. Änderungen werden per /{gameId}/changeGame eingestellt. Diese
     * Schnittstelle wird nicht vom integrierten Client bedient.
     *
     * @return Game-Objekt inklusive der entsprechenden Id die für die Socket-Pfadangabe
     *         benötigt wird
     */
    @ResponseBody
    @RequestMapping(value = "/game/startNewGame", method = RequestMethod.GET)
    public GameDTO getGameId(Principal principal) {
        Player player = playerRepository.findByEmail(principal.getName());
        return this.gameService.getDTOFromObject(gameService.createNewGame(player));
    }

    /***
     * WebSocket - Schnittstelle um Änderungen der Konfiguration vorzunehmen. Die Änderungen werden
     * anschließend über diese Schnittstelle an alle Abonnenten verteilt.
     *
     * @param gameId        Die Spiel Id um die Partie zu identifizieren
     * @param principal     Der Aufrufer zur Prüfung der Berechtigung
     * @param gameDTO       Das Game-Objekt um Änderungen zu verteilen
     * @return              Das veränderte, validierte Game-Objekt
     */
    @MessageMapping("/{gameId}/changeGame")
    @SendTo("/chat/game/{gameId}/gameChanged")
    private GameDTO changeGame (@DestinationVariable Long gameId,
                                      Principal principal,
                                      GameDTO gameDTO) {

        // this.gameService.updateFromDTO(gameDTO);

        return gameDTO;
    }

    /***
     * WebSocket - Schnittstelle um eine Bewegung einer Spielfigur an den Server zu melden.
     * Diese wird überprüft und sofern valid an die Clients verteilt.
     * @param gameId        Die Spiel Id um die Partie zu identifizieren
     * @param principal     Der Aufrufer zur Prüfung der Berechtigung
     * @param playerMove    Die gewünschte Bewegung
     * @return              Die Bewegung die vom Server genehmigt wurde
     */
    @MessageMapping("/{gameId}/makeMove")
    @SendTo("/chat/game/{gameId}/gameChanged")
    public GameDTO makePlayerMove(@DestinationVariable Long gameId,
                                         Principal principal,
                                         PlayerMoveDTO playerMove) {
        GameDTO result;
        Player player = this.playerService.getPlayerByEmail(principal.getName());

        result = this.gameService.makeMove(gameId, player, playerMove);

        return result;
    }

    /***
     * WebSocket - Schnittstelle um das Abfeuern einer Waffe einer Spielfigur
     * durchzuführen. Die Anforderung wird vom Server validiert und eine entsprechende
     * Antwort wird an die beteiligten Clients verteilt. Änderungen des Spiel-Objektes
     * werden über den entsprechenden Socket /{gameId}/gameChange an die Clients verteilt.
     *
     * @param gameId        Die Spiel Id um die Partie zu identifizieren
     * @param principal     Der Aufrufer zur Prüfung der Berechtigung
     * @param fireWeapon    Die Information über den gewünschten Waffeneinsatz.
     * @return              Information über die Flugbahn des Geschosses, den angerichteten Schaden, u.ä.
     */
    @MessageMapping("/{gameId}/fireWeapon")
    @SendTo("/chat/game/{gameId}/weaponFired")
    public FiredWeaponDTO fireWeapon(@DestinationVariable Long gameId,
                                     Principal principal,
                                     FireWeaponDTO fireWeapon) {

        Player player = this.playerService.getPlayerByEmail(principal.getName());
        FiredWeaponDTO result = this.gameService.fireWeapon(gameId, player, fireWeapon);

        GameDTO gameDTO = this.gameService.getGameDTOFromId(gameId);
        this.template.convertAndSend("/chat/game/" + gameId + "gameChanged", gameDTO);

        return result;
    }

    /***
     * WebSocket - Schnittstelle über den die Clients melden, dass sie die Verarbeitung der aktuellen
     * Meldungen abgeschlossen haben. Sobald alle Spieler der Partie gemeldet haben, dass sie bereit sind,
     * fährt der Server damit fort über /{gameId}/gameChanged zu melden welcher Spieler nun an der Reihe ist.
     *
     * @param gameId        Die Spiel Id um die Partie zu identifizieren
     * @param principal     Der Aufrufer des Services zur identifikation wer bereit ist
     * @return              Systemmeldung an die Clients die darüber informiert, ob noch gewartet wird,
     *                      oder ob es weitergeht.
     */
    @MessageMapping("/{gameId}/setReady")
    @SendTo("/chat/game/{gameId}/gameChanged")
    public GameDTO processReadyState(@DestinationVariable Long gameId,
                                     Principal principal) {

        GameDTO gameDTO = this.gameService.setReadyState(gameId, principal.getName());

        return gameDTO;
    }

    /***
     * Request Mapping zum Setzen des Status InGame für den Spieler, nachdem er
     * der Partie beigetreten. Benötigt nur der joinende Player, Creating Player
     * übernimmt das automatisch
     *
     * @return "allInGameSuccess.html" in Templates
     */
    @RequestMapping(value = "/game/{gameId}/allInGame", method = RequestMethod.GET)
    public String allInGame(@PathVariable Long gameId, Model model, Principal principal) {

        Player player   = this.playerService.getPlayerByEmail(principal.getName());
        player.setStatus(OnlineStatus.Ingame);
        this.playerService.save(player);
        return "allInGameSuccess";
    }

    /***
     * WebSocket - Schnittstelle zum Versenden von Einladungen
     * an andere Spieler. Nach Akzeptieren der Einladung wird eine
     * Nachricht an /{gameId}/gameChanged gesendet.
     *
     * @param gameId    Die Id des Spiels um die Partie zu identifizieren
     * @param principal Der Aufrufer zum Prüfen der Berechtigung
     * @param playerDTO Der Spieler der eingeladen werden soll
     * @return          Das neue Spiel Objekt wo der neue Spieler
     *                  als Teilnehmer dabei ist
     */
    @MessageMapping("/{gameId}/sendInvite")
    @SendTo("/chat/game/{gameId}/gameChanged")
    public GameDTO sendInvite(@DestinationVariable Long gameId,
                              Principal principal,
                              @Payload PlayerDTO playerDTO) {

        Long receivingPlayerId = playerDTO.getId();

        MessageDTO inviteMessage =
                this.messageService.sendInviteToPlayer(
                            gameId,
                            principal.getName(),
                            playerDTO.getEmail()
                );

        this.template.convertAndSend("/chat/messages/" + receivingPlayerId, inviteMessage);

        return inviteMessage.getGameInvite();
    }

    @MessageMapping("/{gameId}/refreshGame")
    @SendTo("/chat/game/{gameId}/gameChanged")
    public GameDTO getCurrentGameState(@DestinationVariable Long gameId) {
        GameDTO gameDTO = this.gameService.getGameDTOFromId(gameId);

        return gameDTO;
    }

    @MessageMapping("/{gameId}/denyInvite")
    @SendTo("/chat/game/{gameId}/gameChanged")
    public GameDTO denyInvite(@DestinationVariable Long gameId, MessageDTO messageDTO, Principal principal) {

        Player player = this.playerService.getPlayerByEmail(principal.getName());
        GameDTO gameDTO = this.gameService.getGameDTOFromId(gameId);

        /*
        // Wieder auskommentieren sofern die Invite-Messages persistiert werden
        if (Objects.equals(player.getId(), messageDTO.getReceiver().getId())) {

            this.messageService.deleteInvite(messageDTO);
        }
        */

        return gameDTO;
    }

    @MessageMapping("/{gameId}/acceptInvite")
    @SendTo("/chat/game/{gameId}/gameChanged")
    public GameDTO acceptInvite(@DestinationVariable Long gameId, MessageDTO messageDTO, Principal principal) {

        Player player = this.playerService.getPlayerByEmail(principal.getName());
        GameDTO gameDTO = this.gameService.getGameDTOFromId(gameId);

        // sicherstellen, dass alles seine Richtigkeit hat
        Long playerId = player.getId();
        Long messageReceiverId = messageDTO.getReceiver().getId();
        Long actualGameId = gameDTO.getId();
        Long inviteGameId = messageDTO.getGameInvite().getId();

        if (playerId.equals(messageReceiverId) &&
            actualGameId.equals(inviteGameId)) {

            gameDTO = this.gameService.acceptInvite(gameId, player.getId());

            // Die nächste Zeile auskommentieren, falls Einladungen gespeichert werden
            // this.messageService.deleteInvite(messageDTO);

        } else {
            System.out.println("Invite data mismatch");
        }

        return gameDTO;
    }
}