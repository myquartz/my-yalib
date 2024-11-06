# The Strict Markdown

1. Syntax by [ENBF rendered by PlantUML](strict-mark/synctax-ebnf.png).
2. The [Javadoc of Strict Mark](strict-mark/javadoc)

Get started with the example code by Spring Boot :

``` shell
git clone https://github.com/my-yalib.git
mvn install -f strict-mark
mvn package -f sample-apps/strict-mark-spring-web
java -jar sample-apps/strict-mark-spring-web/target/*.jar
```

Lets open the URL http://localhost:8088 to try it.