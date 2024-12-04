// Scenario 3

p1.addCardToHand(
        p1.addCardToHand( new Card("F5", 5));
        p1.addCardToHand( new Card("F5", 5));
        p1.addCardToHand( new Card("F10",10));
        p1.addCardToHand( new Card("F10",10));
        p1.addCardToHand( new Card("F15", 15));
        p1.addCardToHand( new Card("F15", 15));
        p1.addCardToHand( new Card("F20", 20));
        p1.addCardToHand( new Card("F20", 20));
        p1.addCardToHand( new Card("D5", 5));
        p1.addCardToHand( new Card("D5", 5));
        p1.addCardToHand( new Card("D5", 5));
        p1.addCardToHand( new Card("D5", 5));

        p2.addCardToHand(new Card("F25", 25));
        p2.addCardToHand(new Card("F30", 30));
        p2.addCardToHand(new Card("S10", 10));
        p2.addCardToHand(new Card("S10", 10));
        p2.addCardToHand(new Card("S10", 10));
        p2.addCardToHand(new Card("H10", 10));
        p2.addCardToHand(new Card("H10", 10));
        p2.addCardToHand(new Card("B15", 15));
        p2.addCardToHand(new Card("B15", 15));
        p2.addCardToHand(new Card("L20",20));
        p2.addCardToHand(new Card("L20", 20));
        p2.addCardToHand(new Card("E30",30));

        p3.addCardToHand(new Card("F5", 5));
        p3.addCardToHand(new Card("F5", 5));
        p3.addCardToHand(new Card("F5", 5));
        p3.addCardToHand(new Card("F5", 5));
        p3.addCardToHand(new Card("D5",  5));
        p3.addCardToHand(new Card("D5", 5));
        p3.addCardToHand(new Card("D5", 5));
        p3.addCardToHand(new Card("H10", 10));
        p3.addCardToHand(new Card("H10", 10));
        p3.addCardToHand(new Card("H10", 10));
        p3.addCardToHand(new Card("H10", 10));
        p3.addCardToHand(new Card("H10", 10));

        p2.addCardToHand(new Card("F50", 50));
        p2.addCardToHand(new Card("F70", 70));
        p2.addCardToHand(new Card("S10", 10));
        p2.addCardToHand(new Card("S10", 10));
        p2.addCardToHand(new Card("S10", 10));
        p2.addCardToHand(new Card("H10", 10));
        p2.addCardToHand(new Card("H10", 10));
        p2.addCardToHand(new Card("B15", 15));
        p2.addCardToHand(new Card("B15", 15));
        p2.addCardToHand(new Card("L20", 20));
        p2.addCardToHand(new Card("L20",20));
        p2.addCardToHand(new Card("E30",30));

String input = "yes\n" + // P1 sponsors the quest
                "1\nquit\n" + // Stage 1
                "3\nquit\n" + // Stage 2
                "5\nquit\n" + // Stage 3
                "7\nquit\n" + // Stage 4
                "c\n" + "1\n" + "c\n" + "1\n" + "c\n" + "1\n" + // Players accept quest and trim to 12 cards
                "1\n" + "quit\n" + "1\n" + "quit\n" + "1\n" + "quit\n" + // P2,3,4 set up attack
                "c\n" + "c\n" + "c\n" +// P2,3,4 continue to stage 2
                "1\n" + "quit\n" + "1\n" + "quit\n" + "1\n" + "quit\n" +// P2,3,4 set up attack stage 2
                "c\n" + "c\n" + "c\n" + // P2,3,4 continue to stage 3
                "3\n" + "quit\n" + "3\n" + "quit\n" + "3\n" + "quit\n" + // P2,3,4 set up attack stage 3
                "c\n" + "c\n" +  "c\n" + // P2,3,4 continue to stage 4
                "5\n" + "quit\n" + "5\n" + "quit\n" + "5\n" + "quit\n" +  // P2,3,4 set up attack stage 4
                "1\n" + "1\n" + "2\n" + "2\n" +  // Sponsor trims to 12 cards

                // Event cards
                "\n" + // P2 drew Plague
                "1\n" + "1\n" + // P1 trim after Prosperity
                "1\n" + // P2 trim
                "1\n" + // P3 trim
                "1\n" + // P4 trim
                "2\n" + "4\n" + // P4 trim after Queen's Favor
                "\n" +
                // Next Quest
                "yes\n" +    // P1 sponsors
                "1\n" + "quit\n" +              // Stage 1: F10
                "2\n" + "9\n" + "quit\n" +              // Stage 2: F15
                "6\n" + "10\n" + "quit\n" +              // Stage 3: F20
                "c\n" + "1\n"  + "c\n" + "1\n" + "c\n" + "1\n" +        // P2,3,4 participate stage 1
                "6\n" + "quit\n" + "5\n" + "quit\n" + "5\n" + "quit\n" + // P2,3,4 set up attack Stage 1
                "c\n" + "c\n" + // P2,3 continue to stage 2
                "7\n" + "5\n" + "quit\n" + "6\n" +  "1\n" +"quit\n" + // P2,3 set up attack stage 2
                "c\n" + "c\n" + // P2,3 continue to stage 3
                "6\n" + "1\n" + "quit\n" + "6\n" + "quit\n" + // P2,3 set up attack stage 3
                "1\n" + "1\n" + "1\n";  // Sponsor trims to 12 cards;