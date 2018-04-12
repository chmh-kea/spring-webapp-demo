package kea.techy.demo;

import kea.techy.demo.data.Mongo;
import kea.techy.demo.data.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;


@Controller
public class MongoUsers
{
    @GetMapping("/mongo/userinit")
    public String initUsers()
    {
        Mongo db = Mongo.getInstance();

        for (int i = 0; i< 3; i++)
        {
            db.createUser("bob"+i,"larsen"+i);
        }

        return "users";
    }
    @GetMapping("/mongo/user")
    public String getUsers(ModelMap model)
    {
        Mongo db = Mongo.getInstance();

        Document user = db.getUsers().find().first();

        List<User> users = new ArrayList<>();
        users.add(new User((String)user.get("firstname"), (String)user.get("lastname")));

        model.addAttribute("users", users);

        return "users";
    }

}
