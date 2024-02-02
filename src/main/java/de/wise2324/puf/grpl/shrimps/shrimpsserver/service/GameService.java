package de.wise2324.puf.grpl.shrimps.shrimpsserver.service;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.dto.*;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.entity.GameState;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.entity.MoveDirection;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.entity.OnlineStatus;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.entity.PlayerGameStatus;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.*;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.repository.*;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.util.GamePosition;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.util.HitEntry;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.util.Vector2d;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.util.RandomNameGenerator;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.annotation.ApplicationScope;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@ApplicationScope
public class GameService {
    private final int SHRIMP_PER_PLAYER = 3;

    @Autowired
    GameRepository gameRepository;

    @Autowired
    PlayerStatisticRepository playerStatisticRepository;

    @Autowired
    PlayerStatisticService playerStatisticService;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    PlayerService playerService;

    @Autowired
    ShrimpRepository shrimpRepository;

    @Autowired
    ShrimpService shrimpService;

    @Autowired
    WeaponRepository weaponRepository;

    public Game createNewGame(Player player) {

        Game result = new Game();
        this.loadDefaults(result);

        this.gameRepository.save(result);

        List<PlayerStatistic> playerStatisticList = new ArrayList<>();
        playerStatisticList.add(createPlayerStatistic(player, result));
        result.setPlayerStatisticsList(playerStatisticList);

        this.gameRepository.save(result);

        return result;
    }

    private PlayerStatistic createPlayerStatistic(Player player, Game game) {
        PlayerStatistic playerStatistic = new PlayerStatistic();
        playerStatistic.setPlayer(player);
        playerStatistic.setGame(game);
        playerStatistic.setStatus(PlayerGameStatus.NOT_READY);

        // Abspeichern um eine ID zu erhalten die für die Shrimps
        // benötigt wird
        this.playerStatisticRepository.save(playerStatistic);

        // Liste an Shrimps erstellen
        List<Shrimp> shrimpList = generateShrimpList(playerStatistic);
        playerStatistic.setShrimps(shrimpList);

        // Abschließendes Speichern
        this.playerStatisticRepository.save(playerStatistic);

        return playerStatistic;
    }

    private List<Shrimp> generateShrimpList(PlayerStatistic playerStatistic) {
        List<Shrimp> shrimpList = new ArrayList<>();

        for (int i = 0; i < this.SHRIMP_PER_PLAYER; i++) {
            Shrimp shrimp = new Shrimp();
            shrimp.setX_position(-1);
            shrimp.setY_position(-1);
            shrimp.setPlayerStatistic(playerStatistic);

            String suggestedShrimpName = generateRandomName();
            while (containsListName(shrimpList, suggestedShrimpName)) {
                suggestedShrimpName = generateRandomName();
            }
            shrimp.setName(suggestedShrimpName);

            this.shrimpRepository.save(shrimp);
            shrimpList.add(shrimp);
        }

        return shrimpList;
    }

    private boolean containsListName(List<Shrimp> shrimpList, String name) {
        boolean result = false;

        for (Shrimp shrimp : shrimpList) {
            if (shrimp.getName().equals(name)) {
                result = true;
                break;
            }
        }

        return result;
    }

    private String generateRandomName() {
         return RandomNameGenerator.getRandomName();
    }


    private void loadDefaults(Game game) {
        game.setForegroundMask(loadMask("level_1"));
        game.setTimestamp(new Timestamp(System.currentTimeMillis()));
        game.setRoundTimeLimit(-1);
        game.setRoundToSuddenDeath(-1);
        game.setStatus(GameState.INITIALIZATION);
    }

    private byte[] loadMask(String levelname) {
        byte[] result = new byte[0];

        try {
            String pathname = "classpath:static/levels/"
                    + levelname
                    + "_foreground_mask.png";
            File file = ResourceUtils.getFile(pathname);

            result = Files.readAllBytes(file.toPath());
        } catch (IOException ex) {

            System.out.println("Schwerer Serverfehler: Resource nicht gefunden.");
            System.out.println(ex.toString());

        } catch (SecurityException ex) {

            System.out.println("Schwerer Serverfehler: Keine Berechtigung");
            System.out.println(ex.toString());
        }

        return result;
    }

    public Game getGameFromId(Long gameId) {
        Game game = this.gameRepository.findById(gameId).orElse(null);
        Hibernate.initialize(game);
        return game;
    }

    public GameDTO getGameDTOFromId(Long gameId) {
        Game game = this.getGameFromId(gameId);
        GameDTO gameDTO = this.getDTOFromObject(game);

        return gameDTO;
    }


