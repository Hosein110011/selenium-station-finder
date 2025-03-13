package ir.niopdc.gslatlon.controller;

import ir.niopdc.gslatlon.domain.FuelStation;
import ir.niopdc.gslatlon.domain.FuelStationRepository;
import net.bytebuddy.asm.Advice;
import org.apache.catalina.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.PropertySource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.util.comparator.Comparators;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/selenium")
public class SelenuimController {

    @Autowired
    FuelStationRepository fuelStationRepository;

    @GetMapping("")
    public String scrap() {

//        System.setProperty("webdriver.chrome.driver", "path/to/chromedriver");

        // Initialize the WebDriver


        // Open Google Maps

            // Enter the location name (e.g., "New York")
        List<FuelStation> fuelStations = fuelStationRepository.findAll();
        List<FuelStation> firstRunnableList = fuelStations.subList(0, (fuelStations.size() - 1) / 2);
        List<FuelStation> secondRunnableList = fuelStations.subList((fuelStations.size() - 1) / 2, fuelStations.size());

            // Click the search button
            //            WebElement searchButton = driver.findElement(By.className("igwL8hm"));


        Runnable runnable1 = getRunnable(firstRunnableList);
        Runnable runnable2 = getRunnable(secondRunnableList);


        ExecutorService executorService = Executors.newFixedThreadPool(10);
//
        executorService.execute(runnable1);
        executorService.execute(runnable2);


        return "success";
    }

    private boolean isContained(FuelStation fuelStation, WebElement nameResult) {
        String firstNameResult = arabicToPersianConvertor(nameResult.getText());
        String stationName = arabicToPersianConvertor(fuelStation.getName());
        String[] list = firstNameResult.split(" ");
        String[] otherList = stationName.split(" ");
        ArrayList<String> updatedList = new ArrayList<>();
        ArrayList<String> updatedOther = new ArrayList<>();
        ArrayList<String> resultList = new ArrayList<>();

        for(String ch : list) {

            ch = persianToEnglishConvertor(ch);

            String[] chWithFixedNums = numberSeparator(ch);

            for (String other : otherList) {
                other = persianToEnglishConvertor(other);

                String[] otherWithFixedNums = numberSeparator(other);

                if (ch.equals(other)) {
                    if (other.equals("جایگاه") || other.equals("اختصاصی") || other.equals("پمپ") || other.equals("بنزین") || other.equals("گاز") || other.equals("CNG") || other.isBlank()) {
                        continue;
                    }
                    if (otherWithFixedNums.length > 1) {
                        for (int i = 0; i < otherWithFixedNums.length; i++) {
                            if (!resultList.contains(otherWithFixedNums[i])) {
                                resultList.add(otherWithFixedNums[i]);
                            }
                        }
                    } else {
                        resultList.add(other);
                    }
                }
                if (other.equals("جایگاه") || other.equals("اختصاصی") || other.equals("پمپ") || other.equals("بنزین") || other.equals("گاز") || other.equals("CNG") || ch.isBlank()) {
                    continue;
                }
                if (otherWithFixedNums.length > 1) {
                    for (int i = 0; i < otherWithFixedNums.length; i++) {
                        if (!updatedOther.contains(otherWithFixedNums[i])) {
                            updatedOther.add(otherWithFixedNums[i]);
                        }
                    }
                } else if (!updatedOther.contains(other)) {
                    updatedOther.add(other);
                }
            }

            if (ch.equals("جایگاه") || ch.equals("اختصاصی") || ch.equals("سوخت") || ch.equals("پمپ") || ch.equals("بنزین") || ch.equals("گاز") || ch.equals("گازوئیل") || ch.equals("CNG")
            || ch.equals("گازوئیل)") || ch.equals("و") || ch.equals("(بنزین") || ch.equals("بنزین)") || ch.equals("CNG)") || ch.equals("(CNG") || ch.contains("cng") || ch.contains("(")
                    || ch.contains(")") || ch.contains("گازوییل") || ch.contains(",") || ch.contains(",بنزین") || ch.equals("بنزین،") || ch.equals("بنزین ") || ch.contains("CNG")
                    || ch.equals("خصوصی") || ch.isBlank()) {
                continue;
            }

            if (chWithFixedNums.length > 1) {
                for (int i = 0; i < chWithFixedNums.length; i++) {
                    if (!updatedList.contains(chWithFixedNums[i])) {
                        updatedList.add(chWithFixedNums[i]);
                    }
                }
            } else {
                if (!updatedList.contains(ch)) {
                    updatedList.add(ch);
                }
            }

//            updatedList.add(ch);
        }


        System.out.println(Arrays.toString(updatedList.toArray()));
        System.out.println(Arrays.toString(updatedOther.toArray()));

        boolean isContained = (resultList.size() > updatedList.size()/2);

        if (!isContained && updatedOther.size() == resultList.size()) {
                isContained = true;
        }

        if (!isContained) {
            int i = 0;
            if (updatedOther.size() < updatedList.size()) {
                for (String other : updatedOther) {
                    for (String obj : updatedList) {
                        try {
                            int num = Integer.parseInt(obj);
                            if (other.equals(obj)) {
                                i++;
                            }
                        } catch (NumberFormatException e) {
                            if ((obj.contains(other) || other.contains(obj)) && (!other.isBlank() && !obj.isBlank())) {
                                i++;
                            }
                        }
                    }
                }
                if (i == updatedList.size()) {
                    isContained = true;
                }
            } else if (updatedOther.size() > updatedList.size()) {
                for (String obj : updatedList) {
                    for (String other : updatedOther) {
                        try {
                            int num = Integer.parseInt(other);
                            if (obj.equals(other)) {
                                i++;
                            }
                        } catch (NumberFormatException e) {
                            if ((obj.contains(other) || other.contains(obj)) && (!other.isBlank() && !obj.isBlank())) {
                                i++;
                            }
                        }
                    }
                }
                if (i == updatedOther.size()) {
                    isContained = true;
                }
            } else if(updatedOther.size() == updatedList.size()) {
                for (String obj : updatedList) {
                    for (String other : updatedOther) {
                        try {
                            int num = Integer.parseInt(other);
                            if (obj.equals(other)) {
                                i++;
                            }
                        } catch (NumberFormatException e) {
                            if ((obj.contains(other) || other.contains(obj)) && (!other.isBlank() && !obj.isBlank())) {
                                i++;
                            }
                        }

                    }
                }
                if (i == updatedOther.size() || i == updatedList.size()) {
                    isContained = true;
                }
            }
        }

        return isContained;
    }

