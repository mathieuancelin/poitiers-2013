# Todo app

Ce TP à pour but de vous faire écrire une application web permettant de créer une liste de tâche et de les noter comme 'effectuées'. Cette application sera écrite en utilisant les APIs Java EE 6 standard. Cependant, l'application sera orientée vers le futur et utilisera des technologies en phase avec notre époque :-)

Nous utiliserons donc les APIs suivantes pour la partie serveur :

* JPA 2
* CDI
* EJB
* JAX-RS
* JAXB

Pour la vue, nous utiliserons des technologies plus simple à appréhender que celles fournies par Java EE en standard et plus universelles :

* HTML
* CSS
 * Twitter Bootstrap
* JavaScript
 * JQuery
 * Angular.js
 * Underscore.js
 * Mustache.js

Notre application exposera ses fonctionnalités sous forme de web services REST (exposés via JAX-RS dans un EJB). Une interface écrite en HTML/JS permettra ensuite de consommer ces services et afficher les données nécessaires. Les services exposés seront les suivants :

* GET `/api/todos` => renvoi une liste JSON contenant les tâches en base de données
* POST `/api/todos` => créé une nouvelle tâche et renvoi l'objet créé au format JSON
 * ce service prend un paramètre 'name' permettant de créer une tâche avec un nom
* DELETE `/api/todos/{id}` => supprimer la tâche avec l'id `{id}`
* PUT `/api/todos/{id}` => met à jour la tâche avec l'id `{id}`
 * ce service prend un paramètre 'done' permettant de modifier l'état d'une tâche

## Etape 1 - le projet

Le projet est constitué comme suivant :

* Todo app
 * src
  * conf
   * persistence.xml
  * java
   * fr
    * univpoitiers
     * todo
      * TodoApp.java
      * controller
       * TodoController.java
      * model
       * Todo.java
 * web
  * index.html
  * js
  * bootstrap

## Etape 2 - Le modèle de données

Notre modèle de données est des plus simple. Il va falloir créer une entité représentant une tâche. Cette tâche contient un ID unique (de type `Long`), un nom (de type `String`) et un état représentant le fait que la tâche est effectuée ou non (de type `Boolean`).

Une entité JPA classique ressemble à ceci :

```java
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class User implements Serializable {
    // Attention, toute entité doit absolument posséder un identifiant unique annoté par @Id
    @Id @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private String name;
    private String surname;
    private String email;

    public User() {}
    
    public User(String name, String surname, String email) {
        this.name = name;
        this.surname = surname;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", name=" + name + ", surname=" + surname + ", email=" + email + '}';
    }
}
```

N'hésitez pas à utiliser votre IDE créer une nouvelle entité (dans Netbeans `File/New File/Persistence/Entity class`) et pour générer les méthodes getters/setters, toString, hashCode, equals. Dans Netbeans, menu `Source/Insert code ...`

Il va également être nécessaire d'ajouter un descripteur de persistence dans notre application afin d'accéder à la base de données démarrée dans le serveur GlassFish. Dans Netbeans `File/New File/Persistence/Persistence unit`, choisir la Data source `jdbc/__default` et sélectionner `Drop and create`. Ainsi, à chaque déploiement de votre application, la base de données sera effacée et recréée afin d'être ne phase avec votre modèle de données.

Votre fichier `persistence.xml`doit ressembler à ceci :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="2.0" xmlns="http://java.sun.com/xml/ns/persistence" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd">
  <persistence-unit name="todo-completePU" transaction-type="JTA">
    <provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>
    <jta-data-source>jdbc/__default</jta-data-source>
    <exclude-unlisted-classes>false</exclude-unlisted-classes>
    <properties>
      <property name="eclipselink.ddl-generation" value="drop-and-create-tables"/>
    </properties>
  </persistence-unit>
