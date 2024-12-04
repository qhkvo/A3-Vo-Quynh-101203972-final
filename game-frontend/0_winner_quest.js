// Scenario 4


String input = "yes\n" +
                "1\n" + "3\n" + "5\n" + "7\n" + "9\n" + "11\n" + "quit\n" +       // Stage 1
                "2\n" + "4\n" + "6\n" + "8\n" + "10\n" + "12\n" + "quit\n" +              // Stage 2
                "c\n" + "1\n" + "c\n" + "4\n" + "c\n" + "3\n" + // Players accept quest and trim to 12 cards
                "1\n" + "quit\n" + "quit\n" + "quit\n" + // P2: D5, P3: S10, P4: S10 set up attack
                "1\n" + "1\n";  // No winner, sponsor trims to 12 cards
