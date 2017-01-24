package com.bestm4n;

import org.h2.server.web.WebServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class WeddingWebApplication
{

  public static void main(String[] args) {
    SpringApplication.run(WeddingWebApplication.class, args);
  }

  @RequestMapping("/api/presents")
  public String presents() {
    return "[{\n" +
        "  \"name\": \"Cleaner\",\n" +
        "  \"price\": 200.00\n" +
        "}, {\n" +
        "  \"name\": \"iPad\",\n" +
        "  \"price\": 10001.00\n" +
        "}, {\n" +
        "  \"name\": \"Flowers\",\n" +
        "  \"price\": 10001.00\n" +
        "}, {\n" +
        "  \"name\": \"iPhone\",\n" +
        "  \"price\": 10002.00\n" +
        "}, {\n" +
        "  \"name\": \"Teethbrush\",\n" +
        "  \"price\": 120.00\n" +
        "}]";
  }

  @Bean
  ServletRegistrationBean h2servletRegistration() {
    return new ServletRegistrationBean(new WebServlet(), "/h2/*");
  }
}
