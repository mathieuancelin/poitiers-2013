package models;

import play.db.jpa.*;
import javax.persistence.*;

@Entity
public class Task extends Model {

	public String name;
	public Boolean done;

	public Task(String name) {
		this.name = name;
        this.done = false;
	}
}