</persistence>
```

## Etape 3 - Make it DDD

Contrairement à ce qui est communément admis dans les patterns Java EE, il est intéressant de faire résider les diverses méthodes permettant de gérer une entité directement dans l'entité. Normalement, il aurait été nécessaire de passer par un nouvel objet de type DAO (Data Access Object) permettant de manipuler l'Entity Manager pour une entité donnée. Cependant, il n'y a pas vraiment de procéder ainsi car il est bien plus naturel de faire résider les méthodes pour un objet sur cet objet (pratique courante en Programmation OO). Ce genre de pratique est utilisé dans les méthodologies de type DDD (Domain Driven Development).

Nous allons donc ajouter toutes les méthodes nécessaires pour créer, sélectionner, mettre à jour et supprimer nos entités de la base de données directement dans notre entité.

Nos entités doivent maintenant ressembler à ceci :

```java
import java.io.Serializable;
import java.util.List;
import javax.persistence.*;

@Entity
public class User implements Serializable {
    
    @Id @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private String name;
    private String surname;
    private String email;

    public User() {}
    
    public User(String name, String surname, String email) {
        this.name = name;
        this.surname = surname;
        this.email = email;
    }

    ...
    
    // retourne toutes les entités de type User présentes dans la base de données
    public static List<User> findAll(EntityManager em) {
        return em.createQuery("select u from User u", User.class).getResultList();
    }
    // retourne l'entité de type User possédant un identifiant égal à id
    public static User findById(long id, EntityManager em) {
        return em.find(User.class, id);
    }
    // compte le nombre d'entité de type User dans la base de données
    public static long count(EntityManager em) {
        Long l = Long.parseLong(em.createQuery("select count(u) from User u").getSingleResult().toString());
        if (l == null || l < 0) {
            return 0L;
        }
        return l;
    }
    // Supprime toutes les entités de type User de la base de données    
    public static int deleteAll(EntityManager em) {
        return em.createQuery("delete from User").executeUpdate();
    }
    // Supprime l'entité en cours de la base de données 
    public void delete(EntityManager em) {
        em.remove(findById(id, em));
    }
    // Sauvegarde ou met à jour l'entité courante dans la base de données
    public User save(EntityManager em) {
        if (em.contains(this)) {
            return em.merge(this);
        }
        em.persist(this);
        return findById(id, em);
    }
}
```

## Etape 4 - Le contrôleur

Notre contrôleur principale avec être constitué d'un EJB Stateless utilisé comme ressource JAX-RS. Cependant pour pouvoir utiliser JAX-RS dans notre application, il va être nécessaire de l'activer. Il va donc être nécessaire de créer un nouvelle classe pour cela :

```java
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("api")
public class UserApp extends Application {
    
}
```
Cette classe permet de spécifier le contexte de base de nos URLs services (spécifié par l'annotation `@ApplicationPath`)

Nous pouvons maintenant créer notre contrôleur  qui doit ressembler à ceci :

```java
@Stateless
@Path("users")
public class UserController {
    @GET @Path("hello")
    @Produces("application/json")
    public String hello() {
        return "{"foo":"bar"}";
    }
}
```

Il est possible de tester notre méthode via un simple client HTTP :

```shell
$ curl http://localhost:8080/todo/api/todos/hello
{"foo" : "bar"}
```

Pour ajouter les services définis plus haut dans notre contrôleur, nous allons simplement créer les méthodes correspondantes à nos actions et les annoter pour correspondre aux spécifications :

```java
@Stateless
@Path("/users")
public class UserController {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> list() {
        ...
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public User create(@FormParam("name") String name, @FormParam("surname") String surname) {
        ...
    }

    @Path("{id}") @DELETE
    public void delete(@PathParam("id") Long id) {
        ...
    }

    @Path("{id}") @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public User update(@PathParam("id") Long id, @FormParam("email") String email) {
        ...
    }
}
```

Ici, on remarque les différentes façons de récupérer les paramètres pour une requête :

* via l'URL : utilisation de @Path("{id}") et de @PathParam("id") de façon commune
* via le corps de la requête : utilisation de @FormParam("email"). Typiquement ces valeurs sont envoyées via un formulaire HTML 

On remarque également que chaque méthode définie le type de données qu'elle consomme et le type de données qu'elle produit, respectivement via les annotations @Consumes et @Produces. De plus chaque méthode peut spécifier un 'sous-url' permettant d'affiner le sens de l'URL et fournir des renseignement utilise à la méthode métier.

N'oubliez pas, pour que notre entité soit sérialisable en JSON, il va être nécessaire d'ajouter une annotation JAXB sur notre entité :

```java
@Entity
@XmlRootElement
public class User implements Serializable {
    ...
}
```

## Etape 5 - Modèle <=> contrôleur

Maintenant que nous avons un modèle de données et un contrôleur applicatif, il est nécessaire de les faire fonctionner ensemble. Pour cela nous avons besoin d'avoir accès à la base de données, il va donc être nécessaire d'injecter l'entity manager courant dans notre contrôleur.

Notre contrôleur doit maintenant ressembler à ceci :

```java
@Stateless
@Path("/users")
public class UserController {

