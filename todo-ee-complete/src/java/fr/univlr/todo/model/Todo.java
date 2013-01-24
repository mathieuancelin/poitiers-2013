package fr.univlr.todo.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
public class Todo implements Serializable {
    
    @Id @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    
    private String name;
    
    private Boolean done;

    public Todo() {}

    public Todo(String name) {
        this.name = name;
        this.done = false;
    }

    public Boolean getDone() {
        return done;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public static List<Todo> findAll(EntityManager em) {
        return em.createQuery("select t from Todo t", Todo.class).getResultList();
    }
    
    public static Todo findById(long id, EntityManager em) {
        return em.find(Todo.class, id);
    }
    
    public static long count(EntityManager em) {
        Long l = Long.parseLong(em.createQuery("select count(t) from Todo t").getSingleResult().toString());
        if (l == null || l < 0) {
            return 0L;
        }
        return l;
    }
        
    public static int deleteAll(EntityManager em) {
        return em.createQuery("delete from Todo").executeUpdate();
    }

    public void delete(EntityManager em) {
        em.remove(findById(id, em));
    }
    
    public Todo save(EntityManager em) {
        if (em.contains(this)) {
            return em.merge(this);
        }
        em.persist(this);
        return findById(id, em);
    }
}
