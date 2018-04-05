package kea.techy.demo;

import kea.techy.demo.data.Sql;
import kea.techy.demo.data.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.sql.SQLException;
import java.util.List;

@Controller
public class UserController
{
    @GetMapping("/user")
    public String getUsers(ModelMap model)
    {
        try
        {
            List<User> users = Sql.getInstance().getUsers();
            model.addAttribute("users", users);
            //return "users";
        }
        catch (Exception ex)
        {
            model.addAttribute("error", ex.getMessage());
        }
        //model.addAttribute("error", "No Errors");
        return "users";
    }
}
