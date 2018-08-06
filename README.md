# Akka stream stages

## Project structure

Project is built with [mill](http://www.lihaoyi.com/post/MillBetterScalaBuilds.html), and
as such it adopts mill's project structure

[Install mill](http://www.lihaoyi.com/mill/index.html#installation)

### Import

To prepare the project for intellij, run `mill mill.scalalib.GenIdeaModule/idea`
in the project folder (YMMV)

## Test

`mill all core.test cli.test`

or, to keep testing as the code changes,

`mill --watch all core.test cli.test`

### TODO

Need to find out how to integrate `scoverage` with `mill`

## Build

`mill cli.assembly`

## Run

`mill cli.run`