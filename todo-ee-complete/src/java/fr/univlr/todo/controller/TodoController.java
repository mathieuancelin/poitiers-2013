package fr.univlr.todo.controller;

import fr.univlr.todo.model.Todo;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Stateless
@Path("/todos")
public class TodoController {
    
    @PersistenceContext
    EntityManager em;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Todo> list() {
        return Todo.findAll(em);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Todo create(@FormParam("name") String text) {
        return new Todo(text).save(em);
    }

    @Path("{id}") @DELETE
    public void delete(@PathParam("id") Long id) {
        Todo todo = Todo.findById(id, em);
        if (todo != null) {
            todo.delete(em);
        }
    }

    @Path("{id}") @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Todo update(@PathParam("id") Long id, @FormParam("done") String done) {
        Todo todo = Todo.findById(id, em);
        if (todo != null) {
            todo.setDone("true".equals(done));
            return todo.save(em);
        }
        return null;
    }
}
