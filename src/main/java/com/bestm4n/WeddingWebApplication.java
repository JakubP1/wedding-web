package com.bestm4n;

import org.h2.server.web.WebServlet;
import org.jooq.DSLContext;
import org.jooq.Record3;
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
    final Result<Record3<Object, Object, Object>> result = sql
        .select(
            field("id"),
            field("name"),
            field("price")
        )
        .from(table("presents"))
        .orderBy(field("name").asc())
        .fetch();
    for (Record3<Object, Object, Object> record : result) {
      final Integer id = record.getValue("id", Integer.class);
      final String name = record.getValue("name", String.class);
      final Integer price = record.getValue("price", Integer.class);
      presents.add(new Present(id, name, price));
    }
    return presents;
  }

  static class Present
  {
    private final long id;
    private final String name;
    private final int price;

    Present(long id, String name, int price) {
      this.id = id;
      this.name = name;
      this.price = price;
    }

    public long getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public int getPrice() {
      return price;
    }
  }
}
