[![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j-slf4j.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.elf4j%22%20AND%20a:%22elf4j-slf4j%22)

# elf4j-slf4j

The [SLF4J](https://www.slf4j.org/) service provider binding for the Easy Logging Facade for
Java ([ELF4J](https://github.com/elf4j/elf4j-api)) SPI

## User story

As a service provider of the Easy Logging Facade for Java (ELF4J) SPI, I want to bind the logging capabilities of SLF4J
to the ELF4J client application via
the [Java Service Provider Interfaces (SPI)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) mechanism, so
that any application using the ELF4J API for logging can opt to use the SLF4J framework at deployment time without code
change.

## Prerequisite

- Java 8+
- SLF4J 2.0.3+

## Get it...

[![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j-slf4j.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.elf4j%22%20AND%20a:%22elf4j-slf4j%22)

## Use it...

If you are using the [ELF4J API](https://github.com/elf4j/elf4j-api#the-client-api) for logging, and wish to select or
change to use SLF4J as the run-time implementation, then simply pack this binding JAR in the classpath when the
application deploys. No code change needed. At compile time, the client code is unaware of this run-time logging service
provider. Because of the ELF4J API, opting for SLF4J as the logging implementation is a deployment-time decision.

The usual [SLF4J configuration](https://www.slf4j.org/manual.html#swapping) applies.

With Maven, in addition to the ELF4J API compile-scope dependency, an end-user application would use this provider as a
runtime-scope dependency:

```html

<dependency>
    <groupId>io.github.elf4j</groupId>
    <artifactId>elf4j-api</artifactId>
</dependency>

<dependency>
    <groupId>io.github.elf4j</groupId>
    <artifactId>elf4j-slf4j</artifactId>
    <scope>runtime</scope>
</dependency>
```

Note: Only one logging provider such as this should be in effect at run-time. If different providers end up in the final 
build of an application, somehow, then the `elf4j.logger.factory.fqcn` system property will have to be used to select the 
desired provider.