    // Zur Änderung der Spielkonfiguration oder zum updaten
    // der Spiellogik von den Clients aus muss dieses implementiert werden.
    /*
    public void updateFromDTO(GameDTO gameDTO) {
        Optional<Game> game = this.gameRepository.findById(gameDTO.getId());

    }
    */

    public GameDTO getDTOFromObject(Game game) {
        GameDTO result              = new GameDTO();

        result.setId(game.getId());
        result.setTimestamp(game.getTimestamp());
        result.setRoundTimeLimit(game.getRoundTimeLimit());
        result.setRoundToSuddenDeath(game.getRoundToSuddenDeath());

        result.setGameState(game.getStatus());
        result.setForegroundMask(game.getForegroundMask());
        result.setLevelBackground(game.getLevelBackground());
        result.setLevelForeground(game.getLevelForeground());

        List<PlayerStatisticDTO> playerStatisticDTOList = result.getPlayerStatisticsList();
        for(PlayerStatistic playerStatistic : game.getPlayerStatisticsList()) {
            playerStatisticDTOList.add(
                    this.playerStatisticService.getDTOFromObject(playerStatistic));
        }
        result.setPlayerStatisticsList(playerStatisticDTOList);

        result.setCurrentRound(game.getCurrentRound());
        result.setCurrentPlayerId(game.getCurrentPlayerId());
        result.setCurrentPlayerIdx(game.getCurrentPlayerIdx());
        result.setCurrentShrimpId(game.getCurrentShrimpId());
        result.setCurrentShrimpIdx(game.getCurrentShrimpIdx());

        return result;
    }

    public GameDTO acceptInvite(Long gameId, Long playerId) {

        Player player = this.playerRepository.findById(playerId).orElse(null);
        Game game = this.getGameFromId(gameId);

        if (player != null && game != null) {
            this.addPlayer(game, player);
        }

        return this.getDTOFromObject(game);
    }

    public void addPlayer(Game game, Player player) {

        PlayerStatistic playerStatistic = this.createPlayerStatistic(player, game);
        game.getPlayerStatisticsList().add(playerStatistic);
        this.gameRepository.save(game);
    }

    public GameDTO setReadyState(Long gameId, String playerMail) {
        Player player = this.playerRepository.findByEmail(playerMail);
        Game game = this.getGameFromId(gameId);

        if (player != null) {
            for (PlayerStatistic playerStatistic : game.getPlayerStatisticsList()) {

                Long playerId = player.getId();
                Long playerStatisticPlayerId = playerStatistic.getPlayer().getId();

                if (playerId.equals(playerStatisticPlayerId)) {
                    playerStatistic.setStatus(PlayerGameStatus.READY);

                    playerStatistic = this.playerStatisticService.save(playerStatistic);
                }
            }
        }

        this.checkIfAllAreReady(game);
        GameDTO gameDTO = this.getDTOFromObject(game);

        return gameDTO;
    }

    private void checkIfAllAreReady(Game game) {

        if (game.getStatus().equals(GameState.WAITING_FOR_READY)) {

            boolean allReady = true;

            for (PlayerStatistic currentPlayerStatistic : game.getPlayerStatisticsList()) {
                if (!(currentPlayerStatistic.getStatus().equals(PlayerGameStatus.READY))) {
                    allReady = false;
                    break;
                }
            }

            if (allReady) {
                int nextRound = game.getCurrentRound() + 1;
                game.setCurrentRound(nextRound);

                PlayerStatistic activePlayer = game.getPlayerStatisticsList().get(game.getCurrentPlayerIdx());
                activePlayer.setStatus(PlayerGameStatus.ON_TURN);
                this.playerStatisticRepository.save(activePlayer);

                game.setStatus(GameState.PLAYER_TURN);
                this.save(game);
            }
        }

    }

    public Game startGame(Game game) {

        if (game.getStatus().equals(GameState.INITIALIZATION)) {
            // Erster Aufruf für startGame, sukzessive Aufrufe von
            // weiteren Spielern werden ignoriert

            game.setStatus(GameState.STARTING);
            this.save(game);

            this.randomizeShrimpPositions(game);
            this.assignShrimpLoadout(game);

            game.setStatus(GameState.PLAYER_TURN);
            game.setCurrentRound(1);
            game.setCurrentPlayerIdx(0);
            PlayerStatistic currentPlayerStatistic = game.getPlayerStatisticsList().getFirst();

            game.setCurrentPlayerId(currentPlayerStatistic.getPlayer().getId());
            game.setCurrentShrimpIdx(0);
            game.setCurrentShrimpId(currentPlayerStatistic.getShrimps().getFirst().getId());
            currentPlayerStatistic.setStatus(PlayerGameStatus.ON_TURN);

            this.save(game);
        }

        return game;
    }

