package com.example.hexgame.controller;

import com.example.hexgame.dto.CreateGameRequest;
import com.example.hexgame.dto.JoinResponse;
import com.example.hexgame.model.GameInstance;
import com.example.hexgame.service.GameManagerService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameManagerService manager;

    public GameController(GameManagerService manager) {
        this.manager = manager;
    }

    @PostMapping("/create")
    public ResponseEntity<GameInstance> createGame(@RequestBody(required = false) CreateGameRequest req) {
        GameInstance g = manager.createGame();
        return ResponseEntity.ok(g);
    }

    @GetMapping
    public ResponseEntity<List<GameInstance>> listGames() {
        return ResponseEntity.ok(manager.listGames());
    }

    @PostMapping("/{gameId}/join")
    public ResponseEntity<?> joinGame(@PathVariable String gameId,
                                    @CookieValue(value = "userId", required = false) String userId,
                                    @RequestParam(required = false) String name,
                                    HttpServletResponse response
                                    ) {

        boolean joined = false;
        if (userId != null) {
            Optional<GameInstance> oldgi = manager.getGame(gameId);
            if (oldgi.isPresent() && oldgi.get().getPlayers().get(userId) != null) {
                oldgi.get().getPlayers().get(userId).setName(name);
                manager.sendUpdate(gameId);
                joined = true;
            } else {
                manager.leaveGame(userId);
            }
        } 
        //TODO check if same name already joined
        if (!joined) {
            GameManagerService.JoinResult r = manager.joinGame(gameId, name);
            if (!r.ok) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", r.message));
            }
            userId = r.userId;
            gameId = r.gameId;


            // --- START COOKIE FIX ---
        
            // The SameSite attribute cannot be set directly on the Cookie object
            // You must manually construct the Set-Cookie header string.

            // 1. Define the cookie values
            String userIdValue = r.userId;
            int maxAge = 60 * 60 * 24 * 30; // 30 days
            String path = "/";
            
            // 2. Construct the Secure and SameSite=None header string
            // The components must be URL-encoded, but since r.userId is likely a UUID, it's usually safe.
            
            // Constructing the header for userId
            String userIdCookieHeader = String.format(
                "userId=%s; Path=%s; Max-Age=%d; Secure; SameSite=None", 
                userIdValue, 
                path, 
                maxAge
            );
            response.addHeader("Set-Cookie", userIdCookieHeader);
            // --- END COOKIE FIX ---



        }
        // success response (JSON)
        return ResponseEntity.ok(Map.of(
                "success", true,
                "userId", userId,
                "gameId", gameId,
                "message", "joined"
        ));
    }

    @PostMapping("/leave")
    public ResponseEntity<?> leaveGame(@CookieValue(value = "userId", required = false) String userId) {
        if (userId == null) return ResponseEntity.badRequest().body("no userId cookie");
        boolean ok = manager.leaveGame(userId);
        return ok ? ResponseEntity.ok("left") : ResponseEntity.badRequest().body("leave failed");
    }

    @GetMapping("/whoAmI")
    public ResponseEntity<?> getCookieValues(@CookieValue(value = "userId", required = false) String userId) {
        if (userId == null) return ResponseEntity.badRequest().body("no userId cookie");
        String userName = manager.getPlayerName(userId);
        return ResponseEntity.ok(userId + ";" + userName);
    }

    // Add endpoints to get game state, make moves, etc.
    @GetMapping("/{gameId}")
    public ResponseEntity<?> getGame(@PathVariable String gameId) {
        return manager.getGame(gameId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{gameId}/start")
    public ResponseEntity<?> startGame(@PathVariable String gameId,
                                    @CookieValue(value = "userId", required = false) String userId,
                                    HttpServletResponse response
                                    ) {

        if (userId == null) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "not logged in"));
        }

        Optional<GameInstance> gi = manager.getGame(gameId);
        if (!gi.isPresent() || !gi.get().isGameOwner(userId)) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "not game owner"));
        }

        boolean success = manager.startGame(gameId);

        if (!success) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "starting failed"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "gameId", gameId,
                "message", "started"
        ));
    }

    @PostMapping("/{gameId}/ready")
    public ResponseEntity<?> sendReady(@PathVariable String gameId,
                                    @CookieValue(value = "userId", required = false) String userId,
                                    HttpServletResponse response
                                    ) {

        if (userId == null) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "not logged in"));
        }

        Optional<GameInstance> gi = manager.getGame(gameId);
        if (!gi.isPresent()) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "game not found"));
        }

        boolean success = manager.readyPlayer(gameId, userId);

        if (!success) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "readying failed"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "gameId", gameId,
                "message", "started"
        ));
    }

    @PostMapping("/{gameId}/build/{row}/{col}")
    public ResponseEntity<?> build(@PathVariable String gameId,
                                    @PathVariable int row,
                                    @PathVariable int col,
                                    @CookieValue(value = "userId", required = false) String userId,
                                    HttpServletResponse response
                                    ) {

        if (userId == null) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "not logged in"));
        }

        Optional<GameInstance> gi = manager.getGame(gameId);
        if (!gi.isPresent()) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "game not found"));
        }

        boolean success = manager.build(gameId, userId, row, col);

        if (!success) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "building failed"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "gameId", gameId,
                "message", "built"
        ));
    }

    @PostMapping("/{gameId}/buildRoad/{row}/{col}")
    public ResponseEntity<?> buildRoad(@PathVariable String gameId,
                                    @PathVariable int row,
                                    @PathVariable int col,
                                    @CookieValue(value = "userId", required = false) String userId,
                                    HttpServletResponse response
                                    ) {

        if (userId == null) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "not logged in"));
        }

        Optional<GameInstance> gi = manager.getGame(gameId);
        if (!gi.isPresent()) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "game not found"));
        }

        boolean success = manager.buildRoad(gameId, userId, row, col);

        if (!success) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "building failed"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "gameId", gameId,
                "message", "built"
        ));
    }

    @PostMapping("/{gameId}/buyDevelopment")
    public ResponseEntity<?> buyDevelopment(@PathVariable String gameId,
                                    @CookieValue(value = "userId", required = false) String userId,
                                    HttpServletResponse response
                                    ) {

        if (userId == null) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "not logged in"));
        }

        Optional<GameInstance> gi = manager.getGame(gameId);
        if (!gi.isPresent()) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "game not found"));
        }

        boolean success = manager.buyDevelopment(gameId, userId);

        if (!success) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "buying failed"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "gameId", gameId,
                "message", "bougth"
        ));
    }

    @PostMapping("/{gameId}/playDevelopment/{type}/{resType}")
    public ResponseEntity<?> playDevelopment(@PathVariable String gameId,
                                    @PathVariable String type,
                                    @PathVariable String resType,
                                    @CookieValue(value = "userId", required = false) String userId,
                                    HttpServletResponse response
                                    ) {

        if (userId == null) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "not logged in"));
        }

        Optional<GameInstance> gi = manager.getGame(gameId);
        if (!gi.isPresent()) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "game not found"));
        }

        boolean success = manager.playDevelopment(gameId, userId, type, resType);

        if (!success) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "buying failed"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "gameId", gameId,
                "message", "bougth"
        ));
    }

    @PostMapping("/{gameId}/bankTrade/{wood}/{clay}/{wheat}/{wool}/{stone}")
    public ResponseEntity<?> bankTrade(@PathVariable String gameId,
                                    @PathVariable int wood,
                                    @PathVariable int clay,
                                    @PathVariable int wheat,
                                    @PathVariable int wool,
                                    @PathVariable int stone,
                                    @CookieValue(value = "userId", required = false) String userId,
                                    HttpServletResponse response
                                    ) {

        if (userId == null) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "not logged in"));
        }

        Optional<GameInstance> gi = manager.getGame(gameId);
        if (!gi.isPresent()) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "game not found"));
        }

        boolean success = manager.bankTrade(gameId, userId, wood, clay, wheat, wool, stone);

        if (!success) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "trading failed"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "gameId", gameId,
                "message", "traded"
        ));
    }

    @PostMapping("/{gameId}/askPlayerTrade/{wood}/{clay}/{wheat}/{wool}/{stone}")
    public ResponseEntity<?> askPlayerTrade(@PathVariable String gameId,
                                    @PathVariable int wood,
                                    @PathVariable int clay,
                                    @PathVariable int wheat,
                                    @PathVariable int wool,
                                    @PathVariable int stone,
                                    @CookieValue(value = "userId", required = false) String userId,
                                    HttpServletResponse response
                                    ) {

        if (userId == null) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "not logged in"));
        }

        Optional<GameInstance> gi = manager.getGame(gameId);
        if (!gi.isPresent()) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "game not found"));
        }

        boolean success = manager.askPlayerTrade(gameId, userId, wood, clay, wheat, wool, stone);

        if (!success) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "trading failed"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "gameId", gameId,
                "message", "trade started"
        ));
    }

    @PostMapping("/{gameId}/cancelPlayerTrade")
    public ResponseEntity<?> cancelPlayerTrade(@PathVariable String gameId,
                                    @CookieValue(value = "userId", required = false) String userId,
                                    HttpServletResponse response
                                    ) {

        if (userId == null) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "not logged in"));
        }

        Optional<GameInstance> gi = manager.getGame(gameId);
        if (!gi.isPresent()) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "game not found"));
        }

        boolean success = manager.cancelPlayerTrade(gameId, userId);

        if (!success) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "cancel trading failed"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "gameId", gameId,
                "message", "trade successfully cancled"
        ));
    }

    @PostMapping("/{gameId}/acceptPlayerTrade/{wood}/{clay}/{wheat}/{wool}/{stone}")
    public ResponseEntity<?> acceptPlayerTrade(@PathVariable String gameId,
                                    @PathVariable int wood,
                                    @PathVariable int clay,
                                    @PathVariable int wheat,
                                    @PathVariable int wool,
                                    @PathVariable int stone,
                                    @CookieValue(value = "userId", required = false) String userId,
                                    HttpServletResponse response
                                    ) {

        if (userId == null) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "not logged in"));
        }

        Optional<GameInstance> gi = manager.getGame(gameId);
        if (!gi.isPresent()) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "game not found"));
        }

        boolean success = manager.acceptPlayerTrade(gameId, userId, wood, clay, wheat, wool, stone);

        if (!success) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "trading failed"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "gameId", gameId,
                "message", "trade continued"
        ));
    }

    @PostMapping("/{gameId}/declinePlayerTrade/{wood}/{clay}/{wheat}/{wool}/{stone}")
    public ResponseEntity<?> declinePlayerTrade(@PathVariable String gameId,
                                    @PathVariable int wood,
                                    @PathVariable int clay,
                                    @PathVariable int wheat,
                                    @PathVariable int wool,
                                    @PathVariable int stone,
                                    @CookieValue(value = "userId", required = false) String userId,
                                    HttpServletResponse response
                                    ) {

        if (userId == null) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "not logged in"));
        }

        Optional<GameInstance> gi = manager.getGame(gameId);
        if (!gi.isPresent()) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "game not found"));
        }

        boolean success = manager.declinePlayerTrade(gameId, userId, wood, clay, wheat, wool, stone);

        if (!success) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "trading failed"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "gameId", gameId,
                "message", "trade successfully declined"
        ));
    }

    @PostMapping("/{gameId}/finishPlayerTrade/{partnerId}")
    public ResponseEntity<?> finishPlayerTrade(@PathVariable String gameId,
                                    @PathVariable String partnerId,
                                    @CookieValue(value = "userId", required = false) String userId,
                                    HttpServletResponse response
                                    ) {

        if (userId == null) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "not logged in"));
        }

        Optional<GameInstance> gi = manager.getGame(gameId);
        if (!gi.isPresent()) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "game not found"));
        }

        boolean success = manager.finishPlayerTrade(gameId, userId, partnerId);

        if (!success) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "trading failed"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "gameId", gameId,
                "message", "trade finished"
        ));
    }

    @PostMapping("/{gameId}/settleDebt/{wood}/{clay}/{wheat}/{wool}/{stone}")
    public ResponseEntity<?> settleDebt(@PathVariable String gameId,
                                    @PathVariable int wood,
                                    @PathVariable int clay,
                                    @PathVariable int wheat,
                                    @PathVariable int wool,
                                    @PathVariable int stone,
                                    @CookieValue(value = "userId", required = false) String userId,
                                    HttpServletResponse response
                                    ) {

        if (userId == null) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "not logged in"));
        }

        Optional<GameInstance> gi = manager.getGame(gameId);
        if (!gi.isPresent()) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "game not found"));
        }

        boolean success = manager.settleDebt(gameId, userId, wood, clay, wheat, wool, stone);

        if (!success) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "settleing debt failed"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "gameId", gameId,
                "message", "debt settled"
        ));
    }

    @PostMapping("/{gameId}/moveRobber/{oldRow}/{oldCol}/{row}/{col}")
    public ResponseEntity<?> moveRobber(@PathVariable String gameId,
                                    @PathVariable int oldRow,
                                    @PathVariable int oldCol,
                                    @PathVariable int row,
                                    @PathVariable int col,
                                    @CookieValue(value = "userId", required = false) String userId,
                                    HttpServletResponse response
                                    ) {

        if (userId == null) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "not logged in"));
        }

        Optional<GameInstance> gi = manager.getGame(gameId);
        if (!gi.isPresent()) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "game not found"));
        }

        boolean success = manager.moveRobber(gameId, userId, oldRow, oldCol, row, col);

        if (!success) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "moving robber failed"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "gameId", gameId,
                "message", "moved robber"
        ));
    }

    @PostMapping("/{gameId}/chooseVictim/{victimId}")
    public ResponseEntity<?> chooseVictim(@PathVariable String gameId,
                                    @PathVariable String victimId,
                                    @CookieValue(value = "userId", required = false) String userId,
                                    HttpServletResponse response
                                    ) {

        if (userId == null) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "not logged in"));
        }

        Optional<GameInstance> gi = manager.getGame(gameId);
        if (!gi.isPresent()) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "game not found"));
        }

        boolean success = manager.chooseVictim(gameId, userId, victimId);

        if (!success) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "moving robber failed"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "gameId", gameId,
                "message", "moved robber"
        ));
    }
    

    @PostMapping("/{gameId}/endTurn")
    public ResponseEntity<?> endTurn(@PathVariable String gameId,
                                    @CookieValue(value = "userId", required = false) String userId,
                                    HttpServletResponse response
                                    ) {

        if (userId == null) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "not logged in"));
        }

        Optional<GameInstance> gi = manager.getGame(gameId);
        if (!gi.isPresent()) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "game not found"));
        }

        boolean success = manager.endTurn(gameId, userId);

        if (!success) {
            return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "Ending turn failed"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "gameId", gameId,
                "message", "ended"
        ));
    }

}
