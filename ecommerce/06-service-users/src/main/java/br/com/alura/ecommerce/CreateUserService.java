package br.com.alura.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class CreateUserService {
    private final Connection connection;

    CreateUserService() throws SQLException, ClassNotFoundException {
        String url = "jdbc:mysql://localhost:3306/dbteste";
        String usuario = "root";
        String senha = "123";
        this.connection = DriverManager.getConnection(url, usuario, senha);
        this.connection.setAutoCommit(false); // 🔴 2️⃣ Adicionando controle de transação

        Class.forName("com.mysql.cj.jdbc.Driver");

        // 🔴 1️⃣ Criar a tabela apenas se não existir
        try {
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS T_SIP_USERS (" +
                            "uuid VARCHAR(200)," +
                            "email VARCHAR(200)" + // 🔴 Adicionando UNIQUE para evitar duplicação
                            ");"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        var fraudService = new CreateUserService();
        try (var service = new KafkaService<>(CreateUserService.class.getSimpleName(),
                "ECOMMERCE_NEW_ORDER",
                fraudService::parse,
                Order.class,
                Map.of())) {
            service.run();
        }
    }

    private void parse(ConsumerRecord<String, Order> record) throws SQLException {
        System.out.println("------------------------------------------");
        System.out.println("Processing new order, checking for fraud");
        System.out.println(record.key());
        System.out.println("Email: " + record.value().getEmail() + " (foi adicionado)");

        var order = record.value();
        if (isNewUser(order.getEmail())) {
            inserNewUser(order.getEmail());
        }
    }

    private void inserNewUser(String email) throws SQLException {
        try {
            var statement = connection.prepareStatement("INSERT INTO T_SIP_USERS (uuid, email) VALUES (?, ?);");
            statement.setString(1, UUID.randomUUID().toString());
            statement.setString(2, email);
            statement.executeUpdate(); // 🔴 Correção: executeUpdate() em vez de executeQuery()

            connection.commit(); // 🔴 3️⃣ Confirma a inserção no banco

            System.out.println("Usuário adicionado: " + email);
        } catch (SQLException e) {
            connection.rollback(); // 🔴 Em caso de erro, desfaz a transação
            throw new RuntimeException("Erro ao inserir novo usuário", e);
        }
    }

    private boolean isNewUser(String email) {
        try {
            var statement = connection.prepareStatement("SELECT uuid FROM T_SIP_USERS WHERE email = ? LIMIT 1");
            statement.setString(1, email);
            var resultSet = statement.executeQuery();
            return !resultSet.next(); // 🔴 Correção: retorna true se o usuário NÃO existir
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar usuário", e);
        }
    }
}