    public void assignShrimpLoadout(Game game) {
        // ToDO: Begrenzte Munition einführen
    }

    public void randomizeShrimpPositions(Game game) {

        boolean[][] bitArray = this.getMaskArray(game.getForegroundMask());
        int middleOfMap = bitArray.length / 2;
        List<Integer> usedXValues = new ArrayList<>();
        Random rng = new Random(System.currentTimeMillis());

        for (PlayerStatistic currentPlayerStatistic : game.getPlayerStatisticsList()) {
            for (Shrimp currentShrimp : currentPlayerStatistic.getShrimps()) {

                // Setzen einer freien, validen Position
                int randomXPosition = rng.nextInt(0, bitArray.length);
                int groundOn = landsOn(bitArray, randomXPosition, 0);
                while (     isPositionUsed(usedXValues, randomXPosition) ||
                        (   groundOn == -1)) {
                    randomXPosition = rng.nextInt(0, bitArray.length);
                    groundOn = landsOn(bitArray, randomXPosition, 0);
                }

                // Speichern der Position
                currentShrimp.setX_position(randomXPosition);
                usedXValues.add(randomXPosition);

                // 0 = unten
                currentShrimp.setY_position(groundOn + 1);
                currentShrimp.setLookingRight((randomXPosition < middleOfMap));
                this.shrimpRepository.save(currentShrimp);
            }

            this.playerStatisticRepository.save(currentPlayerStatistic);
        }

        this.gameRepository.save(game);
    }

    private boolean isPositionUsed(List<Integer> usedValues, Integer testValue) {
        boolean result = false;

        // Spielerfigur geht von position x bis x + 40;
        for (Integer element : usedValues) {
            if (testValue >= element && testValue <= (element + 40)) {
                result = true;
                break;              // Der erste Treffer reicht um Ergebnis zu bestimmen
            }
        }

        return result;
    }

    private int landsOn(boolean[][] map, Integer xPosition, Integer initialYPosition) {
        int result  = -1;               // Falls kein Land unter dem Shrimp kommt
        // int width   = map.length;
        int height  = map[0].length -1; // -1 weil 0-basierter Index

        // Von oben nach unten soll unter der Shrimp-Position getestet werden,
        // ob sich unter dem Shrimp Boden befindet (0,0) ist hierbei links oben
        for (int i = initialYPosition; i < height; i++) {
            boolean isPositionHard = map[xPosition][i];
            if (isPositionHard) {
               result = i;
               break;
            }
        }

        return result;
    }

    public boolean[][] getMaskArray(byte[] maskBytes) {

        ByteArrayInputStream inputStream = new ByteArrayInputStream(maskBytes);
        boolean[][] result = null;
        try {
            BufferedImage image = ImageIO.read(inputStream);

            int height = image.getHeight();
            int width = image.getWidth();

            // Aufbau ist von links nach rechts, von oben nach unten
            // 1366 x 768 (default);
            result = new boolean[width][height];

            for (int i = 0; i < image.getHeight(); i++) {
                for (int j = 0; j < image.getWidth(); j++) {

                    int pixel = image.getRGB(j, i);
                    int pixelAlpha = (pixel >> 24) & 0xFF;

                    boolean isHard = pixelAlpha != 0;
                    // result[targetIndex] = isHard;

                    // 2D Array damit die Information über Länge und Breite erhalten bleibt
                    result[j][i] = isHard; // [j][i] == [x][y] <= (x, y)
                }
            }
        } catch (IOException ex) {
            //
            // throw new RuntimeException(ex);
            System.out.println("Fehler: " + ex.toString());
        }

        return result;
    }

    public Game save(Game game) {

        return this.gameRepository.save(game);
    }

    public HistoryDTO getHistoryFromId(Long playerId) {
        HistoryDTO result = new HistoryDTO();
        Optional<Player> op = this.playerRepository.findById(playerId);

        op.ifPresent(player -> result.setPlayer(this.playerService.getDTOFromObject(player)));
        List<PlayerStatistic> playerStatisticList = this.playerStatisticRepository.getPlayedGamesFor(playerId);

        result.setTotalGamesStartet(playerStatisticList.size());
        int gamesWon = 0;
        int gamesLost = 0;
        int gamesFinished = 0;

        for (PlayerStatistic playerStatistic : playerStatisticList) {
            GameState gameState = playerStatistic.getGame().getStatus();

            if (gameState.equals(GameState.FINISHED)) {
                gamesFinished++;

                boolean hasWon = false;
                for (Shrimp currentShrimp : playerStatistic.getShrimps()) {
                    if (currentShrimp.getHitpoints() > 0) {
                        hasWon = true;
                        break;
                    }
                }
                if (hasWon) {
                    gamesWon++;
                } else {
                    gamesLost++;
                }
            }
        }

        result.setTotalGamesFinished(gamesFinished);
        result.setTotalGamesWon(gamesWon);
        result.setTotalGamesLost(gamesLost);

        return result;
    }

