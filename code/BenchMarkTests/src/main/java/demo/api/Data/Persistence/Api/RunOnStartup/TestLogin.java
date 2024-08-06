package demo.api.Data.Persistence.Api.RunOnStartup;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.DoubleStream;

@Component
@Slf4j
public class TestLogin {
    private static final String URL = "http://localhost:8080/api/v1/auth/login";
    private static final String ID = "0001";
    private static final String PASSWORD = "141066";
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Integer run() {
        List<Integer> numberOfThreadsList = new ArrayList<>(Arrays.asList(30,50,100,200));
        for (Integer numberOfThreads: numberOfThreadsList) {
            List<Long> responseTimes = new ArrayList<>();

            try (ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads)) {
                File file = new File("csvFiles/login/concurrentUsers_" + numberOfThreads);
                file.mkdirs();
                final String CSV_FILE_PATH = "loginTimes.csv";
                File csvFilePath = new File(file, CSV_FILE_PATH);
                final String CSV_STATISTICS_PATH = "stats.csv";
                File statisticsFile = new File(file, CSV_STATISTICS_PATH);

                try (FileWriter writer = new FileWriter(csvFilePath);
                     CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                             .withHeader("Thread", "ResponseTime(ms)", "StatusCode"))) {
                    log.info("Starting stress test with {} threads.", numberOfThreads);

                    for (int i = 0; i < numberOfThreads; i++) {
                        int threadIndex = i;
                        executor.submit(() -> {
                            long responseTime = makeRequest(threadIndex, csvPrinter);
                            synchronized (responseTimes) {
                                responseTimes.add(responseTime);
                            }
                        });
                    }

                    executor.shutdown();
                    if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
                        log.warn("Executor did not terminate in the specified time.");
                    } else {
                        log.info("Stress test completed.");
                    }
                } catch (IOException | InterruptedException e) {
                    log.error("Error during stress test setup or execution: ", e);
                }

                writeStatistics(statisticsFile, responseTimes);
            }
        }
        return 1;
    }

    private long makeRequest(int threadIndex, CSVPrinter csvPrinter) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        Map<String, Object> map = new HashMap<>();
        map.put("id", ID);
        map.put("password", PASSWORD);

        String json;
        try {
            json = objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.error("Error converting map to JSON: ", e);
            return -1;
        }

        HttpEntity<String> request = new HttpEntity<>(json, headers);
        long startTime = System.currentTimeMillis();
        try {
            ResponseEntity<String> response = restTemplate.exchange(URL, HttpMethod.POST, request, String.class);
            long responseTime = System.currentTimeMillis() - startTime;
            synchronized (csvPrinter) {
                csvPrinter.printRecord(threadIndex, responseTime,
                        response.getStatusCodeValue());
            }
            log.info("Thread {}: Response received in {} ms with status code {}.",
                    threadIndex, responseTime, response.getStatusCodeValue());
            return responseTime;
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            synchronized (csvPrinter) {
                try {
                    csvPrinter.printRecord(threadIndex, responseTime, "Error: " + e.getMessage());
                } catch (IOException ioException) {
                    log.error("Error writing to CSV: ", ioException);
                }
            }
            log.error("Thread {}: Error occurred after {} ms: {}", threadIndex, responseTime, e.getMessage());
            return responseTime;
        }
    }

    private void writeStatistics(File statisticsFile, List<Long> responseTimes) {
        try (FileWriter writer = new FileWriter(statisticsFile);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("Metric", "Value"))) {

            double[] times = responseTimes.stream()
                    .mapToDouble(Long::doubleValue)
                    .toArray();

            double mean = DoubleStream.of(times).average().orElse(Double.NaN);
            double max = DoubleStream.of(times).max().orElse(Double.NaN);
            double min = DoubleStream.of(times).min().orElse(Double.NaN);
            double stdDev = Math.sqrt(DoubleStream.of(times).map(t -> (t - mean) * (t - mean)).average().orElse(Double.NaN));

            csvPrinter.printRecord("Mean Response Time (ms)", mean);
            csvPrinter.printRecord("Max Response Time (ms)", max);
            csvPrinter.printRecord("Min Response Time (ms)", min);
            csvPrinter.printRecord("Standard Deviation (ms)", stdDev);

            log.info("Statistics written to {}", statisticsFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Error writing statistics to CSV: ", e);
        }
    }
}
