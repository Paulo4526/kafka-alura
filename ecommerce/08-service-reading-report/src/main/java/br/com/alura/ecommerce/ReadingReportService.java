package br.com.alura.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;

//Esta classe é responsãvel por gerar relatórios gravados em Files.
public class ReadingReportService {

    private final Path SOURCE = new File("src/main/resources/report.txt").toPath();

    public static void main(String[] args) {
        var reportService = new ReadingReportService();
        try (var service = new KafkaService<>(ReadingReportService.class.getSimpleName(),
                "USER_READING_RECORD_GENERATE",
                reportService::parse,
                User.class,
                Map.of())) {
            service.run();
        }
    }

    private void parse(ConsumerRecord<String, User> record) throws ExecutionException, InterruptedException, IOException {
        System.out.println("------------------------------------------");
        System.out.println("Processing report for" + record.value());

        var user = record.value();
        var target = new File(user.getReportPath());
        IO.copyTo(SOURCE, target);
        IO.append(target, "Created for " +  user.getUserId());

        System.out.println("File Created: " + target.getAbsolutePath());
    }
}