    private String arabicToPersianConvertor(String arabic) {
        return arabic.replace('ك', 'ک')
                .replace('ي', 'ی')
                .replace("ئی","یی")
                .replace("ئ","ی")
                .replace("شهید","")
                .replace("(ص)","")
                .replace("(ع)","")
                .replace("(عج)","")
                .replace("(ره)","")
                .replace("ولی عصر","ولیعصر")
                .replace("ولي عصر","ولیعصر")
                .replace("علیه السلام","")
                .replace("سه منظوره","")
                .replace("دو منظوره","")
                .replace("دومنظوره","")
                .replace("۲منظوره","")
                .replace("۳منظوره","")
                .replace("۲ منظوره","")
                .replace("۳ منظوره","")
                .replace("حضرت","");
    }

    private String[] numberSeparator(String name) {
        StringBuilder r = new StringBuilder();

        boolean wasDigit = false; // Flag to track if the previous character was a digit

        // Iterate through each character in the string
        for (char c : name.toCharArray()) {

            if (Character.isDigit(c)) {
                if (!wasDigit) {
                    r.append(" ");
                }
                r.append(c);
                wasDigit = true; // Mark that we are in a digit sequence
            } else if (wasDigit) {

                // Append a space only after a sequence of digits ends
                r.append(" ");
                r.append(c);
                wasDigit = false; // Reset flag for non-digit characters
            } else {
                r.append(c);
            }
        }

        return r.toString().split(" ");
    }

    private String persianToEnglishConvertor(String number) {
        if (number.contains("۰") || number.contains("۱") || number.contains("۲") || number.contains("۳") || number.contains("۴")
            || number.contains("۵") || number.contains("۶") || number.contains("۷") || number.contains("۸") || number.contains("۹")
            || number.contains("(") || number.contains(")")) {
            number = number.replace("۰","0");
            number = number.replace("۱","1");
            number = number.replace("۲","2");
            number = number.replace("۳","3");
            number = number.replace("۴","4");
            number = number.replace("۵","5");
            number = number.replace("۶","6");
            number = number.replace("۷","7");
            number = number.replace("۸","8");
            number = number.replace("۹","9");
            number = number.replace(")"," ");
            number = number.replace("(","");
        }
        return number;
    }

