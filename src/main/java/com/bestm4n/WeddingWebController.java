package com.bestm4n;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WeddingWebController
{
  @RequestMapping("/")
  public String home() {
    return "home";
  }

  @RequestMapping("/presents")
  public String presents() {
    return "presents";
  }
}
