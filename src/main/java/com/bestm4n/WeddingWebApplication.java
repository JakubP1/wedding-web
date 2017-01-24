package com.bestm4n;

import org.h2.server.web.WebServlet;
import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.Record4;
import org.jooq.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@SpringBootApplication
@RestController
public class WeddingWebApplication
{

  @Autowired
  private DSLContext sql;

  public static void main(String[] args) {
    SpringApplication.run(WeddingWebApplication.class, args);
  }

  @Bean
  ServletRegistrationBean h2servletRegistration() {
    return new ServletRegistrationBean(new WebServlet(), "/h2/*");
  }

  @RequestMapping(
      method = RequestMethod.GET,
      value = "/api/presents",
      headers = "Accept=application/json"
  )
  public List<Present> presents() {
    final List<Present> presents = new ArrayList<>();
    final Result<Record4<Object, Object, Object, Object>> result = sql
        .select(
            field("id"),
            field("title"),
            field("price"),
            field("status")
        )
        .from(table("presents"))
        .orderBy(field("title").asc())
        .fetch();
    for (Record4<Object, Object, Object, Object> record : result) {
      final Integer id = record.getValue("id", Integer.class);
      final String title = record.getValue("title", String.class);
      final Integer price = record.getValue("price", Integer.class);
      final String status = record.getValue("status", String.class);
      presents.add(new Present(id, title, price, status));
    }
    return presents;
  }

  static class Present
  {
    private final long id;
    private final String title;
    private final int price;
    private final String status;

    Present(long id, String title, int price, String status) {
      this.id = id;
      this.title = title;
      this.price = price;
      this.status = status;
    }

    public long getId() {
      return id;
    }

    public String getTitle() {
      return title;
    }

    public int getPrice() {
      return price;
    }

    public String getStatus() {
      return status;
    }
  }
}