    // injection de l'entity manager pour accéder à la base
    @PersistenceContext EntityManager em;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> list() {
        return User.findAll(em);
    }
    // implémentation des autres méthodes à faire
    ...
}
```

## Etape 6 - Test des services

Maintenant que la partie serveur est finie, il est possible de la tester sans IHM. Pour cela utilisez un client HTTP tel que cURL pour via un plugin du navigateur :

* Test de création des tâches
```shell
$ curl --data "name=Acheter%20des%20bieres" http://localhost:8080/todo/api/todos
{"id":1, "name":"Acheter des bieres", "done":false}
$ curl --data "name=Faire%20du%20Java%20EE" http://localhost:8080/todo/api/todos
{"id":2, "name":"Faire du Java EE", "done":false}
```
* Test de récupération des tâches
```shell
$ curl http://localhost:8080/todo/api/todos
[{"id":1, "name":"Acheter des bières", "done":false}, {"id":2, "name":"Faire du Java EE", "done":false}]
```
* Test de mise à jour des tâches
```shell
$ curl -X PUT --data "done=true" http://localhost:8080/todo/api/todos/1
{"id":1, "name":"Acheter des bières", "done":true}
```
* Test de suppression des tâches
```shell
$ curl -X DELETE http://localhost:8080/todo/api/todos/1
$ curl http://localhost:8080/todo/api/todos
[{"id":2, "name":"Faire du Java EE", "done":false}]
```

## Etape 7 - L'IHM

Vous avez deux options pour construire votre IHM, chacune utilisant un framework JavaScript différent.
JQuery est un framework incontournable pour manipuler vos pages HTML via une API JavaScript mais est un peu bas niveau pour ce que nous souhaitons en faire. Angular.js est un framework JavaScript très récent créé par Google permettant de développer des applications Web dynamique côté client (les données sont récupérées via des web service par techniques AJAX). Cependant, étant très jeune, ce framework n'est pas encore très utilisé.

Les différents cas à couvrir pas votre IHM sont les suivant :

* Un message situé en haut de la page informe l'utilisateur du nombre de tâches effectué par rapport au nombre de tâches total
```html
<div class="alert alert-info">
   Completed: 1 / 2
</div>
```
* Lors du clic sur le bouton 'Add', si le texte entré dans l'input text du formulaire n'est pas vide, créer une tâche (via le service REST approprié) et l'ajouter dans la liste de tâche.
```html
<ul class="unstyled">
    <li>
        <label class="checkbox">
            <input type="checkbox"/>
            <span class="done-false">Faire du Java EE</span>
        </label>                  
    </li>
</ul>
```
* Lors du clic sur une tâche, marquer la tâche comme effectuée (via le service REST approprié) et appliqué le style `done-true` sur le `span` de la tâche afin qu'elle apparaisse barrée.
* Lors du clic sur le bouton remove, toutes les tâche marquées comme effectuée sont effacées (via le service REST approprié) et sont retirées de la liste de tâches.

La notation `{{ paramName }}` est utilisée dans le cadre de templating JavaScript ou `paramName` est remplacée par la valeur du champ JSON (passé dans un objet JSON en paramètre du template) correspondant;

### JQuery

La vue de l'application est constituée comme ceci, dans un fichier `index.html` :

```html
<!DOCTYPE html>
<html>
    <head>
        <title>Todo app</title>
        <script type="text/javascript" src="js/underscore.js"></script>
        <script type="text/javascript" src="js/jquery-1.7.1.min.js"></script>
        <script type="text/javascript" src="js/mustache.js"></script>
        <link rel="stylesheet" href="bootstrap/css/bootstrap.min.css"/>
        <style type="text/css">
            .done-true {
                text-decoration: line-through;        
            }
        </style>
    </head>
    <body>
        <div class="container">
            <h1>Todo list</h1>
            <div class="alert alert-info" id="completed">
                <!-- completed_tmpl -->
            </div>
            <form>
                <ul class="unstyled" id="tasks">
                    <!-- task_tmpl -->
                </ul>
            </form>
            <form>
                <div class="input-append">
                    <input type="text" id="name"/>
                    <button class="btn btn-primary" type="submit" id="add">
                        <i class="icon-plus icon-white"></i> Add
                    </button>          
                </div>
            </form>
            <div>        
                <button class="btn" type="button" id="cleanup">
                    <i class="icon-trash"></i> Remove the completed items
                </button>
            </div>
        </div>
        <div id="templates">
            <!-- définition du template pour le message du total de tâches effectuées ou non -->
            <script type="text/html" id="completed_tmpl">
                Completed: {{ remaining }} / {{ total }}
            </script>
            <!-- définition du template pour afficher une nouvelle tâche -->
            <script type="text/html" id="task_tmpl">
                <li>
                    <label class="checkbox">
                        <input type="checkbox" class="done" id="{{ todo.id }}" {{ checked }}/>
                        <span id="{{ todo.id }}-span" class="done-{{ todo.done }}">{{ todo.name }}</span>
                    </label>                  
                </li>
            </script>
        </div>
        <script type="text/javascript">
            // modèle
            var todos = [];
            