    public GameDTO makeMove(Long gameId, Player player, PlayerMoveDTO playerMoveDTO) {
        Game game = this.getGameFromId(gameId);


        // Prüfen ob Spieler diesen Shrimp bewegen darf
        ShrimpDTO shrimpDTO = playerMoveDTO.getShrimpDTO();
        boolean isValidMove = checkRequestValidity(game, player, shrimpDTO);
        // ...

        // Prüfen ob Bewegung möglich ist
        if (isValidMove) {
            int newXPosition;
            int oldXPosition = shrimpDTO.getX_position();
            int oldYPosition = shrimpDTO.getY_position();

            boolean[][] bitArray = this.getMaskArray(game.getForegroundMask());

            // Solange JUMP nicht dabei ist, reicht if, danach switch notwendig
            if (playerMoveDTO.getMoveDirection().equals(MoveDirection.RIGHT)) {

                newXPosition = oldXPosition + 5;
            } else {

                newXPosition = oldXPosition - 5;
            }

            GamePosition newPosition = checkMoveFromTo(bitArray, oldXPosition, oldYPosition, newXPosition);

            Shrimp currentShrimp = game.getPlayerStatisticsList()
                    .get(game.getCurrentPlayerIdx())
                    .getShrimps()
                    .get(game.getCurrentShrimpIdx());

            currentShrimp.setX_position(newPosition.getX());
            currentShrimp.setY_position(newPosition.getY());
            currentShrimp.setLookingRight(oldXPosition <= newXPosition);
            this.shrimpRepository.save(currentShrimp);
        }
        // ...

        // Änderung anwenden und speichern
            this.save(game);
        // ...

        return this.getDTOFromObject(game);
    }

    private boolean checkRequestValidity(Game game, Player player, ShrimpDTO shrimpDTO) {
        boolean result = true;

        if (!(player.getStatus().equals(OnlineStatus.Ingame))) {
            // Spieler nicht im richtigen Zustand
            result = false;
        }

        if (!(game.getCurrentPlayerId().equals(player.getId()))) {
            // Falscher Spieler hat eine Nachricht geschickt
            result = false;
        }

        if (!(game.getCurrentShrimpId().equals(shrimpDTO.getId()))) {
            // Falscher Shrimp
            result = false;
        }

        return result;
    }

    private GamePosition checkMoveFromTo(boolean[][] map, int oldX, int oldY, int newX) {
        GamePosition result = new GamePosition();
        result.setX(-1);
        result.setY(-1);

        // Prüfe jeden Schritt (pixel) in der jeweiligen Richtung
        int lastY = oldY;
        int stepX = 1;
        int newValidX = oldX;

        if (oldX > newX) { stepX = -1; }
        oldX += stepX;

        for (int j = 1; j < Math.abs(oldX-newX); j++) {
            int i = (stepX * j) + oldX;

            int testY = lastY - 40;                     // Bei einer Prüfung von 40px "höher"
            int newY  = this.landsOn(map, i, testY) - 1;// folgt das auch herabhängende Wände
                                                        // erfasst werden.
            int diffY = lastY - (newY + 5);             // Höhenunterschied von bis zu 5 px nach
                                                        // oben sind ok
            if (diffY <= 0) {
                // Positionaufbau ist (0,0) ist links oben, sei alt Y bei 600, neu bei 595 (also
                // 5px höher dann folgt diffY = 0 = 600 - (595 + 5)
                newValidX = i;
                lastY = newY;
            } else {
                break;
            }
        }

        result.setX(newValidX);
        result.setY(lastY);

        return result;
    }

    public FiredWeaponDTO fireWeapon(Long gameId, Player player, FireWeaponDTO fireWeapon) {
        FiredWeaponDTO result = new FiredWeaponDTO();
        Game game = this.getGameFromId(gameId);

        // Prüfe Validität der Anfrage
        boolean isValidRequest = checkRequestValidity(game, player, fireWeapon.getShrimpDTO());

        if (isValidRequest) {
            // Berechne Resultat des Waffeneinsatzes
            result = this.calculateWeaponUsage(game, fireWeapon);

            // Nächsten Spieler auswählen und Spiel-Objekt aktualisieren
            this.advanceGame(game);
        }

        return result;
    }

