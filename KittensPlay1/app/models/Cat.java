package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import java.util.Random;
import utils.*;

@Entity
public class Cat extends Model {

    public String url;
    public Integer picked;

    public Cat() {}

    public Cat(String url) {
        this.url = url;
        this.picked = 0;
    }

    public void picked() {
        if (picked == null) picked = 0;
        picked++;
        save();
    }

    public static Cat randomCat() {
        return randomCat(null);
    }

    public static Cat randomCat(Long id) {
        long count = Cat.count();
        Random random = new Random();
        int number = random.nextInt((int) count)+1;

        if(id!=null){
            while (number == id.intValue()) {
                number = random.nextInt((int) count)+1;
            }
        }
        if (number == 0) {
            number = 1;
        }
        Cat cat = Cat.findById(new Long(number));
        if (cat == null) {
            return randomCat(id);
        }
        return cat;
    }
}