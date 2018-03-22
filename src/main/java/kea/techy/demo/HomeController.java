package kea.techy.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController
{
    //@GetMapping("/")
    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String Index()
    {
        return "index";
    }

    // GET
    // localhost:8080/greetingtwo?kage=value
    @GetMapping("/greetingtwo")
    public String greetingTwo(@RequestParam(name="kage", defaultValue = "Empty") String name, Model model)
    {
        model.addAttribute("name", name);
        return "greetingtwo";
    }

    // GET
    // localhost:8080/user/1
    @GetMapping("/greetingtwo/{id}")
    public String user(@PathVariable("id") int id, Model model)
    {
        model.addAttribute("name", id);
        return "greetingtwo";
    }



    // POST
    // localhost:8080/greetingtwo
    @RequestMapping(path = "/greetingtwo", method = RequestMethod.POST)
    //@PostMapping("/user")
    public String useradd(@RequestParam("name") String name, Model model)
    {
        String temp = name;
        model.addAttribute("name", name);
        return "greetingtwo";
    }


    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        return "greeting";
    }
}
