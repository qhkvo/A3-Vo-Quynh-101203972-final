package game;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = {"http://127.0.0.1:8081", "http://localhost:8081"})
public class GameController {

    private final Game game;

    public GameController() {
        this.game = new Game();
    }

    // Start the game and initialize decks and players
    @GetMapping("/start")
    public String startGame() {
        game.start(); // This returns the game's start message
        return game.getMessages();
    }


    @GetMapping("/checkForWinnersOrProceed")
    public ResponseEntity<String> checkForWinnersOrProceed(@RequestParam(required = false) String input) {
        try {
            String response = game.checkForWinnersOrProceed(input);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error checking for winners: " + e.getMessage());
        }
    }

    // Handle the initial resolve logic or prompt the next player
    @GetMapping("/resolveEvent")
    public ResponseEntity<String> resolveEvent() {
        try {
            String response = game.resolveEvent(null); // Pass null for the initial step
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error resolving event: " + e.getMessage());
        }
    }

    @GetMapping("/getAllPlayerHand")
    public List<String> getAllPlayerHand() {

            List<String> response = new ArrayList<>();
            for(Player p : game.getPlayers()){
                response.add(String.join(" ",p.displayHand()));
            }
            return response;

    }

    // Process the player's input
    @PostMapping("/resolveEventInput")
    public ResponseEntity<String> resolveEventInput(@RequestBody Map<String, String> request) {
        String input = request.get("input");
        try {
            String response = game.resolveEvent(input); // Pass the user's input to resolveEvent
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing input: " + e.getMessage());
        }
    }



}
