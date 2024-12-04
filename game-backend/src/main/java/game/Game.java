package game;

import java.io.PrintWriter;
import java.util.*;

public class Game {
    private enum GameState {
        IDLE,
        PROMPTING_SPONSORSHIP,
        SETTING_UP_QUEST,
        STARTING_QUEST,
        TRIMMING_HANDS,
        TRIMMING_ONE_HAND,
        PROMPTING_PARTICIPATE,
        BUILDING_ATTACK_CARD,
        END_QUEST
    }

    private GameState currentGameState;
    private List<Player> players;
    private Deck adventureDeck;
    private Deck eventDeck;
     //private int currentPlayerIndex;
    private int initialPlayerIndex;
    private List<Card> discardAPile;
    private List<Card> discardEPile;
    private int sponsorIndex;
    private List<Player> withdrawnParticipants;
    private int stageCount;
    private List<List<Card>> totalStagesCards;
    private List<Player> winners;
    private List<String> messages;
    private boolean isFirstTurn;  // Track if it's the first turn or not]
    private EventCard eventCard1;
    private int playerToAnswerIndex;
    private int currentStage = 0; // Tracks the current stage of the quest
    private int trimmingIndex = 0;
    private Player playerToTrim = new Player();
    private List<Player> staticPlayersToTrim = new ArrayList<>();
    private int currentParticipantIndex = 0;
    private List<Player> eligibleParticipants;
    private List<Card> weaponCards;
    private List<Card> chosenCards = new ArrayList<>();
    private boolean hasFinishedPromptParticipation = false;
    private List<Player> successfulParticipants = new ArrayList<>();
    private List<Player> originalEligibleParticipantsSize ;
    private int promptCount;
    private boolean lastParticipant = false;
    private List<Player> updatedEligibleList = new ArrayList();

    // Constructor to initialize players and set up decks
    public Game() {
        currentGameState = GameState.IDLE;
        players = new ArrayList<>();
        discardAPile = new ArrayList<>();
        discardEPile = new ArrayList<>();
        withdrawnParticipants = new ArrayList<>();
        stageCount = 0;
        currentStage = 0;
        totalStagesCards = new ArrayList<>();
        winners = new ArrayList<>();
        isFirstTurn = true;
        messages = new ArrayList<>();
        playerToAnswerIndex = -1; // Tracks the player being prompted for sponsorship
        promptCount = 0;

        // Create 4 players
        for (int i = 0; i < 4; i++){
            players.add(new Player());
        }

        // Initialize the decks
        setUpDecks();
    }

    public void start() {
        setUpDecks();
        distributeCards();
//        while (winners.isEmpty()) {
//            playTurn();
//            checkForWinnersOrProceed();
//        }
//        if (isFirstTurn) {
//            messages.add("Welcome to the game! This is the first turn.");
//            isFirstTurn = false;
//        }
        messages.add("Game started successfully! Players have been dealt their cards.");
        playTurn();
    }


    // Set up adventure and event decks
    public void setUpDecks() {
        // Initialize the adventure deck with 100 cards
        adventureDeck = new Deck("Adventure", 100);
        adventureDeck.shuffle();
        // Initialize the event deck with 12 quest cards and 5 event cards
        eventDeck = new Deck("Event", 17);
        eventDeck.shuffle();
//        List<Card> test = Arrays.asList(new Card("Queen’s Favor", 0 ));
         List<Card> test = Arrays.asList(new Card("Q2", 2 ));
        eventDeck.addOnTop(test);
    }

    // Distribute 12 cards
    public void distributeCards () {
        for (Player player : players) {
            for (int i = 0; i < 12; i++) {
                player.addCardToHand(adventureDeck.drawCard());
            }
        }
    }

    // Display current player’s turn, hand and the drawn event card
    public void playTurn() {
        messages.clear();

        try {
//            if (isFirstTurn) {
//                messages.add("Processing first turn logic...");
//                isFirstTurn = false;
//            }

            Player currentPlayer = players.get(initialPlayerIndex);
            messages.add("Player " + (players.indexOf(currentPlayer) + 1) + "'s turn");

            currentPlayer.sortHand();
            messages.addAll(currentPlayer.displayHand());

            Card eventCard = eventDeck.drawCard();
            eventCard1 = new EventCard(eventCard.getType(), eventCard.getValue());

            messages.add("Event card drawn: " + eventCard);
            currentGameState = GameState.IDLE;

        } catch (Exception e) {
            messages.add("Error during playTurn: " + e.getMessage());
        }
    }

