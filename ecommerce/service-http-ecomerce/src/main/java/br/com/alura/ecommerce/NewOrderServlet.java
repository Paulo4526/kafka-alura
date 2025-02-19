package br.com.alura.ecommerce;


import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

//Esta classe de acesso Http ẽ uma versao melhorada do nosso NewOrderMain, onde lá fazemos de uma forma mais generica as injecoes de valores
//Aqui como estamos jã trabalhando com metodos https, temos uma forma mais robusta e condizente com o servico de mensageria
public class NewOrderServlet extends HttpServlet {
    private final KafkaDispatcher<Order> orderDispatcher = new KafkaDispatcher<>();
    private final KafkaDispatcher<String> emailDispatcher = new KafkaDispatcher<>();

    @Override
    public void destroy() {
        super.destroy();
        orderDispatcher.close();
        emailDispatcher.close();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    //Metodo doGet em java puro com servLet aprendido no modulo 7 de ADS na FIAP;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                try {
                    //exemplo para requisicao: "http://localhost:8080/new?email=paulosilvabueno@hotmail.com&amount=01"
                    //Utilizando os parametros email e amount

                    var email = req.getParameter("email");
                    var amount = BigDecimal.valueOf(Long.parseLong(req.getParameter("amount")));
                    var orderId = UUID.randomUUID().toString();

                    var order = new Order(orderId, amount, email);
                    orderDispatcher.send("ECOMMERCE_NEW_ORDER", email, order);

                    var emailCode = "Thank you for your order! We are processing your order!";
                    emailDispatcher.send("ECOMMERCE_SEND_EMAIL", email, emailCode);

                    System.out.println("O usuario " + email + " finalizou a nova compra com sucesso!");
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().println("O usuario " + email + " finalizou a nova compra com sucesso!");

                } catch (ExecutionException e) {
                    throw new ServletException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
    }
}
