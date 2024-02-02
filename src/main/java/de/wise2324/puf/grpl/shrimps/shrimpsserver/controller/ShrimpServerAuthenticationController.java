package de.wise2324.puf.grpl.shrimps.shrimpsserver.controller;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.dto.PlayerDTO;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.repository.PlayerRepository;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

/***
 * Controller für die Nutzerauthentifizierung sowie Registrierung neuer Nutzer.
 * Als Basisverzeichnis wird /authenticate genutzt.
 */
@Controller
@RequestMapping("/authenticate")
public class ShrimpServerAuthenticationController {

    /***
     * Zugriff auf das Verzeichnis aller Spieler
     */
    @Autowired
    public PlayerRepository repo;

    @Autowired
    PlayerService playerService;

    /***
     * Stellt einen Funktionstest für den Authentifikationscontroller bereit.
     * @return String Meldung, dass der Test erfolgreich war.
     */
    @ResponseBody
    @RequestMapping(value ="/test", method = RequestMethod.GET)
    public String testingController() {
        return "AuthenticationController working";
    }

    /***
     * Gibt ein Formular zurück, mit dem sich ein neuer Spieler
     * registrieren kann.
     * @param model Es wird eine Referenz auf ein neues
     *              Spieler-Objekt mitgegeben.
     * @return  Der Verweis auf das Formular-Template.
     */
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String viewRegistrationForm(Model model) {
        model.addAttribute("player", new PlayerDTO());
        return "register";
    }

    /***
     * Registriert einen neuen Spieler in der Datenbank. Zuvor wird
     * das übergebene Passwort verschlüsselt. Dies sollte bei einem
     * Produktivsystem bereits im jeweiligen Client passieren.
     *
     * @param player    Das Spieler-Objekt, das hinzufügt werden soll.
     * @return  Verweis auf die Seite für erfolgreiche Registrierung
     */
    @RequestMapping(value = "/process_register", method = RequestMethod.POST)
    @ResponseBody
    public String processRegister(@RequestBody PlayerDTO player) {
        return this.processingRegistration(player);
    }

    @RequestMapping(value = "/process_registerSpring", method = RequestMethod.POST)
    @ResponseBody
    public String processRegisterSpring( PlayerDTO player) {
        return this.processingRegistration(player);
    }

    private String processingRegistration(PlayerDTO player) {
        // @ToDo: Für Produktivsysteme sollte die Verschlüsselung im Client erfolgen
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        player.setPassword("{bcrypt}" + enc.encode(player.getPassword()));

        String result = "Fehler im Registrierungsprozess";
        int userAlreadyInDB = this.playerService.contains(player);
        if (userAlreadyInDB == 0) {
            this.playerService.saveFromDTO(player);
            result = "Erfolg";
        } else {
            if (userAlreadyInDB == 1) {
                result = "Email bereits vorhanden.";
            } else {    // == 2
                result = "Nutzer bereits vorhanden.";
            }
        }

        return result;
    }
}