    // Resolve the event or quest card drawn
    public String resolveEvent(String input) {
        if (currentGameState == GameState.IDLE) {
            if (eventCard1.isQuest()) {
                messages.add("---> A Quest is drawn <---");
                stageCount = eventCard1.getValue();
                currentGameState = GameState.PROMPTING_SPONSORSHIP;
                playerToAnswerIndex = initialPlayerIndex; // Start with the initial player
                // Starting with the current player
                if (input == null) {
                    messages.add("Player " + (playerToAnswerIndex + 1) + ", would you like to sponsor this quest? (yes/no)");
                    return String.join("\n", messages);
                }
            } else {
                if (input == null) {
                    messages.add(("---> A " + eventCard1.getType() + " event is drawn <---"));
                    String eventMessages = eventCard1.applyEvent(players.get(initialPlayerIndex), players, adventureDeck);
                    messages.add(eventMessages);

                    staticPlayersToTrim = trimHandForAll();
                    trimmingIndex = 0;
                    if (!staticPlayersToTrim.isEmpty()) {
                        currentGameState = GameState.TRIMMING_HANDS;
                        return prepareTrimmingMessage(staticPlayersToTrim.get(trimmingIndex));
                    }
                }
                currentGameState = GameState.IDLE;
                return String.join("\n", messages);
            }
            discardEPile.add(eventCard1);
        } else if (currentGameState == GameState.TRIMMING_HANDS) {
            return handleTrimmingPlayers(input);
        } else if (currentGameState == GameState.TRIMMING_ONE_HAND) {
            return trimHandTo12(input, playerToTrim);
        } else if (currentGameState == GameState.PROMPTING_SPONSORSHIP) {
            return promptForSponsorship(input, stageCount);
        } else if (currentGameState == GameState.SETTING_UP_QUEST) {
            Player sponsor = players.get(sponsorIndex);
            String result = sponsor.setUpQuest(input, totalStagesCards, stageCount, currentStage);

            if (sponsor.hasFinishedQuestSetup()) {
                currentStage = 0;
                currentGameState = GameState.STARTING_QUEST;
                return resolveEvent(null);
            } else {
                currentStage = sponsor.getCurrentStage();
            }
            return result;

        } else if (currentGameState == GameState.STARTING_QUEST) {
            return startQuest(input, totalStagesCards);

        } else if (currentGameState == GameState.PROMPTING_PARTICIPATE) {
//            if ((promptCount + 1) == originalEligibleParticipantsSize) {
//                messages.add("MOVE TO BUILD ATTACKS");
//                currentGameState = GameState.BUILDING_ATTACK_CARD;
//                promptCount = 0;
//                return null;
//            }
            return promptParticipantsForQuestStage(input, currentStage);
        } else if (currentGameState == GameState.BUILDING_ATTACK_CARD) {
            System.out.println("PRINT");
            return buildAttackForParticipant(input);
        } else {
            messages.add("Error: Unexpected game state.");
            return String.join("\n", messages);
        }
        return String.join("\n", messages);
    }

    public List<Player> trimHandForAll() {
        List<Player> playersToTrim = new ArrayList<>();
        for (Player player : getPlayers()) {
            if (player.getHandSize() > 12) {
                playersToTrim.add(player);
            }
        }
        return playersToTrim;
    }

    private String prepareTrimmingMessage(Player player) {
        messages.clear();
        messages.add("!ATTENTION PLAYER " + (players.indexOf(player) + 1) + " : Your hand contains more than 12 cards. Please choose a card number (1 to " + player.getHandSize() + ") to discard:");
        player.sortHand();
        messages.addAll(player.displayHand());
        return String.join("\n", messages);
    }


