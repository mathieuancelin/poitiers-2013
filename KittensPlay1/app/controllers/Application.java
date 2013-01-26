package controllers;

import play.mvc.*;

import models.*;

import java.util.*;

public class Application extends Controller {

    public static void index() {
        Cat cat1 = Cat.randomCat();
        Cat cat2 = Cat.randomCat(cat1.id);
        render(cat1, cat2);
    }

    public static void pickACat(Long id, Long panelId) {
        notFoundIfNull(id, "No valid id specified");
        notFoundIfNull(panelId, "No panel Id specified");
        Cat c = Cat.findById(id);
        notFoundIfNull(c);
        c.picked();
        Cat cat1 = null;
        Cat cat2 = null;
        if (panelId == 1) {
        	cat2 = Cat.randomCat(c.id);
        	cat1 = c;
        } else {
        	cat1 = Cat.randomCat(c.id);
        	cat2 = c;
        }
        render("Application/index.html", cat1, cat2);
    }

    public static void stats() {
        Comparator<Cat> byPicked = new Comparator<Cat>() {
            public int compare(Cat cat1, Cat cat2) {
                return cat1.picked.compareTo(cat2.picked);
            }
        };
        List<Cat> cats = Cat.findAll();
        Collections.sort(cats, Collections.reverseOrder(byPicked));
        render(cats);
    }
}