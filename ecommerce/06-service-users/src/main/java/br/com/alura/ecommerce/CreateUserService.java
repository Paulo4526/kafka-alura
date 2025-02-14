package br.com.alura.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class CreateUserService {

    private final Connection connection;

    CreateUserService() throws SQLException, ClassNotFoundException {
        String url = "jdbc:mysql://localhost:3306/dbteste";
        String usuario = "root";
        String senha = "123";
        this.connection = DriverManager.getConnection(url, usuario, senha);
        Class.forName("com.mysql.cj.jdbc.Driver");
        try{
            connection.createStatement().execute(
                    "create table T_SIP_USERS(" +
                            "uuid varchar(200) primary key," +
                            "email varchar(200)" +
                            ");"
            );
        }catch (SQLException e){
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
        System.out.println("Usuãrio: " + record.value().getUserId() + " / Email:" + record.value().getEmail() + "(foi adicionado)");
        var order = record.value();
        if(isNewUser(order.getEmail())){
            inserNewUser(order.getUserId(), order.getEmail());
        }

    }

    private void inserNewUser(String uuid, String email) throws SQLException {
        try{
            var statement = connection.prepareStatement("insert into T_SIP_USERS (uuid, email) " + "values" + " (?,?);");
            statement.setString(1, uuid );
            statement.setString(2, email);
            statement.executeQuery();
            System.out.println("Usuario: uuid, email: " + email + "solicitou uma nova ordem");
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }finally {
            try{
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


    }

    private boolean isNewUser(String email) {
        try{
            var sqlstate = connection.prepareStatement("select uuid from T_SIP_USERS " + "where email = ? limit 1");
            sqlstate.setString(1, email);
            sqlstate.executeQuery();
            var result = sqlstate.executeQuery();
            return result.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
