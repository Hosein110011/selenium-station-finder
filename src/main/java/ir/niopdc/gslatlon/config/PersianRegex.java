package ir.niopdc.gslatlon.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PersianRegex {


    public void persianMatcher(String input) {

        String text = "سلام دنیا! این یک متن فارسی است.";

        // Persian regex pattern (matching Persian words)
        String pattern = "[آ-ی]+";

        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(text);


        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }

}
