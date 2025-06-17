# Spring Design First

A quick "Hello World" Spring Boot application with OpenAPI-powered request validation. Instead of writing loads of boring time-wasting validation code, use Kappa to handle all your request validation straight from the OpenAPI.

This repository was built as sample code for the Bump.sh guide on [API Design-first with Spring Boot](https://docs.bump.sh/guides/openapi/design-first-spring/).

## Usage

Clone the repository down and load it up in your favorite IDE to install dependencies and run the application.

```
# Start the application
$ mvn spring-boot:run
```

Instead of generating OpenAPI from code and annotations, Kappa allows you to take the OpenAPI you created during your planning phase and reuse it as a source of truth to speed up coding the request validation and contract testing, the two most slow and annoying parts of any application development. 

Seeing as the OpenAPI is locally available, and always lives in Git, it's easy to preview it, change it, and get feedback on breaking changes thanks to Bump.sh.

```
npx bump-cli preview api/openapi.yaml
```

Want to see how it will look without downloading anything? Take a look at this documentation [deployed with Bump.sh](https://bump.sh/bump-examples/hub/code-samples/doc/spring-design-first) already.

## Thanks 

The sample code was inspired by https://github.com/spring-guides/tut-rest

## License

The contents of this repository are licensed under [CC BY-NC-SA
4.0](./LICENSE_CC-BY-NC-SA-4.0).
