package br.com.alura.ecommerce;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class HttpEcomerceService {
    public static void main(String[] args) throws Exception {
        var server = new Server(8080);
        var context = new ServletContextHandler();

        context.setContextPath("/");
        context.addServlet(new ServletHolder(new NewOrderServlet()), "/new");
        //Aqui criamos um sistema que irã gerar o relatório pelo path. Caso queira pode ser feita por outro microservico, no momento faremos por essa mesma microservico
        //OBS: Tudo que será atrlado a classe GenerateAllReportServlet, poderá ser feita em um outro microserviço caso haja a necessidade.
        //Caso optar por um novo microserviço, criar uma nosa classe http em um novo microservico e uma nova servlet com o GenerateallReports
        context.addServlet(new ServletHolder(new GenerateAllReportServlet()), "/admin/generate-report");
        server.setHandler(context);

        server.start();
        server.join();

    }
}