            // rend un template html côté client
            function render(templateId, view) {
                return Mustache.render( $(templateId).html(), view )
            }
            
            $(document).ready(function() {
                $('#add').click(function(e) {
                    // code
                })
                $('.done').live('click', function() {
                    // code
                })
                $('#cleanup').click(function(e) {
                    // code
                })   
            })
        </script>
    </body>    
</html>
```

Pour pouvoir finir la vue, voici quelques snippets :

* Pour faire un get http :
```js
$.get('url', function(data) {
    // data contient les données retournées par les services REST
})
```
* Pour faire un post http :
```js
$.post('url', {param1:val1, param2:val2}, function(data) {
    // data contient les données retournées par les services REST
})
```
* Pour faire un put http :
```js
$.ajax({ url: 'url', type: 'put', data: {param1: val1} }, function(data) {
    // data contient les données retournées par les services REST
})
```
* Pour faire un delete http :
```js
$.ajax({ url: 'url', type: 'delete', data: {param1: val1} }, function(data) {
    // data contient les données retournées par les services REST
})
```
* Pour définir une action lorsqu'on clique sur un bouton avec l'id 'add' :
```js
$('#add').click(function(e) {
    e.preventDefault()
    // code    
})
```
* Récupération du nom dans l'input text :
```js
$('#name').val()
```
* Mise à jour du nom dans l'input text :
```js
$('#name').val('')
```
* Réagir au click sur une checkbox de tâche :
```js
$('.done').live('click', function() {
    var id = $(this).attr('id')
    var done = ($(this).val() == 'on') + ''
    // code
})
```
* Faire un for each sur le modèle avec underscore js :
```js
_.each(todos, function(todo) {
    if (todo.done) {
        // code
    } else {
        // code
    }
})
```
* Ajout d'une tâche dans la liste de tâche :
```js
$('#tasks').append( render('#task_tmpl', {todo: todo, checked: checked}) )
```
* Changer le contenu d'une div avec un template :
```js
$('#completed').html(render('#completed_tmpl', {remaining: 3, total: todos.length}))
```

### Angular.js

Angular js est un framework MVVM assurant le double binding des données entre le contrôleur et le modèle. Cela veut dire qu'en plus de votre code côté serveur, vous aurez une mini application MVC côté client écrite en JavaScript.

Pour en savoir plus sur Angular js et pour consulter la documentation :

http://angularjs.org/

La vue de l'application est constituée comme ceci, dans un fichier `index.html` :

```html
<!DOCTYPE html>
<html ng-app>
  <head>
    <title>Todo app</title>
    <script type="text/javascript" src="js/angular.min.js"></script>
    <script type="text/javascript" src="js/underscore.js"></script>
    <script type="text/javascript" src="js/controllers.js"></script>
    <link rel="stylesheet" href="bootstrap/css/bootstrap.min.css"/>
    <style type="text/css">
      .done-true {
        text-decoration: line-through;        
      }
    </style>
  </head>
  <body>
    <div class="container" ng-controller="TodoController">
      <h1>Todo list</h1>
      <div class="alert alert-info">
        Completed: {{ remaining() }} / {{ total() }}
      </div>
      <form>
        <ul class="unstyled">
          <li ng-repeat="todo in todos">
            <label class="checkbox">
              <input type="checkbox" ng-model="todo.done" ng-change="mark(todo)"/>
              <span class="done-{{ todo.done }}">{{ todo.name }}</span>
            </label>                  
          </li>
        </ul>
      </form>
      <form ng-submit="add()">
        <div class="input-append">
          <input type="text" ng-model="text"/>
          <button class="btn btn-primary" type="submit">
            <i class="icon-plus icon-white"></i> Add
          </button>          
        </div>
      </form>
      <div>        
        <button class="btn" type="button" ng-click="cleanup()">
          <i class="icon-trash"></i> Remove the completed items
        </button>
      </div>
    </div>
  </body>    
</html>
```

Vous remarquerez que beaucoup de nouveaux attributs (ng-*)sont ajoutés dans les balises HTML pour qu'Angular puisse lier la vue avec les actions du contrôleur client (JavaScript). Dans la vue précédente, tout le binding de données est déjà effectué, il ne vous reste plus qu'à écrire le code du contrôleur. N'hésitez pas à prendre un peu de temps pour bien comprendre ce qui se passe dans la vue pour pouvoir coder le contrôleur.

Le code du contrôleur côté client se trouve dans un fichier `controller.js` comme spécifié par `ng-controller="TodoController"` :

```js
function TodoController($scope, $http) {
  // modèle
  $scope.todos = [];
  
  $scope.refreshTodos = function() {
    // code
  };  
    
  $scope.total = function() {
    // code
  };
  
  $scope.remaining = function() {
    // code
  };
  
  $scope.add = function() {
    // code
  };
  
  $scope.mark = function(todo) {
    // code
  };
  
  $scope.cleanup = function() {
    // code
  };
}
```

Chaque contrôleur opère sur une `$scope` spécifique ce qui permet d'utiliser plusieurs contrôleurs sur une même page HTML sans qu'il y ait de problème. N'oubliez donc pas de préfixer vos variables et fonctions avec le scope.

Pour pouvoir finir la vue, voici quelques snippets :

* Pour faire un get http :
```js
$http.get('url').success(function(data) {
    // data contient les données retournées par les services REST
});
```
* Pour faire un post http :
```js
$http({
    method: 'POST',
    url: 'url',
    data: 'param1=' + value1,
    headers: {'Content-Type': 'application/x-www-form-urlencoded'}        
}).success(function(data) {
    // data contient les données retournées par les services REST
});
```
* Pour faire un put http :
```js
$http({
    method: 'PUT',
    url: 'url',
    data: 'param1=' + value1,
    headers: {'Content-Type': 'application/x-www-form-urlencoded'}        
}).success(function(data) {
    // data contient les données retournées par les services REST
});
```
* Pour faire un delete http :
```js
$http.delete('url')
```
* Pour filter un tableau avec underscore js
```js
_.filter($scope.todos, function(todo) {
    // code
})
```
* Pour éliminer des éléments d'un tableau avec underscore js
```js
_.reject($scope.todos, function(todo) {
   // code
})
```
* Pour faire un for each sur un tableau avec underscore js
```js
_.each($scope.todos, function(todo) {
    // code
})
```

## Etape 8 - Pour aller plus loin

Modifiez votre application partout ou vous le jugez nécessaire afin de la rendre multi-utilisateurs.
Il sera surement nécessaire de modifier votre modèle de données afin de rajouter l'identité de l'utilisateur au niveau des tâches, de modifier les URLs de vos services pour récupérer l'identité de l'utilisateur et de modifier votre IHM pour que l'utilisateur puisse renseigner son identité.

* GET `/api/{userId}/todos` => renvoi une liste JSON contenant les tâches en base de données
* POST `/api/{userId}/todos` => créé une nouvelle tâche et renvoi l'objet créé au format JSON
* DELETE `/api/{userId}/todos/{id}` => supprimer la tâche avec l'id `{id}`
* PUT `/api/{userId}/todos/{id}` => met à jour la tâche avec l'id `{id}`