    private String handleTrimmingPlayers(String input) {
        if (trimmingIndex < staticPlayersToTrim.size()) {
            Player currentPlayer = staticPlayersToTrim.get(trimmingIndex);

            if (input == null || input.trim().isEmpty()) {
                // Wait for the next input
                return prepareTrimmingMessage(currentPlayer);
            }

            // Process the input for the current player
            String trimMessage = trimHandTo12SingleStep(input, currentPlayer);
            messages.add("Your hand has been trimmed to 12 cards.");

            if (currentPlayer.getHandSize() <= 12) {
                trimmingIndex++; // Move to the next player
                if (trimmingIndex < staticPlayersToTrim.size()) {
                    Player nextPlayer = staticPlayersToTrim.get(trimmingIndex);
                    trimMessage += "\n" + prepareTrimmingMessage(nextPlayer);
                } else {
                    currentGameState = GameState.IDLE;
                    trimMessage += "\nAll players have trimmed their hands.";
                }
            }

            return trimMessage;
        } else {
            currentGameState = GameState.IDLE;
            return "All players have trimmed their hands.";
        }
    }


    public String trimHandTo12SingleStep(String input, Player player) {
        messages.clear();
        System.out.println("input: " + input);
        try {
            int cardToDiscard = Integer.parseInt(input);

            if (cardToDiscard >= 1 && cardToDiscard <= player.getHandSize()) {
                Card removedCard = player.getHand().remove(cardToDiscard - 1);
                messages.add("You have discarded: " + removedCard);
                discardChosenCards(Collections.singletonList(removedCard));
            } else {
                messages.add("Invalid input. Please choose a valid card number (1 to " + player.getHandSize() + "):");
            }
        } catch (NumberFormatException e) {
            messages.add("Invalid input. Please enter a valid card number.");
        }

        if (player.getHandSize() > 12) {
            return prepareTrimmingMessage(player);
        }

        player.sortHand();
        messages.addAll(player.displayHand());
        return String.join("\n", messages);
    }

    // Trim the player's hand to 12 cards
    public String trimHandTo12(String input, Player player) {
        System.out.println("playerToTrim: " + (players.indexOf(playerToTrim) + 1));
        System.out.println("input trim: " + input);
        messages.clear();

        if (input == null || input.trim().isEmpty()) {
            return prepareTrimmingMessage(player);
        }

        try {
            int cardToDiscard = Integer.parseInt(input);
            System.out.println("cardToDiscard: " + cardToDiscard);

            if (cardToDiscard >= 1 && cardToDiscard <= player.getHandSize()) {
                Card removedCard = player.getHand().remove(cardToDiscard - 1);
                System.out.println("You have discarded: " + removedCard);
                messages.add("You have discarded: " + removedCard);
                discardChosenCards(Collections.singletonList(removedCard));
            } else {
                messages.add("Invalid input. Please choose a valid card number (1 to " + player.getHandSize() + "):");
            }
            // return String.join("\n", messages);
        } catch (NumberFormatException e) {
            messages.add("Invalid input. Please enter a valid card number.");
        }

        player.sortHand();
        messages.addAll(player.displayHand());

        if (playerToTrim.getHandSize() <=12) {
            messages.add("Your hand has been trimmed to 12 cards.");
            System.out.println("Your hand has been trimmed to 12 cards.");
            currentParticipantIndex = (currentParticipantIndex + 1);
            currentGameState = GameState.PROMPTING_PARTICIPATE;

            if (currentParticipantIndex == eligibleParticipants.size()) {
                System.out.println("HERRE " + messages);
                messages.add("MOVE TO BUILD ATTACKS");
                currentGameState = GameState.BUILDING_ATTACK_CARD;
                promptCount = 0;

                 resolveEvent(null);
                 return String.join("\n", messages);
            }
            return resolveEvent(null);
        }
        return String.join("\n", messages);
    }

    // A player chooses to sponsor the quest or not, starting from current player
    public String promptForSponsorship(String input, int stageCount) {
        messages.clear();
        Player currentPlayer = players.get(playerToAnswerIndex);
        if (currentGameState == GameState.PROMPTING_SPONSORSHIP) {
            if (input.equalsIgnoreCase("yes")) {
                messages.add("Player " + (playerToAnswerIndex + 1) + " has decided to sponsor the quest.");

                if (currentPlayer.canSponsorQuest(stageCount)) {
                    messages.add("Player " + (playerToAnswerIndex + 1) + " has enough cards to sponsor the quest.");
                    messages.add("---> A SPONSOR FOUND: " + "Player " + (playerToAnswerIndex + 1));
                    sponsorIndex = playerToAnswerIndex;  // Set the sponsor index

                    currentGameState = GameState.SETTING_UP_QUEST;
//                    return String.join("\n", messages);
                    return resolveEvent(null);
                } else {
                    messages.add("Player " + (playerToAnswerIndex + 1) + " does not have enough cards to sponsor the quest.");
                }
            } else if (input.equalsIgnoreCase("no")) {
                messages.add("Player " + (playerToAnswerIndex + 1) + " has decided to decline the quest.");
            } else {
                messages.add("Invalid input. Please input yes/no.");
                return String.join("\n", messages);
            }
        }

        // Move to the next player in turn
        playerToAnswerIndex = (playerToAnswerIndex + 1) % players.size();

        // Check if all players have been asked
        if (playerToAnswerIndex == initialPlayerIndex) {
            messages.add("No players agreed to sponsor the quest.");
            handleNoSponsorship(input);
            currentGameState = GameState.IDLE; // No sponsorship, return to idle state
        }
        return String.join("\n", messages);
    }

