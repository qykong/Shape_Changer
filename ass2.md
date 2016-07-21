**Introduction**

This is a *original* version (with some annotations) of the
ShapeChanger program for Assignment 2, COMP6700.2016. It allows you
to display glyphs (letter forms) and perform morphing (shape transition)
from one glyph to another.

The ``ass2.md`` file (this one) explains how to *easily* compile and run this
application without creating a project in an IDE of your choice.

If you decide to use an IDE (which you should) to work on this project
you will have to import the source files (including the properties and
resources) into the *IDE* of your choice. Netbeans and IntelliJ IDEA
support *JavaFX* projects quite well, so that starting the project from
these sources should be relatively easy. *Eclipse* provides a poorer
approach. When you create a (would be *JavaFX*) project in *Eclipse*, one
has to mark the parent class of the main class which you name when going
through the wizard dialogue: the parent class should (provide the full
name) ``javafx.application.Application``. This should make *Eclipse* aware of
the *JavaFX* resources requirements.

The ``Makefile`` allows one to compile and run the program without relying
on an IDE -- run these commands to achieve the result of

- `make compile` -- compiling to the package structure

- `make run` -- running the program:
- `make doc`  -- creating the API-like documentation of the source
  code (in docs directory) using the `javadoc` utility

- `make` -- performing *both* compilation and document generation at once

- `make clean` -- cleaning (removing what was generated)

**Important** 

If you will use an IDE (and I advise to do it), it can discover the
location of the necessary libraries (including JavaFX) itself. Latest
SDK 8 releases from Oracle have JavaFX integrated with Java Run-Time;
this is not the case for Open JDK, to which JavaFX must be added using
an approach like above.

