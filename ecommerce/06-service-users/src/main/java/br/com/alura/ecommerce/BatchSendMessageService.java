package br.com.alura.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class BatchSendMessageService {

    private final Connection connection;

    BatchSendMessageService() throws SQLException, ClassNotFoundException {
        String url = "jdbc:mysql://localhost:3306/dbteste";
        String usuario = "root";
        String senha = "123";
        this.connection = DriverManager.getConnection(url, usuario, senha);
        this.connection.setAutoCommit(false); // 2️ Adicionando controle de transação

        Class.forName("com.mysql.cj.jdbc.Driver");

        // 1️criar a tabela apenas se não existir
        try {
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS T_SIP_USERS (" +
                            "uuid VARCHAR(200)," +
                            "email VARCHAR(200)" + // Adicionando UNIQUE para evitar duplicação
                            ");"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        var batchService = new BatchSendMessageService();
        try (var service = new KafkaService<>(BatchSendMessageService.class.getSimpleName(),
                "SEND_MESSAGE_TO_ALL_USERS",
                batchService::parse,
                String.class,
                Map.of())) {
            service.run();
        }
    }

    private final KafkaDispatcher<User> userDispatcher = new KafkaDispatcher<>();

    private void parse(ConsumerRecord<String, String> record) throws ExecutionException, InterruptedException, SQLException {
        System.out.println("------------------------------------------");
        System.out.println("Processing Batch");
        System.out.println("Topic: " + record.key());

        for(User user : getAllUsers()){
            //Aqui pegamos o tópico gerado no arquivo 08 para que o arquivo seja gerado
            userDispatcher.send(record.value(), user.getUserId(), user);
        }

    }

    private List<User> getAllUsers() throws SQLException {
        var results = connection.prepareStatement("SELECT uuid FROM T_SIP_USERS;").executeQuery();
        List<User> users = new ArrayList<>();
        while(results.next()){
            users.add(new User(results.getString(1)));
        }
        return users;
    }

}