    public void handleNoSponsorship(String input) {
        messages.add("No one agreed to sponsor the quest. The quest has failed.");
        endCurrentPlayerTurn(input);
    }

    // End the current player's turn and move to the next player
    public String endCurrentPlayerTurn(String input) {
        messages.add("Player " + (initialPlayerIndex + 1) + "'s turn is over.");
        messages.add("Press enter to end your turn.");

        initialPlayerIndex = (initialPlayerIndex + 1) % players.size();
        return String.join("\n", messages);
    }

    // Handles when a player continues the quest
    public String handlePlayerContinuesQuest(Player player, Deck adventureDeck) {
        // Step 1: Draw an adventure card
        Card drawnCard = adventureDeck.drawCard();
        player.addCardToHand(drawnCard);
        messages.add("Player " + (players.indexOf(player) + 1) + " drew: " + drawnCard);
        player.sortHand();

        if (player.getHandSize() > 12) {
            currentGameState = GameState.TRIMMING_ONE_HAND;
            System.out.println(currentGameState);
            return String.join("\n", messages);
        }
        messages.add("Your hand has been trimmed to 12 cards.");
        //currentParticipantIndex = (currentParticipantIndex + 1) % eligibleParticipants.size();
        currentGameState = GameState.PROMPTING_PARTICIPATE;
        //return resolveEvent(null);  // This triggers the attack phase for the first player

        //messages.add("Player " + (players.indexOf(player) + 1) + " is setting up their attack.");
        return String.join("\n", messages);
    }

    public String promptParticipantsForQuestStage(String input, int stageNumber) {
        messages.clear();
        // Check if there are no participants left
        if (eligibleParticipants.isEmpty()) {
            messages.add("MOVE TO BUILD ATTACKS");
            currentGameState = GameState.BUILDING_ATTACK_CARD;
            promptCount = 0;
            //updatedEligibleList = eligibleParticipants;
            return resolveEvent(null);
        }

        System.out.println("currentParticipantIndex " + currentParticipantIndex);

        // Get the current participant
        playerToTrim = eligibleParticipants.get(currentParticipantIndex);

        if (input == null) {
            messages.add("Player " + (players.indexOf(playerToTrim) + 1) +
                    ", would you like to continue or withdraw from the quest? (c/w)");
            return String.join("\n", messages);
        }

        // Validate input
        if (!input.equalsIgnoreCase("c") && !input.equalsIgnoreCase("w")) {
            messages.add("Invalid input. Please choose 'c' to continue or 'w' to withdraw.");
            return String.join("\n", messages);
        }

        // displayEligibleParticipants(eligibleParticipants);
        // Process decision
        if (input.equalsIgnoreCase("w")) {
            promptCount++;
            messages.add("Player " + (players.indexOf(playerToTrim) + 1) + " has withdrawn from the quest.");
            withdrawnParticipants.add(playerToTrim);
            eligibleParticipants.remove(playerToTrim);
            System.out.println("PLAYER W " + eligibleParticipants);
            return resolveEvent(null);
        } else {
            promptCount++;
            messages.add("Player " + (players.indexOf(playerToTrim) + 1) + " is tackling the current stage.");
            Card drawnCard = adventureDeck.drawCard();
            playerToTrim.addCardToHand(drawnCard);
            messages.add("Player " + (players.indexOf(playerToTrim) + 1) + " drew: " + drawnCard);
            playerToTrim.sortHand();

            if (playerToTrim.getHandSize() > 12) {
                currentGameState = GameState.TRIMMING_ONE_HAND;
                System.out.println(currentGameState);
                return resolveEvent(null);
            }

//            currentParticipantIndex = (currentParticipantIndex + 1);
//            if (currentParticipantIndex == eligibleParticipants.size()) {
//                System.out.println("HERRE " + messages);
//                messages.add("MOVE TO BUILD ATTACKS");
//                currentGameState = GameState.BUILDING_ATTACK_CARD;
//                promptCount = 0;
//
//                resolveEvent(null);
//                return String.join("\n", messages);
//            }
            //eligibleParticipants.remove(playerToTrim);
            //updatedEligibleList.add(playerToTrim);
        }
            currentGameState = GameState.PROMPTING_PARTICIPATE;
             return resolveEvent(null);
    }