    public void advanceGame(Game game) {

        Long currentPlayerId = game.getCurrentPlayerId();
        Long currentShrimpId = game.getCurrentShrimpId();
        int currentPlayerIdx = game.getCurrentPlayerIdx();
        int currentShrimpIdx = game.getCurrentShrimpIdx();
        int currentRound     = game.getCurrentRound();

        int livePlayerCount = this.gameRepository.getLivePlayersFor(game.getId());

        if (livePlayerCount > 1)  {
            // Spiel geht noch weiter → nächsten Shrimp auswählen
            // Herausforderung hierbei ist, dass es ggf. eine Partie mit
            // mehr als zwei Spielern ist, in der bereits Spieler keine
            // lebenden Shrimps mehr haben und darum übersprungen werden
            // müssen, oder der nächste Shrimp bereits ausgeschieden ist und
            // darum ebenso übersprungen werden muss.

            int playerCount     = game.getPlayerStatisticsList().size();
            int newPlayerIdx    = (currentPlayerIdx + 1) % playerCount;
            PlayerStatistic newPlayerStatistic = game.getPlayerStatisticsList().get(newPlayerIdx);

            while (this.playerStatisticRepository
                    .getLiveShrimpCountFor(newPlayerStatistic.getId()) == 0)  {
                newPlayerIdx = (newPlayerIdx + 1) % playerCount;
                newPlayerStatistic = game.getPlayerStatisticsList().get(newPlayerIdx);
            }

            // Setzen der Werte zur Nachverfolgung
            PlayerStatistic currentPlayerStatistic = game.getPlayerStatisticsList().get(currentPlayerIdx);
            Shrimp currentShrimp = currentPlayerStatistic.getShrimps().get(currentShrimpIdx);

            currentShrimp.setLastActiveOnRound(game.getCurrentRound());
            this.shrimpRepository.save(currentShrimp);

            // Bereinigen der alten Status
            for (PlayerStatistic playerStatistic : game.getPlayerStatisticsList()) {
                playerStatistic.setStatus(PlayerGameStatus.NOT_READY);
                this.playerStatisticRepository.save(playerStatistic);
            }

            // Setzen des nächsten Spielers
            game.setCurrentPlayerId(newPlayerStatistic.getPlayer().getId());
            game.setCurrentPlayerIdx(newPlayerIdx);

            // Finde den nächsten Shrimp
            Shrimp nextShrimp = this.shrimpRepository.getNextShrimpFor(newPlayerStatistic.getId());
            game.setCurrentShrimpId(nextShrimp.getId());

            List<Shrimp> newShrimpList = newPlayerStatistic.getShrimps();
            int shrimpListSize = newShrimpList.size();

            for (int i = 0; i < shrimpListSize; i++) {

                Shrimp shrimpIterator = newShrimpList.get(i);
                if (shrimpIterator.getId().equals(nextShrimp.getId())) {
                    game.setCurrentShrimpIdx(i);
                    break;
                }
            }

            game.setStatus(GameState.WAITING_FOR_READY);

        } else if (livePlayerCount == 1) {
            // Spiel hat nur noch einen Spieler
            // → Spieler ist Gewinner
            // Nächster Shrimp -> -1 (invalid)
            // Nächster Spieler = Gewinner
            // Game.Status = FINISHED

            game.setCurrentShrimpId(-1L);
            game.setCurrentShrimpIdx(-1);

            PlayerStatistic winnerStatistic = this.gameRepository.getWinnerPlayerStatisticFor(game.getId());
            int winnerIdx = this.getIdxInStatisticList(game, winnerStatistic.getId());
            game.setCurrentPlayerId(winnerStatistic.getPlayer().getId());
            game.setCurrentPlayerIdx(winnerIdx);
            game.setStatus(GameState.FINISHED);

        } else {
            // Keine "lebenden" Spieler → Draw.
            // Das keiner gewonnen hat, erkennt man daran dass,
            // entsprechend alle Id's auf -1 gesetzt (invalid) gesetzt werden,
            // und der Spielstatus auf "FINISHED" steht.

            game.setCurrentPlayerId(-1L);
            game.setCurrentPlayerIdx(-1);
            game.setCurrentShrimpId(-1L);
            game.setCurrentShrimpIdx(-1);
            game.setStatus(GameState.FINISHED);

        }

        this.save(game);
    }

