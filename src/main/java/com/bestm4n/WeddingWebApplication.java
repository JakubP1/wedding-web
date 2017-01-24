package com.bestm4n;

import org.h2.server.web.WebServlet;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.jooq.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@SpringBootApplication
@RestController
public class WeddingWebApplication
{

  @Autowired
  private DSLContext dsl;

  public static void main(String[] args) {
    final ConfigurableApplicationContext ctx =
        SpringApplication.run(WeddingWebApplication.class, args);
    final DSLContext dsl = ctx.getBean(DSLContext.class);
    ensureDbSchemaExists(dsl);
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
    final Result<Record4<Object, Object, Object, Object>> result = dsl
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

  private static void ensureDbSchemaExists(DSLContext dsl) {
    final ClassPathResource schema = new ClassPathResource("db/schema.sql");
    try (final Scanner scanner = new Scanner(schema.getInputStream())) {
      scanner.useDelimiter("\\A");
      final String sql = scanner.hasNext() ? scanner.next() : "SELECT 1";
      dsl.execute(sql);
    } catch (IOException e) {
      // ignore
    }
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