    private String buildAttackForParticipant(String input) {
        messages.clear();
        System.out.println("input build attack: " + input);
        int attackValue = 0;
        List<Card> stageCards = totalStagesCards.get(currentStage - 1);
        int stageValue = calculateStageValue(stageCards);
//        if (weaponCards.isEmpty()) {
//            messages.add("You have no weapon cards available. Attack value will be 0.");
//            return String.join("\n", messages);
//        }

        if(input == null){
//            System.out.println("hreihfeiorheo");
            messages.add("Resolving attacks for Stage " + (currentStage) + " (Stage value: " + stageValue + "):");
            messages.add("Player " + (players.indexOf(playerToTrim) + 1) + ", please build your attack.");
            displayWeaponCards(weaponCards);
            return String.join("\n", messages);
        }


        try {
            int cardPosition = Integer.parseInt(input);

            if (cardPosition < 1 || cardPosition > weaponCards.size()) {
                messages.add("Invalid position. Please choose a valid card number (1 to " + weaponCards.size() + ").");
            } else {
                Card selectedCard = weaponCards.get(cardPosition - 1);

                // Check if the card was already chosen
                boolean alreadyChosen = false;
                for (Card chosenCard : chosenCards) {
                    if (chosenCard.getType().equals(selectedCard.getType())) {
                        alreadyChosen = true;
                        break;
                    }
                }

                if (alreadyChosen) {
                    messages.add("You have already selected this card. Please choose a different card.");
                } else {
                    playerToTrim.addToAttack(selectedCard);
                    chosenCards.add(selectedCard);
                    weaponCards.remove(cardPosition - 1);
                    messages.add("You have selected " + selectedCard + " for your attack.");
                    displayWeaponCards(weaponCards);
                }
            }
        } catch (NumberFormatException e) {
            // If the input is not a valid number, prompt the player again
            messages.add("Invalid input. Please enter a valid number (1 to " + weaponCards.size() + ") or type 'Quit'.");
        }

        if (input.equalsIgnoreCase("quit")) {
            messages.add("You have chosen to quit the attack phase.");
            messages.add("Your final attack includes the following cards:");
            for (Card card : chosenCards) {
                messages.add("- " + card);
            }
            messages.add("Your final attack value is: " + playerToTrim.getAttackValue());
            discardUsedAttackCards(playerToTrim);
            resolveStage(playerToTrim.getAttackValue(), stageValue, currentStage, stageCount);
            chosenCards.clear();
        }
        return String.join("\n", messages);
    }

    public void resolveStage( int attackValue, int stageValue, int stageNumber, int totalStages) {

        // Check if player succeeds or is eliminated
        if (attackValue < stageValue) {
            messages.add("Player " + (players.indexOf(playerToTrim) + 1) + " is eliminated with an attack value of " + attackValue);
            messages.add("Player " + (players.indexOf(playerToTrim) + 1) + " earns 0 shields.");
            withdrawnParticipants.add(playerToTrim);
            messages.add(playerToTrim.displayUpdatedHand());
        } else {
            messages.add("Player " + (players.indexOf(playerToTrim) + 1) + " succeeds with an attack value of " + attackValue);
            messages.add(playerToTrim.displayUpdatedHand());

            if (stageNumber == totalStages) {
                // If this is the last stage, the player wins the quest
                playerToTrim.addShields(totalStages);
                withdrawnParticipants.clear();
                messages.add("Player " + (players.indexOf(playerToTrim) + 1) + " wins the quest.");
                messages.add("Player " + (players.indexOf(playerToTrim) + 1) + " earns " + totalStages + " shields.");
            } else {
                successfulParticipants.add(playerToTrim);  // Move the player to the next stage
            }
        }

        // Update eligible participants for the next round
        if (withdrawnParticipants.size() == 4) {
            messages.add("All participants have been eliminated.");
            eligibleParticipants.clear();
        } else {
            eligibleParticipants.clear();
            eligibleParticipants.addAll(successfulParticipants);
            messages.add("Participants remaining for the next stage:");
            for (Player player : eligibleParticipants) {
                messages.add("Player " + (players.indexOf(player) + 1));
            }
        }
    }


