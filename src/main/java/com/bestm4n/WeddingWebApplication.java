package com.bestm4n;

import org.h2.server.web.WebServlet;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.*;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@SpringBootApplication
@RestController
public class WeddingWebApplication
{
  private static final Logger LOG = LoggerFactory.getLogger(WeddingWebApplication.class);

  @Autowired
  private DSLContext dsl;

  @Autowired
  private JavaMailSender sender;

  @Value("${reservation.notification.mail.from}")
  private String notificationFrom;

  @Value("${reservation.notification.mail.to}")
  private String notificationTo;

  public static void main(String[] args) {
    final ConfigurableApplicationContext ctx =
        SpringApplication.run(WeddingWebApplication.class, args);
    final DSLContext dsl = ctx.getBean(DSLContext.class);
    ensureDbSchemaExists(dsl);
  }

  @Bean
  ServletRegistrationBean h2servletRegistration() {
    final ServletRegistrationBean registration = new ServletRegistrationBean(new WebServlet());
    registration.addUrlMappings("/admin/*");
    registration.addInitParameter("webAllowOthers", "true");
    return registration;
  }

  @RequestMapping(
      method = RequestMethod.POST,
      value = "/api/presents/{presentId}/reservations",
      headers = "Accept=application/json"
  )
  public Map<String, Object> submitReservation(
      final @PathVariable("presentId") long presentId,
      final @RequestBody Map<String, Object> body)
  {
    if (!body.containsKey("mobile") ||
        body.get("mobile") == null ||
        body.get("mobile").toString().isEmpty())
    {
      throw new IllegalArgumentException("missing 'mobile' value");
    }

    dsl.transaction(configuration -> {

      final Record1<Object> status = dsl
          .select(field("status"))
          .from(table("presents"))
          .where(field("id").eq(presentId))
          .fetchOne();
      if (status == null) {
        throw new IllegalArgumentException(
            String.format("present '%s' doesn't exist", presentId)
        );
      }
      if (!"AVAILABLE".equals(status.value1().toString())) {
        throw new IllegalStateException(
            String.format(
                "present '%d' is not in AVAILABLE state, got '%s'",
                presentId,
                status.value1().toString()
            )
        );
      }

      final int updated = dsl
          .update(table("presents"))
          .set(field("status"), "VERIFYING")
          .set(field("contact"), body.get("mobile").toString())
          .where(field("id").eq(presentId))
          .execute();
      if (updated != 1) {
        throw new IllegalStateException(
            String.format("unable to change status of present '%d'", presentId)
        );
      }

      final Record4<Object, Object, Object, Object> present = dsl
          .select(
              field("title"),
              field("url"),
              field("status"),
              field("contact")
          )
          .from(table("presents"))
          .where(field("id").eq(presentId))
          .fetchOne();


      final String title = present.getValue("title", String.class);
      final String url = present.getValue("url", String.class);
      final String contact = present.getValue("contact", String.class);
      LOG.info(
          "ordered reservation of /presents/{} '{}' by '{}'",
          presentId,
          title,
          contact
      );

      final MimeMessage mail = sender.createMimeMessage();
      final MimeMessageHelper message = new MimeMessageHelper(mail);
      message.setFrom(notificationFrom);
      message.setTo(notificationTo);
      message.setSubject("Rezervace: " + contact + " - " + title);
      message.setText(
          title + "\n" + url +
              "\n\nKontakt: " + contact +
              "\n\nUPDATE presents SET status='RESERVED' WHERE id=" + presentId + ";" +
              "\n\nUPDATE presents SET status='AVAILABLE' WHERE id=" + presentId + ";" +
              "\n\nhttp://honzasiberegabi.cz/admin" +
              "\n\n---"
      );

      new Thread(() -> {
        sender.send(mail);
        LOG.info("notification email sent to '{}'", notificationTo);
      }).start();

    });

    return new HashMap<>();
  }


  @RequestMapping(
      method = RequestMethod.GET,
      value = "/api/presents",
      headers = "Accept=application/json"
  )
  public List<Present> presents() {
    final List<Present> presents = new ArrayList<>();
    final Result<Record6<Object, Object, Object, Object, Object, Object>> result = dsl
        .select(
            field("id"),
            field("title"),
            field("price"),
            field("status"),
            field("url"),
            field("image_url")
        )
        .from(table("presents"))
        .orderBy(field("title").asc())
        .fetch();
    for (Record6<Object, Object, Object, Object, Object, Object> record : result) {
      final Integer id = record.getValue("id", Integer.class);
      final String title = record.getValue("title", String.class);
      final Integer price = record.getValue("price", Integer.class);
      final String status = record.getValue("status", String.class);
      final String url = record.getValue("url", String.class);
      final String imageUrl = record.getValue("image_url", String.class);
      presents.add(new Present(id, title, price, status, url, imageUrl));
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
    private final String url;
    private final String imageUrl;

    Present(long id, String title, int price, String status, String url, String imageUrl) {
      this.id = id;
      this.title = title;
      this.price = price;
      this.status = status;
      this.url = url;
      this.imageUrl = imageUrl;
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

    public String getUrl() {
      return url;
    }

    public String getImageUrl() {
      return imageUrl;
    }
  }
}
