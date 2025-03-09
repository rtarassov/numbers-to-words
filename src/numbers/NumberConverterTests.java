package numbers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NumberConverterTests {

    @Test
    public void canHandleRandomNumbersBetween1000AndMaxIntegerET() {
        NumberConverter converter = new NumberConverter("et");

        assertThat(converter.numberInWords(1234)).isEqualTo("üks tuhat kakssada kolmkümmend neli");
        assertThat(converter.numberInWords(5678)).isEqualTo("viis tuhat kuussada seitsekümmend kaheksa");
        assertThat(converter.numberInWords(91011)).isEqualTo("üheksakümmend üks tuhat üksteist");
        assertThat(converter.numberInWords(23456)).isEqualTo("kakskümmend kolm tuhat nelisada viiskümmend kuus");
        assertThat(converter.numberInWords(789012)).isEqualTo("seitsesada kaheksakümmend üheksa tuhat kaksteist");
        assertThat(converter.numberInWords(345678)).isEqualTo("kolmsada nelikümmend viis tuhat kuussada seitsekümmend kaheksa");
        assertThat(converter.numberInWords(901234)).isEqualTo("üheksasada üks tuhat kakssada kolmkümmend neli");
        assertThat(converter.numberInWords(567890)).isEqualTo("viissada kuuskümmend seitse tuhat kaheksasada üheksakümmend");
        assertThat(converter.numberInWords(123456)).isEqualTo("ükssada kakskümmend kolm tuhat nelisada viiskümmend kuus");
        assertThat(converter.numberInWords(987654321)).isEqualTo("üheksasada kaheksakümmend seitse miljonit kuussada viiskümmend neli tuhat kolmsada kakskümmend üks");
        assertThat(converter.numberInWords(2147483647)).isEqualTo("kaks miljardit ükssada nelikümmend seitse miljonit nelisada kaheksakümmend kolm tuhat kuussada nelikümmend seitse");
    }

    @Test
    public void canHandleRandomNumbersBetween1000AndMaxIntegerEN() {
        NumberConverter converter = new NumberConverter("en");

        assertThat(converter.numberInWords(1234)).isEqualTo("one thousand two hundred thirty-four");
        assertThat(converter.numberInWords(5678)).isEqualTo("five thousand six hundred seventy-eight");
        assertThat(converter.numberInWords(91011)).isEqualTo("ninety-one thousand eleven");
        assertThat(converter.numberInWords(23456)).isEqualTo("twenty-three thousand four hundred fifty-six");
        assertThat(converter.numberInWords(789012)).isEqualTo("seven hundred eighty-nine thousand twelve");
        assertThat(converter.numberInWords(345678)).isEqualTo("three hundred forty-five thousand six hundred seventy-eight");
        assertThat(converter.numberInWords(901234)).isEqualTo("nine hundred one thousand two hundred thirty-four");
        assertThat(converter.numberInWords(567890)).isEqualTo("five hundred sixty-seven thousand eight hundred ninety");
        assertThat(converter.numberInWords(123456)).isEqualTo("one hundred twenty-three thousand four hundred fifty-six");
        assertThat(converter.numberInWords(987654321)).isEqualTo("nine hundred eighty-seven million six hundred fifty-four thousand three hundred twenty-one");
        assertThat(converter.numberInWords(2147483647)).isEqualTo("two billion one hundred forty-seven million four hundred eighty-three thousand six hundred forty-seven");
    }

    @Test
    public void selectingMissingLanguageThrows() {
        assertThrows(MissingLanguageFileException.class,
                () -> new NumberConverter("ru"));
    }

    @Test
    public void selectingBrokenLanguageFileThrows() {
        assertThrows(BrokenLanguageFileException.class,
                () -> new NumberConverter("fr"));
    }

    @Test
    public void missingEssentialTranslationThrows() {
        assertThrows(MissingTranslationException.class,
                () -> new NumberConverter("es").numberInWords(1));

        assertThrows(MissingTranslationException.class,
                () -> new NumberConverter("es").numberInWords(2));

        assertThrows(MissingTranslationException.class,
                () -> new NumberConverter("es").numberInWords(3));
    }

    @Test
    public void canConvertOneBillion() {
        NumberConverter converter = new NumberConverter("et");
        assertThat(converter.numberInWords(1000000000)).isEqualTo("üks miljard");

        NumberConverter converter2 = new NumberConverter("en");
        assertThat(converter2.numberInWords(1000000000)).isEqualTo("one billion");
    }

    @Test
    public void canConvertNumbersToEnglish() {
        NumberConverter converter = new NumberConverter("en");

        assertThat(converter.numberInWords(0)).isEqualTo("zero");

        assertThat(converter.numberInWords(1)).isEqualTo("one");

        assertThat(converter.numberInWords(13)).isEqualTo("thirteen");

        assertThat(converter.numberInWords(123)).isEqualTo("one hundred twenty-three");
    }

    @Test
    public void canConvertNumbersUpTo130ToEnglish() {
        assertCanConvertNumbersUpTo(130, "en");
    }

    @Test
    public void canConvertNumbersUpTo130ToEstonian() {
         assertCanConvertNumbersUpTo(130, "et");
    }

    @Test
    public void canConvertNumbersUpTo130ToCustomLanguage() {
        assertCanConvertNumbersUpTo(130, "cu");
    }

    private void assertCanConvertNumbersUpTo(int upperBound, String lang) {
        for (int i = 0; i <= upperBound; i++) {
            String numberInWords = new NumberConverter(lang).numberInWords(i);

            assertThat(numberInWords).isEqualTo(getExpected(lang, i));
        }
    }

    private String getExpected(String lang, int index) {
        List<String> strings = map.get(lang);

        if (strings == null) {
            throw new RuntimeException("unexpected language: " + lang);
        }

        if (index < 0 || index >= strings.size()) {
            throw new RuntimeException("no test data for number: " + index);
        }

        return strings.get(index);
    }

    private final Map<String, List<String>> map = new HashMap<>();

    @BeforeEach
    public void setUp() throws IOException {
        String template = "src/numbers/expected-%s.txt";

        map.put("en", getAllLines(template, "en"));
        map.put("et", getAllLines(template, "et"));
        map.put("cu", getAllLines(template, "cu"));
    }

    private static List<String> getAllLines(String template, String lang) throws IOException {
        return Files.readAllLines(Paths.get(String.format(template, lang)),
                StandardCharsets.ISO_8859_1);
    }
}