    // Helper method to discard used attack cards after each stage
    private String discardUsedAttackCards(Player player) {
        List<Card> usedCards = player.getSelectedAttackCards();
        discardChosenCards(usedCards);
        player.getHand().removeAll(usedCards);

        messages.add("Player " + (players.indexOf(player) + 1) + " discarded the following attack cards:");
        for (Card card : usedCards) {
            messages.add("- " + card);
        }
        return String.join("\n", messages);
    }

    public String startQuest(String input, List<List<Card>> sponsorBuiltStages) {
        messages.clear();

        int totalStages = sponsorBuiltStages.size();
        List<Card> stageCards = sponsorBuiltStages.get(currentStage);

        Player sponsor = players.get(sponsorIndex);
        eligibleParticipants = new ArrayList<>(players);
        eligibleParticipants.remove(sponsor); // Sponsor doesn't participate
        withdrawnParticipants.add(sponsor);

        originalEligibleParticipantsSize = eligibleParticipants;

        System.out.println("Current stage: " + currentStage);
        System.out.println("Current stage sponsor cards: " + stageCards);

        // Initialize eligible participants and current stage on the first call
        if (currentStage == 0) {

            if (eligibleParticipants.isEmpty()) {
                currentGameState = GameState.IDLE;
                return "No players are eligible to participate in this quest. The quest is canceled.";
            }

            currentStage = 1; // Start at stage 1
            messages.add("Quest begins! There are " + sponsorBuiltStages.size() + " stages.");
        }


            // Handle participant decisions for the current stage
        if (input == null) {
            currentGameState = GameState.PROMPTING_PARTICIPATE;
            displayEligibleParticipants(eligibleParticipants);
            Player currentPlayer = eligibleParticipants.get(currentParticipantIndex);
            messages.add("Player " + (players.indexOf(currentPlayer) + 1) +
                    ", would you like to continue or withdraw from the quest? (c/w)");
//            hasFinishedPromptParticipation = true;
            return String.join("\n", messages);
        }


//        String participantResult = promptParticipantsForQuestStage(input, eligibleParticipants, currentStage);

         // resolveStage(input, eligibleParticipants, stageCards, currentStage, totalStages);

        // If participants are still eligible, handle the current stage
//        if (currentParticipantIndex == 0 && input != null) { // After all participants are processed
//            messages.add("---> Stage " + currentStage + " Completed <---");
//            messages.add("Stage cards were:");
//            for (Card card : stageCards) {
//                messages.add(card.toString());
//            }
//
//            currentStage++;
//
//            // If all stages are completed
//            if (currentStage > sponsorBuiltStages.size()) {
//                currentGameState = GameState.IDLE;
//                return String.join("\n", messages) + "\nThe quest has been successfully completed!";
//            }
//        }

        return String.join("\n", messages) + "\n" ;
    }

    // Calculate the total value of a stage based on its cards
    private int calculateStageValue(List<Card> stageCards) {
        return stageCards.stream().mapToInt(Card::getValue).sum(); // Sum up the card values
    }

    public void displayEligibleParticipants(List<Player> eligibleParticipants) {
        if (eligibleParticipants.isEmpty()) {
            messages.add("No players are eligible to participate in this quest.");
        } else {
            messages.add("Eligible participants for the quest:");
            for (Player player : eligibleParticipants) {
                int playerNumber = players.indexOf(player) + 1;
                messages.add("Player " + playerNumber);
            }
        }
    }