    private int getIdxInStatisticList(Game game, Long playerStatisticId) {
        List<PlayerStatistic> list = game.getPlayerStatisticsList();
        int result = -1;      // Standard = invalid => nicht vorhanden

        int listSize = list.size();

        for (int i = 0; i < listSize; i++) {
            PlayerStatistic currentStatistic = list.get(i);
            if (currentStatistic.getId().equals(playerStatisticId)) {
                result = i;
            }
        }

        return result;
    }

    private FiredWeaponDTO calculateWeaponUsage(Game game, FireWeaponDTO weapon) {
        FiredWeaponDTO result = new FiredWeaponDTO();

        boolean isForceUsed = false;
        boolean isGravityAllied = false;
        Vector2d aimVector = calculateVector(weapon.getAngle());

        boolean[][] bitArray = this.getMaskArray(game.getForegroundMask());
        long[][] gameMap = this.upgradeBitArrayToGameMap(game, bitArray, weapon.getShrimpDTO().getId());

        Long weaponId = weapon.getId();
        if (weaponId.equals(1L)) {
            // Pistole
            int damage = 50;
            GamePosition currentPosition = new GamePosition();
            // Nutze die Mitte des Shrimps statt nur seiner Position
            currentPosition.setX(weapon.getShrimpDTO().getX_position());
            currentPosition.setY(weapon.getShrimpDTO().getY_position() - 20);

            List<GamePosition> bulletPath = getLinearPositions(currentPosition, aimVector, gameMap, true);
            GamePosition lastPosition = bulletPath.getLast();

            long stopper = gameMap[lastPosition.getX()][lastPosition.getY()];
            if (stopper > -1L){
                Shrimp shrimp = this.shrimpRepository.findById(stopper).orElse(null);
                if (shrimp != null) {

                    int oldHitpoints = shrimp.getHitpoints();
                    int newHitpoints = oldHitpoints - damage;
                    newHitpoints = Math.max(0, newHitpoints);

                    shrimp.setHitpoints(newHitpoints);
                    this.shrimpRepository.save(shrimp);

                    ShrimpDTO shrimpDTO = this.shrimpService.getDTOFromObject(shrimp);
                    result.getTargetsHit().add(new HitEntry(shrimpDTO, lastPosition, damage));
                }
            }

            result.setProjectilePath(bulletPath);
        }
        else if(weaponId.equals(2L)){
            // Bazooka
            boolean stopsOnFirstHit = true;
            boolean isBouncy = false;
            int initialVelocity = 117;          // m/s
            List<GamePosition> bulletPath =
                    this.getProjectilePath(gameMap, weapon, initialVelocity, stopsOnFirstHit, isBouncy);


        }

        return result;
    }

    private List<GamePosition> getProjectilePath(long[][] gameMap,
                                                 FireWeaponDTO weapon,
                                                 int weaponVelocity0,
                                                 boolean stopsOnFirstHit,
                                                 boolean isBouncy) {
        List<GamePosition> positionList = new ArrayList<>();
        double timeScalingForClients = 0.1;     // 100ms => 10 Frames/s

        // 1 ms → bei Geschwindigkeiten bis 1000 m/s sollte nicht
        // mehr als 1px je Koordinate sich ändern
        double timeScalingForHitCheck = 0.001;
        double angleInGameDeg = weapon.getAngle();
        final double degToRad = Math.PI / 180;
        double angleInRad = ((360 - (angleInGameDeg - 90)) % 360) * degToRad;

        double gravity = 9.81;
        double h0 = 20.0;
        double weaponForce = weapon.getForce();
        double clampedForce = Math.min(1.0, Math.max(0.1, weaponForce));

        double v0 = weaponVelocity0 * clampedForce;

        boolean isFinished = false;
        double deltaT = 0.0;

        double lastXOffset = 0.0;
        double lastYOffset = 0.0;

        int startXPosition = weapon.getShrimpDTO().getX_position();
        int startYPosition = weapon.getShrimpDTO().getY_position();

        while (!isFinished) {
            deltaT += timeScalingForHitCheck;

            double newX = v0 * deltaT * Math.cos(angleInRad);
            double newY = h0 +
                          (v0 * deltaT * Math.sin(angleInRad)) -
                          (0.5 * gravity * Math.pow(deltaT, 2));

            double newXOffset = newX > 0 ? Math.floor(newX) : Math.ceil(newX);
            // Für Y - Koordinate andersrum, da für das Zeichnen oben und unten vertauscht sind
            double newYOffset = newY > 0 ? Math.ceil(newY) : Math.floor(newY);

            if (lastXOffset != newXOffset || lastYOffset != newYOffset) {
                int xPosition = startXPosition + (int) newXOffset;
                int yPosition = startYPosition + (int) newYOffset;

                
            }

            lastXOffset = newXOffset;
            lastYOffset = newYOffset;

            if ((deltaT % timeScalingForClients) == 0.0) {
                // Übernehmen des aktuellen Schritts in das Ergebnis, sofern sich der aktuelle Schritt
                // mit dem Stepping der Client-Antwort (100ms) überschneidet


            }
        }

        return positionList;
    }

