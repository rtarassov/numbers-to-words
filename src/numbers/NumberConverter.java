package numbers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class NumberConverter {

  private Properties properties;

  public NumberConverter(String lang) {
    setProperties(lang);
  }

  public String numberInWords(Integer number) {
    if (number == 0) {
      return properties.getProperty("0");
    }
    if (number == 1000000000) {
      return convertOneBillion(number);
    }

    return calculateWords(number);
  }

  private String calculateWords(Integer number) {
    StringBuilder response = new StringBuilder();

    int billions = getBillions(number);
    int millions = getMillions(number);
    int thousands = getThousands(number);
    int remainder = getRemainder(number);

    if (billions > 0) {
      response.append(processLargeNumber(billions, "billion"));
    }

    if (millions > 0) {
      response.append(processLargeNumber(millions, "million"));
    }

    if (thousands > 0) {
      response.append(processThousands(thousands));
    }

    if (remainder > 0) {
      response.append(processRemainder(remainder));
    }
    System.out.printf("Number %d -> %s%n", number, response.toString().trim());
    return response.toString().trim();
  }

  private String processLargeNumber(int number, String unit) {
    StringBuilder result = new StringBuilder();
    int hundreds = getHundreds(number);
    int remainder = number % 100;

    if (hundreds > 0) {
      result.append(processHundreds(hundreds, 0, 0)).append(" ");
    }

    if (remainder > 0) {
      result.append(calculateWords(remainder)).append(" ");
    }

    String unitKey = number == 1 ? unit + "-singular" : unit + "-plural";
    String unitValue = properties.getProperty(unitKey, properties.getProperty(unit + "-singular", ""));
    result.append(unitValue)
        .append(properties.getProperty(unit + "-after-delimiter", ""));
    return result.toString();
  }

  private String processThousands(int thousands) {
    return calculateWords(thousands) + properties.getProperty("thousand-before-delimiter", "") + properties.getProperty("thousand", "") + properties.getProperty("thousand-after-delimiter", "");
  }

  private String processRemainder(int remainder) {
    StringBuilder sb = new StringBuilder();
    while (remainder > 0) {
      if (properties.containsKey(String.valueOf(remainder))) {
        sb.insert(0, properties.getProperty(String.valueOf(remainder)) + " ");
        remainder /= 1000;
        continue;
      }
      if (remainder % 1000 != 0) {
        int value = remainder % 1000;
        sb.insert(0, processThreeDigitNumber(value));
      }
      remainder /= 1000;
    }
    return sb.toString();
  }

  private String processThreeDigitNumber(int value) {
    String temporary = "";

    int hundreds = getHundreds(value);
    int tens = getTens(value);
    int ones = getOnes(value);

    if (hundreds > 0) {
      temporary += processHundreds(hundreds, tens, ones);
    }

    if (tens == 1) {
      temporary += processTeens(tens, ones);
    } else {
      if (tens > 1) {
        temporary += processTens(tens, ones);
      }
      if (ones > 0) {
        temporary += processOnes(ones);
      }
    }

    return temporary;
  }

  private String processHundreds(int hundreds, int tens, int ones) {
    String hundredsTranslation = properties.getProperty(String.valueOf(hundreds), "");
    String hundred = properties.getProperty("hundred", "");
    String hundredBeforeDelimiter = properties.getProperty("hundred-before-delimiter", "");
    String hundredAfterDelimiter = properties.getProperty("hundred-after-delimiter", "");
    if (!hundredsTranslation.isEmpty() && !hundred.isEmpty()) {
      String result = hundredsTranslation + hundredBeforeDelimiter + hundred;
      if (tens > 0 || ones > 0) {
        result += hundredAfterDelimiter;
      }
      return result;
    }
    throw new MissingTranslationException("Translation for hundreds or 'hundred' is missing in the language file");
  }

  private String processTeens(int tens, int ones) {
    String teenTranslation = properties.getProperty(String.valueOf(tens * 10 + ones), "");
    if (!teenTranslation.isEmpty()) {
      return teenTranslation + " ";
    }
    String onesTranslation = properties.getProperty(String.valueOf(ones), "");
    String teen = properties.getProperty("teen", "");
    if (!onesTranslation.isEmpty() && !teen.isEmpty()) {
      return onesTranslation + teen;
    }
    throw new MissingTranslationException("Translation for teens is missing in the language file");
  }

  private String processTens(int tens, int ones) {
    String tensTranslation = properties.getProperty(String.valueOf(tens * 10), "");
    String tensSuffix = properties.getProperty("tens-suffix", "");
    String tensAfterDelimiter = properties.getProperty("tens-after-delimiter", "");
    if (!tensTranslation.isEmpty()) {
      String result = tensTranslation;
      if (ones > 0 && !tensAfterDelimiter.isEmpty()) {
        result += tensAfterDelimiter;
      }
      return result;
    }
    if (!tensSuffix.isEmpty() && !tensAfterDelimiter.isEmpty()) {
      String result = properties.getProperty(String.valueOf(tens), "") + tensSuffix;
      if (ones > 0) {
        result += tensAfterDelimiter;
      }
      return result;
    }
    throw new MissingTranslationException("Translation for tens or 'tens-suffix' or 'tens-after-delimiter' is missing in the language file");
  }

  private String processOnes(int ones) {
    String onesTranslation = properties.getProperty(String.valueOf(ones), "");
    if (!onesTranslation.isEmpty()) {
      return onesTranslation + " ";
    }
    throw new MissingTranslationException("Translation for ones is missing in the language file");
  }

  private int getBillions(int number) {
    return number / 1_000_000_000;
  }

  private int getMillions(int number) {
    return number % 1_000_000_000 / 1_000_000;
  }

  private int getThousands(int number) {
    return number % 1_000_000 / 1_000;
  }

  private int getHundreds(int number) {
    return number / 100;
  }

  private int getTens(int number) {
    return number % 100 / 10;
  }

  private int getOnes(int number) {
    return number % 10;
  }

  private int getRemainder(int number) {
    return number % 1_000;
  }

  private String convertOneBillion(int number) {
    if (number == 1000000000) {
      String billionSingular = properties.getProperty("billion-singular");
      if (billionSingular == null) {
        throw new MissingTranslationException("Translation for 'billion-singular' is missing in the language file");
      }
      return String.format("%s %s", properties.getProperty("1"), billionSingular);
    }
    return "";
  }

  private void setProperties(String lang) {
    String filepath = String.format("src/numbers/numbers_%s.properties", lang);
    properties = new Properties();
    try (FileInputStream fileInputStream = new FileInputStream(filepath);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.ISO_8859_1)) {
      properties.load(inputStreamReader);
    } catch (FileNotFoundException e) {
      throw new MissingLanguageFileException("Language file not found: " + filepath, e);
    } catch (IllegalArgumentException e) {
      throw new BrokenLanguageFileException("Malformed language file: " + filepath, e);
    } catch (IOException e) {
      throw new RuntimeException("Error loading properties file: " + filepath, e);
    }

    if (properties.getProperty("0") == null || properties.getProperty("1") == null) {
      throw new MissingTranslationException("Essential translations are missing in the language file: " + filepath);
    }
  }
}