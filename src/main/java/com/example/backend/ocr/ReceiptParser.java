package com.example.backend.ocr;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReceiptParser {

  public static int extractTotalAmount(String text) {
    String cleanedText = text.replace(",", "").replace(" ", "");

    Pattern p1 =
        Pattern.compile(
            "(?:합계|결제금액|승인금액|총금액|받을금액|영수금액|TOTAL|AMOUNT)[:\\s]*(\\d{3,8})",
            Pattern.CASE_INSENSITIVE);
    Matcher m1 = p1.matcher(cleanedText);

    int maxAmount = 0;
    while (m1.find()) {
      int current = Integer.parseInt(m1.group(1));
      if (current > maxAmount) maxAmount = current;
    }

    if (maxAmount == 0) {
      Pattern p2 = Pattern.compile("(\\d{3,7})(?:원|\\b)");
      Matcher m2 = p2.matcher(cleanedText);
      while (m2.find()) {
        int current = Integer.parseInt(m2.group(1));
        if (current >= 1000 && current <= 1000000 && current > maxAmount) {
          maxAmount = current;
        }
      }
    }
    return maxAmount;
  }

  public static LocalDateTime extractTradeDate(String text) {
    Pattern p =
        Pattern.compile(
            "(20\\d{2})[\\./\\-\\s](0[1-9]|1[0-2])[\\./\\-\\s](0[1-9]|[12][0-9]|3[01])(?:[\\s\\|]+([01][0-9]|2[0-3]):([0-5][0-9])(?::([0-5][0-9]))?)?");

    Matcher m = p.matcher(text);
    if (m.find()) {
      int year = Integer.parseInt(m.group(1));
      int month = Integer.parseInt(m.group(2));
      int day = Integer.parseInt(m.group(3));

      int hour = (m.group(4) != null) ? Integer.parseInt(m.group(4)) : 0;
      int minute = (m.group(5) != null) ? Integer.parseInt(m.group(5)) : 0;
      int second = (m.group(6) != null) ? Integer.parseInt(m.group(6)) : 0;

      return LocalDateTime.of(year, month, day, hour, minute, second);
    }
    return LocalDateTime.now();
  }

  public static String extractStoreName(String text) {
    String[] lines = text.split("\n");
    for (int i = 0; i < Math.min(lines.length, 10); i++) {
      String line = lines[i].trim();
      if (line.length() < 2 || line.matches("^[0-9\\-\\s/\\.:]+$")) continue;
      if (line.matches(".*(고객용|영수증|매출|전표|카드|번호|전화|주소|가맹|신고|포상금|금융|협회|Smartro|결제|승인|사업자).*"))
        continue;

      return line.replaceAll("^[0-9\\s]+", "").replace("상호:", "").replace("상호", "").trim();
    }
    return "알 수 없는 상호";
  }
}