    private Runnable getRunnable(List<FuelStation> fuelStations) {
        return () -> {
            for (FuelStation fuelStation : fuelStations) {


                if (fuelStation.getLatitude() != null && fuelStation.getLongitude() != null) {
                    continue;
                }
                WebDriver driver = new ChromeDriver();

                try {

                    driver.get("https://neshan.org/maps/search");

                    // Wait for the search box to be present
                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

                    wait = new WebDriverWait(driver, Duration.ofSeconds(10));

                    WebElement searchBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("AZLBjuP")));

                    if (fuelStation.getName().contains("مجتمع") || fuelStation.getName().contains("مجتمع خدمات") || fuelStation.getName().contains("مجتمع خدمات رفاهی")) {
                        searchBox.sendKeys(fuelStation.getName() + " " + fuelStation.getProvince());
                        //            searchButton.click();


                        // Wait for the results to load
                        WebElement searchButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("paRlV7r")));
                        //
                        //            WebElement searchButton = driver.findElement(By.className("paRlV7r"));
                        //
                        searchButton.click();


                        //            WebElement firstResult = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("nrFZBE4")));

                        WebElement firstResult;

                        Thread.sleep(2000);

                        List<WebElement> elements = driver.findElements(By.className("wtfezuH"));

                        if (elements.size() > 0) {
                            driver.findElement(By.className("igwL8hm")).click();
                            //                    searchBox.sendKeys("جایگاه نگین خلیج فارس");
                            continue;
                        }

                        Thread.sleep(2000);

                        elements = driver.findElements(By.className("SWIQUYQ"));

                        if (elements.size() > 0) {
                            firstResult = driver.findElement(By.className("SWIQUYQ"));

                            WebElement nameResult = driver.findElement(By.className("ZzIY7hD"));

                            boolean isContained = isContained(fuelStation, nameResult);

                            if (!isContained) {
                                continue;
                            }
                        } else {
                            try {
                                firstResult = driver.findElement(By.className("xz2hYYU"));
                            } catch (Exception ee) {
                                firstResult = driver.findElement(By.className("VwTlEkO"));
                            }

                            //                        if (!firstResult.getText().contains(arabicToPersianConvertor(fuelStation.getCity()))
                            //                                && !firstResult.getText().contains(arabicToPersianConvertor(fuelStation.getProvince()))
                            //                                && !firstResult.getText().contains(arabicToPersianConvertor(fuelStation.getName()))) {
                            firstResult = driver.findElement(By.className("nrFZBE4"));

                            firstResult.click();

                            Thread.sleep(2000);

                            firstResult = driver.findElement(By.className("SWIQUYQ"));

                            WebElement nameResult = driver.findElement(By.className("ZzIY7hD"));

                            boolean isContained = isContained(fuelStation, nameResult);

                            if (!isContained) {
                                continue;
                            }
                            //                        }
                        }

                        // Extract the URL (which contains latitude and longitude)
                        String currentUrl = driver.getCurrentUrl();
                        System.out.println("Current URL: " + currentUrl);

                        // Extract latitude and longitude from the URL
                        String[] parts = currentUrl.split("-");
                        String latitude = parts[0].split("#c")[1];
                        String longitude = parts[1];
                        System.out.println("Latitude: " + latitude);
                        System.out.println("Longitude: " + longitude);

                        fuelStation.setLatitude(latitude);
                        fuelStation.setLongitude(longitude);
                        fuelStationRepository.save(fuelStation);
                    } else if (fuelStation.getName().contains("جایگاه") || fuelStation.getName().contains("جایگاه ") || fuelStation.getName().contains(" جایگاه")) {

                        searchBox.sendKeys(fuelStation.getName() + " " + fuelStation.getProvince());
                        //            searchButton.click();


                        // Wait for the results to load
                        WebElement searchButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("paRlV7r")));
                        //
                        //            WebElement searchButton = driver.findElement(By.className("paRlV7r"));
                        //
                        searchButton.click();


                        //            WebElement firstResult = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("nrFZBE4")));

                        WebElement firstResult;

                        Thread.sleep(2000);

                        List<WebElement> elements = driver.findElements(By.className("wtfezuH"));

                        if (elements.size() > 0) {
                            driver.findElement(By.className("igwL8hm")).click();
                            //                    searchBox.sendKeys("جایگاه نگین خلیج فارس");
                            continue;
                        }

                        Thread.sleep(2000);

                        elements = driver.findElements(By.className("SWIQUYQ"));

                        if (elements.size() > 0) {
                            firstResult = driver.findElement(By.className("SWIQUYQ"));

                            WebElement nameResult = driver.findElement(By.className("ZzIY7hD"));

                            boolean isContained = isContained(fuelStation, nameResult);

                            if (!isContained) {
                                continue;
                            }
                        } else {
                            try {
                                firstResult = driver.findElement(By.className("xz2hYYU"));
                            } catch (Exception ee) {
                                firstResult = driver.findElement(By.className("VwTlEkO"));
                            }

                            //                        if (!firstResult.getText().contains(arabicToPersianConvertor(fuelStation.getCity()))
                            //                                && !firstResult.getText().contains(arabicToPersianConvertor(fuelStation.getProvince()))
                            //                                && !firstResult.getText().contains(arabicToPersianConvertor(fuelStation.getName()))) {
                            firstResult = driver.findElement(By.className("nrFZBE4"));

                            firstResult.click();

                            Thread.sleep(2000);

                            firstResult = driver.findElement(By.className("SWIQUYQ"));

                            WebElement nameResult = driver.findElement(By.className("ZzIY7hD"));

                            boolean isContained = isContained(fuelStation, nameResult);

                            if (!isContained) {
                                continue;
                            }
                            //                        }
                        }

                        // Extract the URL (which contains latitude and longitude)
                        String currentUrl = driver.getCurrentUrl();
                        System.out.println("Current URL: " + currentUrl);

                        // Extract latitude and longitude from the URL
                        String[] parts = currentUrl.split("-");
                        String latitude = parts[0].split("#c")[1];
                        String longitude = parts[1];
                        System.out.println("Latitude: " + latitude);
                        System.out.println("Longitude: " + longitude);

                        fuelStation.setLatitude(latitude);
                        fuelStation.setLongitude(longitude);
                        fuelStationRepository.save(fuelStation);

                    } else {
                        String searchParam = "جایگاه " + fuelStation.getName() + " " + fuelStation.getProvince();

                        System.out.println("searchParam: " + searchParam);

                        searchBox.sendKeys(searchParam);

                        //            searchButton.click();


                        // Wait for the results to load
                        WebElement searchButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("paRlV7r")));
                        //
                        //            WebElement searchButton = driver.findElement(By.className("paRlV7r"));
                        //
                        searchButton.click();


                        //            WebElement firstResult = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("nrFZBE4")));

                        WebElement firstResult;

                        Thread.sleep(2000);

                        List<WebElement> elements = driver.findElements(By.className("wtfezuH"));

                        if (elements.size() > 0) {
                            driver.findElement(By.className("igwL8hm")).click();
                            //                    searchBox.sendKeys("جایگاه نگین خلیج فارس");
                            continue;
                        }

                        Thread.sleep(2000);

                        elements = driver.findElements(By.className("SWIQUYQ"));

                        if (elements.size() > 0) {
                            firstResult = driver.findElement(By.className("SWIQUYQ"));

                            WebElement nameResult = driver.findElement(By.className("ZzIY7hD"));

                            boolean isContained = isContained(fuelStation, nameResult);

                            System.out.println(isContained);

                            if (!isContained) {
                                continue;
                            }
                        } else {
                            try {
                                firstResult = driver.findElement(By.className("xz2hYYU"));
                            } catch (Exception ee) {
                                firstResult = driver.findElement(By.className("VwTlEkO"));
                            }

                            //                        if (!firstResult.getText().contains(arabicToPersianConvertor(fuelStation.getCity()))
                            //                                && !firstResult.getText().contains(arabicToPersianConvertor(fuelStation.getProvince()))
                            //                                && !firstResult.getText().contains(arabicToPersianConvertor(fuelStation.getName()))) {
                            firstResult = driver.findElement(By.className("nrFZBE4"));

                            firstResult.click();

                            Thread.sleep(2000);

                            firstResult = driver.findElement(By.className("SWIQUYQ"));

                            WebElement nameResult = driver.findElement(By.className("ZzIY7hD"));

                            boolean isContained = isContained(fuelStation, nameResult);

                            System.out.println(isContained);

                            //                            if (!firstResult.getText().contains(arabicToPersianConvertor(fuelStation.getCity()))
                            //                                    && !firstResult.getText().contains(arabicToPersianConvertor(fuelStation.getProvince()))
                            //                                    && !firstResult.getText().contains(arabicToPersianConvertor(fuelStation.getName())) && !isContained) {
                            //                                continue;
                            //                            }

                            if (!isContained) {
                                continue;
                            }
                            //                        }
                        }


                        //
                        //                        Thread.sleep(10000);


                        // Extract the URL (which contains latitude and longitude)
                        String currentUrl = driver.getCurrentUrl();
                        System.out.println("Current URL: " + currentUrl);

                        // Extract latitude and longitude from the URL
                        String[] parts = currentUrl.split("-");
                        String latitude = parts[0].split("#c")[1];
                        String longitude = parts[1];
                        System.out.println("Latitude: " + latitude);
                        System.out.println("Longitude: " + longitude);

                        fuelStation.setLatitude(latitude);
                        fuelStation.setLongitude(longitude);
                        fuelStationRepository.save(fuelStation);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    driver.quit();
                }

            }
        };
    }

}
