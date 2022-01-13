package Hangman;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.FileReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Hangman {

    /**
     * initialize the text box for the player's guesses
     */
    public static List<String> dashes = new ArrayList<>();

    /**
     * uses a HTTP GET request to a Heroku app that generates a random word - thus not requiring memory to store a large array of strings
     * @return the word the player must find
     * @throws Exception connection timeout
     */
    public static String wordBank() throws Exception {
        StringBuilder word = new StringBuilder();
        URL url = new URL("https://random-word-api.herokuapp.com/word?number=1");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            for (String line; (line = reader.readLine()) != null; ) {
                word.append(line);
            }
        }
        return word.toString();
    }

    /**
     * logic to allow the program to iterate through the player's guesses and fill the text box accordingly
     * @param word the word of interest
     * @param guess the player's current guess
     * @return hacky and non-ideal way to return the text box each time
     */
    public static String wordBuilder(String word, String guess) {

        ArrayList<String> wordList = new ArrayList<>();

        for (int i = 0; i < word.length(); i++) {
            dashes.add("_ ");
            wordList.add(String.valueOf(word.charAt(i)));
        }

        dashes = dashes.subList(0, word.length());

        int i = 0;

        while (wordList.contains(guess) && i < dashes.size()) {
            if (wordList.get(i).equals(guess)) {
                dashes.set(i, guess);
            }
            i++;
        }

        return dashes.toString().replaceAll(",", "");
    }

    /**
     * provides the user a hint by filling in odd numbered letters
     * @param getWord the word of interest
     * @return a text box with odd numbered letters filled in
     */
    public static String hint(String getWord){

        StringBuilder hint = new StringBuilder();
        int length = (getWord.length() % 2 == 0) ? getWord.length() : getWord.length() + 1;

        for (int i = 0; i < length; i += 2){
            hint.append(getWord.charAt(i)).append(" _ ");
        }

        return hint.toString();
    }

    /**
     * parses a text file containing ascii art to build the hangman graphic
     * @param wrongs number of wrong guesses which will change the graphic
     * @return the graphic without any text
     */
    public static String graphicBuilder(int wrongs){

        StringBuilder line = new StringBuilder();

        try {
            LineNumberReader br = new LineNumberReader((new FileReader("graphic.txt")));
            br.skip((47L * wrongs) + wrongs);

            for (int i = 0; i < 6; i++) {
                line.append(br.readLine()).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return line.toString();
    }

    /**
     * allows the user to enter "lexicondevil" to solve the game
     * @param getWord the word of interest
     * @return a completed word, then the game ends
     */
    public static String cheatCode(String getWord){
        return "The word is.... +\n" + "\n" + "~~~~" + getWord.toUpperCase(Locale.ROOT) + "~~~~";
    }

    /**
     * takes user's input for gameplay and calls wordBuilder method
     * @param getWord accepts auto-generated word
     * @param wrongs number of user's wrong attempts
     */
    public static void game(String getWord, int wrongs) {
        Scanner sc = new Scanner(System.in);
        int numHints = 0;

        while (true) {
            System.out.print("\n\nGuess a letter: ");
            String guess = sc.next();
            if (guess.equals("lexicondevil")) {
                System.out.println(cheatCode(getWord));
                break;
            }

            String block = wordBuilder(getWord, guess);

            if (!getWord.contains(guess)) {
                wrongs++;
            }

            System.out.print(graphicBuilder(wrongs) + "\n" + block);

            if (!block.contains("_")) {
                System.out.println("\nYou win!");
                dashes.clear();
                break;
            } else if (wrongs == 4 && numHints < 1){
                System.out.println("\nNeed a hint? y/n");
                char hint = sc.next().charAt(0);
                if (hint == 'y') {
                    System.out.println(hint(getWord));
                    numHints++;
                } else {
                    System.out.println("No more hints allowed!");
                }
            } else if (wrongs == 6) {
                System.out.println("\nYou lose!");
                dashes.clear();
                break;
            }
        }
    }

    /**
     * gets word from the HTTP GET request method and adds another layer of exception handling
     * @return removes array formatting "["...."]" and returns only the word of interest
     */
    public static String getWord() {
        String getWord = null;

        try {
            getWord = wordBank();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert getWord != null;
        return getWord.replaceAll("[^a-z]", "");
    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        String word = getWord();
        int wrongs = 0;

        game(word, wrongs);

        System.out.println("Play again? y / n");
        char playAgain = sc.next().charAt(0);

        if (playAgain == 'y') game(getWord(), wrongs);
    }
}