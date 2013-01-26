import play.*;
import play.jobs.*;
import play.test.*;

import models.*;

import java.util.List;
import java.io.File;

@OnApplicationStart
public class Bootstrap extends Job {

    public void doJob() {
    	if (Cat.count() > 0) { Cat.deleteAll(); }
        File root = 
            new File(play.Play.applicationPath + "/public/img");
        for (File img : root.listFiles()) {
        	Cat cat = new Cat("/public/img/" 
                + img.getName()).save();
        }
    }
}