    /**
     * Berechnung der Flugbahn für "Strahlen"-Waffen ohne Reichweite. Die Berechnung
     * erfolgt mit Bresenham-Linien-Algorithmus
     *
     * @param currentPosition   Die Startposition von der geschossen wird
     * @param aim               Vector der angibt in wohin gezielt wird
     * @param map               Kartendaten
     * @param stopOnFirstObstacle   "Greedy"-Schalter
     * @return                  Gibt eine Liste an Punkten zurück bis zum Endpunkt
     */
    public List<GamePosition> getLinearPositions(GamePosition currentPosition,
                                                 Vector2d aim,
                                                 long[][] map,
                                                 boolean stopOnFirstObstacle) {

        int width = map.length;
        int height = map[0].length;

        double xPart = currentPosition.getX();
        double yPart = currentPosition.getY();

        double aimDeltaX = aim.getX();
        double aimDeltaY = aim.getY();

        // Finde einen Punkt in gerader Linie den Vektor entlang der ausserhalb des Bildes ist

        double factorX = 0.0;
        double factorY = 0.0;

        GamePosition endPosition = new GamePosition();
        boolean xIsPositiv = false;
        boolean yIsPositiv = false;
        double newXPart;
        double newYPart;

        if (aimDeltaX < 0) { factorX = xPart / Math.abs(aimDeltaX); xIsPositiv = false; }
        if (aimDeltaX > 0) { factorX = (width - xPart) / aimDeltaX; xIsPositiv = true; }
        if (aimDeltaY < 0) { factorY = yPart / Math.abs(aimDeltaY); yIsPositiv = false; }
        if (aimDeltaY > 0) { factorY = (height - yPart) / aimDeltaY; yIsPositiv = true; }

        if (Math.abs(factorX) > Math.abs(factorY)) {

            // x Wert braucht größeren Faktor um Spielfeld zu verlassen
            newXPart = xPart + aimDeltaX * factorY;
            newYPart = yPart + aimDeltaY * factorY;
        } else {

            // y Wert braucht größeren Faktor um Spielfeld zu verlassen
            newXPart = xPart + aimDeltaX * factorX;
            newYPart = yPart + aimDeltaY * factorX;
        }
        if (xIsPositiv) { newXPart = Math.ceil(newXPart); } else { newXPart = Math.floor(newXPart); }
        if (yIsPositiv) { newYPart = Math.ceil(newYPart); } else { newYPart = Math.floor(newYPart); }

        endPosition.setX((int) newXPart);
        endPosition.setY((int) newYPart);

        List<GamePosition> resultList = new ArrayList<>();
        List<GamePosition> linearList = getLineBresenham(currentPosition, endPosition);

        int listLength = linearList.size();

        for (int i = 0; i < listLength; i++) {

            GamePosition testPosition = linearList.get(i);

            int x = testPosition.getX();
            int y = testPosition.getY();

            // Als Erstes prüfen ob Koordinaten noch gültig sind
            if (x < 0 ||
                y < 0 ||
                x >= width ||
                y >= height) {
                break;
            }

            resultList.add(testPosition);

            // Prüfen, ob etwas getroffen wurde
            if (!(map[x][y] == -1L) && stopOnFirstObstacle) {
                // -1L ist Luft, -2L ist Wand > -1 ist ShrimpId daher
                // stellt was anderes als -1L das Ende der Flugbahn dar.
                // Diese letzte Position soll jedoch mit übernommen werden.
                break;
            }
        }

        return resultList;
    }

    /**
     * Angepasste Implementation des Bresenham Linien Algorithmus von
     * "www.sanfoundry.com/java-program-bresenham-line-algorithm/".
     * Findet eine gerasterte Linie von Punkt A nach Punkt B
     *
     * @param a Anfangspunkt
     * @param b Endpunkt
     * @return Liste an unterschiedlichen Punkten die auf der Linie liegen.
     */
    private List<GamePosition> getLineBresenham(GamePosition a, GamePosition b) {

        List<GamePosition> result = new ArrayList<>();

        int x0 = a.getX();
        int y0 = a.getY();
        int x1 = b.getX();
        int y1 = b.getY();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx-dy;
        int e2;

        while (true) {

            // line.add(grid[x0][y0]);
            GamePosition newPoint = new GamePosition();
            newPoint.setX(x0);
            newPoint.setY(y0);
            result.add(newPoint);

            if (x0 == x1 && y0 == y1)
                break;

            e2 = 2 * err;
            if (e2 > -dy) {

                err = err - dy;
                x0 = x0 + sx;
            }

            if (e2 < dx) {

                err = err + dx;
                y0 = y0 + sy;
            }
        }

        return result;
    }

