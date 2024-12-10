package client;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Email and Password Form Validation
public class RegexEmail {

    // Email regex (same as before)
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static Pattern emailpattern = Pattern.compile(EMAIL_REGEX);

    // Modified password regex without special characters
    // The password must have at least one digit, one lowercase letter, one uppercase letter,
    // and must be at least 8 characters long. No special characters are required.
    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z0-9!@#$%^&*(),.?\":{}|<>]{8,}$";
    private static Pattern passwordpattern = Pattern.compile(PASSWORD_REGEX);
    // Validate password based on regex
    public static boolean validPassword(String password) {
        Matcher matcher = passwordpattern.matcher(password);
        return matcher.find();
    }

    // Validate email address based on regex
    public static boolean validEmailAddress(String emailaddress) {
        Matcher matcher = emailpattern.matcher(emailaddress);
        return matcher.find();
    }

    // State machine for email validation (unchanged)
    public static boolean stateMachine(String emailaddress) {
        boolean result = true;
        int state = 0; // -- start state
        int cursor = 0;

        while (state != -99) { // -- terminal state
            char ch = emailaddress.charAt(cursor);
            switch (state) {
                case 0: // -- look for the first character
                    if ((ch == '_') || (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || (ch == '-') || (ch == '+')) {
                        state = 1;
                    } else {
                        result = false;
                        state = -99;
                    }
                    break;
                case 1: // -- look for more characters (stay in this state), . (back to previous state), @ (next state)
                    if ((ch == '_') || (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || (ch == '-') || (ch == '+')) {
                        state = 1;
                    } else if (ch == '.') {
                        state = 0;
                    } else if (ch == '@') {
                        state = 2;
                    } else {
                        result = false;
                        state = -99;
                    }
                    break;
                case 2: // look for first character after @
                    if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || (ch == '-')) {
                        state = 3;
                    } else {
                        result = false;
                        state = -99;
                    }
                    break;
                case 3: // -- look for second character after @ (next state), . (back to previous state)
                    if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || (ch == '-')) {
                        state = 3;
                    } else if (ch == '.') {
                        state = 4;
                    } else {
                        result = false;
                        state = -99;
                    }
                    break;
                case 4: // -- look for subsequent characters after @ (stay in this state), . (back 2 states)
                    if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || (ch == '-')) {
                        state = 5;
                    } else {
                        result = false;
                        state = -99;
                    }
                    break;
                case 5: // -- look for subsequent characters after @ (stay in this state), . (back 2 states)
                    if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || (ch == '-')) {
                        state = 6;
                    } else if (ch == '.') {
                        state = 4;
                    } else {
                        result = false;
                        state = -99;
                    }
                    break;
                case 6: // -- look for subsequent characters after @ (stay in this state), . (back 2 states)
                    if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || (ch == '-')) {
                        state = 6;
                    } else if (ch == '.') {
                        state = 4;
                    } else {
                        result = false;
                        state = -99;
                    }
                    break;
            }

            ++cursor;
            // -- if we hit the end of string and not seen an illegal character then success and done
            if (cursor == emailaddress.length()) {
                if (state == 6) {
                    result = true;
                } else {
                    result = false;
                }
                state = -99;
            }
        }

        return result;
    }
}