# SimpleFTPD


## Overview

SimpleFTPD is a simple FTP server written in Java. Actually, it is a command-line wrapper around the excellent Apache Mina FTP server library.

## Installation

You need to have JRE 6.x+ installed. Then, just extract `simpleftpd_x.x.zip` somewhere on your filesystem.

## Usage

To run the server give:

    java -jar simpleftpd.jar [-p port] [-u users_file]


To run the user management utility give:

    java -jar simpleftpd-manager

and you will get a somewhat descriptive usage message :)

## How to build

You will need git, Apache Ant and JDK 6+ to build from source code.
Then give:

    git clone git://github.com/cyberpython/simpleftpd.git
    cd simpleftpd
    ant clean jar

... and that's it! You should now have a `dist` directory containing the executable jar files.

## Information and license

The project's source code is distributed under the terms of the Apache License v2.0.

Libraries used:

* Apache MINA core
* Apache MINA FTP server
* Log4J
* SLF4J
* JOptSimple


