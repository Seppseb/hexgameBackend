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
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
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

            Cookie cookie = new Cookie("userId", r.userId);
            cookie.setPath("/");
            cookie.setHttpOnly(false);
            cookie.setMaxAge(60 * 60 * 24 * 30);
            response.addCookie(cookie);
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

    @PostMapping("/{gameId}/endTurn}")
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
                        .body(Map.of("success", false, "message", "building failed"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "gameId", gameId,
                "message", "built"
        ));
    }

}