    /**
     * Ergebnis ist ein 2d Array des Levels, -1 = Luft, -2 = Wand, x = ShrimpId der Hitbox
     * @param game              Das Spiel-Objekt für das die Karte erstellt wird
     * @param levelMask         Die aufbereitete 2d-Array Maske des Levels
     * @param firingShrimpId    Die Id des Shrimps der feuert
     * @return                  2d-Array Karte mit eingetragenen Hitboxen der Shrimps
     */
    private long[][] upgradeBitArrayToGameMap(Game game, boolean[][] levelMask, Long firingShrimpId) {
        long[][] map = new long[levelMask.length][levelMask[0].length];
        int mapWidth = map.length;
        int mapHeight = map[0].length;

        // Als erstes Terrain übertragen
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                long value = levelMask[x][y] ? -2L : -1L;
                map[x][y] = value;
            }
        }

        // Für jeden anderen (als den der dran ist) Shrimp eine Hitbox erzeugen und in die Map eintragen.
        // Die Hitbox geht dabei +-20px links und rechts von der x Position des Shrimps und
        // -40px von der y Position. -40 meint dabei oben.
        for (PlayerStatistic currentPlayerStatistic : game.getPlayerStatisticsList()) {
            for (Shrimp shrimp : currentPlayerStatistic.getShrimps()) {

                Long currentShrimpId = shrimp.getId();
                if (!(currentShrimpId.equals(firingShrimpId))) {
                    int xPos = shrimp.getX_position();
                    int yPos = shrimp.getY_position();

                    // Tote Shrimps sind im Hintergrund
                    if (shrimp.getHitpoints() > 0) {
                        for (int y = yPos - 40; y <= yPos; y++) {
                            for (int x = xPos - 20; x <= xPos + 20; x++) {
                                // Sicherstellen, dass die Array-Grenzen eingehalten werden
                                if ((x < 0 || x >= mapWidth) ||
                                    (y < 0 || y >= mapHeight)) {
                                    continue;
                                }

                                if (!(levelMask[x][y])) {
                                    map[x][y] = currentShrimpId;
                                }
                            }
                        }
                    }
                }
            }
        }

        return map;
    }

    private Vector2d calculateVector(int angle) {
        Vector2d vector = new Vector2d();

        // Die Winkelangabe sieht 0° als oben vor und positiver Winkel im Uhrzeigersinn. Für die Berechnung
        // muss der Winkel zum einen in einen überführt werden, mit dem man mathematisch rotieren kann
        // (im Spielkontext), daher nach +x nach 0° und dann gegen den Uhrzeigersinn geht
        // (Standard / Right-Hand notation). Daher ist Oben 90°.
        final double degToRad = Math.PI / 180;
        double angleInRad = ((360 - (angle - 90)) % 360) * degToRad;

        // Wir nehmen an: (x, y) = (1, 0) ist der Normalvektor für den Schuss, wie oben beschrieben.

        // Volle Formel zum Verständnis, da Teile gleich 0 sind fallen entsprechende Teile weg
        double xPart = (1 * Math.cos(angleInRad)) - (0 * Math.sin(angleInRad));
        double yPart = (0 * Math.cos(angleInRad)) + (1 * Math.sin(angleInRad));

        vector.setX(xPart);
        // Das Ergebnis ist in kartesischen Koordinaten (^ +y, > +x)
        // Dementsprechend muss für die Spielkoordinaten die y-Koordinate gespiegelt werden
        vector.setY(yPart * -1);

        return vector;
    }
    public List<LeaderboardDTO> getPlayerLeaderboard() {
        List<Object[]> leaderboardData = playerStatisticRepository.getPlayerLeaderboard();

        return leaderboardData.stream()
                .map(data -> {
                    LeaderboardDTO dto = new LeaderboardDTO();
                    dto.setPlayerId((Long) data[0]);
                    dto.setUsername((String) data[1]);
                    dto.setGamesFinished((Integer) data[2]);
                    dto.setGamesStarted((Integer) data[3]);
                    dto.setWins((Integer) data[4]);
                    dto.setLosses((Integer) data[5]);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}