    public String endQuest(List<List<Card>> stages, Deck adventureDeck) {
        messages.add("---> THE QUEST HAS ENDED <---");
        Player sponsor = players.get(sponsorIndex);

        // Sponsor discards all cards used to build the quest
        for (List<Card> stage : stages) {
            discardChosenCards(stage);
        }
        messages.add("Sponsor discarded: " + stages);

        // Calculate the total number of cards discarded by the sponsor
        int totalDiscardedCards = stages.stream().mapToInt(List::size).sum();
        int cardsToDraw = totalDiscardedCards + stages.size();  // Cards to draw = discarded cards + number of stages

        messages.add("Sponsor will draw " + cardsToDraw + " new cards.");

        // Sponsor draws the cards
        for (int i = 0; i < cardsToDraw; i++) {
            Card drawnCard = adventureDeck.drawCard();
            if (drawnCard != null) {
                sponsor.addCardToHand(drawnCard);
                messages.add("Sponsor drew: " + drawnCard);
            } else {
                // If the deck is empty, reshuffle the discard pile and put it back into the adventure deck
                messages.add("No more cards left in the adventure deck to draw. Reshuffling the discard pile.");
                adventureDeck.reshuffle(discardAPile);
                i--;
            }
        }
        sponsor.sortHand();
        if (sponsor.getHandSize() > 12) {
            messages.add("Sponsor has more than 12 cards and needs to trim their hand.");
            currentGameState = GameState.TRIMMING_HANDS;
            return resolveEvent(null);
        }
        withdrawnParticipants.clear();
        currentGameState = GameState.IDLE;
        return resolveEvent(null);
    }

    // Filter weapon cards from the player's hand
    private List<Card> filterWeaponCards(List<Card> hand) {
        List<Card> weaponCards = new ArrayList<>();
        for (Card card : hand) {
            if (isWeaponCard(card)) {
                weaponCards.add(card);
            }
        }
        return weaponCards;
    }

    // Check if a card is a weapon card
    private boolean isWeaponCard(Card card) {
        return card.getType().startsWith("D") || card.getType().startsWith("H") ||
                card.getType().startsWith("S") || card.getType().startsWith("B") ||
                card.getType().startsWith("L") || card.getType().startsWith("E");
    }

    // Display available weapon cards
    private String displayWeaponCards(List<Card> weaponCards) {
        messages.add("Weapon cards in your hand:");
        for (int i = 0; i < weaponCards.size(); i++) {
            messages.add((i + 1) + ". " + weaponCards.get(i));
        }
        return String.join("\n", messages);
    }

    // Discard the selected cards after building the attack
    private void discardChosenCards(List<Card> chosenCards) {
        discardAPile.addAll(chosenCards);
    }

    public String checkForWinnersOrProceed(String input) {
        for (Player player : players) {
            if (player.getTotalShield() >= 7) {
                winners.add(player);
            }
        }

        if (!winners.isEmpty()) {
            messages.add("---> We have one or more winners! <---");
            for (Player winner : winners) {
                messages.add("Player " + (players.indexOf(winner) + 1) + " has won the game with " + winner.getTotalShield() + " shields!");
            }

            messages.add("---> All players' total shields after Quest game: ");
            for (Player player : players) {
                messages.add("Player " + (players.indexOf(player) + 1) + ": " + player.getTotalShield() + " shields");
            }
        } else {
            messages.add("No players have won yet. Moving to the next player's turn.");
            endCurrentPlayerTurn(input);
            playTurn();
        }
        return String.join("\n", messages);
    }

    //Getters
    public int getAdventureDeckSize() { return adventureDeck.size(); }
    public int getEventDeckSize() { return eventDeck.size(); }
    public Deck getAdventureDeck() { return adventureDeck; }
    public Deck getEventDeck() { return eventDeck; }
    public List<Card> getDiscardAPile() { return discardAPile; }
    public int getTotalStagesCount() { return stageCount;}
    public List<List<Card>> getTotalStagesCards() { return totalStagesCards;}

    public List<Player> getWinners() {
        return winners;
    }
    public boolean isGameOver() {
        return !winners.isEmpty();
    }

    // Getter for players
    public List<Player> getPlayers() { return players; }
//    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
//    public Player getCurrentPlayer() { return players.get(currentPlayerIndex); }
    public String getMessages() { return String.join("\n", messages);}

    // Setters
//    public void setCurrentPlayerIndex(int index) {
//        // Ensure the index is within the valid range of players
//        if (index >= 0 && index < players.size()) {
//            this.currentPlayerIndex = index;
//        } else {
//            throw new IllegalArgumentException("Invalid player index: " + index);
//        }
//    }
//
//    public static void main(String[] args) {
//        Game game = new Game();
//        Scanner input = new Scanner(System.in);
//        PrintWriter output = new PrintWriter(System.out, true);
//
//        game.start();
//    }
}